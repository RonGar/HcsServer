/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.server.MinecraftServer
 *  vintarz.core.VSP
 *  ye
 */
package vintarz.exchange.server;

import hcsmod.player.ExtendedPlayer;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import vintarz.core.VSP;
import vintarz.core.server.VCoreServer;
import vintarz.exchange.server.ExchangeServer;

class ExchangeEntry {
    final EntityPlayerMP player;
    final ExtendedPlayer ep;
    final ItemStack[] main;
    private final int commission;
    ExchangeEntry othr;
    private final List<Integer> items = new ArrayList<Integer>();
    private int balance;
    private int cash;
    private boolean ready;
    private int transaction;

    ExchangeEntry(EntityPlayerMP player, String other) {
        this.player = player;
        this.ep = ExtendedPlayer.server((EntityPlayer)player);
        this.main = ExchangeEntry.copyItemStacks(player.bn.mainInventory);
        this.commission = ExchangeServer.moderators.contains(player.bu.toLowerCase()) ? 100 : 15;
        VSP os = new VSP(1, "vExchange");
        try {
            os.write(other.getBytes("UTF8"));
        }
        catch (IOException iOException) {
            // empty catch block
        }
        os.send((EntityPlayer)player);
    }

    public boolean check(boolean close) {
        if (!this.player.T()) {
            if (close) {
                ExchangeServer.closeExchange(this);
            }
            return false;
        }
        if (!ExchangeEntry.checkItemStacks(this.player.bn.mainInventory, this.main)) {
            VSP os = new VSP(0, "vExchange");
            os.send((EntityPlayer)this.player);
            if (close) {
                ExchangeServer.closeExchange(this);
            }
            return false;
        }
        return true;
    }

    void sendMoneyToExchangers(int balance) {
        this.balance = balance;
        balance = Math.min(balance, 65535);
        VSP os = new VSP(2, "vExchange");
        try {
            os.writeShort(balance);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        os.send((EntityPlayer)this.player);
        os = new VSP(3, "vExchange");
        try {
            os.writeShort(balance);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        os.send((EntityPlayer)this.othr.player);
    }

    private static ItemStack[] copyItemStacks(ItemStack[] from) {
        ItemStack[] to = new ItemStack[from.length];
        for (int i = 0; i < from.length; ++i) {
            ItemStack is = from[i];
            if (is == null) continue;
            to[i] = is.copy();
        }
        return to;
    }

    private static boolean checkItemStacks(ItemStack[] array0, ItemStack[] array1) {
        if (array0.length != array1.length) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < array0.length; ++i) {
            if (ItemStack.areItemStacksEqual((ItemStack)array0[i], (ItemStack)array1[i])) continue;
            return false;
        }
        return true;
    }

    void close() {
        new VSP(0, "vExchange").send((EntityPlayer)this.player);
    }

    void addItem(int id) {
        if (this.ready || this.player.bn.mainInventory[id] == null) {
            return;
        }
        Integer i = id;
        if (this.items.contains(i)) {
            return;
        }
        this.items.add(i);
        if (++this.transaction >= 256) {
            this.transaction = 0;
        }
        VSP os = new VSP(4, "vExchange");
        try {
            Packet.writeItemStack((ItemStack)this.player.bn.mainInventory[id], (DataOutput)os);
            os.writeByte(id);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        os.send((EntityPlayer)this.player);
        os = new VSP(6, "vExchange");
        try {
            os.write(this.transaction);
            Packet.writeItemStack((ItemStack)this.player.bn.mainInventory[id], (DataOutput)os);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        os.send((EntityPlayer)this.othr.player);
        if (this.othr.ready) {
            this.othr.ready = false;
            new VSP(12, "vExchange").send((EntityPlayer)this.player);
        }
    }

    void remItem(int id) {
        if (this.ready) {
            return;
        }
        this.items.remove(id);
        if (++this.transaction >= 256) {
            this.transaction = 0;
        }
        VSP os = new VSP(5, "vExchange");
        try {
            os.writeByte(id);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        os.send((EntityPlayer)this.player);
        os = new VSP(7, "vExchange");
        try {
            os.write(this.transaction);
            os.writeByte(id);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        os.send((EntityPlayer)this.othr.player);
        if (this.othr.ready) {
            this.othr.ready = false;
            new VSP(12, "vExchange").send((EntityPlayer)this.player);
        }
    }

    void setCash(int cash) {
        if (this.ready) {
            return;
        }
        boolean changed = this.cash != cash;
        this.cash = cash;
        cash = this.calculateFee();
        if (cash > this.balance) {
            cash = 0;
            this.cash = 0;
            changed = true;
        }
        VSP os = new VSP(8, "vExchange");
        try {
            os.writeShort(cash);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        os.send((EntityPlayer)this.player);
        if (changed) {
            if (++this.transaction >= 256) {
                this.transaction = 0;
            }
            os = new VSP(9, "vExchange");
            try {
                os.write(this.transaction);
                os.writeShort(this.cash);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            os.send((EntityPlayer)this.othr.player);
            if (this.othr.ready) {
                this.othr.ready = false;
                new VSP(12, "vExchange").send((EntityPlayer)this.player);
            }
        }
    }

    private int calculateFee() {
        if (this.cash == 0) {
            return 0;
        }
        int fee = this.cash * (100 + this.commission) / 100;
        if (fee <= this.cash) {
            ++fee;
        }
        return fee;
    }

    void accept(int transaction) {
        if (transaction != this.othr.transaction) {
            return;
        }
        if (!this.check(true)) {
            return;
        }
        this.ready = true;
        new VSP(10, "vExchange").send((EntityPlayer)this.othr.player);
        if (this.othr.ready) {
            if (!this.othr.check(true)) {
                return;
            }
            if (this.items.size() == 0 && this.othr.items.size() == 0 && this.cash == 0 && this.othr.cash == 0) {
                ExchangeServer.closeExchange(this);
                return;
            }
            ItemStack[] itemsSelf = new ItemStack[this.items.size()];
            ItemStack[] itemsOthr = new ItemStack[this.othr.items.size()];
            int i = 0;
            for (int item : this.items) {
                itemsSelf[i++] = this.player.bn.mainInventory[item].copy();
                this.player.bn.mainInventory[item] = null;
            }
            i = 0;
            for (int item : this.othr.items) {
                itemsOthr[i++] = this.othr.player.bn.mainInventory[item].copy();
                this.othr.player.bn.mainInventory[item] = null;
            }
            if (this.cash == 0 && this.othr.cash == 0) {
                ExchangeEntry.addItemStacksToInventory((EntityPlayer)this.player, itemsOthr);
                ExchangeEntry.addItemStacksToInventory((EntityPlayer)this.othr.player, itemsSelf);
                ExchangeServer.closeExchange(this);
            } else {
                VCoreServer.asyncExecutor.submit((Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$accept$2(ye[] ye[] ), ()V)((ExchangeEntry)this, (ye[])itemsSelf, (ye[])itemsOthr));
            }
        }
    }

    private static void addItemStacksToInventory(EntityPlayer p, ItemStack[] itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            if (p.inventory.addItemStackToInventory(itemStack)) continue;
            p.dropPlayerItem(itemStack);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private /* synthetic */ void lambda$accept$2(ItemStack[] itemsSelf, ItemStack[] itemsOthr) {
        boolean success = false;
        try (Connection con = ExchangeServer.ds.getConnection();
             PreparedStatement buyStmt = con.prepareStatement("UPDATE ms_realmoney SET cash = cash - ? WHERE name = ? AND cash >= ?");
             PreparedStatement newStmt = con.prepareStatement("INSERT IGNORE ms_realmoney(name,cash) VALUES(?,0)");
             PreparedStatement addStmt = con.prepareStatement("UPDATE ms_realmoney SET cash = cash + ? WHERE name = ?");
             PreparedStatement logStmt = con.prepareStatement("INSERT INTO bs_wallet_log(username, pay_in, pay_out, system_comment, time, ip, usermode_comment) VALUES (?, ?, ?, ?, ?, ?, '\u043e\u0431\u043c\u0435\u043d')");){
            try {
                try {
                    int ownFee = this.calculateFee();
                    int othFee = this.othr.calculateFee();
                    con.setAutoCommit(false);
                    if (ownFee > 0) {
                        newStmt.setString(1, this.othr.player.bu);
                        newStmt.executeUpdate();
                    }
                    if (othFee > 0) {
                        newStmt.setString(1, this.player.bu);
                        newStmt.executeUpdate();
                    }
                    if (ownFee > 0) {
                        buyStmt.setLong(1, ownFee);
                        buyStmt.setString(2, this.player.bu);
                        buyStmt.setLong(3, ownFee);
                        if (buyStmt.executeUpdate() == 0) {
                            return;
                        }
                        addStmt.setLong(1, this.cash);
                        addStmt.setString(2, this.othr.player.bu);
                        addStmt.executeUpdate();
                    }
                    if (othFee > 0) {
                        buyStmt.setLong(1, othFee);
                        buyStmt.setString(2, this.othr.player.bu);
                        buyStmt.setLong(3, othFee);
                        if (buyStmt.executeUpdate() == 0) {
                            return;
                        }
                        addStmt.setLong(1, this.othr.cash);
                        addStmt.setString(2, this.player.bu);
                        addStmt.executeUpdate();
                    }
                    String server = MinecraftServer.getServer().getMOTD();
                    long time = System.currentTimeMillis() / 1000L;
                    logStmt.setString(1, this.player.bu);
                    logStmt.setLong(2, this.othr.cash);
                    logStmt.setLong(3, ownFee);
                    logStmt.setString(4, "\u041e\u0431\u043c\u0435\u043d " + itemsSelf.length + " \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u043d\u0430 " + itemsOthr.length + " \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u0441 \u0438\u0433\u0440\u043e\u043a\u043e\u043c " + this.othr.player.bu + ", \u0441\u0435\u0440\u0432\u0435\u0440 " + server);
                    logStmt.setLong(5, time);
                    logStmt.setString(6, ((InetSocketAddress)this.player.playerNetServerHandler.netManager.getSocketAddress()).getHostString());
                    logStmt.executeUpdate();
                    logStmt.setString(1, this.othr.player.bu);
                    logStmt.setLong(2, this.cash);
                    logStmt.setLong(3, othFee);
                    logStmt.setString(4, "\u041e\u0431\u043c\u0435\u043d " + itemsOthr.length + " \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u043d\u0430 " + itemsSelf.length + " \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u0441 \u0438\u0433\u0440\u043e\u043a\u043e\u043c " + this.player.bu + ", \u0441\u0435\u0440\u0432\u0435\u0440 " + server);
                    logStmt.setLong(5, time);
                    logStmt.setString(6, ((InetSocketAddress)this.othr.player.playerNetServerHandler.netManager.getSocketAddress()).getHostString());
                    logStmt.executeUpdate();
                    con.commit();
                    success = true;
                    VCoreServer.syncQueue.offer((Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$null$0(ye[] ye[] ), ()V)((ExchangeEntry)this, (ye[])itemsOthr, (ye[])itemsSelf));
                    return;
                }
                finally {
                    con.rollback();
                }
            }
            finally {
                con.setAutoCommit(true);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        finally {
            if (!success) {
                VCoreServer.syncQueue.offer((Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$null$1(ye[] ye[] ), ()V)((ExchangeEntry)this, (ye[])itemsSelf, (ye[])itemsOthr));
            }
        }
    }

    private /* synthetic */ void lambda$null$1(ItemStack[] itemsSelf, ItemStack[] itemsOthr) {
        ExchangeEntry.addItemStacksToInventory((EntityPlayer)this.player, itemsSelf);
        ExchangeEntry.addItemStacksToInventory((EntityPlayer)this.othr.player, itemsOthr);
        ExchangeServer.closeExchange(this);
    }

    private /* synthetic */ void lambda$null$0(ItemStack[] itemsOthr, ItemStack[] itemsSelf) {
        ExchangeEntry.addItemStacksToInventory((EntityPlayer)this.player, itemsOthr);
        ExchangeEntry.addItemStacksToInventory((EntityPlayer)this.othr.player, itemsSelf);
        ExchangeServer.closeExchange(this);
    }
}

