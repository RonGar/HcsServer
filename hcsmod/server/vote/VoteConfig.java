/*
 * Decompiled with CFR 0.150.
 */
package hcsmod.server.vote;

public class VoteConfig {
    public String saveName = "";
    public String backend = "";
    public int claimTime = 300;
    public double afkMove = 2.0;
    public int afkPath = 10;
    public int afkSeconds = 5;
    public Zone[] disableZones;

    public static class Zone {
        public String inZoneWarning;
        public double minX;
        public double minY;
        public double minZ;
        public double maxX;
        public double maxY;
        public double maxZ;
    }
}

