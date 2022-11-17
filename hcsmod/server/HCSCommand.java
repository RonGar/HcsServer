/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.clans.ClansMod
 *  hcsmod.entity.EntityZombieDayZ
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.block.Block
 *  net.minecraft.command.CommandBase
 *  net.minecraft.command.CommandException
 *  net.minecraft.command.ICommand
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.ChatMessageComponent
 *  net.minecraft.util.MathHelper
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.EnumGameType
 */
package hcsmod.server;

import hcsmod.clans.ClansMod;
import hcsmod.entity.EntityZombieDayZ;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.EntityHouseServer;
import hcsmod.server.ExtendedDamageCalculator;
import hcsmod.server.ExtendedStorage;
import hcsmod.server.HarxCoreArmor;
import hcsmod.server.HcsServer;
import hcsmod.server.MapMarkersServer;
import hcsmod.server.PlayGift;
import hcsmod.server.RandomSpawn;
import hcsmod.server.SPacketHandler;
import hcsmod.server.STickHandler;
import hcsmod.server.airdrop.AirdropSystem;
import hcsmod.server.event.EventSystem;
import hcsmod.server.vote.VoteGift;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumGameType;

public class HCSCommand
implements ICommand {
    private static MinecraftServer mcs = MinecraftServer.getServer();
    public static RuntimeException forcedCrash;
    private List aliases = new ArrayList();
    private List actions;

    public HCSCommand() {
        this.aliases.add("hcs");
        this.aliases.add("h");
        this.actions = new ArrayList();
        this.actions.add("reload");
        this.actions.add("feed");
        this.actions.add("water");
        this.actions.add("creative");
        this.actions.add("survival");
        this.actions.add("adventure");
        this.actions.add("tppos");
        this.actions.add("randomRespawnReload");
        this.actions.add("heal");
        this.actions.add("itemInfo");
        this.actions.add("setItemDamage");
        this.actions.add("blood");
        this.actions.add("kick");
        this.actions.add("xray");
        this.actions.add("aprilFool");
        this.actions.add("dumpItemList");
        this.actions.add("addStartLoot");
        this.actions.add("halloween");
        this.actions.add("storageReload");
        this.actions.add("play-gift");
        this.actions.add("pos0");
        this.actions.add("pos1");
        this.actions.add("pos2");
        this.actions.add("randomTeleport");
        this.actions.add("triggersReload");
        this.actions.add("eventReload");
        this.actions.add("airdropReload");
    }

    public int compareTo(Object arg0) {
        return 0;
    }

    public String c() {
        return "hcs";
    }

    public String c(ICommandSender icommandsender) {
        return "/hcs <\u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435> [\u0430\u0440\u0433\u0443\u043c\u0435\u043d\u0442\u044b]  - \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u044f:\n/hcs opens [\u0438\u0433\u0440\u043e\u043a]  -  \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435\n/hcs feed [\u0438\u0433\u0440\u043e\u043a]  - \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u0430\u0432\u043b\u0438\u0432\u0430\u0435\u0442 \u0433\u043e\u043b\u043e\u0434\n/hcs water [\u0438\u0433\u0440\u043e\u043a]  - \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u0430\u0432\u043b\u0438\u0432\u0430\u0435\u0442 \u0436\u0430\u0436\u0434\u0443\n/hcs time <\u0432\u0440\u0435\u043c\u044f>  - \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c \u0432\u0440\u0435\u043c\u044f \u0432 \u043c\u0438\u0440\u0435\n<> - \u043e\u0431\u044f\u0437\u0430\u0442\u0435\u043b\u044c\u043d\u044b\u0439 \u043f\u0430\u0440\u0430\u043c\u0435\u0442\u0440, [] - \u043d\u0435\u043e\u0431\u044f\u0437\u0430\u0442\u0435\u043b\u044c\u043d\u044b\u0439.\n\u0432\u0440\u0435\u043c\u044f: day/night (d/n) \u0438\u043b\u0438 \u0447\u0438\u0441\u043b\u043e \u0432 \u0442\u0438\u043a\u0430\u0445.";
    }

    public List b() {
        return this.aliases;
    }

    public void b(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)this.c(sender)));
        } else if ("reload".equals(args[0])) {
            HarxCoreArmor.reload();
            ExtendedDamageCalculator.reload();
            HcsServer.reloadBannedItems();
            HcsServer.readStorageConfig();
            MapMarkersServer.reloadMapMarkers();
            HcsServer.reloadCPSData();
            HcsServer.readHCSConfig();
            VoteGift.readConfig();
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"hcsmod configs reloaded"));
        } else if ("feed".equals(args[0])) {
            Object p = args.length == 2 ? mcs.getConfigurationManager().getPlayerForUsername(args[1]) : (EntityPlayer)sender;
            if (p != null) {
                ExtendedPlayer.server((EntityPlayer)p).feed(1, 78000);
            } else {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0418\u0433\u0440\u043e\u043a \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d!"));
            }
        } else if ("water".equals(args[0])) {
            Object p = args.length == 2 ? mcs.getConfigurationManager().getPlayerForUsername(args[1]) : (EntityPlayer)sender;
            if (p != null) {
                ExtendedPlayer.server((EntityPlayer)p).water(1, 78000);
            } else {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0418\u0433\u0440\u043e\u043a \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d!"));
            }
        } else if ("creative".equals(args[0])) {
            EntityPlayer p = (EntityPlayer)sender;
            p.setGameType(EnumGameType.CREATIVE);
        } else if ("survival".equals(args[0])) {
            EntityPlayer p = (EntityPlayer)sender;
            p.setGameType(EnumGameType.SURVIVAL);
        } else if ("adventure".equals(args[0])) {
            EntityPlayer p = (EntityPlayer)sender;
            p.setGameType(EnumGameType.ADVENTURE);
        } else if ("tppos".equals(args[0]) && args.length == 4) {
            if (sender instanceof EntityPlayer) {
                try {
                    EntityPlayer ep = (EntityPlayer)sender;
                    ep.a((double)Integer.parseInt(args[1]), (double)Integer.parseInt(args[2]), (double)Integer.parseInt(args[3]));
                }
                catch (Throwable ep) {}
            }
        } else if ("randomRespawnReload".equals(args[0]) || "r".equals(args[0])) {
            RandomSpawn.readSpawns();
        } else if ("heal".equals(args[0])) {
            EntityPlayer p = (EntityPlayer)sender;
            p.aK();
            p.g(p.aT());
            ExtendedPlayer.server((EntityPlayer)p).feed(1, 78000);
            ExtendedPlayer.server((EntityPlayer)p).water(1, 78000);
        } else if ("itemInfo".equals(args[0])) {
            EntityPlayer p = (EntityPlayer)sender;
            if (p.o instanceof EntityHouseServer) {
                EntityHouseServer house = (EntityHouseServer)p.o;
                house.checkupTime = System.currentTimeMillis() - 1L;
                house.checkupDebug = 1;
                try {
                    house.checkupDebug = Integer.parseInt(args[1]);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                p.a(ChatMessageComponent.createFromText((String)("House debug enabled: " + house.checkupDebug)));
            } else {
                ItemStack is = p.getCurrentEquippedItem();
                ArrayList<String> desc = new ArrayList<String>();
                desc.add("In hand item info:");
                if (is != null) {
                    Item i = is.getItem();
                    desc.add("itemID: " + is.itemID);
                    desc.add("isDamageable: " + i.isDamageable());
                    desc.add("itemDamage: " + is.getItemDamage());
                    desc.add("maxDamage: " + is.getMaxDamage());
                    desc.add("UnlocalizedName: " + (Object)is.getItem());
                } else {
                    desc.add("Null item in hand");
                }
                for (String s : desc) {
                    p.a(ChatMessageComponent.createFromText((String)s));
                }
            }
        } else if ("setItemDamage".equals(args[0])) {
            EntityPlayer p = (EntityPlayer)sender;
            ItemStack is = p.getCurrentEquippedItem();
            if (is != null && is.isItemStackDamageable()) {
                is.setItemDamage(is.getMaxDamage() - 1);
            }
        } else if ("blood".equals(args[0])) {
            if (args.length == 2) {
                EntityPlayer p = (EntityPlayer)sender;
                p.g((float)Integer.parseInt(args[1]) / 600.0f);
            }
        } else if ("lcs".equals(args[0])) {
            if (args.length == 2) {
                HcsServer.lagCompensator.setSnapshots(Integer.parseInt(args[1]));
            }
        } else if ("CRASH".equals(args[0])) {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"Server will be crashed next tick"));
            forcedCrash = new RuntimeException("Forced server crash by " + sender.getCommandSenderName());
        } else if ("kick".equals(args[0])) {
            if (sender instanceof EntityPlayerMP) {
                EntityPlayerMP p = (EntityPlayerMP)sender;
                p.playerNetServerHandler.kickPlayerFromServer(null);
            }
        } else if ("xray".equals(args[0])) {
            if (sender instanceof EntityPlayerMP) {
                EntityPlayerMP p = (EntityPlayerMP)sender;
                p.setPositionAndUpdate(p.u, p.v - 2.0, p.w);
                p.q.setBlock(MathHelper.floor_double((double)p.u), MathHelper.floor_double((double)p.E.maxY), MathHelper.floor_double((double)p.w), Block.tnt.blockID);
            }
        } else if ("sleep".equals(args[0])) {
            if (args.length == 2) {
                STickHandler.SLEEP = Integer.parseInt(args[1]);
            }
        } else if ("banTest".equals(args[0])) {
            SPacketHandler.sendHint((EntityPlayer)sender, "banTest", "\u00a7e" + sender.getCommandSenderName() + " joined the game.", 20);
        } else if ("aprilFool".equals(args[0])) {
            boolean aprilFool = !HcsServer.aprilFool();
            try {
                aprilFool = Boolean.parseBoolean(args[1]);
            }
            catch (Throwable is) {
                // empty catch block
            }
            HcsServer.aprilFool(aprilFool);
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("AprilFool mode: " + aprilFool)));
        } else if ("kz".equals(args[0])) {
            try {
                for (Object o : MinecraftServer.getServer().worldServers[0].e) {
                    if (!(o instanceof EntityZombieDayZ)) continue;
                    ((EntityZombieDayZ)o).M = true;
                }
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        } else if ("iron".equals(args[0])) {
            if (args.length == 1) {
                EntityPlayer p = (EntityPlayer)sender;
                ExtendedStorage.get((ExtendedPlayer)ExtendedPlayer.server((EntityPlayer)p)).dropDelay = 0;
            }
        } else if ("dumpItemList".equals(args[0])) {
            try (PrintStream ps = new PrintStream(new File("dumpitems.txt"));){
                boolean flag = false;
                for (int i = 0; i < Item.itemsList.length; ++i) {
                    boolean empty;
                    boolean bl = empty = Item.itemsList[i] == null;
                    if (flag != empty) {
                        flag = empty;
                        ps.println(i + ": " + (flag ? "EMPTY START (INCLUSIVE)\n" : "EMPTY END (EXCLUSIVE)"));
                    }
                    if (empty) continue;
                    ps.println(i + ": " + Item.itemsList[i].getClass().getCanonicalName());
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        } else if ("addStartLoot".equals(args[0])) {
            if (sender instanceof EntityPlayerMP) {
                EntityPlayerMP p = (EntityPlayerMP)sender;
                RandomSpawn.addStartLoot((EntityPlayer)p);
            }
        } else if ("savedebug".equals(args[0]) && sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)sender;
            if (player.o instanceof EntityHouseServer) {
                if (args.length == 3 && "gen".equals(args[1])) {
                    int megabytes = Integer.parseUnsignedInt(args[2]);
                    EntityHouseServer house = (EntityHouseServer)player.o;
                    house.saveDebug = new byte[0x100000 * megabytes];
                    new SecureRandom().nextBytes(house.saveDebug);
                    int hash = Arrays.hashCode(house.saveDebug);
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("hash: " + Integer.toHexString(hash))));
                } else if (args.length == 3 && "rem".equals(args[1])) {
                    EntityHouseServer house = (EntityHouseServer)player.o;
                    int hash = Arrays.hashCode(house.saveDebug);
                    house.saveDebug = null;
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("hash: " + Integer.toHexString(hash))));
                } else {
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"savedebug gen size_in_megabytes"));
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"savedebug rem"));
                }
            } else {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0442\u043e\u043b\u044c\u043a\u043e \u0441\u0438\u0434\u044f \u0432 \u0434\u043e\u043c\u0435"));
            }
        } else if ("halloween".equals(args[0])) {
            try {
                HcsServer.overrideDimensionId = Boolean.parseBoolean(args[1]) ? 1 : 0;
            }
            catch (Throwable player) {
                // empty catch block
            }
            for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
                if (!(o instanceof EntityPlayerMP)) continue;
                SPacketHandler.sendOverrideDimension((EntityPlayer)o);
            }
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("halloween mode: " + HcsServer.overrideDimensionId)));
        } else if ("storageReload".equals(args[0])) {
            HcsServer.readStorageConfig();
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"Config Reloaded"));
        } else if ("play-gift".equals(args[0])) {
            if (args.length == 2 && "reload".equals(args[1])) {
                boolean result = PlayGift.reloadConfig();
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u041f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u043e: " + result)));
            } else if (args.length == 4 && "configure".equals(args[1])) {
                boolean result = PlayGift.setConfigProperty(args[2], args[3]);
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0421\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u043e: " + result)));
            } else {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"reload"));
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"configure <key> <value>"));
            }
        } else if ("pos0".equals(args[0])) {
            if (!(sender instanceof EntityPlayerMP)) {
                throw new CommandException("Only usable by player", new Object[0]);
            }
            EntityPlayer p = (EntityPlayer)sender;
            ExtendedStorage es = ExtendedStorage.get(ExtendedPlayer.server((EntityPlayer)p));
            es.removeSelectionBox(p);
        } else if ("pos1".equals(args[0])) {
            if (!(sender instanceof EntityPlayerMP)) {
                throw new CommandException("Only usable by player", new Object[0]);
            }
            EntityPlayer p = (EntityPlayer)sender;
            ExtendedStorage es = ExtendedStorage.get(ExtendedPlayer.server((EntityPlayer)p));
            Vec3 one = Vec3.createVectorHelper((double)p.u, (double)p.v, (double)p.w);
            es.setOrUpdateSelection(p, one, null);
        } else if ("pos2".equals(args[0])) {
            if (!(sender instanceof EntityPlayerMP)) {
                throw new CommandException("Only usable by player", new Object[0]);
            }
            EntityPlayer p = (EntityPlayer)sender;
            ExtendedStorage es = ExtendedStorage.get(ExtendedPlayer.server((EntityPlayer)p));
            Vec3 two = Vec3.createVectorHelper((double)p.u, (double)p.v, (double)p.w);
            es.setOrUpdateSelection(p, null, two);
        } else if ("randomTeleport".equals(args[0])) {
            if (args.length == 3) {
                EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(args[1]);
                if (p == null) {
                    return;
                }
                for (char ch : args[2].toCharArray()) {
                    if (ClansMod.inRange((char)ch, (char)'A', (char)'Z') || ClansMod.inRange((char)ch, (char)'a', (char)'z') || ClansMod.inRange((char)ch, (char)'0', (char)'9') || ch == '-' || ch == '_') continue;
                    return;
                }
                try {
                    ArrayList<String> locations = new ArrayList<String>();
                    try (BufferedReader r = new BufferedReader(new FileReader("hcsConfig/randomTeleport/" + args[2] + ".txt"));){
                        String line;
                        while ((line = r.readLine()) != null) {
                            locations.add(line);
                        }
                    }
                    String[] butt = ((String)locations.get(new SecureRandom().nextInt(locations.size()))).split(" ", 4);
                    long x = Long.parseLong(butt[0]);
                    long y = Long.parseLong(butt[1]);
                    long z = Long.parseLong(butt[2]);
                    p.a((double)((float)x + 0.5f), (double)((float)y + 0.5f), (double)((float)z + 0.5f));
                }
                catch (IOException locations) {}
            }
        } else if ("triggersReload".equals(args[0])) {
            HcsServer.reloadTriggers();
        } else if ("eventReload".equals(args[0])) {
            EventSystem.reloadConfig();
        } else if ("airdropReload".equals(args[0])) {
            AirdropSystem.reloadConfig();
        } else if ("lcd".equals(args[0])) {
            ExtendedStorage storage = ExtendedStorage.get(ExtendedPlayer.server((EntityPlayer)MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(args[1])));
            storage.lagCompMaxDebug = Integer.parseInt(args[2]);
            storage.applyLagCompDebugLimit();
        } else if ("lcv".equals(args[0]) && sender instanceof EntityPlayerMP) {
            EntityPlayerMP target = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(args[1]);
            ExtendedStorage targetStorage = ExtendedStorage.get(ExtendedPlayer.server((EntityPlayer)target));
            if (args.length == 2) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("available:" + targetStorage.lagCompDebug.size())));
                return;
            }
            targetStorage.sendDebug(Integer.parseInt(args[2]) - 1, (EntityPlayer)sender);
        } else {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)this.c(sender)));
        }
    }

    public boolean a(ICommandSender sender) {
        return sender.canCommandSenderUseCommand(4, sender.getCommandSenderName());
    }

    public List a(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return CommandBase.getListOfStringsFromIterableMatchingLastWord((String[])args, (Iterable)this.actions);
        }
        return null;
    }

    public boolean a(String[] args, int i) {
        return this.actions.contains(args[0]) && i == 1;
    }
}

