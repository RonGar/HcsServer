/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 */
package hcsmod.clans.server;

import net.minecraft.entity.player.EntityPlayer;

public class ClansStore {
    public int clanCreateAttempt;

    public void tick(EntityPlayer p) {
        if (this.clanCreateAttempt > 0) {
            --this.clanCreateAttempt;
        }
    }
}

