/*
 * Decompiled with CFR 0.150.
 */
package vintarz.tradesystem.server;

import vintarz.tradesystem.server.TradeData;

class TraderServer {
    final int x;
    final int y;
    final int z;
    final int r;
    final TradeData exchange;

    TraderServer(int x, int y, int z, int r, TradeData exchange) {
        this.x = Math.min(Math.max(x, -32768), 32767);
        this.z = Math.min(Math.max(z, -32768), 32767);
        this.y = Math.min(Math.max(y, 0), 255);
        this.r = Math.min(Math.max(r, 0), 3);
        this.exchange = exchange;
    }
}

