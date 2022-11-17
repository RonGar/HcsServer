/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.util.DamageSource
 */
package hcsmod.server.zones;

import hcsmod.server.SPacketHandler;
import hcsmod.server.zones.regions.PveRegion;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public class PveSystem {
    private static final String PVE_CH = "pve-inv";
    private static final String PVE_DMG = "pve-dmg";
    public Map<String, PveRegion> regions = new HashMap<String, PveRegion>();
    private transient boolean isPvEAttack = false;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean pveAttack(Entity target, DamageSource damageSource, float damage) {
        try {
            this.isPvEAttack = true;
            boolean bl = target.attackEntityFrom(damageSource, damage);
            return bl;
        }
        finally {
            this.isPvEAttack = false;
        }
    }

    public void tick(EntityPlayer player, Player pvePlayer) {
        PveRegion region = this.getRegion(player.u, player.v, player.w);
        long now = System.currentTimeMillis();
        if (region != null) {
            if (!pvePlayer.pvpDisabled && now > pvePlayer.lastAttack + (long)region.enterSeconds * 1000L) {
                pvePlayer.pvpDisabled = true;
                pvePlayer.pvpImmune = true;
                pvePlayer.disabledNextSend = 0L;
            }
            if (pvePlayer.pvpDisabled) {
                pvePlayer.setTemporaryPvPImmune(player, region.exitSeconds);
                if (now > pvePlayer.disabledNextSend) {
                    this.sendMessage(PVE_CH, player, "\u00a7e\u041d\u0435\u0432\u043e\u0441\u043f\u0440\u0438\u0438\u043c\u0447\u0438\u0432 \u043a \u0443\u0440\u043e\u043d\u0443 \u0438\u0433\u0440\u043e\u043a\u043e\u0432", 12);
                    pvePlayer.disabledNextSend = now + 10000L;
                }
            }
        } else {
            pvePlayer.pvpDisabled = false;
            if (pvePlayer.pvpImmune) {
                if (now > pvePlayer.immuneTimeout) {
                    pvePlayer.pvpImmune = false;
                } else if (player.o != null) {
                    pvePlayer.pvpImmune = false;
                } else {
                    double z;
                    double y;
                    double x = player.u - pvePlayer.posX;
                    if (x * x + (y = player.v - pvePlayer.posY) * y + (z = player.w - pvePlayer.posZ) * z > 100.0) {
                        pvePlayer.pvpImmune = false;
                    } else {
                        pvePlayer.updatePlayerPosition(player);
                    }
                }
            }
            if (pvePlayer.pvpImmune) {
                int immuneHintSeconds = (int)((pvePlayer.immuneTimeout - System.currentTimeMillis()) / 1000L);
                if (pvePlayer.immuneHintSeconds != immuneHintSeconds) {
                    pvePlayer.immuneHintSeconds = immuneHintSeconds;
                    this.sendMessage(PVE_CH, player, "\u00a7e\u041d\u0435\u0432\u043e\u0441\u043f\u0440\u0438\u0438\u043c\u0447\u0438\u0432 \u043a \u0443\u0440\u043e\u043d\u0443 \u0438\u0433\u0440\u043e\u043a\u043e\u0432 " + (immuneHintSeconds + 1) + "\u0441\n\u00a7e\u0410\u0442\u0430\u043a\u0430 \u0438\u0433\u0440\u043e\u043a\u0430 \u043e\u0442\u043a\u043b\u044e\u0447\u0438\u0442 \u043d\u0435\u0443\u044f\u0437\u0432\u0438\u043c\u043e\u0441\u0442\u044c", 12);
                }
            } else {
                pvePlayer.immuneTimeout = 0L;
                pvePlayer.immuneHintSeconds = 0;
                this.sendMessage(PVE_CH, player, "", 0);
            }
        }
    }

    public boolean ensureCanPvPAttack(EntityPlayer attacker, Player pveAttacker) {
        long now = System.currentTimeMillis();
        pveAttacker.lastAttack = now;
        if (pveAttacker.pvpDisabled) {
            return false;
        }
        if (pveAttacker.pvpImmune) {
            pveAttacker.pvpImmune = false;
            pveAttacker.immuneTimeout = now + 1000L;
            this.sendMessage(PVE_CH, attacker, "\u00a7c\u041d\u0435\u0432\u043e\u0441\u043f\u0440\u0438\u0438\u043c\u0447\u0438\u0432\u043e\u0441\u0442\u044c \u043a \u0443\u0440\u043e\u043d\u0443 \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0430", 3);
            this.sendMessage(PVE_DMG, attacker, "\u00a7c\u0423\u0440\u043e\u043d \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d \u043d\u0430 1 \u0441\u0435\u043a\u0443\u043d\u0434\u0443", 1);
            return false;
        }
        return now > pveAttacker.immuneTimeout;
    }

    public boolean isPvPDisabled(Player pvePlayer) {
        return pvePlayer.pvpDisabled;
    }

    public boolean isImmune(Player pvePlayer) {
        return !this.isPvEAttack && pvePlayer.pvpImmune;
    }

    private PveRegion getRegion(double x, double y, double z) {
        for (PveRegion region : this.regions.values()) {
            if (!region.inside(x, y, z)) continue;
            return region;
        }
        return null;
    }

    private void sendMessage(String channel, EntityPlayer p, String message, int seconds) {
        SPacketHandler.sendHint(p, channel, message, seconds * 20);
    }

    public static class Player {
        private double posX;
        private double posY;
        private double posZ;
        private boolean pvpDisabled;
        private long lastAttack;
        private long disabledNextSend;
        private boolean pvpImmune;
        private long immuneTimeout;
        private int immuneHintSeconds;

        public void setTemporaryPvPImmune(EntityPlayer player, int immuneSeconds) {
            this.pvpImmune = true;
            this.immuneTimeout = System.currentTimeMillis() + (long)immuneSeconds * 1000L;
            this.updatePlayerPosition(player);
        }

        public long immuneTimeout() {
            return this.immuneTimeout;
        }

        private void updatePlayerPosition(EntityPlayer player) {
            this.posX = player.u;
            this.posY = player.v;
            this.posZ = player.w;
        }
    }
}

