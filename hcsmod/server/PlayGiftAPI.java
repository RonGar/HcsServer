/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.server.MinecraftServer
 */
package hcsmod.server;

import hcsmod.player.ExtendedPlayer;
import hcsmod.server.ExtendedStorage;
import hcsmod.server.api.PlayGift;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class PlayGiftAPI
implements PlayGift {
    @Override
    public boolean claim(String playerName) {
        EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(playerName);
        if (player == null) {
            throw new RuntimeException("No such player online: " + playerName);
        }
        return ExtendedStorage.get((ExtendedPlayer)ExtendedPlayer.server((EntityPlayer)player)).playGift.claim((EntityPlayer)player);
    }
}

