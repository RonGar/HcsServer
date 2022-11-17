/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.MathHelper
 */
package hcsmod.server.zones.regions;

import hcsmod.server.zones.PvpZoneData;
import hcsmod.server.zones.regions.Region;
import net.minecraft.util.MathHelper;

public class PvpRegion
extends Region {
    public PvpZoneData pvpZoneData;
    public int chunkX;
    public int chunkZ;

    public PvpRegion(PvpZoneData pvpZoneData, int chunkX, int chunkZ, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
        this.pvpZoneData = pvpZoneData;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public boolean inside(double x, double y, double z) {
        int roundedX = MathHelper.floor_double((double)x);
        int roundedY = MathHelper.floor_double((double)y);
        int roundedZ = MathHelper.floor_double((double)z);
        int chunkXReal = this.chunkX * 16;
        int chunkZReal = this.chunkZ * 16;
        return (double)roundedX >= (double)chunkXReal + this.minX && (double)roundedX <= (double)chunkXReal + this.maxX && (double)roundedZ >= (double)chunkZReal + this.minZ && (double)roundedZ <= (double)chunkZReal + this.maxZ && (double)roundedY >= this.minY && (double)roundedY <= this.maxY;
    }
}

