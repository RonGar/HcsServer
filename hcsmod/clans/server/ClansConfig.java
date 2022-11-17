/*
 * Decompiled with CFR 0.150.
 */
package hcsmod.clans.server;

public class ClansConfig {
    public boolean enabled;
    public int maxMembers = 5;
    public int capturePoints;
    public int defenceSeconds;
    public int defencePoints;
    public int teleportDelay;
    public int teleportCooldown;
    public ProtectionOption[] protectionOptions;
    public int protectionTimeStepSeconds;
    public ProtectionTimeFrame[] protectionTimeFrames;

    public static class ProtectionTimeFrame {
        public String start;
        public String end;
    }

    public static class ProtectionOption {
        public String name;
        public int item;
        public int quantity;
    }
}

