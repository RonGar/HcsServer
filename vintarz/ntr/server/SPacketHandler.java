/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.common.network.IPacketHandler
 *  cpw.mods.fml.common.network.PacketDispatcher
 *  cpw.mods.fml.common.network.Player
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.network.INetworkManager
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.Packet250CustomPayload
 *  net.minecraft.server.MinecraftServer
 */
package vintarz.ntr.server;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import vintarz.ntr.server.NtrPlayerData;
import vintarz.ntr.server.ServerNtr;

public class SPacketHandler
implements IPacketHandler {
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
        if (ServerNtr.disable) {
            return;
        }
        EntityPlayer p = (EntityPlayer)player;
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.data));
        try {
            byte type = dis.readByte();
            switch (type) {
                case 0: {
                    String usr = dis.readUTF();
                    if (p.username.equals(ServerNtr.targetingPlayers.get(usr))) {
                        EntityPlayerMP target = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(usr);
                        NtrPlayerData d = NtrPlayerData.get(p);
                        if (target != null && !d.allies.contains(usr)) {
                            d.addAlly(usr);
                            NtrPlayerData.get((EntityPlayer)target).addAlly(p.username);
                            ServerNtr.targetingPlayers.put(p.username, "");
                            ServerNtr.targetingPlayers.put(usr, "");
                        }
                        break;
                    }
                    ServerNtr.targetingPlayers.put(p.username, usr);
                    break;
                }
                case 1: {
                    ServerNtr.targetingPlayers.put(p.username, "");
                    break;
                }
                case 2: {
                    String usr = dis.readUTF();
                    NtrPlayerData.get(p).remAlly(usr);
                    EntityPlayerMP tgt = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(usr);
                    if (tgt == null) break;
                    NtrPlayerData.get((EntityPlayer)tgt).remAlly(p.username);
                    break;
                }
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected static void sendAllies(EntityPlayer p, List<String> allies) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(0);
            for (String ally : allies) {
                dos.writeUTF(ally);
            }
            Packet250CustomPayload packet = new Packet250CustomPayload("vzNtr", baos.toByteArray());
            PacketDispatcher.sendPacketToPlayer((Packet)packet, (Player)((Player)p));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void addAlly(EntityPlayer p, String ally) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(1);
            dos.writeUTF(ally);
            Packet250CustomPayload packet = new Packet250CustomPayload("vzNtr", baos.toByteArray());
            PacketDispatcher.sendPacketToPlayer((Packet)packet, (Player)((Player)p));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void remAlly(EntityPlayer p, String ally) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(2);
            dos.writeUTF(ally);
            Packet250CustomPayload packet = new Packet250CustomPayload("vzNtr", baos.toByteArray());
            PacketDispatcher.sendPacketToPlayer((Packet)packet, (Player)((Player)p));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void disable(EntityPlayer p) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(100);
            Packet250CustomPayload packet = new Packet250CustomPayload("vzNtr", baos.toByteArray());
            PacketDispatcher.sendPacketToPlayer((Packet)packet, (Player)((Player)p));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

