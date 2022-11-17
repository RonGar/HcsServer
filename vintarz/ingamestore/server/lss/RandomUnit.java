/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 */
package vintarz.ingamestore.server.lss;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RandomUnit {
    public static final RandomUnit dummy = new RandomUnit();
    private static Map<String, RandomUnit> units = new HashMap<String, RandomUnit>();
    private String name = "";
    private final List<ItemStack> items = new ArrayList<ItemStack>();

    public static RandomUnit get(String name) {
        return units.get(name);
    }

    public String name() {
        return this.name;
    }

    public static void read() {
        units.clear();
        File root = new File("vShop/randomUnits/");
        if (root.isDirectory()) {
            RandomUnit u = new RandomUnit();
            for (File f : root.listFiles()) {
                System.out.println("Reading units file " + f.getName());
                Scanner in = null;
                try {
                    in = new Scanner(f, "UTF-8");
                    while (u.read(in)) {
                        System.out.println("Succesfully loaded unit " + u.name);
                        units.put(u.name, u);
                        u = new RandomUnit();
                    }
                }
                catch (FileNotFoundException fileNotFoundException) {
                    // empty catch block
                }
                try {
                    in.close();
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
    }

    public ItemStack[] getLoot() {
        ItemStack[] loot = new ItemStack[this.items.size()];
        int i = 0;
        for (ItemStack is : this.items) {
            loot[i++] = is.copy();
        }
        return loot;
    }

    private boolean read(Scanner in) {
        String s;
        if (in.hasNextLine()) {
            this.name = in.nextLine();
        } else {
            return false;
        }
        while (in.hasNextLine() && !(s = in.nextLine()).isEmpty()) {
            String[] tmp = s.split(" ", 4);
            if (tmp.length < 3) {
                System.err.println("Unit " + this.name + " wrong entry: " + s);
                continue;
            }
            Item item = null;
            int quantity = -1;
            int metadata = -1;
            try {
                item = Item.itemsList[Integer.parseInt(tmp[0])];
                quantity = Integer.parseInt(tmp[1]);
                metadata = Integer.parseInt(tmp[2]);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (item == null) {
                System.err.println("Unit " + this.name + " no item: " + tmp[0]);
                continue;
            }
            if (quantity == -1) {
                System.err.println("Unit " + this.name + " quantity invalid: " + tmp[1]);
                continue;
            }
            if (metadata == -1) {
                System.err.println("Unit " + this.name + " metadata invalid: " + tmp[2]);
                continue;
            }
            ItemStack is = new ItemStack(item, quantity, metadata);
            this.items.add(is);
        }
        return !this.items.isEmpty();
    }
}

