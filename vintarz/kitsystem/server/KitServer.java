/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.common.IPlayerTracker
 *  cpw.mods.fml.common.ITickHandler
 *  cpw.mods.fml.common.TickType
 *  cpw.mods.fml.common.network.IPacketHandler
 *  cpw.mods.fml.common.network.NetworkRegistry
 *  cpw.mods.fml.common.network.PacketDispatcher
 *  cpw.mods.fml.common.network.Player
 *  cpw.mods.fml.common.registry.GameRegistry
 *  cpw.mods.fml.common.registry.TickRegistry
 *  cpw.mods.fml.relauncher.Side
 *  net.minecraft.command.CommandHandler
 *  net.minecraft.command.ICommand
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.inventory.Slot
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.CompressedStreamTools
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.network.INetworkManager
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.Packet250CustomPayload
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.ChatMessageComponent
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.ForgeSubscribe
 *  net.minecraftforge.event.entity.EntityJoinWorldEvent
 *  vintarz.core.VRP
 *  vintarz.kitsystem.common.KitData
 *  vintarz.kitsystem.common.KitSystem
 */
package vintarz.kitsystem.server;

import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.StringJoiner;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import vintarz.core.VRP;
import vintarz.ingamestore.server.ContainerShop;
import vintarz.kitsystem.common.KitData;
import vintarz.kitsystem.common.KitSystem;
import vintarz.kitsystem.server.KitDataServer;

public class KitServer
implements IPacketHandler,
ICommand,
IPlayerTracker,
ITickHandler {
    public static final File root = new File("kitsystem");
    public static final KitServer kitServer = new KitServer();
    private static int[] kit_ids;
    private static KitDataServer[] kit_srv;
    private static Packet kits_packet;

    public static void init() {
        NetworkRegistry.instance().registerChannel((IPacketHandler)kitServer, "kitsys", Side.SERVER);
        MinecraftForge.EVENT_BUS.register((Object)kitServer);
        TickRegistry.registerTickHandler((ITickHandler)kitServer, (Side)Side.SERVER);
        GameRegistry.registerPlayerTracker((IPlayerTracker)kitServer);
    }

    public static void serverStart() {
        KitServer.reloadKits();
        CommandHandler cmdHandler = (CommandHandler)MinecraftServer.getServer().getCommandManager();
        cmdHandler.registerCommand((ICommand)kitServer);
    }

    public static void reloadKits() {
        File[] files = root.listFiles();
        if (files != null) {
            ArrayList<Integer> ids = new ArrayList<Integer>();
            ArrayList<KitDataServer> srv = new ArrayList<KitDataServer>();
            for (File f : files) {
                String name = f.getName();
                if (!name.endsWith(".kit")) continue;
                name = name.substring(0, name.length() - 4);
                try {
                    NBTTagCompound tag = CompressedStreamTools.read((File)f);
                    if (tag == null) continue;
                    KitDataServer kit = new KitDataServer(name, tag);
                    int index = ids.indexOf(kit.id);
                    if (index == -1) {
                        ids.add(kit.id);
                        srv.add(kit);
                        continue;
                    }
                    System.out.println("Failed to load kitsystem/" + f.getName());
                    System.out.println("Kit id " + kit.id + " is already taken by kit " + ((KitDataServer)srv.get((int)index)).name);
                }
                catch (IOException e) {
                    System.out.println("Failed to load kitsystem/" + f.getName());
                    e.printStackTrace(System.out);
                }
            }
            kit_ids = new int[ids.size()];
            kit_srv = srv.toArray((T[])new KitDataServer[srv.size()]);
            KitSystem.kit_ids = kit_ids;
            KitSystem.kit_data = new KitData[kit_ids.length];
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            try {
                out.writeByte(0);
                out.writeByte(kit_ids.length);
                for (int i = 0; i < kit_ids.length; ++i) {
                    KitServer.kit_ids[i] = (Integer)ids.get(i);
                    KitDataServer kit = kit_srv[i];
                    KitSystem.kit_data[i] = new KitData(kit.id, kit.name, kit.description);
                    out.writeShort(kit_ids[i]);
                    StringJoiner data = new StringJoiner("\n");
                    data.add(KitServer.kit_srv[i].name);
                    if (KitServer.kit_srv[i].description != null) {
                        for (String s : KitServer.kit_srv[i].description) {
                            data.add(s);
                        }
                    }
                    out.writeUTF(data.toString());
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
            kits_packet = new Packet250CustomPayload("kitsys", bytes.toByteArray());
        } else {
            kit_ids = null;
            kit_srv = null;
            KitSystem.kit_ids = null;
            KitSystem.kit_data = null;
            kits_packet = new Packet250CustomPayload("kitsys", new byte[]{0, 0});
        }
        PacketDispatcher.sendPacketToAllPlayers((Packet)kits_packet);
    }

    public static KitDataServer kitForId(int id) {
        if (kit_ids == null) {
            return null;
        }
        for (int i = 0; i < kit_ids.length; ++i) {
            if (id != kit_ids[i]) continue;
            return kit_srv[i];
        }
        return null;
    }

    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
        KitDataServer kit;
        ItemStack is;
        EntityPlayer p = (EntityPlayer)player;
        VRP in = new VRP(packet);
        if (in.type == 0 && (is = p.getCurrentEquippedItem()) != null && is.getItem() == KitSystem.item && (kit = KitServer.kitForId(is.getItemDamage())) != null) {
            p.inventory.mainInventory[p.inventory.currentItem] = null;
            if (!kit.giveKit(p, null)) {
                p.inventory.mainInventory[p.inventory.currentItem] = is;
            }
        }
    }

    private void sendKitDataToPlayer(EntityPlayer p) {
        if (kits_packet != null && p instanceof Player) {
            PacketDispatcher.sendPacketToPlayer((Packet)kits_packet, (Player)((Player)p));
        }
    }

    public void b(ICommandSender sender, String[] args) {
        if (args.length >= 1) {
            if (sender instanceof EntityPlayer && args.length >= 3 && "create".equals(args[0])) {
                EntityPlayer p = (EntityPlayer)sender;
                int id = Integer.parseInt(args[1]);
                KitDataServer kit = KitServer.kitForId(id);
                StringJoiner sj = new StringJoiner(" ");
                for (int i = 2; i < args.length; ++i) {
                    sj.add(args[i]);
                }
                String name = sj.toString();
                if (kit != null && !kit.name.equals(name)) {
                    p.a(ChatMessageComponent.createFromText((String)("ID " + id + " \u0443\u0436\u0435 \u0437\u0430\u043d\u044f\u0442 \u043d\u0430\u0431\u043e\u0440\u043e\u043c " + kit.name)));
                    return;
                }
                kit = new KitDataServer(name, id, p, kit == null ? null : kit.description);
                String result = kit.writeToKitSystemDir();
                p.a(ChatMessageComponent.createFromText((String)result));
                this.b(sender, new String[]{"reload"});
                return;
            }
            if (args.length >= 3 && "addinfo".equals(args[0])) {
                String string;
                int id = Integer.parseInt(args[1]);
                KitDataServer kit = KitServer.kitForId(id);
                if (kit == null) {
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u041d\u0435\u0442 \u043d\u0430\u0431\u043e\u0440\u0430 \u0441 id " + id)));
                    return;
                }
                StringJoiner line = new StringJoiner(" ");
                for (int i = 2; i < args.length; ++i) {
                    line.add(args[i]);
                }
                if (kit.description == null) {
                    kit.description = new String[1];
                } else {
                    String[] array = new String[kit.description.length + 1];
                    System.arraycopy(kit.description, 0, array, 0, kit.description.length);
                    kit.description = array;
                }
                kit.description[kit.description.length - 1] = string = line.toString().replace("$", "\u00a7");
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u041d\u0430\u0431\u043e\u0440\u0443 " + kit.name + " \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0430 \u0441\u0442\u0440\u043e\u043a\u0430 \u043e\u043f\u0438\u0441\u0430\u043d\u0438\u044f:")));
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)string));
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0434\u043b\u044f \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0438\u044f \u043e\u043f\u0438\u0441\u0430\u043d\u0438\u044f \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439 /kitsystem save"));
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0434\u043b\u044f \u043e\u0442\u043c\u0435\u043d\u044b \u043c\u043e\u0436\u0435\u0448\u044c \u043f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c /kitsystem reload"));
                return;
            }
            if (args.length >= 2 && "clearinfo".equals(args[0])) {
                int id = Integer.parseInt(args[1]);
                KitDataServer kit = KitServer.kitForId(id);
                if (kit == null) {
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u041d\u0435\u0442 \u043d\u0430\u0431\u043e\u0440\u0430 \u0441 id " + id)));
                    return;
                }
                kit.description = null;
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u041e\u043f\u0438\u0441\u0430\u043d\u0438\u0435 \u043d\u0430\u0431\u043e\u0440 " + kit.name + " \u043e\u0447\u0438\u0449\u0435\u043d\u043e.")));
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0434\u043b\u044f \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0438\u044f \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439 /kitsystem save"));
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0434\u043b\u044f \u043e\u0442\u043c\u0435\u043d\u044b \u0443\u0434\u0430\u043b\u0435\u043d\u0438\u044f /kitsystem reload"));
                return;
            }
            if ("list".equals(args[0])) {
                for (int i = 0; i < kit_ids.length; ++i) {
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)(kit_ids[i] + ": " + KitServer.kit_srv[i].name)));
                }
                return;
            }
            if ("reload".equals(args[0])) {
                KitServer.reloadKits();
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0421\u043f\u0438\u0441\u043e\u043a \u043d\u0430\u0431\u043e\u0440\u043e\u0432 \u043f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d:"));
                this.b(sender, new String[]{"list"});
                return;
            }
            if ("save".equals(args[0])) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0421\u043f\u0438\u0441\u043e\u043a \u043d\u0430\u0431\u043e\u0440\u043e\u0432 \u0441\u043e\u0445\u0440\u0430\u043d\u044f\u0435\u0442\u0441\u044f"));
                for (KitDataServer kit : kit_srv) {
                    kit.writeToKitSystemDir();
                }
                this.b(sender, new String[]{"reload"});
                return;
            }
        }
        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"/kitsystem create <id> <\u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435> (\u0442\u043e\u043b\u044c\u043a\u043e \u0438\u0433\u0440\u043e\u043a\u043e\u043c, \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u043c\u043e\u0436\u0435\u0442 \u0441\u043e\u0434\u0435\u0440\u0436\u0430\u0442\u044c \u043f\u0440\u043e\u0431\u0435\u043b\u044b \u0438 \u0440\u0443\u0441\u0441\u043a\u0438\u0435 \u0431\u0443\u043a\u0432\u044b)"));
        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"/kitsystem list"));
        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"/kitsystem reload"));
        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"/kitsystem save"));
        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"/kitsystem addinfo <id> <\u0441\u0442\u0440\u043e\u043a\u0430 \u043e\u043f\u0438\u0441\u0430\u043d\u0438\u044f>"));
        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"/kitsystem clearinfo <idF>"));
    }

    @ForgeSubscribe
    public void removeDroppedKit(EntityJoinWorldEvent ev) {
        EntityItem e;
        ItemStack is;
        if (ev.entity instanceof EntityItem && (is = (e = (EntityItem)ev.entity).getEntityItem()) != null && is.getItem() == KitSystem.item) {
            e.x();
            ev.setCanceled(true);
        }
    }

    private void removeOpenContainerKitItem(EntityPlayer p) {
        if (p.openContainer != p.inventoryContainer && !(p.openContainer instanceof ContainerShop)) {
            for (Slot slot : p.openContainer.inventorySlots) {
                ItemStack is = slot.getStack();
                if (is == null || is.getItem() != KitSystem.item) continue;
                slot.putStack(null);
            }
        }
    }

    public void onPlayerLogin(EntityPlayer player) {
        this.sendKitDataToPlayer(player);
    }

    public boolean a(ICommandSender sender) {
        return sender.canCommandSenderUseCommand(4, sender.getCommandSenderName());
    }

    public void onPlayerLogout(EntityPlayer player) {
    }

    public void onPlayerChangedDimension(EntityPlayer player) {
    }

    public void onPlayerRespawn(EntityPlayer player) {
    }

    public String c() {
        return "kitsystem";
    }

    public String c(ICommandSender icommandsender) {
        return null;
    }

    public List b() {
        return null;
    }

    public List a(ICommandSender icommandsender, String[] astring) {
        return null;
    }

    public boolean a(String[] astring, int i) {
        return false;
    }

    public int compareTo(Object o) {
        return 0;
    }

    public void tickStart(EnumSet<TickType> type, Object ... tickData) {
    }

    public void tickEnd(EnumSet<TickType> type, Object ... tickData) {
        this.removeOpenContainerKitItem((EntityPlayer)tickData[0]);
    }

    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.PLAYER);
    }

    public String getLabel() {
        return "KitSysTH";
    }
}

