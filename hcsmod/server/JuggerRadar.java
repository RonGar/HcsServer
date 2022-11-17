/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  co.uk.flansmods.common.driveables.EntityDriveable
 *  hcsmod.HCS
 *  hcsmod.cunches.IVehicle
 *  hcsmod.entity.EntityKoster
 *  hcsmod.entity.EntityPalatka
 *  hcsmod.entity.EntityZombieDayZ
 *  hcsmod.entity.EntityZombieHead
 *  hcsmod.jugger.RenderSpot
 *  hcsmod.player.ExtendedPlayer
 *  mcheli.aircraft.MCH_EntityAircraft
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.item.EntityXPOrb
 *  net.minecraft.entity.passive.EntityChicken
 *  net.minecraft.entity.passive.EntityCow
 *  net.minecraft.entity.passive.EntityPig
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.potion.Potion
 *  net.minecraft.potion.PotionEffect
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.MathHelper
 *  net.minecraft.world.chunk.Chunk
 *  vintarz.core.VSP
 */
package hcsmod.server;

import co.uk.flansmods.common.driveables.EntityDriveable;
import hcsmod.HCS;
import hcsmod.cunches.IVehicle;
import hcsmod.entity.EntityKoster;
import hcsmod.entity.EntityPalatka;
import hcsmod.entity.EntityZombieDayZ;
import hcsmod.entity.EntityZombieHead;
import hcsmod.jugger.RenderSpot;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.EntityHouseServer;
import java.util.ArrayList;
import java.util.List;
import mcheli.aircraft.MCH_EntityAircraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.chunk.Chunk;
import vintarz.core.VSP;
import vintarz.ntr.server.NtrPlayerData;

public class JuggerRadar {
    private static final List<Entity> TMP = new ArrayList<Entity>();

    public static void detect(EntityPlayer p) {
        if (p.getCurrentArmor(3) == null || p.getCurrentArmor((int)3).itemID != HCS.JAG[0].cv) {
            return;
        }
        VSP bos = new VSP(6, "HCSMOD");
        for (Entity e : JuggerRadar.getEntitiesNear(p)) {
            if (e instanceof EntityZombieHead || e instanceof EntityXPOrb) continue;
            byte x = (byte)((e.posX - p.u) / 1.5);
            byte z = (byte)((e.posZ - p.w) / 1.5);
            int type = -1;
            float r = -1.0f;
            float g = -1.0f;
            float b = -1.0f;
            if (e instanceof EntityZombieDayZ) {
                type = RenderSpot.ZOMBIE;
            } else if (e instanceof EntityPlayer) {
                EntityPlayer plr = (EntityPlayer)e;
                PotionEffect potion = plr.b(Potion.invisibility);
                boolean invisible = potion != null && potion.duration > 0;
                boolean op = MinecraftServer.getServer().getConfigurationManager().getOps().contains(plr.username.toLowerCase().trim());
                if (!invisible || !op) {
                    type = RenderSpot.PLAYER_OTHER;
                    if (plr.capabilities.allowFlying) {
                        type = RenderSpot.PLAYER_ADMIN;
                    }
                    try {
                        ArrayList<String> allies = NtrPlayerData.get((EntityPlayer)p).allies;
                        if (allies.contains(plr.username)) {
                            type = RenderSpot.PLAYER_FRIEND;
                        }
                    }
                    catch (Throwable throwable) {}
                }
            } else if (e instanceof EntityCow || e instanceof EntityChicken || e instanceof EntityPig) {
                type = RenderSpot.ANIMAL;
            } else if (e instanceof EntityItem || e instanceof EntityPalatka || e instanceof EntityKoster || e instanceof EntityHouseServer) {
                type = RenderSpot.UNKNOWN;
            } else if (e instanceof IVehicle) {
                if (e instanceof EntityDriveable) {
                    type = RenderSpot.VEH_LAND;
                } else if (e instanceof MCH_EntityAircraft) {
                    type = RenderSpot.VEH_AIR;
                }
            }
            if (type == -1) continue;
            bos.write((int)x);
            bos.write((int)z);
            bos.write(type);
        }
        if (bos.size() < 32766) {
            bos.send(p);
        }
    }

    private static List<Entity> getEntitiesNear(EntityPlayer p) {
        TMP.clear();
        Chunk c = p.q.getChunkFromBlockCoords(MathHelper.floor_double((double)p.u), MathHelper.floor_double((double)p.w));
        for (int cx = c.xPosition - 7; cx <= c.xPosition + 7; ++cx) {
            for (int cz = c.zPosition - 7; cz <= c.zPosition + 7; ++cz) {
                Chunk chunk = p.q.getChunkFromChunkCoords(cx, cz);
                for (List es : chunk.entityLists) {
                    for (Entity e : es) {
                        if (e instanceof EntityPlayer) continue;
                        TMP.add(e);
                    }
                }
            }
        }
        for (EntityPlayer plr : p.q.playerEntities) {
            double z;
            double x;
            if (plr == p || !((x = p.u - plr.u) * x + (z = p.w - plr.w) * z < 36864.0)) continue;
            ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)plr);
            TMP.add((Entity)plr);
        }
        return TMP;
    }

    private static double dst(Entity e, Entity e2) {
        double dx = e.posX - e2.posX;
        double dz = e.posZ - e2.posZ;
        return Math.sqrt(dx * dx + dz * dz);
    }
}

