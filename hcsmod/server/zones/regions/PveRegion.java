/*
 * Decompiled with CFR 0.150.
 */
package hcsmod.server.zones.regions;

import hcsmod.server.zones.regions.Region;

public class PveRegion
extends Region {
    public int enterSeconds;
    public int exitSeconds;

    public PveRegion(int enterSeconds, int exitSeconds, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
        this.exitSeconds = exitSeconds;
        this.enterSeconds = enterSeconds;
    }
}

