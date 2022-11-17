/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.inventory.Container
 *  net.minecraft.inventory.IInventory
 *  net.minecraft.inventory.Slot
 *  net.minecraft.item.ItemStack
 */
package vintarz.ingamestore.server;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import vintarz.core.server.VCoreServer;
import vintarz.ingamestore.server.InventoryShop;
import vintarz.ingamestore.server.ShopLog;
import vintarz.ingamestore.server.ShopServer;
import vintarz.ingamestore.server.SlotShop;

public class ContainerShop
extends Container {
    static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static final Charset UTF8 = Charset.forName("UTF-8");
    InventoryShop shop;
    private int numRows;

    public ContainerShop(IInventory par1IInventory, InventoryShop par2IInventory) {
        int k;
        int j;
        this.shop = par2IInventory;
        this.numRows = par2IInventory.j_() / 9;
        par2IInventory.k_();
        int i = (this.numRows - 4) * 18;
        for (j = 0; j < this.numRows; ++j) {
            for (k = 0; k < 9; ++k) {
                this.a(new SlotShop(par2IInventory, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }
        for (j = 0; j < 3; ++j) {
            for (k = 0; k < 9; ++k) {
                this.a(new Slot(par1IInventory, k + j * 9 + 9, 8 + k * 18, 103 + j * 18 + i));
            }
        }
        for (j = 0; j < 9; ++j) {
            this.a(new Slot(par1IInventory, j, 8 + j * 18, 161 + i));
        }
    }

    public boolean a(EntityPlayer par1EntityPlayer) {
        return this.shop.a(par1EntityPlayer);
    }

    public void b(EntityPlayer par1EntityPlayer) {
        super.onContainerClosed(par1EntityPlayer);
        this.shop.g();
    }

    public IInventory getLowerChestInventory() {
        return this.shop;
    }

    public ItemStack b(EntityPlayer par1EntityPlayer, int par2) {
        return null;
    }

    public ItemStack a(int i, int par2, int par3, EntityPlayer par4EntityPlayer) {
        ItemStack is = super.slotClick(i, par2, par3, par4EntityPlayer);
        if (is != null && i >= 0 && this.shop.prices.length > i) {
            int cost = this.shop.prices[i];
            if (ShopServer.buy(cost, par4EntityPlayer)) {
                VCoreServer.asyncExecutor.execute(new ShopLog(this, i, par4EntityPlayer));
                is = new ItemStack(is.itemID, is.stackSize, is.getItemDamage());
                par4EntityPlayer.inventory.setItemStack(is);
                return is;
            }
            par4EntityPlayer.inventory.setItemStack(null);
            return null;
        }
        return is;
    }
}

