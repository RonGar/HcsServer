/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.clans.ClansMod
 *  hcsmod.clans.common.Clan
 *  hcsmod.clans.common.ClanBase$State
 *  hcsmod.clans.common.ClanPlayer
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  vintarz.core.VSP
 */
package hcsmod.clans.server;

import hcsmod.clans.ClansMod;
import hcsmod.clans.common.Clan;
import hcsmod.clans.common.ClanBase;
import hcsmod.clans.common.ClanPlayer;
import hcsmod.clans.server.ClansServer;
import hcsmod.clans.server.ServerBase;
import hcsmod.clans.server.ServerClan;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import vintarz.core.VSP;

public class ClansNetwork {
    private static ByteArrayOutputStream tempBytes = new ByteArrayOutputStream();
    private static DataOutput tempOutput = new DataOutputStream(tempBytes);

    static void writeCan(Clan clan, DataOutput out) throws IOException {
        out.writeLong(clan.id.getMostSignificantBits());
        out.writeLong(clan.id.getLeastSignificantBits());
        ClansMod.writeString((DataOutput)out, (String)clan.tag);
        ClansMod.writeString((DataOutput)out, (String)clan.name);
        out.writeLong(clan.createdAtUnix);
        out.writeInt(clan.clanPoints);
    }

    static void sendClansDisabled(EntityPlayer p) {
        VSP os = new VSP(9, "HCSMOD");
        try {
            os.writeByte(0);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        os.send(p);
    }

    static void sendPlayerClan(EntityPlayer p, ClanPlayer clanPlayer) {
        VSP os = new VSP(9, "HCSMOD");
        try {
            os.writeByte(4);
            if (clanPlayer != null) {
                os.writeLong(clanPlayer.clan.getMostSignificantBits());
                os.writeLong(clanPlayer.clan.getLeastSignificantBits());
                os.writeByte(clanPlayer.role.ordinal());
                os.writeLong(clanPlayer.memberSinceUnix);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        os.send(p);
    }

    static void sendClansInit(EntityPlayer p) {
        VSP os = new VSP(9, "HCSMOD");
        try {
            os.writeByte(1);
            os.writeByte(ClansServer.config.maxMembers);
            for (Clan clan : ClansServer.clans.values()) {
                tempBytes.reset();
                ClansNetwork.writeCan(clan, tempOutput);
                if (os.written() + tempBytes.size() >= 32767) {
                    os.send(p);
                    os = new VSP(9, "HCSMOD");
                    os.writeByte(2);
                }
                tempBytes.writeTo((OutputStream)os);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        os.send(p);
    }

    static void broadcastClanCreated(Clan clan) {
        VSP os = new VSP(9, "HCSMOD");
        try {
            os.writeByte(2);
            ClansNetwork.writeCan(clan, (DataOutput)os);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        os.sendAll();
    }

    static void broadcastClanDeleted(Clan clan) {
        VSP os = new VSP(9, "HCSMOD");
        try {
            os.writeByte(3);
            ClansNetwork.writeCan(clan, (DataOutput)os);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        os.sendAll();
    }

    static void sendBaseList(EntityPlayer p) {
        VSP os = new VSP(9, "HCSMOD");
        try {
            os.writeByte(8);
            os.writeShort(ClansServer.config.capturePoints);
            os.writeShort(ClansServer.config.defencePoints);
            for (ServerBase clanBase : ClansServer.bases.values()) {
                os.writeLong(clanBase.id.getMostSignificantBits());
                os.writeLong(clanBase.id.getLeastSignificantBits());
                ClansMod.writeString((DataOutput)os, (String)clanBase.name);
                os.writeShort(clanBase.posX);
                os.writeByte(clanBase.posY);
                os.writeShort(clanBase.posZ);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if (p == null) {
            os.sendAll();
        } else {
            os.send(p);
        }
    }

    static void sendClanInfoResponse(EntityPlayerMP p, UUID uuid, ServerClan clan) throws IOException {
        VSP os = new VSP(9, "HCSMOD");
        os.writeByte(5);
        os.writeLong(uuid.getMostSignificantBits());
        os.writeLong(uuid.getLeastSignificantBits());
        clan.forEachMemberAndGuest(member -> {
            try {
                ClansMod.writeString((DataOutput)os, (String)member.name);
                os.writeByte(member.role.ordinal());
                os.writeLong(member.memberSinceUnix);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
        os.send((EntityPlayer)p);
    }

    static void sendMemberListResponse(EntityPlayerMP p, UUID uuid, ServerClan clan) throws IOException {
        VSP os = new VSP(9, "HCSMOD");
        os.writeByte(6);
        os.writeLong(uuid.getMostSignificantBits());
        os.writeLong(uuid.getLeastSignificantBits());
        clan.forEachMemberAndGuest(member -> {
            try {
                ClansMod.writeString((DataOutput)os, (String)member.name);
                os.writeByte(member.role.ordinal());
                os.writeLong(member.memberSinceUnix);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
        os.send((EntityPlayer)p);
    }

    static void sendBasesState(EntityPlayer p) {
        VSP os = new VSP(9, "HCSMOD");
        try {
            os.writeByte(9);
            for (ServerBase base : ClansServer.bases.values()) {
                os.writeLong(base.id.getMostSignificantBits());
                os.writeLong(base.id.getLeastSignificantBits());
                os.writeByte(base.state.ordinal());
                if (base.state == ClanBase.State.LOCKED) {
                    os.writeLong(base.capturedBy.getMostSignificantBits());
                    os.writeLong(base.capturedBy.getLeastSignificantBits());
                    os.writeInt(base.timer);
                    continue;
                }
                if (base.state == ClanBase.State.DEFENCE) {
                    os.writeLong(base.capturedBy.getMostSignificantBits());
                    os.writeLong(base.capturedBy.getLeastSignificantBits());
                    os.writeBoolean(base.depositCount > 0);
                    os.writeShort(base.timer);
                    os.writeShort(base.points);
                    os.writeByte(Math.min(base.alliesInRegion, 255));
                    os.writeByte(Math.min(base.enemiesInRegion, 255));
                    continue;
                }
                if (base.state != ClanBase.State.CAPTURING) continue;
                os.writeLong(base.capturedBy.getMostSignificantBits());
                os.writeLong(base.capturedBy.getLeastSignificantBits());
                os.writeShort(base.points);
                os.writeByte(Math.min(base.alliesInRegion, 255));
                os.writeByte(Math.min(base.enemiesInRegion, 255));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if (p == null) {
            os.sendAll();
        } else {
            os.send(p);
        }
    }
}

