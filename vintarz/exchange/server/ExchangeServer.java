/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.mysql.jdbc.Driver
 *  cpw.mods.fml.common.IPlayerTracker
 *  cpw.mods.fml.common.ITickHandler
 *  cpw.mods.fml.common.TickType
 *  cpw.mods.fml.common.network.IPacketHandler
 *  cpw.mods.fml.common.network.NetworkRegistry
 *  cpw.mods.fml.common.network.Player
 *  cpw.mods.fml.common.registry.GameRegistry
 *  cpw.mods.fml.common.registry.TickRegistry
 *  cpw.mods.fml.relauncher.Side
 *  hcsmod.HCS
 *  hcsmod.effects.Effect
 *  hcsmod.items.ItemCustomGoldApple
 *  hcsmod.items.ItemHeal
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.INetworkManager
 *  net.minecraft.network.packet.Packet250CustomPayload
 *  net.minecraft.potion.Potion
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.ForgeSubscribe
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  org.apache.commons.dbcp2.BasicDataSource
 */
package vintarz.exchange.server;

import com.mysql.jdbc.Driver;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import hcsmod.HCS;
import hcsmod.effects.Effect;
import hcsmod.items.ItemCustomGoldApple;
import hcsmod.items.ItemHeal;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.SPacketHandler;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.potion.Potion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.apache.commons.dbcp2.BasicDataSource;
import vintarz.core.server.VCoreServer;
import vintarz.exchange.server.ExchangeEntry;

public class ExchangeServer
implements ITickHandler,
IPacketHandler,
IPlayerTracker {
    public static final ExchangeServer INSTANCE = new ExchangeServer();
    private static final Map<String, EntityPlayer> targetedPlayers = new HashMap<String, EntityPlayer>();
    private static final Map<EntityPlayer, ExchangeEntry> openExchanges = new HashMap<EntityPlayer, ExchangeEntry>();
    private static final Map<String, Integer> balance = new HashMap<String, Integer>();
    private static final List<ExchangeEntry> needsClosing = new ArrayList<ExchangeEntry>();
    private static long targetBroadcast;
    public static Set<String> moderators;
    static final BasicDataSource ds;
    static long nextDatabaseUpdate;

    public static void init() {
        NetworkRegistry.instance().registerChannel((IPacketHandler)INSTANCE, "vExchange", Side.SERVER);
        TickRegistry.registerTickHandler((ITickHandler)INSTANCE, (Side)Side.SERVER);
        GameRegistry.registerPlayerTracker((IPlayerTracker)INSTANCE);
        MinecraftForge.EVENT_BUS.register((Object)INSTANCE);
        VCoreServer.initDatabaseConnectionPool(ds, new File("databases/exchange.properties"));
    }

    private ExchangeServer() {
        try {
            new Driver();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
        EntityPlayerMP p = (EntityPlayerMP)player;
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(packet.data));
        try {
            int type = in.readUnsignedByte();
            if (type == 0) {
                this.unTarget((EntityPlayer)p);
            } else if (type == 1) {
                EntityPlayerMP target = (EntityPlayerMP)p.q.getEntityByID(in.readInt());
                if (target == null) {
                    return;
                }
                if (target.e((Entity)p) > 16.0) {
                    return;
                }
                if (HCS.isHealOtherItem((ItemStack)p.bn.getCurrentItem())) {
                    if (p.bn.getCurrentItem().getItem() instanceof ItemCustomGoldApple) {
                        p.bn.mainInventory[p.bn.currentItem] = null;
                        ExtendedPlayer.server((EntityPlayer)target).startHeal(1.0f);
                    } else {
                        ItemHeal ih = (ItemHeal)p.bn.getCurrentItem().getItem();
                        if (HCS.firstAidKit_b != null && p.bn.getCurrentItem().getItem().itemID == HCS.firstAidKit_b.cv && target.a((Potion)Effect.bleeding)) {
                            target.k(Effect.bleeding.H);
                        }
                        p.bn.getCurrentItem().setItemDamage(p.bn.getCurrentItem().getItemDamage() + 1);
                        if (p.bn.getCurrentItem().getItemDamage() >= p.bn.getCurrentItem().getMaxDamage()) {
                            p.bn.mainInventory[p.bn.currentItem] = null;
                        }
                        ExtendedPlayer.server((EntityPlayer)target).startHeal(ih.healing, (float)ih.healDamageCancel);
                    }
                    return;
                }
                if (moderators != null && p == targetedPlayers.get(target.bu)) {
                    ExchangeEntry ee1;
                    this.unTarget((EntityPlayer)target);
                    ExchangeEntry ee0 = new ExchangeEntry(p, target.bu);
                    ee0.othr = ee1 = new ExchangeEntry(target, p.bu);
                    ee1.othr = ee0;
                    openExchanges.put((EntityPlayer)p, ee0);
                    openExchanges.put((EntityPlayer)target, ee1);
                } else {
                    targetedPlayers.put(p.bu, (EntityPlayer)target);
                    this.sendTargetNotification((EntityPlayer)target, p.bu);
                }
            } else {
                ExchangeEntry ee = openExchanges.get((Object)p);
                if (ee == null) {
                    return;
                }
                if (type == 2) {
                    ExchangeServer.closeExchange(ee);
                } else if (type == 3) {
                    ee.addItem(in.readUnsignedByte());
                } else if (type == 4) {
                    ee.remItem(in.readUnsignedByte());
                } else if (type == 5) {
                    ee.setCash(in.readUnsignedShort());
                } else if (type == 6) {
                    ee.accept(in.readUnsignedByte());
                }
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private void sendTargetNotification(EntityPlayer target, String targetedBy) {
        SPacketHandler.sendHint(target, "exchtgt", targetedBy + " \u043f\u0440\u0435\u0434\u043b\u0430\u0433\u0430\u0435\u0442 \u043e\u0431\u043c\u0435\u043d", 255);
    }

    private void unTarget(EntityPlayer p) {
        EntityPlayer target = targetedPlayers.remove(p.username);
        if (target == null) {
            return;
        }
        SPacketHandler.sendHint(target, "exchtgt", null, 0);
    }

    private void closeExchange(EntityPlayer player) {
        ExchangeServer.closeExchange(openExchanges.remove((Object)player));
    }

    static void closeExchange(ExchangeEntry ee) {
        if (ee == null) {
            return;
        }
        ee.close();
        ee.othr.close();
        openExchanges.remove((Object)ee.player, ee);
        openExchanges.remove((Object)ee.othr.player, ee.othr);
    }

    @ForgeSubscribe
    public void onDeath(LivingDeathEvent e) {
        if (e.entityLiving instanceof EntityPlayer) {
            this.closeExchange((EntityPlayer)e.entityLiving);
        }
    }

    public void onPlayerLogout(EntityPlayer player) {
        this.unTarget(player);
        this.closeExchange(player);
    }

    private void databaseUpdate() {
        final long now = System.currentTimeMillis();
        if (now < nextDatabaseUpdate) {
            return;
        }
        nextDatabaseUpdate = Long.MAX_VALUE;
        balance.clear();
        for (EntityPlayer p : openExchanges.keySet()) {
            balance.put(p.getCommandSenderName(), 0);
        }
        VCoreServer.asyncExecutor.submit(() -> {
            block67: {
                boolean success = false;
                try {
                    try (Connection con = ds.getConnection();
                         PreparedStatement getmoney = con.prepareStatement("SELECT cash FROM ms_realmoney WHERE name = ?");
                         PreparedStatement getmoders = con.prepareStatement("SELECT name FROM bs_moders");){
                        final HashSet<String> moders = new HashSet<String>();
                        for (String player : balance.keySet()) {
                            getmoney.setString(1, player);
                            ResultSet rs = getmoney.executeQuery();
                            Throwable throwable = null;
                            try {
                                if (!rs.next()) continue;
                                balance.put(player, rs.getInt(1));
                            }
                            catch (Throwable throwable2) {
                                throwable = throwable2;
                                throw throwable2;
                            }
                            finally {
                                if (rs == null) continue;
                                if (throwable != null) {
                                    try {
                                        rs.close();
                                    }
                                    catch (Throwable throwable3) {
                                        throwable.addSuppressed(throwable3);
                                    }
                                    continue;
                                }
                                rs.close();
                            }
                        }
                        try (ResultSet rs = getmoders.executeQuery();){
                            while (rs.next()) {
                                moders.add(rs.getString(1).toLowerCase());
                            }
                        }
                        success = true;
                        VCoreServer.syncQueue.offer(new Runnable(){

                            @Override
                            public void run() {
                                moderators = moders;
                                for (ExchangeEntry ee : openExchanges.values()) {
                                    Integer cash = (Integer)balance.get(ee.player.c_());
                                    if (cash == null) continue;
                                    ee.sendMoneyToExchangers(cash);
                                }
                                nextDatabaseUpdate = now + 3000L;
                            }
                        });
                    }
                    if (success) break block67;
                    VCoreServer.syncQueue.offer(new Runnable(now){
                        final /* synthetic */ long val$now;
                        {
                            this.val$now = l;
                        }

                        @Override
                        public void run() {
                            nextDatabaseUpdate = this.val$now + 1000L;
                        }
                    });
                }
                catch (SQLException e) {
                    try {
                        e.printStackTrace();
                        if (success) break block67;
                        VCoreServer.syncQueue.offer(new /* invalid duplicate definition of identical inner class */);
                    }
                    catch (Throwable throwable) {
                        if (!success) {
                            VCoreServer.syncQueue.offer(new /* invalid duplicate definition of identical inner class */);
                        }
                        throw throwable;
                    }
                }
            }
        });
    }

    public void tickStart(EnumSet<TickType> type, Object ... tickData) {
        for (ExchangeEntry ee : openExchanges.values()) {
            if (ee.check(false)) continue;
            needsClosing.add(ee);
        }
        for (ExchangeEntry ee : needsClosing) {
            ExchangeServer.closeExchange(ee);
        }
        needsClosing.clear();
        long time = System.currentTimeMillis();
        if (targetBroadcast < time) {
            targetBroadcast = time + 10000L;
            for (Map.Entry<String, EntityPlayer> e : targetedPlayers.entrySet()) {
                this.sendTargetNotification(e.getValue(), e.getKey());
            }
        }
        this.databaseUpdate();
    }

    public void onPlayerLogin(EntityPlayer player) {
    }

    public void onPlayerChangedDimension(EntityPlayer player) {
    }

    public void onPlayerRespawn(EntityPlayer player) {
    }

    public void tickEnd(EnumSet<TickType> type, Object ... tickData) {
    }

    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.SERVER);
    }

    public String getLabel() {
        return "ExchangeServer";
    }

    static {
        ds = new BasicDataSource();
    }
}

