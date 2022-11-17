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
 *  cpw.mods.fml.common.network.PacketDispatcher
 *  cpw.mods.fml.common.network.Player
 *  cpw.mods.fml.common.registry.GameRegistry
 *  cpw.mods.fml.common.registry.TickRegistry
 *  cpw.mods.fml.relauncher.Side
 *  jv
 *  net.minecraft.command.ICommand
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.item.Item
 *  net.minecraft.network.INetworkManager
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.Packet250CustomPayload
 *  net.minecraft.util.IntHashMap
 *  net.minecraft.util.MathHelper
 *  net.minecraftforge.common.MinecraftForge
 *  org.apache.commons.dbcp2.BasicDataSource
 *  vintarz.core.VSP
 */
package vintarz.tradesystem.server;

import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.invoke.LambdaMetafactory;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.function.BiConsumer;
import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.dbcp2.BasicDataSource;
import vintarz.core.VSP;
import vintarz.core.server.VCoreServer;
import vintarz.tradesystem.server.TradeCommand;
import vintarz.tradesystem.server.TradeData;
import vintarz.tradesystem.server.TradeEntry;
import vintarz.tradesystem.server.TradeItem;
import vintarz.tradesystem.server.TraderServer;

public class TradeSystemServer
implements ITickHandler,
IPacketHandler,
IPlayerTracker {
    public static final TradeSystemServer INSTANCE = new TradeSystemServer();
    private static final Map<EntityPlayerMP, TradeData> openTraders = new HashMap<EntityPlayerMP, TradeData>();
    private static final IntHashMap quantity = new IntHashMap();
    private static Packet250CustomPayload packetTraders;
    private static TradeData[] tradeData;
    static String TRADE_PREFIX;
    private static TraderServer[] traders;
    static final BasicDataSource ds;
    private long nextQuantityUpdate;

    public static void init() {
        TickRegistry.registerTickHandler((ITickHandler)INSTANCE, (Side)Side.SERVER);
        NetworkRegistry.instance().registerChannel((IPacketHandler)INSTANCE, "vtradesys", Side.SERVER);
        GameRegistry.registerPlayerTracker((IPlayerTracker)INSTANCE);
        MinecraftForge.EVENT_BUS.register((Object)INSTANCE);
        Properties properties = VCoreServer.initDatabaseConnectionPool(ds, new File("databases/trade.properties"));
        TRADE_PREFIX = properties.getProperty("trade_table");
    }

    public static void start(FMLServerStartingEvent ev) {
        TradeSystemServer.reload();
        ev.registerServerCommand((ICommand)new TradeCommand());
    }

    public static void close(EntityPlayerMP p) {
        openTraders.remove((Object)p).close(p);
    }

    public static void reload() {
        tradeData = null;
        packetTraders = new Packet250CustomPayload("vtradesys", new byte[]{0});
        PacketDispatcher.sendPacketToAllPlayers((Packet)packetTraders);
        openTraders.forEach((BiConsumer<jv, TradeData>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;Ljava/lang/Object;)V, lambda$reload$0(jv vintarz.tradesystem.server.TradeData ), (Ljv;Lvintarz/tradesystem/server/TradeData;)V)());
        openTraders.clear();
        VCoreServer.asyncExecutor.submit(() -> {
            try (Connection con = ds.getConnection();
                 Statement stmt = con.createStatement();){
                quantity.clearMap();
                ArrayList<TradeData> list = new ArrayList<TradeData>();
                ArrayList<String> tables = new ArrayList<String>();
                ResultSet rs = stmt.executeQuery("SHOW FULL TABLES IN admin_trade LIKE 'trade\\_" + TRADE_PREFIX + "%'");
                Object object = null;
                try {
                    while (rs.next()) {
                        tables.add(rs.getString(1));
                    }
                }
                catch (Throwable throwable) {
                    object = throwable;
                    throw throwable;
                }
                finally {
                    if (rs != null) {
                        if (object != null) {
                            try {
                                rs.close();
                            }
                            catch (Throwable throwable) {
                                ((Throwable)object).addSuppressed(throwable);
                            }
                        } else {
                            rs.close();
                        }
                    }
                }
                IntHashMap items = new IntHashMap();
                try (ResultSet rs2 = stmt.executeQuery("SELECT * FROM inventory_" + TRADE_PREFIX);){
                    while (rs2.next()) {
                        int id = rs2.getInt("id");
                        int redirect = rs2.getInt("redirect");
                        if (rs2.getInt("item_id") <= 0 || rs2.getInt("item_id") >= Item.itemsList.length || Item.itemsList[rs2.getInt("item_id")] == null) continue;
                        TradeItem item = new TradeItem(rs2.getInt("item_id"), rs2.getInt("item_meta"), id, (TradeItem)items.lookup(redirect), rs2.getInt("quantity"));
                        items.addKey(id, (Object)item);
                        quantity.addKey(id, (Object)item);
                    }
                }
                for (String table : tables) {
                    String name = table.split("_", 3)[2].replace("_", " ");
                    ArrayList<TradeEntry> entries = new ArrayList<TradeEntry>();
                    try (ResultSet rs3 = stmt.executeQuery("SELECT * FROM " + table);){
                        while (rs3.next()) {
                            TradeItem money = (TradeItem)items.lookup(rs3.getInt("money"));
                            TradeItem product = (TradeItem)items.lookup(rs3.getInt("product"));
                            if (money == null || product == null) continue;
                            TradeEntry entry = new TradeEntry(money, product, rs3.getInt("quantity"), rs3.getInt("cost_buy_full"), rs3.getInt("cost_buy_empty"), rs3.getInt("cost_sell_full"), rs3.getInt("cost_sell_empty"), rs3.getInt("full_count"));
                            entries.add(entry);
                        }
                    }
                    list.add(new TradeData(name, entries.toArray(new TradeEntry[0])));
                }
                VCoreServer.syncQueue.offer(() -> TradeSystemServer.loadTraders(list));
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private static void loadTraders(List<TradeData> input) {
        try (Scanner in = new Scanner(new File("traders.txt"));){
            ArrayList<TradeData> list = new ArrayList<TradeData>();
            ArrayList<TraderServer> traders = new ArrayList<TraderServer>();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bos);
            out.writeByte(0);
            while (in.hasNextLine()) {
                String[] str = in.nextLine().split(" ", 5);
                if (str.length != 5) continue;
                TradeData data = null;
                for (TradeData exchangeData : input) {
                    if (!exchangeData.name.equals(str[4])) continue;
                    data = exchangeData;
                    break;
                }
                if (data == null) continue;
                if (!list.contains(data)) {
                    list.add(data);
                }
                TraderServer trader = new TraderServer(Integer.parseInt(str[0]), Integer.parseInt(str[1]), Integer.parseInt(str[2]), Integer.parseInt(str[3]), data);
                out.writeLong(trader.exchange.check);
                out.writeShort(trader.x);
                out.writeShort(trader.z);
                out.writeByte(trader.y);
                traders.add(trader);
                byte[] name = trader.exchange.name.getBytes(StandardCharsets.UTF_8);
                int tmp = name.length & 0x3F | trader.r << 6;
                out.writeByte(tmp);
                out.write(name);
            }
            tradeData = list.toArray(new TradeData[0]);
            TradeSystemServer.traders = traders.toArray(new TraderServer[0]);
            packetTraders = new Packet250CustomPayload("vtradesys", bos.toByteArray());
            PacketDispatcher.sendPacketToAllPlayers((Packet)packetTraders);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void spawn(EntityPlayer player, String trader) {
        try (PrintStream ps = new PrintStream((OutputStream)new FileOutputStream(new File("traders.txt"), true), false, "UTF8");){
            ps.println(MathHelper.floor_double((double)player.u) + " " + MathHelper.floor_double((double)(player.v + 1.0)) + " " + MathHelper.floor_double((double)player.w) + " " + MathHelper.floor_float((float)(player.A / 90.0f + 0.5f)) + " " + trader);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        TradeSystemServer.reload();
    }

    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
        EntityPlayerMP p = (EntityPlayerMP)player;
        if (tradeData == null) {
            TradeSystemServer.sendError((EntityPlayer)p, "\u0421\u0438\u0441\u0442\u0435\u043c\u0430 \u0442\u043e\u0440\u0433\u043e\u0432\u043b\u0438 \u043f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0436\u0430\u0435\u0442\u0441\u044f.");
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(packet.data));
        try {
            int action = in.readUnsignedByte();
            TradeData open = openTraders.get((Object)p);
            if (action == 0 && open == null) {
                int id = in.readUnsignedByte();
                String error = "\u041e\u0431\u0440\u0430\u0442\u0438\u0441\u044c \u043a \u0442\u043e\u0440\u0433\u043e\u0432\u0446\u0443 \u0435\u0449\u0435 \u0440\u0430\u0437.";
                if (id >= traders.length || (error = (open = TradeSystemServer.traders[id].exchange).checkAccess(p, in.readLong(), traders[id])) != null) {
                    TradeSystemServer.sendError((EntityPlayer)p, error);
                    return;
                }
                open.openGui(p);
                openTraders.put(p, open);
            } else if (action == 1 && open != null) {
                open.buyOrSell(in.readUnsignedByte(), p);
            } else if (action == 2 && open != null) {
                open.sellConfirm(p);
            } else {
                TradeSystemServer.sendError((EntityPlayer)p, "\u041e\u0431\u0440\u0430\u0442\u0438\u0441\u044c \u043a \u0442\u043e\u0440\u0433\u043e\u0432\u0446\u0443 \u0435\u0449\u0435 \u0440\u0430\u0437.");
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static void modify(EntityPlayerMP p) {
        TradeData open = openTraders.get((Object)p);
        if (open == null) {
            return;
        }
        open.modify(p);
    }

    static void sendError(EntityPlayer p, String error) {
        VSP os = new VSP(1, "vtradesys");
        try {
            os.write(error.getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException iOException) {
            // empty catch block
        }
        os.send(p);
    }

    public void onPlayerLogin(EntityPlayer player) {
        if (packetTraders != null) {
            PacketDispatcher.sendPacketToPlayer((Packet)packetTraders, (Player)((Player)player));
        }
    }

    public void tickStart(EnumSet<TickType> type, Object ... tickData) {
        long now = System.currentTimeMillis();
        if (now < this.nextQuantityUpdate) {
            return;
        }
        this.nextQuantityUpdate = Long.MAX_VALUE;
        VCoreServer.asyncExecutor.submit(() -> {
            try (Connection con = ds.getConnection();
                 PreparedStatement quantityStatement = con.prepareStatement("SELECT id, quantity FROM inventory_" + TRADE_PREFIX + " WHERE redirect IS NULL");
                 ResultSet rs = quantityStatement.executeQuery();){
                while (rs.next()) {
                    TradeItem item = (TradeItem)quantity.lookup(rs.getInt(1));
                    if (item == null) continue;
                    item.quantity = rs.getInt(2);
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            finally {
                VCoreServer.syncQueue.offer(() -> {
                    if (tradeData != null) {
                        for (TradeData td : tradeData) {
                            td.updateQuantities();
                        }
                    }
                    this.nextQuantityUpdate = now + 3000L;
                });
            }
        });
    }

    public void tickEnd(EnumSet<TickType> type, Object ... tickData) {
    }

    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.SERVER);
    }

    public String getLabel() {
        return "ExchangerServerTH";
    }

    public void onPlayerLogout(EntityPlayer player) {
    }

    public void onPlayerChangedDimension(EntityPlayer player) {
    }

    public void onPlayerRespawn(EntityPlayer player) {
    }

    private static /* synthetic */ void lambda$reload$0(EntityPlayerMP k, TradeData v) {
        v.close(k);
    }

    static {
        ds = new BasicDataSource();
    }
}

