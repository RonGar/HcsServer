/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.common.network.PacketDispatcher
 *  cpw.mods.fml.common.network.Player
 *  jv
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.Packet250CustomPayload
 *  net.minecraft.util.MathHelper
 *  vintarz.core.VSP
 *  vintarz.tradesystem.TradeSystem
 *  ye
 */
package vintarz.tradesystem.server;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import hcsmod.server.SPacketHandler;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.MathHelper;
import vintarz.core.VSP;
import vintarz.core.server.VCoreServer;
import vintarz.tradesystem.TradeSystem;
import vintarz.tradesystem.server.TradeEntry;
import vintarz.tradesystem.server.TradeItem;
import vintarz.tradesystem.server.TradeSystemServer;
import vintarz.tradesystem.server.TraderServer;

class TradeData {
    static final String TRY_AGAIN = "\u041e\u0431\u0440\u0430\u0442\u0438\u0441\u044c \u043a \u0442\u043e\u0440\u0433\u043e\u0432\u0446\u0443 \u0435\u0449\u0435 \u0440\u0430\u0437.";
    private final List<EntityPlayerMP> open = new ArrayList<EntityPlayerMP>();
    final String name;
    final long check = System.nanoTime();
    private final TradeEntry[] entries;
    private final Packet250CustomPayload packetGui;
    private Packet250CustomPayload packetPrices;
    private final TradeItem[] items;
    private final int[] quantity;
    private final Map<EntityPlayer, SellEntry> sell = new HashMap<EntityPlayer, SellEntry>();

    TradeData(String name, TradeEntry[] entries) {
        this.name = name;
        this.entries = entries;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);
        ArrayList<TradeItem> items = new ArrayList<TradeItem>();
        try {
            out.writeByte(2);
            byte[] data = name.getBytes(StandardCharsets.UTF_8);
            out.writeByte(data.length);
            out.write(data);
            for (TradeEntry tradeEntry : entries) {
                if (!items.contains(tradeEntry.money)) {
                    items.add(tradeEntry.money);
                }
                if (items.contains(tradeEntry.product)) continue;
                items.add(tradeEntry.product);
            }
            out.writeByte(items.size());
            for (TradeItem item : items) {
                out.writeShort(item.id);
                out.writeShort(item.meta);
            }
            for (TradeEntry tradeEntry : entries) {
                out.writeByte(items.indexOf(tradeEntry.money));
                out.writeByte(items.indexOf(tradeEntry.product));
                out.writeByte(tradeEntry.quantity);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.items = items.toArray(new TradeItem[items.size()]);
        this.quantity = new int[this.items.length];
        this.packetGui = new Packet250CustomPayload("vtradesys", bos.toByteArray());
        this.updateQuantities();
    }

    void updateQuantities() {
        boolean needUpdate = this.packetPrices == null;
        for (int i = 0; i < this.items.length; ++i) {
            if (this.quantity[i] == this.items[i].quantity) continue;
            this.quantity[i] = this.items[i].quantity;
            needUpdate = true;
        }
        if (needUpdate) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bos);
            try {
                out.writeByte(3);
                for (TradeItem item : this.items) {
                    TradeSystem.write3byte((int)(item.qRdrct == null ? item.quantity : item.qRdrct.quantity / item.quantity), (DataOutput)out);
                }
                for (TradeEntry entry : this.entries) {
                    out.writeShort(entry.getBuyPrice());
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
            this.packetPrices = new Packet250CustomPayload("vtradesys", bos.toByteArray());
            for (EntityPlayerMP p : this.open) {
                p.playerNetServerHandler.sendPacketToPlayer((Packet)this.packetPrices);
            }
        }
    }

    void openGui(EntityPlayerMP p) {
        p.openGui((Object)TradeSystem.instance, 0, null, 0, 0, 0);
        this.open.add(p);
        PacketDispatcher.sendPacketToPlayer((Packet)this.packetGui, (Player)((Player)p));
        PacketDispatcher.sendPacketToPlayer((Packet)this.packetPrices, (Player)((Player)p));
    }

    void buyOrSell(int id, EntityPlayerMP p) {
        ItemStack is;
        int j;
        TradeEntry entry = this.entries[id];
        if (p.bn.getItemStack() != null) {
            ItemStack is2 = p.bn.getItemStack();
            if (entry.product.id == is2.itemID && (is2.isItemStackDamageable() && is2.stackSize == 1 || is2.getItemDamage() == entry.product.meta)) {
                int sellPrice = entry.getSellPrice();
                sellPrice *= is2.stackSize;
                sellPrice = MathHelper.floor_float((float)((float)sellPrice / (float)entry.quantity));
                if (is2.isItemStackDamageable()) {
                    float integrity = is2.getMaxDamage();
                    integrity = (integrity - (float)is2.getItemDamage()) / integrity;
                    sellPrice = MathHelper.floor_float((float)((float)sellPrice * integrity));
                }
                SellEntry sell = new SellEntry(entry, p.bn.getItemStack(), sellPrice);
                this.sell.put((EntityPlayer)p, sell);
                VSP os = new VSP(4, "vtradesys");
                try {
                    os.writeShort(entry.money.id);
                    os.writeShort(sellPrice);
                    os.writeShort(entry.money.meta);
                    Packet.writeItemStack((ItemStack)is2, (DataOutput)os);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                os.send((EntityPlayer)p);
            }
            return;
        }
        int price = entry.getBuyPrice();
        int qtty = 0;
        for (j = 0; j < p.bn.mainInventory.length; ++j) {
            is = p.bn.mainInventory[j];
            if (is == null || is.itemID != entry.money.id || is.getItemDamage() != entry.money.meta) continue;
            qtty += is.stackSize;
        }
        if (qtty >= price) {
            qtty = price;
            for (j = 0; j < p.bn.mainInventory.length; ++j) {
                is = p.bn.mainInventory[j];
                if (is == null || is.itemID != entry.money.id || is.getItemDamage() != entry.money.meta) continue;
                int count = Math.min(is.stackSize, qtty);
                if ((is.stackSize -= count) <= 0) {
                    p.bn.mainInventory[j] = null;
                }
                if ((qtty -= count) <= 0) break;
            }
            SPacketHandler.sendHint((EntityPlayer)p, "vtrade", "\u0422\u043e\u0440\u0433\u043e\u0432\u0435\u0446:\n\u0414\u043e\u0436\u0434\u0438\u0441\u044c \u043e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u044f \u043f\u043e\u043a\u0443\u043f\u043a\u0438.\n\u0415\u0441\u043b\u0438 \u0432\u043e\u0437\u043d\u0438\u043a\u043d\u0443\u0442 \u043f\u0440\u043e\u0431\u043b\u0435\u043c\u044b\n\u0442\u043e \u044f \u0432\u0435\u0440\u043d\u0443 \u0442\u0435\u0431\u0435 \u0434\u0435\u043d\u044c\u0433\u0438.\n\u041d\u0435 \u0432\u044b\u0445\u043e\u0434\u0438 \u0441 \u0441\u0435\u0440\u0432\u0435\u0440\u0430 60\u0441\u0435\u043a.", 255);
            ItemStack taken = new ItemStack(entry.money.id, price, entry.money.meta);
            VCoreServer.asyncExecutor.submit((Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$buyOrSell$3(vintarz.tradesystem.server.TradeEntry int jv ye ), ()V)((TradeEntry)entry, (int)price, (jv)p, (ye)taken));
        } else {
            TradeSystemServer.sendError((EntityPlayer)p, "\u0423 \u0442\u0435\u0431\u044f \u043d\u0435\u0434\u043e\u0441\u0442\u0430\u0442\u043e\u0447\u043d\u043e \u0434\u0435\u043d\u0435\u0433.");
        }
    }

    void sellConfirm(EntityPlayerMP p) {
        SellEntry sell = this.sell.remove((Object)p);
        if (sell == null || System.currentTimeMillis() > sell.time) {
            TradeSystemServer.sendError((EntityPlayer)p, "\u0412\u0440\u0435\u043c\u044f \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043d\u0438\u044f \u0438\u0441\u0442\u0435\u043a\u043b\u043e.\n\u041f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0430\u0439 \u043f\u0440\u043e\u0434\u0430\u0436\u0443 \u0431\u044b\u0441\u0442\u0440\u0435\u0435.");
            return;
        }
        if (!ItemStack.areItemStacksEqual((ItemStack)sell.sell, (ItemStack)p.bn.getItemStack())) {
            TradeSystemServer.sendError((EntityPlayer)p, "\u041f\u0435\u0440\u0435\u0437\u0430\u0439\u0434\u0438 \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440.");
            return;
        }
        p.bn.setItemStack(null);
        SPacketHandler.sendHint((EntityPlayer)p, "vtrade", "\u0422\u043e\u0440\u0433\u043e\u0432\u0435\u0446:\n\u0414\u043e\u0436\u0434\u0438\u0441\u044c \u043e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u044f \u043f\u0440\u043e\u0434\u0430\u0436\u0438.\n\u0415\u0441\u043b\u0438 \u0432\u043e\u0437\u043d\u0438\u043a\u043d\u0443\u0442 \u043f\u0440\u043e\u0431\u043b\u0435\u043c\u044b\n\u0442\u043e \u044f \u0432\u0435\u0440\u043d\u0443 \u0442\u0435\u0431\u0435 \u043b\u0443\u0442.\n\u041d\u0435 \u0432\u044b\u0445\u043e\u0434\u0438 \u0441 \u0441\u0435\u0440\u0432\u0435\u0440\u0430 60\u0441\u0435\u043a.", 255);
        VCoreServer.asyncExecutor.submit((Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$sellConfirm$7(vintarz.tradesystem.server.TradeData$SellEntry jv ), ()V)((SellEntry)sell, (jv)p));
    }

    String checkAccess(EntityPlayerMP p, long check, TraderServer trader) {
        if (check != this.check) {
            return TRY_AGAIN;
        }
        if (trader == null) {
            return "";
        }
        double x = p.u - (double)trader.x;
        double y = p.v - (double)trader.y;
        double z = p.w - (double)trader.z;
        return x * x + y * y + z * z < 36.0 ? null : "\u041f\u043e\u0434\u043e\u0439\u0434\u0438 \u0431\u043b\u0438\u0436\u0435 \u043a \u0442\u043e\u0440\u0433\u043e\u0432\u0446\u0443.";
    }

    public void close(EntityPlayerMP p) {
        if (p == null) {
            for (EntityPlayerMP P : this.open) {
                P.closeScreen();
            }
            this.open.clear();
            this.sell.clear();
            return;
        }
        this.open.remove((Object)p);
        this.sell.remove((Object)p);
    }

    public void modify(EntityPlayerMP p) {
        this.sell.remove((Object)p);
    }

    private static void addItemStackToInventory(EntityPlayer p, ItemStack itemStack) {
        if (!p.inventory.addItemStackToInventory(itemStack)) {
            p.dropPlayerItem(itemStack);
        }
    }

    private static boolean dbBuy(TradeItem item, int amount, Connection con) throws SQLException {
        try (PreparedStatement stmt_buy = con.prepareStatement("UPDATE inventory_" + TradeSystemServer.TRADE_PREFIX + " SET quantity = quantity - ? WHERE id = ? AND quantity >= ?");){
            if (item.qRdrct != null) {
                amount *= item.quantity;
                item = item.qRdrct;
            }
            stmt_buy.setInt(1, amount);
            stmt_buy.setInt(2, item.table);
            stmt_buy.setInt(3, amount);
            boolean bl = stmt_buy.executeUpdate() > 0;
            return bl;
        }
    }

    private static void dbSell(TradeItem item, int amount, Connection con) throws SQLException {
        try (PreparedStatement stmt_sell = con.prepareStatement("UPDATE inventory_" + TradeSystemServer.TRADE_PREFIX + " SET quantity = quantity + ? WHERE id = ?");){
            if (item.qRdrct != null) {
                amount *= item.quantity;
                item = item.qRdrct;
            }
            stmt_sell.setInt(1, amount);
            stmt_sell.setInt(2, item.table);
            stmt_sell.executeUpdate();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static /* synthetic */ void lambda$sellConfirm$7(SellEntry sell, EntityPlayerMP p) {
        boolean success = false;
        try (Connection con = TradeSystemServer.ds.getConnection();){
            if (TradeData.dbBuy(((SellEntry)sell).entry.money, sell.cost, con)) {
                if (!sell.sell.isItemStackDamageable() || sell.sell.getItemDamage() == 0) {
                    TradeData.dbSell(((SellEntry)sell).entry.product, ((SellEntry)sell).sell.stackSize, con);
                }
                success = true;
                VCoreServer.syncQueue.offer((Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$null$4(jv vintarz.tradesystem.server.TradeData$SellEntry ), ()V)((jv)p, (SellEntry)sell));
            } else {
                success = true;
                VCoreServer.syncQueue.offer((Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$null$5(jv vintarz.tradesystem.server.TradeData$SellEntry ), ()V)((jv)p, (SellEntry)sell));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (!success) {
                VCoreServer.syncQueue.offer((Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$null$6(jv vintarz.tradesystem.server.TradeData$SellEntry ), ()V)((jv)p, (SellEntry)sell));
            }
        }
    }

    private static /* synthetic */ void lambda$null$6(EntityPlayerMP p, SellEntry sell) {
        TradeData.addItemStackToInventory((EntityPlayer)p, sell.sell);
        SPacketHandler.sendHint((EntityPlayer)p, "vtrade", "", 0);
        TradeSystemServer.sendError((EntityPlayer)p, "\u041f\u0440\u043e\u0434\u0430\u0436\u0430 \u043d\u0435 \u0443\u0434\u0430\u043b\u0430\u0441\u044c\n\u0442\u043e\u0432\u0430\u0440 \u0432\u043e\u0437\u0432\u0440\u0430\u0449\u0451\u043d.\n\u041f\u044b\u0442\u0430\u0439\u0441\u044f \u043f\u0440\u043e\u0434\u0430\u0442\u044c \u0435\u0449\u0451 \u0440\u0430\u0437.");
    }

    private static /* synthetic */ void lambda$null$5(EntityPlayerMP p, SellEntry sell) {
        TradeData.addItemStackToInventory((EntityPlayer)p, sell.sell);
        SPacketHandler.sendHint((EntityPlayer)p, "vtrade", "", 0);
        TradeSystemServer.sendError((EntityPlayer)p, "\u041f\u0440\u043e\u0434\u0430\u0436\u0430 \u043d\u0435 \u0443\u0434\u0430\u043b\u0430\u0441\u044c\n\u0442\u043e\u0432\u0430\u0440 \u0432\u043e\u0437\u0432\u0440\u0430\u0449\u0451\u043d.\n\u0423 \u043c\u0435\u043d\u044f \u043d\u0435\u0442 \u0434\u0435\u043d\u0435\u0433 \u0434\u043b\u044f \u043e\u043f\u043b\u0430\u0442\u044b \u0442\u0432\u043e\u0435\u0433\u043e \u0442\u043e\u0432\u0430\u0440\u0430.\n\u041f\u043e\u043f\u0440\u043e\u0441\u0438 \u0434\u0440\u0443\u0433\u0438\u0445 \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u043a\u0443\u043f\u0438\u0442\u044c \u0447\u0442\u043e-\u043d\u0438\u0431\u0443\u0434\u044c.");
    }

    private static /* synthetic */ void lambda$null$4(EntityPlayerMP p, SellEntry sell) {
        SPacketHandler.sendHint((EntityPlayer)p, "vtrade", "", 0);
        TradeData.addItemStackToInventory((EntityPlayer)p, new ItemStack(((SellEntry)sell).entry.money.id, sell.cost, ((SellEntry)sell).entry.money.meta));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static /* synthetic */ void lambda$buyOrSell$3(TradeEntry entry, int price, EntityPlayerMP p, ItemStack taken) {
        boolean success = false;
        try (Connection con = TradeSystemServer.ds.getConnection();){
            if (TradeData.dbBuy(entry.product, entry.quantity, con)) {
                TradeData.dbSell(entry.money, price, con);
                success = true;
                VCoreServer.syncQueue.offer((Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$null$0(jv vintarz.tradesystem.server.TradeEntry ), ()V)((jv)p, (TradeEntry)entry));
            } else {
                success = true;
                VCoreServer.syncQueue.offer((Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$null$1(jv ye ), ()V)((jv)p, (ye)taken));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (!success) {
                VCoreServer.syncQueue.offer((Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$null$2(jv ye ), ()V)((jv)p, (ye)taken));
            }
        }
    }

    private static /* synthetic */ void lambda$null$2(EntityPlayerMP p, ItemStack taken) {
        TradeData.addItemStackToInventory((EntityPlayer)p, taken);
        SPacketHandler.sendHint((EntityPlayer)p, "vtrade", "", 0);
        TradeSystemServer.sendError((EntityPlayer)p, "\u041f\u043e\u043a\u0443\u043f\u043a\u0430 \u043d\u0435 \u0443\u0434\u0430\u043b\u0430\u0441\u044c\n\u0434\u0435\u043d\u044c\u0433\u0438 \u0432\u043e\u0437\u0432\u0440\u0430\u0449\u0435\u043d\u044b.\n\u041f\u044b\u0442\u0430\u0439\u0441\u044f \u043a\u0443\u043f\u0438\u0442\u044c \u0435\u0449\u0451 \u0440\u0430\u0437.");
    }

    private static /* synthetic */ void lambda$null$1(EntityPlayerMP p, ItemStack taken) {
        TradeData.addItemStackToInventory((EntityPlayer)p, taken);
        SPacketHandler.sendHint((EntityPlayer)p, "vtrade", "", 0);
        TradeSystemServer.sendError((EntityPlayer)p, "\u041f\u043e\u043a\u0443\u043f\u043a\u0430 \u043d\u0435 \u0443\u0434\u0430\u043b\u0430\u0441\u044c\n\u0434\u0435\u043d\u044c\u0433\u0438 \u0432\u043e\u0437\u0432\u0440\u0430\u0449\u0435\u043d\u044b.\n\u041d\u0430 \u0441\u043a\u043b\u0430\u0434\u0435 \u043d\u0435\u0442 \u043d\u0443\u0436\u043d\u043e\u0433\u043e \u0442\u0435\u0431\u0435 \u0442\u043e\u0432\u0430\u0440\u0430.\n\u041f\u043e\u043f\u0440\u043e\u0441\u0438 \u0434\u0440\u0443\u0433\u0438\u0445 \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u043f\u0440\u043e\u0434\u0430\u0442\u044c \u0435\u0433\u043e \u043c\u043d\u0435.");
    }

    private static /* synthetic */ void lambda$null$0(EntityPlayerMP p, TradeEntry entry) {
        SPacketHandler.sendHint((EntityPlayer)p, "vtrade", "", 0);
        TradeData.addItemStackToInventory((EntityPlayer)p, new ItemStack(entry.product.id, entry.quantity, entry.product.meta));
    }

    private static class SellEntry {
        private final long time = System.currentTimeMillis() + 5000L;
        private final TradeEntry entry;
        private final ItemStack sell;
        private final int cost;

        private SellEntry(TradeEntry entry, ItemStack sell, int cost) {
            this.entry = entry;
            this.sell = sell.copy();
            this.cost = cost;
        }
    }
}

