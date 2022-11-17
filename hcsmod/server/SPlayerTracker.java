/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  co.uk.flansmods.common.ItemTool
 *  cpw.mods.fml.common.IPlayerTracker
 *  cpw.mods.fml.common.network.PacketDispatcher
 *  cpw.mods.fml.common.network.Player
 *  hcsmod.flashlight.Flashlight
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.ChatMessageComponent
 *  vintarz.core.VSP
 */
package hcsmod.server;

import co.uk.flansmods.common.ItemTool;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import hcsmod.clans.server.ClansServer;
import hcsmod.flashlight.Flashlight;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.ExtendedStorage;
import hcsmod.server.HcsNotification;
import hcsmod.server.HcsServer;
import hcsmod.server.Location;
import hcsmod.server.MapMarkersServer;
import hcsmod.server.SPacketHandler;
import hcsmod.server.STickHandler;
import hcsmod.server.airdrop.AirdropSystem;
import hcsmod.server.storage.StorageInventory;
import hcsmod.server.zones.PvpSystem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import vintarz.core.VSP;

public class SPlayerTracker
implements IPlayerTracker {
    public static final boolean ENABLE_RESPAWN = Boolean.parseBoolean(System.getProperty("vz.dayz.respawn", "true"));
    public static final List<Item> sendMaxDamage = new ArrayList<Item>();

    public SPlayerTracker() {
        for (Item item : Item.itemsList) {
            if (!(item instanceof ItemTool)) continue;
            sendMaxDamage.add(item);
        }
    }

    public void onPlayerLogin(EntityPlayer player) {
        VSP os = new VSP(10, "HCSMOD");
        try {
            os.writeUTF(MinecraftServer.getServer().getMOTD());
            os.writeInt(STickHandler.LIMIT_MAX_X);
            os.writeInt(STickHandler.LIMIT_MAX_Z);
            os.writeInt(STickHandler.LIMIT_MIN_X);
            os.writeInt(STickHandler.LIMIT_MIN_Z);
            os.writeUTF(HcsServer.mapServerConfig.map);
            os.writeFloat(HcsServer.mapServerConfig.textureSize);
            os.writeFloat(HcsServer.mapServerConfig.originX);
            os.writeFloat(HcsServer.mapServerConfig.originY);
            os.writeFloat(HcsServer.mapServerConfig.scaleFactor);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        os.send(player);
        HcsNotification.playerLogin(player);
        StorageInventory.playerLogin(player);
        ClansServer.playerJoined(player);
        if (HcsServer.worldMarkersPacket != null) {
            PacketDispatcher.sendPacketToPlayer((Packet)HcsServer.worldMarkersPacket, (Player)((Player)player));
        }
        for (Item item : sendMaxDamage) {
            os = new VSP(21, "HCSMOD");
            try {
                os.writeShort(item.itemID);
                os.writeShort(item.getMaxDamage());
                os.send(player);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        SPacketHandler.sendHitboxExtend(player);
        os = new VSP(17, "HCSMOD");
        try {
            os.writeBoolean(HcsServer.aprilFool());
        }
        catch (IOException iOException) {
            // empty catch block
        }
        os.send(player);
        try {
            STickHandler.plrMPinitialInvulnerability.setInt((Object)player, 0);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
        SPacketHandler.broadcastExtendedData(player, ep);
        try {
            os = new VSP(1, "HCSMOD");
            os.writeByte(0);
            os.writeInt(ep.zombieKills);
            os.send(player);
            os = new VSP(1, "HCSMOD");
            os.writeByte(1);
            os.writeInt(ep.playerKills);
            os.send(player);
            os = new VSP(2, "HCSMOD");
            os.writeInt(ep.thirst);
            os.writeInt(ep.hunger);
            os.send(player);
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (MapMarkersServer.markerGroups.size() > 0) {
            SPacketHandler.sendMapMarkers(player);
        }
        if (HcsServer.CPSData.size() > 0) {
            SPacketHandler.sendCPSData(player);
        }
        if (AirdropSystem.config.enabled) {
            for (AirdropSystem.Airdrop airdrop : AirdropSystem.airdrops.values()) {
                AirdropSystem.sendUpdate(airdrop.state, true, airdrop, player);
            }
        }
        if (PvpSystem.countAirZones > 0) {
            PvpSystem.sendData(player);
        }
        for (String un : MinecraftServer.getServer().getConfigurationManager().getAllUsernames()) {
            EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(un);
            ep = ExtendedPlayer.server((EntityPlayer)p);
            SPacketHandler.sendExtendedInventoryToPlayer((EntityPlayer)p, ep, player);
        }
        ep.checkHousesForTransfer(player);
        if (ep.hasHousesForTransfer) {
            player.a(ChatMessageComponent.createFromText((String)" "));
            ep.hasHousesForTransfer(player);
            player.a(ChatMessageComponent.createFromText((String)" "));
            ep.hasHousesForTransfer(player);
            player.a(ChatMessageComponent.createFromText((String)" "));
            ep.hasHousesForTransfer(player);
            player.a(ChatMessageComponent.createFromText((String)" "));
        }
        if (HcsServer.isHarxcoreServer) {
            SPacketHandler.sendExtendedArmorToPlayer(player, ExtendedStorage.get(ep));
        }
        SPacketHandler.sendOverrideDimension(player);
    }

    public void onPlayerLogout(EntityPlayer p) {
        ClansServer.playerQuit(p);
        SPlayerTracker.turnFlashlightOff(ExtendedPlayer.server((EntityPlayer)p).inventory.inventoryStacks[4]);
        for (int i = 0; i < p.inventory.mainInventory.length; ++i) {
            SPlayerTracker.turnFlashlightOff(p.inventory.mainInventory[i]);
        }
    }

    public void onPlayerChangedDimension(EntityPlayer player) {
    }

    public void onPlayerRespawn(EntityPlayer p) {
        ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
        SPacketHandler.broadcastExtendedData(p, ep);
        ep.feed(0, 0);
        ep.water(0, 0);
        if (ENABLE_RESPAWN) {
            Location spawnLocation = HcsServer.hcsConfig.spawnLocation;
            p.a((double)spawnLocation.X, (double)spawnLocation.Y, (double)spawnLocation.Z);
        }
    }

    public static void turnFlashlightOff(ItemStack is) {
        if (is == null || is.itemID != Flashlight.flashlight.itemID || is.stackTagCompound == null) {
            return;
        }
        NBTTagCompound tag = is.stackTagCompound;
        int charge = tag.getInteger("F");
        if (charge != 0) {
            tag.removeTag("F");
            if ((charge -= Flashlight.getFlashlightTime((boolean)false)) > 0) {
                tag.setShort("f", (short)charge);
            }
        }
    }
}

