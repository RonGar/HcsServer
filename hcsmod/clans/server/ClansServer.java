/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  cpw.mods.fml.common.event.FMLServerAboutToStartEvent
 *  hcsmod.clans.ClansMod
 *  hcsmod.clans.common.Clan
 *  hcsmod.clans.common.ClanBase$State
 *  hcsmod.clans.common.ClanPlayer
 *  hcsmod.clans.common.ClanPlayer$Role
 *  hcsmod.common.HCSUtils
 *  hcsmod.player.ExtendedPlayer
 *  hn
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.item.ItemStack
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.management.ServerConfigurationManager
 *  net.minecraft.util.ChatMessageComponent
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.ForgeSubscribe
 *  net.minecraftforge.event.world.WorldEvent$Load
 *  net.minecraftforge.event.world.WorldEvent$Save
 *  vintarz.core.VSP
 */
package hcsmod.clans.server;

import com.google.gson.Gson;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import hcsmod.clans.ClansMod;
import hcsmod.clans.common.Clan;
import hcsmod.clans.common.ClanBase;
import hcsmod.clans.common.ClanPlayer;
import hcsmod.clans.server.ClansConfig;
import hcsmod.clans.server.ClansIO;
import hcsmod.clans.server.ClansNetwork;
import hcsmod.clans.server.ClansStore;
import hcsmod.clans.server.PlayerBaseCaptureOptions;
import hcsmod.clans.server.ServerBase;
import hcsmod.clans.server.ServerClan;
import hcsmod.common.HCSUtils;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.ExtendedStorage;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.invoke.LambdaMetafactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import vintarz.core.VSP;

public class ClansServer {
    public static ClansConfig config;
    public static final Map<UUID, ServerClan> writableClans;
    public static final Map<String, ClanPlayer> writablePlayers;
    public static final Map<UUID, ServerBase> writeableBases;
    public static final Map<UUID, ServerClan> clans;
    public static final Map<String, ClanPlayer> players;
    public static final Map<UUID, ServerBase> bases;
    public static long nextTick;
    public static final Map<String, PlayerBaseCaptureOptions> captureOptions;

    public static void init() {
        MinecraftForge.EVENT_BUS.register((Object)new ClansServer());
    }

    public static void tick() {
        long now = System.currentTimeMillis();
        if (nextTick > now) {
            return;
        }
        nextTick = now + 1000L;
        for (ServerBase base : bases.values()) {
            base.tick(now);
        }
        ClansNetwork.sendBasesState(null);
    }

    public static void serverAboutToStart(FMLServerAboutToStartEvent event) {
        Gson gson = new Gson();
        try (FileReader r = new FileReader("clans/config.json");){
            config = (ClansConfig)gson.fromJson((Reader)r, ClansConfig.class);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void playerJoined(EntityPlayer p) {
        if (!ClansServer.config.enabled) {
            ClansNetwork.sendClansDisabled(p);
            return;
        }
        ClansNetwork.sendClansInit(p);
        ClansNetwork.sendBaseList(p);
        ClanPlayer player = players.get(p.username);
        if (player != null && player.clan != null) {
            ClansNetwork.sendPlayerClan(p, player);
        }
        ClansNetwork.sendBasesState(p);
    }

    public static void playerQuit(EntityPlayer p) {
        captureOptions.remove(p.username);
    }

    public static void handleClansMessage(EntityPlayerMP p, DataInputStream input) throws IOException {
        if (!ClansServer.config.enabled) {
            throw new RuntimeException("clans packet on clans-disabled server: " + p.c_());
        }
        int type = input.readUnsignedByte();
        if (type == 0) {
            ClansServer.createClan(p, input);
        } else if (type == 1) {
            ClansServer.handleClanInfoRequest(p, input);
        } else if (type == 2) {
            ClansServer.handleMemberListRequest(p, input);
        } else if (type == 3) {
            ClansServer.handlePlayerSearchRequest(p, input);
        } else if (type == 4) {
            ClansServer.handleClanJoinFromClanList(p, input);
        } else if (type == 5) {
            ClansServer.handleClanJoinFromClanScreen(p, input);
        } else if (type == 6) {
            ClansServer.handleAcceptJoinRequest(p, input);
        } else if (type == 7) {
            ClansServer.handleTeleportToClanBase(p, input);
        } else if (type == 8) {
            ClansServer.handleKickFromClan(p, input);
        } else if (type == 9) {
            ClansServer.handleQuitClan(p, input);
        } else if (type == 10) {
            ClansServer.handleCaptureInfo(p, input);
        } else if (type == 11) {
            ClansServer.handleCaptureFinish((EntityPlayer)p, input);
        }
    }

    private static void handleCaptureFinish(EntityPlayer p, DataInputStream input) throws IOException {
        ItemStack is;
        int j;
        ClanPlayer clanPlayer = players.get(p.username);
        if (clanPlayer == null || clanPlayer.role.lowerThan(ClanPlayer.Role.OFFICER)) {
            return;
        }
        ServerClan clan = clans.get(clanPlayer.clan);
        if (clan == null) {
            return;
        }
        UUID baseID = new UUID(input.readLong(), input.readLong());
        ServerBase base = bases.get(baseID);
        if (base == null || !clanPlayer.clan.equals(base.capturedBy)) {
            return;
        }
        if (base.state == ClanBase.State.CAPTURING ? base.points < ClansServer.config.capturePoints : base.state != ClanBase.State.DEFENCE) {
            return;
        }
        PlayerBaseCaptureOptions playerBaseCaptureOptions = captureOptions.get(clanPlayer.name);
        int selectedOption = input.readUnsignedShort();
        if (selectedOption >= playerBaseCaptureOptions.options.size()) {
            return;
        }
        PlayerBaseCaptureOptions.Option captureOption = playerBaseCaptureOptions.options.get(selectedOption);
        int qtty = 0;
        for (j = 0; j < p.inventory.mainInventory.length; ++j) {
            is = p.inventory.mainInventory[j];
            if (is == null || is.itemID != captureOption.item) continue;
            qtty += is.stackSize;
        }
        if (qtty >= captureOption.cost) {
            qtty = captureOption.cost;
            for (j = 0; j < p.inventory.mainInventory.length; ++j) {
                is = p.inventory.mainInventory[j];
                if (is == null || is.itemID != captureOption.item) continue;
                int count = Math.min(is.stackSize, qtty);
                if ((is.stackSize -= count) <= 0) {
                    p.inventory.mainInventory[j] = null;
                }
                if ((qtty -= count) > 0) {
                    continue;
                }
                break;
            }
        } else {
            p.a(ChatMessageComponent.createFromText((String)"\u00a7c\u0422\u0435\u0431\u0435 \u043d\u0435\u0447\u0435\u043c \u043e\u043f\u043b\u0430\u0442\u0438\u0442\u044c \u0437\u0430\u043b\u043e\u0433!"));
            return;
        }
        int capturePoints = 1;
        if (playerBaseCaptureOptions.multiplyProtectionPoints) {
            capturePoints = selectedOption + 1;
        }
        base.defenceAtUnix = captureOption.unixTime;
        if (base.state == ClanBase.State.CAPTURING) {
            base.state = ClanBase.State.LOCKED;
            clan.capturedBase = base;
            clan.clanPoints += capturePoints;
            ClansNetwork.broadcastClanCreated(clan);
            base.tick(System.currentTimeMillis());
        } else {
            base.depositCount = capturePoints;
        }
        System.out.println(baseID + " " + selectedOption);
    }

    private static void handleCaptureInfo(EntityPlayerMP p, DataInputStream input) throws IOException {
        ClanPlayer clanPlayer = players.get(p.bu);
        if (clanPlayer == null || clanPlayer.role.lowerThan(ClanPlayer.Role.OFFICER)) {
            return;
        }
        UUID baseID = new UUID(input.readLong(), input.readLong());
        ServerBase base = bases.get(baseID);
        if (base == null || !clanPlayer.clan.equals(base.capturedBy)) {
            return;
        }
        if (base.state == ClanBase.State.CAPTURING ? base.points < ClansServer.config.capturePoints : base.state != ClanBase.State.DEFENCE) {
            return;
        }
        VSP os = new VSP(9, "HCSMOD");
        os.writeByte(10);
        os.writeShort(input.readUnsignedShort());
        PlayerBaseCaptureOptions playerBaseCaptureOptions = new PlayerBaseCaptureOptions();
        playerBaseCaptureOptions.calculateForCurrentTime();
        captureOptions.put(p.bu, playerBaseCaptureOptions);
        for (PlayerBaseCaptureOptions.Option option : playerBaseCaptureOptions.options) {
            ClansMod.writeString((DataOutput)os, (String)option.description);
            os.writeInt(option.item);
            os.writeShort(option.cost);
        }
        os.send((EntityPlayer)p);
    }

    private static void handleClanInfoRequest(EntityPlayerMP p, DataInputStream input) throws IOException {
        UUID uuid = new UUID(input.readLong(), input.readLong());
        ServerClan clan = clans.get(uuid);
        if (clan != null) {
            ClansNetwork.sendClanInfoResponse(p, uuid, clan);
        }
    }

    private static void handleMemberListRequest(EntityPlayerMP p, DataInputStream input) throws IOException {
        UUID uuid = new UUID(input.readLong(), input.readLong());
        ServerClan clan = clans.get(uuid);
        if (clan != null) {
            ClansNetwork.sendMemberListResponse(p, uuid, clan);
        }
    }

    private static void handleClanJoinFromClanList(EntityPlayerMP p, DataInputStream input) throws IOException {
        if (players.containsKey(p.bu)) {
            return;
        }
        UUID uuid = new UUID(input.readLong(), input.readLong());
        ServerClan clan = clans.get(uuid);
        if (clan == null) {
            return;
        }
        ClansServer.addPlayerToClan(clan, (EntityPlayer)p, ClanPlayer.Role.GUEST);
        ClansNetwork.sendMemberListResponse(p, uuid, clan);
    }

    private static void handleClanJoinFromClanScreen(EntityPlayerMP p, DataInputStream input) throws IOException {
        if (players.containsKey(p.bu)) {
            return;
        }
        UUID uuid = new UUID(input.readLong(), input.readLong());
        ServerClan clan = clans.get(uuid);
        if (clan == null) {
            return;
        }
        ClansServer.addPlayerToClan(clan, (EntityPlayer)p, ClanPlayer.Role.GUEST);
        ClansNetwork.sendClanInfoResponse(p, uuid, clan);
    }

    private static void handlePlayerSearchRequest(EntityPlayerMP p, DataInputStream input) throws IOException {
        int autism = input.readUnsignedShort();
        String playerName = ClansMod.readString((DataInput)input);
        VSP os = new VSP(9, "HCSMOD");
        os.writeByte(7);
        os.writeShort(autism);
        ClanPlayer clanPlayer = players.get(playerName);
        if (clanPlayer != null && clanPlayer.clan != null) {
            os.writeLong(clanPlayer.clan.getMostSignificantBits());
            os.writeLong(clanPlayer.clan.getLeastSignificantBits());
        }
        os.send((EntityPlayer)p);
    }

    private static void handleAcceptJoinRequest(EntityPlayerMP p, DataInputStream input) throws IOException {
        ClanPlayer clanPlayer = players.get(p.bu);
        if (clanPlayer == null || clanPlayer.clan == null || clanPlayer.role.lowerThan(ClanPlayer.Role.OFFICER)) {
            return;
        }
        ServerClan clan = clans.get(clanPlayer.clan);
        if (clan == null) {
            return;
        }
        if (clan.memberCount() >= ClansServer.config.maxMembers) {
            return;
        }
        ClanPlayer target = players.get(ClansMod.readString((DataInput)input));
        if (target == null || !clanPlayer.clan.equals(target.clan) || target.role != ClanPlayer.Role.GUEST) {
            return;
        }
        EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(target.name);
        if (player != null) {
            ClansServer.addPlayerToClan(clan, (EntityPlayer)player, ClanPlayer.Role.PRIVATE);
        } else {
            ClansServer.addPlayerToClan(clan, target.name, ClanPlayer.Role.PRIVATE);
        }
        ClansNetwork.sendClanInfoResponse(p, clanPlayer.clan, clan);
    }

    private static void handleTeleportToClanBase(EntityPlayerMP p, DataInputStream input) throws IOException {
        ClanPlayer clanPlayer = players.get(ClansMod.readString((DataInput)input));
        if (clanPlayer == null || clanPlayer.role.lowerThan(ClanPlayer.Role.PRIVATE)) {
            return;
        }
        ServerClan clan = clans.get(clanPlayer.clan);
        if (clan == null) {
            return;
        }
        ServerBase base = clan.capturedBase;
        if (base == null) {
            return;
        }
        ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
        if (ep == null) {
            return;
        }
        ExtendedStorage es = ExtendedStorage.get(ep);
        if (es == null) {
            return;
        }
        if (es.lastClanTeleport + (long)ClansServer.config.teleportCooldown * 1000L < System.currentTimeMillis()) {
            if (es.lastPVPtime + (long)ClansServer.config.teleportDelay * 1000L < System.currentTimeMillis() && es.lastWalkTime + (long)ClansServer.config.teleportDelay * 1000L < System.currentTimeMillis()) {
                es.lastClanTeleport = System.currentTimeMillis();
                p.closeScreen();
                p.setPositionAndUpdate((double)base.posX, (double)base.posY, (double)base.posZ);
            } else {
                long cooldown = Math.max(es.lastWalkTime, es.lastPVPtime);
                p.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0412\u044b \u043d\u0435\u0434\u0430\u0432\u043d\u043e \u0434\u0432\u0438\u0433\u0430\u043b\u0438\u0441\u044c \u0438\u043b\u0438 \u043f\u043e\u043b\u0443\u0447\u0430\u043b\u0438 \u0443\u0440\u043e\u043d! \u041d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u043e \u043f\u043e\u0434\u043e\u0436\u0434\u0430\u0442\u044c " + (cooldown + 1000L + (long)ClansServer.config.teleportDelay * 1000L - System.currentTimeMillis()) / 1000L + "c.")));
            }
        } else {
            int delay = (int)((es.lastClanTeleport + 1000L + (long)ClansServer.config.teleportCooldown * 1000L - System.currentTimeMillis()) / 1000L);
            String delayStr = HCSUtils.timerText((int)delay);
            p.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0421\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0439 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442 \u0431\u0443\u0434\u0435\u0442 \u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0447\u0435\u0440\u0435\u0437 " + delayStr)));
        }
    }

    private static void handleKickFromClan(EntityPlayerMP p, DataInputStream input) throws IOException {
        ClanPlayer clanPlayer = players.get(p.bu);
        if (clanPlayer == null || clanPlayer.clan == null || clanPlayer.role.lowerThan(ClanPlayer.Role.OFFICER)) {
            return;
        }
        ServerClan clan = clans.get(clanPlayer.clan);
        if (clan == null) {
            return;
        }
        ClanPlayer target = players.get(ClansMod.readString((DataInput)input));
        if (target == null || !clanPlayer.clan.equals(target.clan) || clanPlayer.role.loweOrEquals(target.role)) {
            return;
        }
        ClansServer.kickPlayer(target);
        ClansNetwork.sendClanInfoResponse(p, clanPlayer.clan, clan);
    }

    private static void handleQuitClan(EntityPlayerMP p, DataInputStream input) throws IOException {
        ClanPlayer clanPlayer = players.get(p.bu);
        if (clanPlayer == null) {
            return;
        }
        ClansServer.kickPlayer(clanPlayer);
    }

    private static void createClan(EntityPlayerMP p, DataInputStream input) throws IOException {
        char ch;
        if (input.available() < 7 || input.available() > 33) {
            p.playerNetServerHandler.kickPlayerFromServer("\u0422\u0432\u043e\u0439 \u043f\u0430\u0446\u043a\u0435\u0442\u0445\u0430\u0446\u043a \u043d\u0435\u0443\u0434\u0430\u043b\u0441\u044f");
            return;
        }
        ClanPlayer clanPlayer = players.get(p.c_());
        if (clanPlayer != null && clanPlayer.clan != null && clans.get(clanPlayer.clan) != null) {
            return;
        }
        ClansStore clansStore = ExtendedStorage.get((ExtendedPlayer)ExtendedPlayer.server((EntityPlayer)p)).clansStore;
        if (clansStore.clanCreateAttempt > 0) {
            return;
        }
        clansStore.clanCreateAttempt = 20;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; ++i) {
            ch = (char)input.readByte();
            if (!ClansMod.inRange((char)ch, (char)'A', (char)'Z') && !ClansMod.inRange((char)ch, (char)'0', (char)'9')) {
                p.playerNetServerHandler.kickPlayerFromServer("\u0422\u0432\u043e\u0439 \u043f\u0430\u0446\u043a\u0435\u0442\u0445\u0430\u0446\u043a \u043d\u0435\u0443\u0434\u0430\u043b\u0441\u044f");
                return;
            }
            sb.append(ch);
        }
        String tag = sb.toString();
        sb.setLength(0);
        while (input.available() > 0) {
            ch = (char)input.readByte();
            if (!(ClansMod.inRange((char)ch, (char)'A', (char)'Z') || ClansMod.inRange((char)ch, (char)'a', (char)'z') || ClansMod.inRange((char)ch, (char)'0', (char)'9') || ch == ' ')) {
                p.playerNetServerHandler.kickPlayerFromServer("\u0422\u0432\u043e\u0439 \u043f\u0430\u0446\u043a\u0435\u0442\u0445\u0430\u0446\u043a \u043d\u0435\u0443\u0434\u0430\u043b\u0441\u044f");
                return;
            }
            sb.append(ch);
        }
        String name = sb.toString();
        System.out.println(p.bu + " [" + tag + "]" + name);
        for (Clan clan : clans.values()) {
            if (tag.equals(clan.tag)) {
                return;
            }
            if (!name.equalsIgnoreCase(clan.name)) continue;
            return;
        }
        ServerClan clan = new ServerClan();
        clan.id = UUID.randomUUID();
        clan.tag = tag;
        clan.name = name;
        clan.createdAtUnix = System.currentTimeMillis() / 1000L;
        writableClans.put(clan.id, clan);
        ClanPlayer clanPlayer2 = ClansServer.addPlayerToClan(clan, (EntityPlayer)p, ClanPlayer.Role.HEAD);
        ClansNetwork.broadcastClanCreated(clan);
        ClansNetwork.sendPlayerClan((EntityPlayer)p, clanPlayer2);
    }

    public static void disbandClan(ServerClan clan) {
        writableClans.remove(clan.id);
        ClansNetwork.broadcastClanDeleted(clan);
        ServerConfigurationManager scm = MinecraftServer.getServer().getConfigurationManager();
        clan.forEachMemberAndGuest((Consumer<ClanPlayer>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)V, lambda$disbandClan$0(hn hcsmod.clans.common.ClanPlayer ), (Lhcsmod/clans/common/ClanPlayer;)V)((hn)scm));
    }

    private static void kickPlayer(ClanPlayer clanPlayer) {
        if (clanPlayer.role == ClanPlayer.Role.HEAD) {
            ServerClan clan = clans.get(clanPlayer.clan);
            if (clan != null && clan.capturedBase == null) {
                ClansServer.disbandClan(clan);
            }
            return;
        }
        writablePlayers.remove(clanPlayer.name, (Object)clanPlayer);
        if (clanPlayer.clan == null) {
            return;
        }
        ServerClan clan = clans.get(clanPlayer.clan);
        if (clan == null) {
            return;
        }
        clan.removeMember(clanPlayer);
        ServerConfigurationManager scm = MinecraftServer.getServer().getConfigurationManager();
        EntityPlayerMP p = scm.getPlayerForUsername(clanPlayer.name);
        if (p == null) {
            return;
        }
        ClansNetwork.sendPlayerClan((EntityPlayer)p, null);
    }

    private static ClanPlayer addPlayerToClan(ServerClan clan, EntityPlayer player, ClanPlayer.Role role) {
        ClanPlayer clanPlayer = ClansServer.addPlayerToClan(clan, player.username, role);
        ClansNetwork.sendPlayerClan(player, clanPlayer);
        return clanPlayer;
    }

    private static ClanPlayer addPlayerToClan(ServerClan clan, String playerName, ClanPlayer.Role role) {
        ClanPlayer clanPlayer = new ClanPlayer();
        clanPlayer.name = playerName;
        clanPlayer.clan = clan.id;
        clanPlayer.role = role;
        clanPlayer.memberSinceUnix = role == ClanPlayer.Role.HEAD ? clan.createdAtUnix : System.currentTimeMillis() / 1000L;
        clan.addMember(clanPlayer);
        ClanPlayer prev = writablePlayers.put(clanPlayer.name, clanPlayer);
        if (prev != null) {
            ClansServer.kickPlayer(prev);
        }
        return clanPlayer;
    }

    public static ClanPlayer getClanPlayer(String name) {
        ClanPlayer clanPlayer = players.get(name);
        if (clanPlayer == null) {
            return null;
        }
        if (clanPlayer.clan == null) {
            writablePlayers.remove(clanPlayer.name);
            return null;
        }
        Clan clan = clans.get(clanPlayer.clan);
        if (clan == null) {
            writablePlayers.remove(clanPlayer.name);
            return null;
        }
        return clanPlayer;
    }

    @ForgeSubscribe
    public void fuckMeLoad(WorldEvent.Load e) throws IOException {
        if (!e.world.isRemote && e.world.provider.dimensionId == 0) {
            ClansIO.load();
        }
    }

    @ForgeSubscribe
    public void fuckMeSave(WorldEvent.Save e) throws IOException {
        if (!e.world.isRemote && e.world.provider.dimensionId == 0) {
            ClansIO.save();
        }
    }

    public static String getPlayerClanTag(String playerName) {
        ClanPlayer player = players.get(playerName);
        if (player == null || player.role == ClanPlayer.Role.GUEST) {
            return "";
        }
        Clan clan = clans.get(player.clan);
        if (clan == null) {
            return "";
        }
        return clan.tag;
    }

    private static /* synthetic */ void lambda$disbandClan$0(ServerConfigurationManager scm, ClanPlayer member) {
        writablePlayers.remove(member.name);
        EntityPlayerMP p = scm.getPlayerForUsername(member.name);
        if (p == null) {
            return;
        }
        ClansNetwork.sendPlayerClan((EntityPlayer)p, null);
    }

    static {
        writableClans = new LinkedHashMap<UUID, ServerClan>();
        writablePlayers = new LinkedHashMap<String, ClanPlayer>();
        writeableBases = new HashMap<UUID, ServerBase>();
        clans = Collections.unmodifiableMap(writableClans);
        players = Collections.unmodifiableMap(writablePlayers);
        bases = Collections.unmodifiableMap(writeableBases);
        captureOptions = new HashMap<String, PlayerBaseCaptureOptions>();
    }
}

