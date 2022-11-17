/*
 * Decompiled with CFR 0.150.
 */
package vintarz.tradesystem.server;

class TradeItem {
    final int id;
    final int meta;
    final int table;
    final TradeItem qRdrct;
    int quantity;

    TradeItem(int id, int meta, int table, TradeItem quantityRedirect, int quantity) {
        this.id = id;
        this.meta = meta;
        this.table = table;
        this.qRdrct = quantityRedirect;
        this.quantity = quantity;
    }

    public boolean equals(Object o) {
        return o instanceof TradeItem && ((TradeItem)o).id == this.id && ((TradeItem)o).meta == this.meta;
    }

    public int hashCode() {
        return this.table;
    }
}

