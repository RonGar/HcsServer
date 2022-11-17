/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  co.uk.flansmods.common.guns.DamageMultiplier
 *  co.uk.flansmods.common.guns.EntityBullet
 *  co.uk.flansmods.vintarz.PlayerRayTraceResult
 *  cpw.mods.fml.common.ITickHandler
 *  cpw.mods.fml.common.TickType
 *  cpw.mods.fml.common.registry.TickRegistry
 *  cpw.mods.fml.relauncher.Side
 *  hcsmod.common.Line
 *  hcsmod.entity.EntityZombieDayZ
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.Packet250CustomPayload
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.MovingObjectPosition
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.World
 */
package co.uk.flansmods.vintarz.server;

import co.uk.flansmods.common.guns.DamageMultiplier;
import co.uk.flansmods.common.guns.EntityBullet;
import co.uk.flansmods.vintarz.PlayerRayTraceResult;
import co.uk.flansmods.vintarz.server.LagEntry;
import co.uk.flansmods.vintarz.server.LagSnapshot;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import extendedDmgSrc.ExtendedDamageSource;
import hcsmod.common.Line;
import hcsmod.entity.EntityZombieDayZ;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.ExtendedStorage;
import hcsmod.server.HcsServer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class LagCompensator
implements ITickHandler {
    private final List<LagEntry> temp = new ArrayList<LagEntry>();
    private LagSnapshot[] snapshots;
    private int nextTick;

    public LagCompensator(int snapshotCount) {
        TickRegistry.registerTickHandler((ITickHandler)this, (Side)Side.SERVER);
        this.setSnapshots(snapshotCount);
    }

    public void setSnapshots(int snapshotCount) {
        this.nextTick = 0;
        this.snapshots = new LagSnapshot[snapshotCount];
        for (int i = 0; i < this.snapshots.length; ++i) {
            this.snapshots[i] = new LagSnapshot();
        }
    }

    public static ExtendedDamageSource.BodyPart getHitBodyPart(PlayerRayTraceResult hitInfo, Entity shooter) {
        if (hitInfo == null) {
            return null;
        }
        AxisAlignedBB hitBox = hitInfo.hitBox;
        Vec3 hitVec = hitInfo.hit.hitVec;
        if (hitBox.maxY - hitBox.minY > 1.0) {
            if (hitVec.yCoord >= hitBox.maxY - 0.5) {
                return ExtendedDamageSource.BodyPart.HEAD;
            }
            if (hitVec.yCoord <= hitBox.minY + 0.75) {
                return ExtendedDamageSource.BodyPart.LEGS;
            }
        } else {
            double x = (hitBox.minX + hitBox.maxX) / 2.0;
            double z = (hitBox.minZ + hitBox.maxZ) / 2.0;
            float rotation = (float)Math.toDegrees(Math.atan2(x - hitVec.xCoord, hitVec.zCoord - z));
            float angle = EntityZombieDayZ.angularDistance((float)rotation, (float)hitInfo.playerYaw);
            if (angle < 45.0f) {
                return ExtendedDamageSource.BodyPart.HEAD;
            }
            if (angle > 135.0f) {
                return ExtendedDamageSource.BodyPart.LEGS;
            }
        }
        return ExtendedDamageSource.BodyPart.BODY;
    }

    public static DamageMultiplier getBulletDamage(EntityPlayer entity, EntityBullet bullet, PlayerRayTraceResult hitInfo, ExtendedDamageSource.BodyPart part) {
        if (HcsServer.isHarxcoreServer) {
            ExtendedDamageSource.hitBodyPart = part;
            return null;
        }
        AxisAlignedBB hitBox = hitInfo.hitBox;
        Vec3 hitVec = hitInfo.hit.hitVec;
        if ((double)entity.P < 1.5 || hitBox.maxY - hitBox.minY < 1.5) {
            return DamageMultiplier.RETARD;
        }
        if (hitVec.yCoord >= hitBox.maxY - 0.5) {
            return DamageMultiplier.HEAD;
        }
        return DamageMultiplier.BODY;
    }

    public LagSnapshot getSnapshot(int lcTick) {
        LagSnapshot snapshot;
        int i;
        for (i = this.nextTick - 1; i >= 0; --i) {
            snapshot = this.snapshots[i];
            if (snapshot.tick != lcTick) continue;
            return snapshot;
        }
        for (i = this.snapshots.length - 1; i > this.nextTick; --i) {
            snapshot = this.snapshots[i];
            if (snapshot.tick != lcTick) continue;
            return snapshot;
        }
        return this.snapshots[this.nextTick];
    }

    public PlayerRayTraceResult rayTracePlayersForTick(EntityPlayerMP owner, Vec3 vecStart, Vec3 vecEnd, int lcTick) {
        LagSnapshot snapshot = this.getSnapshot(lcTick);
        double min_distance = vecEnd.distanceTo(vecStart);
        LagEntry hitEntry = null;
        double hitX = 0.0;
        double hitY = 0.0;
        double hitZ = 0.0;
        this.temp.clear();
        snapshot.addEntries(this.temp, Math.min(vecStart.xCoord, vecEnd.xCoord), Math.min(vecStart.zCoord, vecEnd.zCoord), Math.max(vecStart.xCoord, vecEnd.xCoord), Math.max(vecStart.zCoord, vecEnd.zCoord));
        for (LagEntry entry : this.temp) {
            double distance;
            MovingObjectPosition movingobjectposition1;
            if (!entry.entity.isEntityAlive() || entry.entity == owner || (movingobjectposition1 = entry.aabb.calculateIntercept(vecStart, vecEnd)) == null || !((distance = vecStart.distanceTo(movingobjectposition1.hitVec)) < min_distance) && min_distance != 0.0) continue;
            hitEntry = entry;
            hitX = movingobjectposition1.hitVec.xCoord;
            hitY = movingobjectposition1.hitVec.yCoord;
            hitZ = movingobjectposition1.hitVec.zCoord;
            min_distance = distance;
        }
        ExtendedStorage storage = ExtendedStorage.get(ExtendedPlayer.server((EntityPlayer)owner));
        if (storage.lagCompMaxDebug > 0) {
            Line[] debugLines = new Line[1 + this.temp.size() * 12];
            debugLines[0] = hitEntry != null ? new Line(0xFFFFFF, vecStart.xCoord, vecStart.yCoord, vecStart.zCoord, hitX, hitY, hitZ) : new Line(0xFFFFFF, vecStart.xCoord, vecStart.yCoord, vecStart.zCoord, vecEnd.xCoord, vecEnd.yCoord, vecEnd.zCoord);
            int i = 0;
            for (LagEntry entry : this.temp) {
                int color = entry == hitEntry ? 0x55FF55 : 0xFF5555;
                AxisAlignedBB aabb = entry.aabb;
                debugLines[++i] = new Line(color, aabb.minX, aabb.minY, aabb.minZ, aabb.minX, aabb.maxY, aabb.minZ);
                debugLines[++i] = new Line(color, aabb.maxX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.minZ);
                debugLines[++i] = new Line(color, aabb.minX, aabb.minY, aabb.maxZ, aabb.minX, aabb.maxY, aabb.maxZ);
                debugLines[++i] = new Line(color, aabb.maxX, aabb.minY, aabb.maxZ, aabb.maxX, aabb.maxY, aabb.maxZ);
                debugLines[++i] = new Line(color, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.minY, aabb.minZ);
                debugLines[++i] = new Line(color, aabb.minX, aabb.maxY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.minZ);
                debugLines[++i] = new Line(color, aabb.minX, aabb.minY, aabb.maxZ, aabb.maxX, aabb.minY, aabb.maxZ);
                debugLines[++i] = new Line(color, aabb.minX, aabb.maxY, aabb.maxZ, aabb.maxX, aabb.maxY, aabb.maxZ);
                debugLines[++i] = new Line(color, aabb.minX, aabb.minY, aabb.minZ, aabb.minX, aabb.minY, aabb.maxZ);
                debugLines[++i] = new Line(color, aabb.maxX, aabb.minY, aabb.minZ, aabb.maxX, aabb.minY, aabb.maxZ);
                debugLines[++i] = new Line(color, aabb.minX, aabb.maxY, aabb.minZ, aabb.minX, aabb.maxY, aabb.maxZ);
                debugLines[++i] = new Line(color, aabb.maxX, aabb.maxY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
            }
            storage.addLagCompDebug(debugLines);
            storage.sendDebug(storage.lagCompDebug.size() - 1, (EntityPlayer)owner);
        }
        this.temp.clear();
        if (hitEntry != null) {
            MovingObjectPosition movingObjectPosition = new MovingObjectPosition((Entity)hitEntry.entity);
            movingObjectPosition.hitVec = Vec3.createVectorHelper((double)hitX, (double)hitY, (double)hitZ);
            return new PlayerRayTraceResult(movingObjectPosition, hitEntry.aabb.copy(), hitEntry.rotation);
        }
        return null;
    }

    public static void resetDamage() {
        ExtendedDamageSource.reset();
    }

    public void tickStart(EnumSet<TickType> type, Object ... tickData) {
    }

    public void tickEnd(EnumSet<TickType> type, Object ... tickData) {
        Packet250CustomPayload pt = new Packet250CustomPayload("P", new byte[]{(byte)this.nextTick});
        for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (!(o instanceof EntityPlayerMP)) continue;
            EntityPlayerMP p = (EntityPlayerMP)o;
            p.playerNetServerHandler.sendPacketToPlayer((Packet)pt);
        }
        this.snapshots[this.nextTick].capture(this.nextTick, (World)MinecraftServer.getServer().worldServers[0]);
        if (++this.nextTick >= this.snapshots.length) {
            this.nextTick = 0;
        }
    }

    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.SERVER);
    }

    public String getLabel() {
        return "LagComp";
    }
}

