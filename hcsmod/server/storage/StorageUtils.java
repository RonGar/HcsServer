/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.ItemStack
 */
package hcsmod.server.storage;

import hcsmod.player.ExtendedPlayer;
import hcsmod.server.ExtendedStorage;
import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class StorageUtils {
    public static final String[] chatMessages = new String[]{"\u0431\u0435\u0437 \u043f\u0432\u043f", "\u0431\u0435\u0437 \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f", "\u0431\u0435\u0437 \u043f\u043e\u0434\u0431\u043e\u0440\u0430 \u043b\u0443\u0442\u0430"};

    public static void checkInventoryChange(EntityPlayer p) {
        ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
        ExtendedStorage es = ExtendedStorage.get(ep);
        for (ItemStack is : p.inventory.mainInventory) {
            StorageUtils.checkSlot(es, is);
        }
        for (ItemStack is : p.inventory.armorInventory) {
            StorageUtils.checkSlot(es, is);
        }
        es.playerInventoryMap.values().removeIf(inventoryMap -> inventoryMap.current == 0);
        Iterator<Integer> iterator = es.playerInventoryMap.keySet().iterator();
        while (iterator.hasNext()) {
            int id = (Integer)iterator.next();
            ExtendedStorage.InventoryMap inventoryMap2 = es.playerInventoryMap.get(id);
            if (inventoryMap2.current > inventoryMap2.old) {
                es.lastInventoryChangeTime = System.currentTimeMillis();
            }
            inventoryMap2.old = inventoryMap2.current;
            inventoryMap2.current = 0;
        }
    }

    private static void checkSlot(ExtendedStorage es, ItemStack is) {
        if (is == null) {
            return;
        }
        if (es.playerInventoryMap.containsKey(is.itemID)) {
            ExtendedStorage.InventoryMap inventoryMap = es.playerInventoryMap.get(is.itemID);
            ++inventoryMap.current;
        } else {
            es.lastInventoryChangeTime = System.currentTimeMillis();
            es.playerInventoryMap.put(is.itemID, new ExtendedStorage.InventoryMap(0, 1));
        }
    }
}

