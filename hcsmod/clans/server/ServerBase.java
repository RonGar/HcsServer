/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.clans.common.ClanBase
 *  hcsmod.clans.common.ClanBase$State
 *  hcsmod.clans.common.ClanPlayer
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.world.World
 *  net.minecraft.world.WorldServer
 */
package hcsmod.clans.server;

import hcsmod.clans.common.ClanBase;
import hcsmod.clans.common.ClanPlayer;
import hcsmod.clans.server.ClansNetwork;
import hcsmod.clans.server.ClansServer;
import hcsmod.clans.server.ServerClan;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class ServerBase
extends ClanBase {
    public final AxisAlignedBB captureRegion = AxisAlignedBB.getBoundingBox((double)0.0, (double)0.0, (double)0.0, (double)0.0, (double)0.0, (double)0.0);
    public final AxisAlignedBB protectionRegion = AxisAlignedBB.getBoundingBox((double)0.0, (double)0.0, (double)0.0, (double)0.0, (double)0.0, (double)0.0);
    public long defenceAtUnix;

    public ServerBase() {
        this.state = ClanBase.State.FREE;
    }

    public ServerBase(String name, int posX, int posY, int posZ) {
        this();
        this.id = UUID.randomUUID();
        this.name = name;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public void tick(long now) {
        MinecraftServer server = MinecraftServer.getServer();
        WorldServer world = server.worldServers[0];
        ServerClan owners = null;
        if (this.capturedBy != null && (owners = ClansServer.clans.get(this.capturedBy)) == null) {
            this.capturedBy = null;
        }
        if (this.state == ClanBase.State.FREE) {
            List players = world.getEntitiesWithinAABB(EntityPlayer.class, this.captureRegion);
            for (EntityPlayer player : players) {
                if (player.capabilities.isCreativeMode || player.capabilities.disableDamage || player.capabilities.allowFlying) continue;
                ClanPlayer clanPlayer = ClansServer.players.get(player.username);
                if (clanPlayer == null) {
                    this.capturedBy = null;
                    break;
                }
                boolean capturingAnotherBase = false;
                for (ServerBase base : ClansServer.bases.values()) {
                    if (!clanPlayer.clan.equals(base.capturedBy)) continue;
                    capturingAnotherBase = true;
                    break;
                }
                if (capturingAnotherBase) {
                    this.capturedBy = null;
                    break;
                }
                if (this.capturedBy == null) {
                    this.capturedBy = clanPlayer.clan;
                    continue;
                }
                if (this.capturedBy.equals(clanPlayer.clan)) continue;
                this.capturedBy = null;
                break;
            }
            if (this.capturedBy != null) {
                this.state = ClanBase.State.CAPTURING;
                this.points = 1;
            }
        } else if (this.state == ClanBase.State.LOCKED) {
            if (owners == null) {
                this.state = ClanBase.State.FREE;
                return;
            }
            this.timer = (int)(this.defenceAtUnix - now / 1000L);
            if (this.timer <= 0) {
                this.state = ClanBase.State.DEFENCE;
                this.timer = ClansServer.config.defenceSeconds;
                this.points = ClansServer.config.defencePoints;
                this.depositCount = 0;
            }
        } else if (this.state == ClanBase.State.DEFENCE) {
            if (owners == null) {
                this.state = ClanBase.State.FREE;
                return;
            }
            this.tickCapture((World)world, false, ClansServer.config.defencePoints);
            if (this.state == ClanBase.State.FREE) {
                this.timer = 0;
                owners.capturedBase = null;
                return;
            }
            if (--this.timer > 0) {
                return;
            }
            if (this.depositCount == 0) {
                this.state = ClanBase.State.CAPTURING;
                this.timer = 0;
                owners.capturedBase = null;
                return;
            }
            this.state = ClanBase.State.LOCKED;
            this.timer = (int)(this.defenceAtUnix - now / 1000L);
            owners.clanPoints += this.depositCount;
            ClansNetwork.broadcastClanCreated(owners);
        } else if (this.state == ClanBase.State.CAPTURING) {
            if (owners == null) {
                this.state = ClanBase.State.FREE;
                return;
            }
            this.tickCapture((World)world, true, ClansServer.config.capturePoints);
        }
    }

    private void tickCapture(World world, boolean reduceWhenEmpty, int maxPoints) {
        if (this.capturedBy == null) {
            this.state = ClanBase.State.FREE;
            return;
        }
        List players = world.getEntitiesWithinAABB(EntityPlayer.class, this.captureRegion);
        this.alliesInRegion = 0;
        this.enemiesInRegion = 0;
        for (EntityPlayer player : players) {
            if (player.capabilities.isCreativeMode || player.capabilities.disableDamage || player.capabilities.allowFlying) continue;
            ClanPlayer clanPlayer = ClansServer.players.get(player.username);
            if (clanPlayer != null && this.capturedBy.equals(clanPlayer.clan)) {
                boolean capturingAnotherBase = false;
                for (ServerBase base : ClansServer.bases.values()) {
                    if (base == this || !clanPlayer.clan.equals(base.capturedBy)) continue;
                    capturingAnotherBase = true;
                    break;
                }
                if (!capturingAnotherBase) {
                    ++this.alliesInRegion;
                    continue;
                }
            }
            ++this.enemiesInRegion;
        }
        if (this.alliesInRegion > 0) {
            if (this.enemiesInRegion == 0) {
                ++this.points;
            }
        } else if (reduceWhenEmpty || this.enemiesInRegion > 0) {
            --this.points;
        }
        if (this.points <= 0) {
            this.state = ClanBase.State.FREE;
            this.capturedBy = null;
            this.points = 0;
            return;
        }
        if (this.points > maxPoints) {
            this.points = maxPoints;
        }
    }
}

