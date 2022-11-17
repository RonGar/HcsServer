/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.common.IPlayerTracker
 *  cpw.mods.fml.common.ITickHandler
 *  cpw.mods.fml.common.TickType
 *  cpw.mods.fml.common.event.FMLServerStartingEvent
 *  cpw.mods.fml.common.network.IPacketHandler
 *  cpw.mods.fml.common.network.NetworkRegistry
 *  cpw.mods.fml.common.network.Player
 *  cpw.mods.fml.common.registry.TickRegistry
 *  cpw.mods.fml.relauncher.Side
 *  net.minecraft.command.ICommand
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.inventory.ICrafting
 *  net.minecraft.inventory.IInventory
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.INetworkManager
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.Packet100OpenWindow
 *  net.minecraft.network.packet.Packet250CustomPayload
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.ChatMessageComponent
 *  org.apache.commons.dbcp2.BasicDataSource
 *  vintarz.core.VRP
 *  vintarz.core.VSP
 */
package vintarz.ingamestore.server;

import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet100OpenWindow;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import org.apache.commons.dbcp2.BasicDataSource;
import vintarz.core.VRP;
import vintarz.core.VSP;
import vintarz.core.server.VCoreServer;
import vintarz.ingamestore.server.ContainerShop;
import vintarz.ingamestore.server.InventoryShop;
import vintarz.ingamestore.server.PlayerBalance;
import vintarz.ingamestore.server.lss.RandomGroup;
import vintarz.ingamestore.server.lss.RandomUnit;

public class ShopServer
implements IPacketHandler,
ICommand,
IPlayerTracker,
ITickHandler {
    private static final BasicDataSource ds = new BasicDataSource();
    static String dbTableMoney;
    static String dbColumnUser;
    static String dbColumnMoney;
    static ShopServer instance;
    static final List<String> names;
    static final List<InventoryShop> invs;
    static byte[] packetData;
    static final Map<String, PlayerBalance> playerBalance;
    static long playerBalanceNextUpdate;
    final List<String> aliases = new ArrayList<String>();

    public static void init() {
        Properties conf = VCoreServer.initDatabaseConnectionPool(ds, new File("databases/shop.properties"));
        dbTableMoney = conf.getProperty("table");
        dbColumnUser = conf.getProperty("column_user");
        dbColumnMoney = conf.getProperty("column_money");
        instance = new ShopServer();
        NetworkRegistry.instance().registerChannel((IPacketHandler)instance, "vzshop", Side.SERVER);
        TickRegistry.registerTickHandler((ITickHandler)instance, (Side)Side.SERVER);
    }

    public static void start(FMLServerStartingEvent ev) {
        ShopServer.instance.aliases.add("vStore");
        ShopServer.instance.aliases.add("vstore");
        ShopServer.instance.aliases.add("vshop");
        ShopServer.instance.aliases.add("vs");
        ev.registerServerCommand((ICommand)instance);
        try {
            ShopServer.reload();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void reload() throws Exception {
        RandomUnit.read();
        RandomGroup.read();
        names.clear();
        invs.clear();
        Scanner in = new Scanner(MinecraftServer.getServer().getFile("vShop/categories.txt"), "UTF-8");
        while (in.hasNextLine()) {
            String line = in.nextLine();
            ArrayList<String> items = new ArrayList<String>();
            Scanner cat = new Scanner(MinecraftServer.getServer().getFile("vShop/categories/" + line + ".txt"), "UTF-8");
            while (cat.hasNextLine()) {
                items.add(cat.nextLine());
            }
            cat.close();
            names.add(line);
            invs.add(new InventoryShop(line, items));
        }
        in.close();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(names.size());
        for (String s : names) {
            dos.writeUTF(s);
        }
        dos.close();
        packetData = bos.toByteArray();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static boolean buy(int cost, EntityPlayer p) {
        PlayerBalance balance = playerBalance.get(p.getCommandSenderName());
        if (balance == null) return false;
        if (balance.balance < (long)cost) {
            return false;
        }
        try (Connection con = ds.getConnection();
             PreparedStatement stmt = con.prepareStatement("UPDATE `" + dbTableMoney + "` SET `" + dbColumnMoney + "` = `" + dbColumnMoney + "` - ? WHERE `" + dbColumnUser + "` = ? AND `" + dbColumnMoney + "` >= ?");){
            stmt.setQueryTimeout(1);
            stmt.setLong(1, cost);
            stmt.setString(2, p.getCommandSenderName());
            stmt.setLong(3, cost);
            if (stmt.executeUpdate() == 1) {
                balance.balance -= (long)cost;
                ShopServer.sendBalance(p, true);
                boolean bl = true;
                return bl;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        ShopServer.sendBalance(p, false);
        return false;
    }

    public static void sendBalance(final EntityPlayer p, boolean sendCached) {
        PlayerBalance balance = playerBalance.get(p.getCommandSenderName());
        if (sendCached && balance != null) {
            try {
                VSP os = new VSP(1, "vzshop");
                os.writeInt((int)balance.balance);
                os.send(p);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (balance == null) {
            balance = new PlayerBalance();
            playerBalance.put(p.getCommandSenderName(), balance);
        }
        if (balance.requesting) {
            return;
        }
        balance.requesting = true;
        final PlayerBalance finalBalance = balance;
        VCoreServer.asyncExecutor.execute(new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                int available = 0;
                try {
                    try (Connection con = ds.getConnection();
                         PreparedStatement stmt = con.prepareStatement("SELECT `" + dbColumnMoney + "` FROM `" + dbTableMoney + "` WHERE `" + dbColumnUser + "` = ?");){
                        stmt.setQueryTimeout(1);
                        stmt.setString(1, p.getCommandSenderName());
                        try (ResultSet rs = stmt.executeQuery();){
                            if (rs.next()) {
                                available = (int)rs.getDouble(dbColumnMoney);
                            }
                        }
                    }
                    int finalAvailable = available;
                    VCoreServer.syncQueue.add(new Runnable(this, finalAvailable){
                        final /* synthetic */ int val$finalAvailable;
                        final /* synthetic */ 1 this$0;
                        {
                            this.this$0 = this$0;
                            this.val$finalAvailable = n;
                        }

                        @Override
                        public void run() {
                            this.this$0.finalBalance.balance = this.val$finalAvailable;
                            this.this$0.finalBalance.requesting = false;
                            if (!this.this$0.p.T()) {
                                return;
                            }
                            try {
                                VSP os = new VSP(1, "vzshop");
                                os.writeInt(this.val$finalAvailable);
                                os.send(this.this$0.p);
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                catch (Exception e) {
                    try {
                        e.printStackTrace();
                        int finalAvailable = available;
                        VCoreServer.syncQueue.add(new /* invalid duplicate definition of identical inner class */);
                    }
                    catch (Throwable throwable) {
                        int finalAvailable = available;
                        VCoreServer.syncQueue.add(new /* invalid duplicate definition of identical inner class */);
                        throw throwable;
                    }
                }
            }
        });
    }

    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
        VRP p = new VRP(packet);
        EntityPlayerMP plr = (EntityPlayerMP)player;
        switch (p.type) {
            case 0: {
                try {
                    plr.closeScreen();
                    VSP os = new VSP(0, "vzshop");
                    os.write(packetData);
                    os.send((EntityPlayer)player);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case 1: {
                try {
                    plr.closeScreen();
                    byte cat = p.readByte();
                    InventoryShop inv = invs.get(cat);
                    if (plr.bp != plr.bo) {
                        plr.closeScreen();
                    }
                    plr.incrementWindowID();
                    plr.playerNetServerHandler.sendPacketToPlayer((Packet)new Packet100OpenWindow(plr.currentWindowId, 0, inv.b(), inv.j_(), inv.c()));
                    plr.bp = new ContainerShop((IInventory)plr.bn, inv);
                    plr.bp.windowId = plr.currentWindowId;
                    plr.bp.addCraftingToCrafters((ICrafting)plr);
                    ShopServer.sendBalance((EntityPlayer)plr, true);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case 2: {
                try {
                    VSP os = new VSP(2, "vzshop");
                    os.writeByte(RandomGroup.groups.size());
                    for (Map.Entry<String, RandomGroup> e : RandomGroup.groups.entrySet()) {
                        os.writeUTF(e.getValue().description);
                        os.writeShort(e.getValue().cost);
                    }
                    os.send((EntityPlayer)plr);
                    ShopServer.sendBalance((EntityPlayer)plr, true);
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
                break;
            }
            case 3: {
                try {
                    String group = p.readUTF();
                    RandomGroup g = RandomGroup.groups.get(group);
                    if (ShopServer.buy(g.cost, (EntityPlayer)plr)) {
                        List<ItemStack> items = g.getRandomLoot();
                        VSP os = new VSP(3, "vzshop");
                        os.writeByte(items.size());
                        for (ItemStack is : items) {
                            Packet.writeItemStack((ItemStack)is.copy(), (DataOutput)os);
                            plr.bn.addItemStackToInventory(is);
                            if (is.stackSize <= 0) continue;
                            plr.b(is);
                        }
                        os.send((EntityPlayer)plr);
                        break;
                    }
                    VSP os = new VSP(2, "vzshop");
                    os.writeByte(RandomGroup.groups.size());
                    for (Map.Entry<String, RandomGroup> e : RandomGroup.groups.entrySet()) {
                        os.writeUTF(e.getKey());
                        os.writeShort(e.getValue().cost);
                    }
                    os.send((EntityPlayer)plr);
                    ShopServer.sendBalance((EntityPlayer)plr, true);
                    break;
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        try {
            p.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int compareTo(Object arg0) {
        return 0;
    }

    public String c() {
        return "vShop";
    }

    public String c(ICommandSender icommandsender) {
        return "vShop reload";
    }

    public List b() {
        return this.aliases;
    }

    public void b(ICommandSender icommandsender, String[] astring) {
        if (astring.length == 1 && (astring[0].equalsIgnoreCase("reload") || astring[0].equalsIgnoreCase("r"))) {
            icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"[vShop] reloading..."));
            try {
                ShopServer.reload();
                icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("[vShop] loaded " + names.size() + " categories.")));
            }
            catch (Exception e) {
                icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("[vShop] error: " + e.getMessage())));
            }
        } else {
            icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText((String)this.c(icommandsender)));
        }
    }

    public boolean a(ICommandSender icommandsender) {
        return MinecraftServer.getServer().isSinglePlayer() || !(icommandsender instanceof EntityPlayer) || MinecraftServer.getServer().getConfigurationManager().getOps().contains(icommandsender.getCommandSenderName().toLowerCase().trim());
    }

    public List a(ICommandSender icommandsender, String[] astring) {
        return null;
    }

    public boolean a(String[] astring, int i) {
        return false;
    }

    public void onPlayerLogin(EntityPlayer player) {
    }

    public void onPlayerLogout(EntityPlayer player) {
        playerBalance.remove(player.getCommandSenderName());
    }

    public void onPlayerChangedDimension(EntityPlayer player) {
    }

    public void onPlayerRespawn(EntityPlayer player) {
    }

    public void tickStart(EnumSet<TickType> type, Object ... tickData) {
        long now = System.currentTimeMillis();
        if (now > playerBalanceNextUpdate) {
            playerBalanceNextUpdate = now + 3000L;
            Iterator<Map.Entry<String, PlayerBalance>> iterator = playerBalance.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, PlayerBalance> entry = iterator.next();
                if (entry.getValue().requesting) continue;
                EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(entry.getKey());
                if (player == null || !(player.openContainer instanceof ContainerShop)) {
                    iterator.remove();
                    continue;
                }
                ShopServer.sendBalance((EntityPlayer)player, false);
            }
        }
    }

    public void tickEnd(EnumSet<TickType> type, Object ... tickData) {
    }

    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.SERVER);
    }

    public String getLabel() {
        return "RetardedIngameStore";
    }

    static {
        names = new ArrayList<String>();
        invs = new ArrayList<InventoryShop>();
        packetData = new byte[]{0};
        playerBalance = new HashMap<String, PlayerBalance>();
        playerBalanceNextUpdate = 0L;
    }
}

