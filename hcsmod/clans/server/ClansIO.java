/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.clans.common.Clan
 *  hcsmod.clans.common.ClanBase$State
 *  hcsmod.clans.common.ClanPlayer
 *  hcsmod.clans.common.ClanPlayer$Role
 *  net.minecraft.nbt.CompressedStreamTools
 *  net.minecraft.nbt.NBTBase
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.nbt.NBTTagList
 */
package hcsmod.clans.server;

import hcsmod.clans.common.Clan;
import hcsmod.clans.common.ClanBase;
import hcsmod.clans.common.ClanPlayer;
import hcsmod.clans.server.ClansServer;
import hcsmod.clans.server.ServerBase;
import hcsmod.clans.server.ServerClan;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ClansIO {
    private static final Path clansSaveFile = Paths.get("clans/save.dat", new String[0]);

    public static void load() throws IOException {
        NBTTagCompound tag = null;
        if (Files.isRegularFile(clansSaveFile, new LinkOption[0])) {
            try (InputStream in = Files.newInputStream(clansSaveFile, new OpenOption[0]);){
                tag = CompressedStreamTools.readCompressed((InputStream)in);
            }
        }
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        ClansIO.loadClans(tag.getTagList("clans"));
        ClansIO.loadPlayers(tag.getTagList("players"));
        ClansIO.loadBases(tag.getTagList("bases"));
    }

    public static void save() throws IOException {
        NBTTagCompound tag = ClansIO.createNbt();
        try (OutputStream out = Files.newOutputStream(clansSaveFile, new OpenOption[0]);){
            CompressedStreamTools.writeCompressed((NBTTagCompound)tag, (OutputStream)out);
        }
    }

    public static NBTTagCompound createNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("clans", (NBTBase)ClansIO.createClansNbt());
        tag.setTag("players", (NBTBase)ClansIO.createPlayersNbt());
        tag.setTag("bases", (NBTBase)ClansIO.createBasesNbt());
        return tag;
    }

    private static void loadClans(NBTTagList clansList) {
        ClansServer.writableClans.clear();
        for (int i = 0; i < clansList.tagCount(); ++i) {
            NBTTagCompound clanTag = (NBTTagCompound)clansList.tagAt(i);
            ServerClan clan = new ServerClan();
            clan.id = new UUID(clanTag.getLong("id_most"), clanTag.getLong("id_least"));
            clan.tag = clanTag.getString("tag");
            clan.name = clanTag.getString("name");
            clan.createdAtUnix = clanTag.getLong("createdAt");
            clan.clanPoints = clanTag.getInteger("clanPoints");
            ClansServer.writableClans.put(clan.id, clan);
        }
    }

    private static void loadPlayers(NBTTagList playerList) {
        ClansServer.writablePlayers.clear();
        for (int i = 0; i < playerList.tagCount(); ++i) {
            NBTTagCompound playerTag = (NBTTagCompound)playerList.tagAt(i);
            ClanPlayer player = new ClanPlayer();
            player.name = playerTag.getString("name");
            player.clan = new UUID(playerTag.getLong("clan_id_most"), playerTag.getLong("clan_id_least"));
            player.role = (ClanPlayer.Role)ClanPlayer.Role.valueOf(ClanPlayer.Role.class, (String)playerTag.getString("role"));
            player.memberSinceUnix = playerTag.getLong("memberSince");
            ServerClan clan = ClansServer.clans.get(player.clan);
            if (clan == null) continue;
            ClansServer.writablePlayers.put(player.name, player);
            clan.addMember(player);
        }
    }

    private static void loadBases(NBTTagList basesList) {
        ClansServer.writeableBases.clear();
        for (int i = 0; i < basesList.tagCount(); ++i) {
            NBTTagCompound baseTag = (NBTTagCompound)basesList.tagAt(i);
            ServerBase base = new ServerBase();
            base.id = new UUID(baseTag.getLong("id_most"), baseTag.getLong("id_least"));
            base.name = baseTag.getString("name");
            base.posX = baseTag.getShort("posX");
            base.posY = baseTag.getShort("posY");
            base.posZ = baseTag.getShort("posZ");
            base.captureRegion.minX = baseTag.getDouble("captureX0");
            base.captureRegion.minY = baseTag.getDouble("captureY0");
            base.captureRegion.minZ = baseTag.getDouble("captureZ0");
            base.captureRegion.maxX = baseTag.getDouble("captureX1");
            base.captureRegion.maxY = baseTag.getDouble("captureY1");
            base.captureRegion.maxZ = baseTag.getDouble("captureZ1");
            base.protectionRegion.minX = baseTag.getDouble("protectionX0");
            base.protectionRegion.minY = baseTag.getDouble("protectionY0");
            base.protectionRegion.minZ = baseTag.getDouble("protectionZ0");
            base.protectionRegion.maxX = baseTag.getDouble("protectionX1");
            base.protectionRegion.maxY = baseTag.getDouble("protectionY1");
            base.protectionRegion.maxZ = baseTag.getDouble("protectionZ1");
            base.state = ClanBase.State.valueOf((String)baseTag.getString("state"));
            base.capturedBy = new UUID(baseTag.getLong("captured_id_most"), baseTag.getLong("captured_id_least"));
            base.capturedAtUnix = baseTag.getLong("capturedAt");
            base.defenceAtUnix = baseTag.getLong("defenceAt");
            base.points = baseTag.getInteger("points");
            base.depositCount = baseTag.getInteger("depositCount");
            ClansServer.writeableBases.put(base.id, base);
            ServerClan clan = ClansServer.clans.get(base.capturedBy);
            if (clan == null) continue;
            if (clan.capturedBase != null) {
                throw new RuntimeException("Clan [" + clan.tag + "] has 2 captured bases: \"" + (Object)((Object)clan.capturedBase) + "\" and \"" + base.name + "\"");
            }
            clan.capturedBase = base;
        }
    }

    private static NBTTagList createClansNbt() {
        NBTTagList clansList = new NBTTagList();
        for (ServerClan clan : ClansServer.clans.values()) {
            if (!ClansIO.validateClan(clan)) continue;
            NBTTagCompound clanTag = new NBTTagCompound();
            clanTag.setLong("id_most", clan.id.getMostSignificantBits());
            clanTag.setLong("id_least", clan.id.getLeastSignificantBits());
            clanTag.setString("tag", clan.tag);
            clanTag.setString("name", clan.name);
            clanTag.setLong("createdAt", clan.createdAtUnix);
            clanTag.setInteger("clanPoints", clan.clanPoints);
            clansList.appendTag((NBTBase)clanTag);
        }
        return clansList;
    }

    private static NBTTagList createPlayersNbt() {
        NBTTagList playersList = new NBTTagList();
        for (ClanPlayer player : ClansServer.players.values()) {
            Clan clan = ClansServer.clans.get(player.clan);
            if (clan == null) continue;
            NBTTagCompound playerTag = new NBTTagCompound();
            playerTag.setString("name", player.name);
            playerTag.setLong("clan_id_most", clan.id.getMostSignificantBits());
            playerTag.setLong("clan_id_least", clan.id.getLeastSignificantBits());
            playerTag.setString("role", player.role.name());
            playerTag.setLong("memberSince", player.memberSinceUnix);
            playersList.appendTag((NBTBase)playerTag);
        }
        return playersList;
    }

    private static NBTTagList createBasesNbt() {
        NBTTagList basesList = new NBTTagList();
        for (ServerBase base : ClansServer.bases.values()) {
            NBTTagCompound baseTag = new NBTTagCompound();
            baseTag.setLong("id_most", base.id.getMostSignificantBits());
            baseTag.setLong("id_least", base.id.getLeastSignificantBits());
            baseTag.setString("name", base.name);
            baseTag.setShort("posX", (short)base.posX);
            baseTag.setShort("posY", (short)base.posY);
            baseTag.setShort("posZ", (short)base.posZ);
            baseTag.setString("state", base.state.name());
            baseTag.setDouble("captureX0", base.captureRegion.minX);
            baseTag.setDouble("captureY0", base.captureRegion.minY);
            baseTag.setDouble("captureZ0", base.captureRegion.minZ);
            baseTag.setDouble("captureX1", base.captureRegion.maxX);
            baseTag.setDouble("captureY1", base.captureRegion.maxY);
            baseTag.setDouble("captureZ1", base.captureRegion.maxZ);
            baseTag.setDouble("protectionX0", base.protectionRegion.minX);
            baseTag.setDouble("protectionY0", base.protectionRegion.minY);
            baseTag.setDouble("protectionZ0", base.protectionRegion.minZ);
            baseTag.setDouble("protectionX1", base.protectionRegion.maxX);
            baseTag.setDouble("protectionY1", base.protectionRegion.maxY);
            baseTag.setDouble("protectionZ1", base.protectionRegion.maxZ);
            Clan clan = ClansServer.clans.get(base.capturedBy);
            if (clan != null) {
                baseTag.setLong("captured_id_most", clan.id.getMostSignificantBits());
                baseTag.setLong("captured_id_least", clan.id.getLeastSignificantBits());
            }
            baseTag.setLong("capturedAt", base.capturedAtUnix);
            baseTag.setLong("defenceAt", base.defenceAtUnix);
            baseTag.setInteger("points", base.points);
            baseTag.setInteger("depositCount", base.depositCount);
            basesList.appendTag((NBTBase)baseTag);
        }
        return basesList;
    }

    private static boolean validateClan(ServerClan clan) {
        clan.validateMembers();
        return clan.clanPoints > 0 || clan.memberAndGuestCount() >= 2;
    }
}

