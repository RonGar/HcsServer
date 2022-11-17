/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.player.ExtendedPlayer
 *  hcsmod.player.InventoryExtended
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.InventoryPlayer
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.CompressedStreamTools
 *  net.minecraft.nbt.NBTBase
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.nbt.NBTTagList
 *  net.minecraft.nbt.NBTTagString
 *  net.minecraft.util.ChatMessageComponent
 *  vintarz.core.VSP
 *  vintarz.kitsystem.common.KitData
 */
package vintarz.kitsystem.server;

import hcsmod.player.ExtendedPlayer;
import hcsmod.player.InventoryExtended;
import hcsmod.server.HcsServer;
import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatMessageComponent;
import vintarz.core.VSP;
import vintarz.kitsystem.common.KitData;
import vintarz.kitsystem.server.KitServer;

public class KitDataServer
extends KitData {
    private static final ItemStack dummy = new ItemStack(0, 0, 0);
    private static final ItemStack[] temp = new ItemStack[36];
    private static final int slots_size = 46;
    private static final BitSet slots = new BitSet(46);
    private final ItemStack[] inventory = new ItemStack[36];
    private final ItemStack[] armor = new ItemStack[4];
    private final ItemStack[] hcsmod = new ItemStack[6];

    KitDataServer(String name, NBTTagCompound tag) {
        super((int)tag.getShort("id"), name, KitDataServer.readDescription(tag));
        KitDataServer.readItems(tag, this.inventory, "inventory");
        KitDataServer.readItems(tag, this.armor, "armor");
        KitDataServer.readItems(tag, this.hcsmod, "hcsmod");
    }

    KitDataServer(String name, int id, EntityPlayer p, String[] description) {
        super(id, name, description);
        this.copyItemsToArrayFrom(this.inventory, p.inventory.mainInventory);
        this.copyItemsToArrayFrom(this.armor, p.inventory.armorInventory);
        this.copyItemsToArrayFrom(this.hcsmod, ExtendedPlayer.server((EntityPlayer)p).inventory.inventoryStacks);
    }

    public boolean giveKit(EntityPlayer p, int[] binds) {
        if (!this.fillTempUsingBinds(binds)) {
            p.a(ChatMessageComponent.createFromText((String)"\u0418\u043b\u0438 \u0442\u044b \u043c\u0430\u043c\u043a\u0438\u043d \u043d\u0435\u0434\u043e\u0445\u0430\u043a\u0435\u0440 \u0438\u043b\u0438 \u0442\u0432\u043e\u0438 \u0431\u0438\u043d\u0434\u044b \u043a\u0438\u0442\u0430 \u043f\u043e\u0432\u0440\u0435\u0436\u0434\u0435\u043d\u044b."));
            p.a(ChatMessageComponent.createFromText((String)"\u041e\u0442\u043a\u0440\u043e\u0439 \u0431\u0438\u043d\u0434\u044b \u044d\u0442\u043e\u0433\u043e \u043a\u0438\u0442\u0430 \u0438 \u043d\u0430\u0436\u043c\u0438 \"\u0421\u0442\u0430\u0440\u0434\u0430\u0440\u0442\u043d\u043e\u0435 \u0440\u0430\u0441\u043f\u043e\u043b\u043e\u0436\u0435\u043d\u0438\u0435\""));
            return false;
        }
        InventoryExtended ie = ExtendedPlayer.server((EntityPlayer)p).inventory;
        boolean clear = this.checkPlayerInventory(p);
        clear &= this.checkItemsArray(p.inventory.armorInventory, this.armor, 36);
        if (!(clear &= this.checkItemsArray(ie.inventoryStacks, this.hcsmod, 40))) {
            this.sendSlotsMustBeClear(p);
            return false;
        }
        this.copyItemsToArrayFrom(p.inventory.mainInventory, temp);
        this.copyItemsToArrayFrom(p.inventory.armorInventory, this.armor);
        this.copyItemsToArrayFrom(ie.inventoryStacks, this.hcsmod);
        return true;
    }

    private boolean fillTempUsingBinds(int[] binds) {
        if (binds != null) {
            int i;
            for (i = 0; i < 36; ++i) {
                KitDataServer.temp[i] = null;
            }
            for (i = 0; i < 36; ++i) {
                int slot = binds[i];
                if (temp[slot] != null) {
                    return false;
                }
                ItemStack is = this.inventory[i];
                KitDataServer.temp[slot] = is == null ? dummy : is;
            }
            for (i = 0; i < 36; ++i) {
                if (temp[i] == null) {
                    return false;
                }
                if (temp[i] != dummy) continue;
                KitDataServer.temp[i] = null;
            }
        } else {
            System.arraycopy(this.inventory, 0, temp, 0, 36);
        }
        return true;
    }

    private boolean checkPlayerInventory(EntityPlayer p) {
        boolean clear = true;
        InventoryPlayer ip = p.inventory;
        for (int i = 0; i < this.inventory.length; ++i) {
            ItemStack is;
            boolean occupied = temp[i] != null;
            slots.set(i, occupied);
            if (!occupied || (is = ip.mainInventory[i]) == null) continue;
            if (HcsServer.isStartLoot(is)) {
                p.dropPlayerItemWithRandomChoice(is.copy(), true);
                ip.mainInventory[i] = null;
                continue;
            }
            clear = false;
        }
        return clear;
    }

    private boolean checkItemsArray(ItemStack[] target, ItemStack[] items, int offset) {
        boolean clear = true;
        int len = Math.max(target.length, items.length);
        for (int i = 0; i < len; ++i) {
            boolean occupied = target[i] != null;
            slots.set(offset + i, occupied);
            if (!occupied || items[i] == null) continue;
            clear = false;
        }
        return clear;
    }

    private void sendSlotsMustBeClear(EntityPlayer p) {
        VSP os = new VSP(1, "kitsys");
        for (int i = 0; i < 46; i += 8) {
            int e = Math.min(i + 8, 46);
            int v = 0;
            for (int k = i; k < e; ++k) {
                boolean b = slots.get(k);
                if (!b) continue;
                v |= 1 << k - i;
            }
            try {
                os.writeByte(v);
                continue;
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        os.send(p);
    }

    private void copyItemsToArrayFrom(ItemStack[] to, ItemStack[] from) {
        int len = Math.max(to.length, from.length);
        for (int i = 0; i < len; ++i) {
            if (from[i] == null) continue;
            to[i] = from[i].copy();
        }
    }

    public String writeToKitSystemDir() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setShort("id", (short)this.id);
        NBTTagList list = new NBTTagList();
        if (this.description != null) {
            for (String s : this.description) {
                list.appendTag((NBTBase)new NBTTagString(null, s));
            }
        }
        tag.setTag("description", (NBTBase)list);
        KitDataServer.writeItems(tag, this.inventory, "inventory");
        KitDataServer.writeItems(tag, this.armor, "armor");
        KitDataServer.writeItems(tag, this.hcsmod, "hcsmod");
        try {
            CompressedStreamTools.write((NBTTagCompound)tag, (File)new File(KitServer.root, this.name + ".kit"));
        }
        catch (IOException e) {
            return e.toString();
        }
        return "\u041d\u0430\u0431\u043e\u0440 " + this.name + " \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d.";
    }

    private static String[] readDescription(NBTTagCompound tag) {
        NBTTagList list = tag.getTagList("description");
        String[] description = new String[list.tagCount()];
        for (int i = 0; i < description.length; ++i) {
            description[i] = list.tagAt(i).toString();
        }
        return description;
    }

    private static void readItems(NBTTagCompound tag, ItemStack[] array, String name) {
        NBTTagList list = tag.getTagList(name);
        for (int i = 0; i < array.length; ++i) {
            NBTTagCompound item = (NBTTagCompound)list.tagAt(i);
            array[i] = item == null ? null : ItemStack.loadItemStackFromNBT((NBTTagCompound)item);
        }
    }

    private static void writeItems(NBTTagCompound tag, ItemStack[] array, String name) {
        NBTTagList list = new NBTTagList();
        for (ItemStack is : array) {
            NBTTagCompound item = new NBTTagCompound();
            if (is != null) {
                is.writeToNBT(item);
            }
            list.appendTag((NBTBase)item);
        }
        tag.setTag(name, (NBTBase)list);
    }
}

