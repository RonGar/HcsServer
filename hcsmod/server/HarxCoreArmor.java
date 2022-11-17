/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 */
package hcsmod.server;

import extendedDmgSrc.ExtendedDamageSource;
import hcsmod.server.ExtendedDamageCalculator;
import hcsmod.server.HcsServer;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class HarxCoreArmor {
    private static int[] ids;
    private static int[] values;

    public static int getValue(ItemStack is) {
        if (ids == null || is == null) {
            return 0;
        }
        int id = is.itemID;
        for (int i = 0; i < ids.length; ++i) {
            if (ids[i] != id) continue;
            return values[i];
        }
        return 0;
    }

    public static void reload() {
        Properties properties = new Properties();
        try (FileInputStream in2 = new FileInputStream("hardcore/armor_items_list.properties");){
            properties.load(in2);
        }
        catch (IOException in2) {
            // empty catch block
        }
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ArrayList<Integer> values = new ArrayList<Integer>();
        for (Map.Entry<Object, Object> e : properties.entrySet()) {
            ids.add(Integer.parseUnsignedInt((String)e.getKey()));
            values.add(Integer.parseUnsignedInt(((String)e.getValue()).split(" ")[0]));
        }
        HarxCoreArmor.ids = HcsServer.intListToArray(ids);
        HarxCoreArmor.values = HcsServer.intListToArray(values);
    }

    public static void serverStarting() {
        if (HcsServer.isHarxcoreServer) {
            ExtendedDamageSource.calculator = new ExtendedDamageCalculator();
        }
        for (int id : ids) {
            Item item;
            if (id < 500 || (item = Item.itemsList[id]) == null) continue;
            item.setNoRepair();
        }
    }

    static {
        HarxCoreArmor.reload();
    }
}

