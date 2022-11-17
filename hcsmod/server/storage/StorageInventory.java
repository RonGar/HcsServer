/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.inventory.ContainerChest
 *  net.minecraft.inventory.IInventory
 *  net.minecraft.inventory.InventoryEnderChest
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NBTBase
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.nbt.NBTTagList
 *  uf
 */
package hcsmod.server.storage;

import hcsmod.server.HcsServer;
import hcsmod.server.storage.InvLoadCallback;
import hcsmod.server.storage.StorageGroup;
import java.lang.invoke.LambdaMetafactory;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class StorageInventory
implements IInventory {
    public static final int MAX_STORAGE_ROWS = 5;
    public final String playerName;
    public final StorageGroup storageGroup;
    public ItemStack[] items = new ItemStack[45];
    final List<InvLoadCallback> loadCallbacks = new LinkedList<InvLoadCallback>();
    private final Set<EntityPlayer> openBy = new HashSet<EntityPlayer>();

    public StorageInventory(String playerName, StorageGroup storageGroup) {
        this.playerName = playerName;
        this.storageGroup = storageGroup;
    }

    public void openBy(EntityPlayer player) {
        player.displayGUIChest((IInventory)this);
        this.openBy.add(player);
    }

    public int j_() {
        return this.storageGroup.slots * 9;
    }

    public ItemStack a(int i) {
        if (!this.isActive()) {
            return null;
        }
        return this.items[i];
    }

    public void forceClose() {
        this.openBy.clear();
    }

    public boolean isActive() {
        this.openBy.removeIf((Predicate<uf>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Z, lambda$isActive$0(uf ), (Luf;)Z)((StorageInventory)this));
        return !this.openBy.isEmpty();
    }

    public ItemStack a(int i, int j) {
        if (!this.isActive()) {
            return null;
        }
        if (this.items[i] != null) {
            if (this.items[i].stackSize <= j) {
                ItemStack itemstack = this.items[i];
                this.items[i] = null;
                return itemstack;
            }
            ItemStack itemstack = this.items[i].splitStack(j);
            if (this.items[i].stackSize == 0) {
                this.items[i] = null;
            }
            return itemstack;
        }
        return null;
    }

    public ItemStack a_(int i) {
        return null;
    }

    public void a(int i, ItemStack itemstack) {
        if (!this.isActive()) {
            return;
        }
        this.items[i] = itemstack;
        if (itemstack != null && itemstack.stackSize > this.d()) {
            itemstack.stackSize = this.d();
        }
    }

    public String b() {
        if (this.storageGroup.slots == 0) {
            return "\u00a7f\u0425\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u043e GOD \u0438 \u0432\u044b\u0448\u0435";
        }
        return "\u00a7f\u0425\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435 " + this.playerName + " " + this.storageGroup.slots + "/" + 5;
    }

    public boolean c() {
        return false;
    }

    public int d() {
        return 64;
    }

    public void e() {
    }

    public boolean a(EntityPlayer entityplayer) {
        return this.isActive();
    }

    public void k_() {
    }

    public void g() {
    }

    public boolean b(int i, ItemStack itemstack) {
        return false;
    }

    public void load(NBTTagCompound tag) {
        NBTTagList nbttaglist = tag.getTagList("Items");
        int len = (nbttaglist.tagCount() - 1) / 9 + 1;
        len = Math.max(45, len * 9);
        this.items = new ItemStack[len];
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            ItemStack is;
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 0xFF;
            if (j < 0 || j >= this.items.length) continue;
            this.items[j] = is = ItemStack.loadItemStackFromNBT((NBTTagCompound)nbttagcompound1);
        }
    }

    public void save(NBTTagCompound tag) {
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < this.items.length; ++i) {
            if (this.items[i] == null) continue;
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Slot", (byte)i);
            this.items[i].writeToNBT(nbttagcompound1);
            nbttaglist.appendTag((NBTBase)nbttagcompound1);
        }
        tag.setTag("Items", (NBTBase)nbttaglist);
    }

    public static void playerLogin(EntityPlayer player) {
        InventoryEnderChest invEC = player.getInventoryEnderChest();
        for (int i = 0; i < invEC.j_(); ++i) {
            if (invEC.a(i) == null) continue;
            StorageInventory.copyPlayerEC(player.getCommandSenderName(), (IInventory)invEC);
            break;
        }
    }

    private static void copyPlayerEC(String player, final IInventory ec) {
        HcsServer.customStorage.loadInventory(player, StorageGroup.MAX, true, new InvLoadCallback(){

            @Override
            public void loadingDone(StorageInventory inv) {
                for (int i = 0; i < ec.getSizeInventory(); ++i) {
                    ItemStack is = ec.getStackInSlot(i);
                    if (is == null) continue;
                    ec.setInventorySlotContents(i, null);
                    inv.a(i, is.copy());
                }
            }
        });
    }

    private /* synthetic */ boolean lambda$isActive$0(EntityPlayer entityPlayer) {
        if (!(entityPlayer.openContainer instanceof ContainerChest)) {
            return true;
        }
        ContainerChest cc = (ContainerChest)entityPlayer.openContainer;
        return cc.lowerChestInventory != this;
    }
}

