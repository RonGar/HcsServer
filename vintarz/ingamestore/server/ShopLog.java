/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.ItemStack
 *  net.minecraft.server.MinecraftServer
 */
package vintarz.ingamestore.server;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.GregorianCalendar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import vintarz.ingamestore.server.ContainerShop;

public class ShopLog
implements Runnable {
    static final DecimalFormat df2 = new DecimalFormat(".00");
    EntityPlayer player;
    ContainerShop shop;
    int slotId;

    public ShopLog(ContainerShop shop, int slotId, EntityPlayer par4EntityPlayer) {
        this.shop = shop;
        this.slotId = slotId;
        this.player = par4EntityPlayer;
    }

    @Override
    public void run() {
        try {
            ItemStack is = this.shop.shop.items[this.slotId];
            File f = MinecraftServer.getServer().getFile("vShop/logs/" + this.player.username.toLowerCase().trim() + ".txt");
            if (!f.exists()) {
                f.mkdirs();
                f.delete();
                f.createNewFile();
            }
            long fileLength = f.length();
            RandomAccessFile raf = new RandomAccessFile(f, "rw");
            raf.seek(fileLength);
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(ContainerShop.sdf.format(GregorianCalendar.getInstance().getTime()));
            sb.append("] ");
            sb.append("\u043a\u0443\u043f\u0438\u043b x");
            sb.append(Integer.toString(is.itemID));
            sb.append(" \"");
            sb.append(this.shop.shop.itemdesc[this.slotId]);
            sb.append("\" \u043d\u0430 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u0430\u0445 ");
            sb.append(df2.format(this.player.u));
            sb.append(" ");
            sb.append(df2.format(this.player.v));
            sb.append(" ");
            sb.append(df2.format(this.player.w));
            sb.append("\n");
            raf.write(sb.toString().getBytes(ContainerShop.UTF8));
            raf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

