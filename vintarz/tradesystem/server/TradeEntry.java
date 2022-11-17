/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.MathHelper
 */
package vintarz.tradesystem.server;

import net.minecraft.util.MathHelper;
import vintarz.tradesystem.server.TradeItem;

class TradeEntry {
    final TradeItem money;
    final TradeItem product;
    final int quantity;
    private final int cost_buy_empty;
    private final int cost_buy_full_;
    private final int cost_sell_empty;
    private final int cost_sell_full_;
    private final int full_count;

    TradeEntry(TradeItem money, TradeItem product, int quantity, int cost_buy_full, int cost_buy_empty, int cost_sell_full, int cost_sell_empty, int full_count) {
        this.money = money;
        this.product = product;
        this.quantity = quantity;
        this.cost_buy_empty = cost_buy_empty;
        this.cost_buy_full_ = cost_buy_empty - cost_buy_full;
        this.cost_sell_empty = cost_sell_empty;
        this.cost_sell_full_ = cost_sell_empty - cost_sell_full;
        this.full_count = full_count > 0 ? full_count : 1;
    }

    int getBuyPrice() {
        double quantity = Math.min((double)Math.max(this.product.quantity, 0) / (double)this.full_count, 1.0);
        return this.cost_buy_empty - MathHelper.floor_double((double)((double)this.cost_buy_full_ * quantity));
    }

    int getSellPrice() {
        double quantity = Math.min((double)Math.max(this.product.quantity, 1) / (double)this.full_count, 1.0);
        return (int)((double)this.cost_sell_empty - (double)this.cost_sell_full_ * quantity);
    }
}

