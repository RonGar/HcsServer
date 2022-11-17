/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.inventory.IInventory
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NBTBase
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.nbt.NBTTagList
 *  net.minecraft.nbt.NBTTagString
 */
package vintarz.ingamestore.server;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public class InventoryShop
implements IInventory {
    final String[] itemdesc;
    final String name;
    ItemStack[] items;
    int[] prices;

    InventoryShop(String name, List<String> lines) {
        this.name = "ViS" + name;
        int len = lines.size();
        while (len % 9 != 0) {
            ++len;
        }
        this.items = new ItemStack[len];
        this.prices = new int[lines.size()];
        this.itemdesc = new String[lines.size()];
        int index = 0;
        for (String line : lines) {
            String[] tmp = line.split(";");
            this.itemdesc[index] = tmp[0];
            int cost = Integer.parseInt(tmp[4]);
            int itemID = Integer.parseInt(tmp[1]);
            ItemStack is = new ItemStack(itemID, Integer.parseInt(tmp[2]), Integer.parseInt(tmp[3]));
            NBTTagList lore = new NBTTagList("Lore");
            String s = "Cost: " + cost;
            lore.appendTag((NBTBase)new NBTTagString("cost", s));
            for (int i = 5; i < tmp.length; ++i) {
                lore.appendTag((NBTBase)new NBTTagString("line" + (i - 4), tmp[i]));
            }
            NBTTagCompound display = new NBTTagCompound("display");
            display.setTag("Lore", (NBTBase)lore);
            NBTTagCompound nbt = new NBTTagCompound("tag");
            nbt.setTag("display", (NBTBase)display);
            is.setTagCompound(nbt);
            this.prices[index] = cost;
            this.items[index] = is;
            ++index;
        }
    }

    public int j_() {
        return this.items.length;
    }

    public ItemStack a(int i) {
        return this.items[i];
    }

    public ItemStack a(int i, int j) {
        return this.items[i] != null ? this.items[i].copy() : null;
    }

    public ItemStack a_(int i) {
        return null;
    }

    public void a(int i, ItemStack itemstack) {
    }

    public String b() {
        return this.name;
    }

    public boolean c() {
        return false;
    }

    public int d() {
        return 0;
    }

    public void e() {
    }

    public boolean a(EntityPlayer entityplayer) {
        return true;
    }

    public void k_() {
    }

    public void g() {
    }

    public boolean b(int i, ItemStack itemstack) {
        return false;
    }
}

