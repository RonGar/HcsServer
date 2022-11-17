/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 */
package vintarz.ingamestore.server.lss;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import net.minecraft.item.ItemStack;
import vintarz.ingamestore.server.lss.RandomUnit;

public class RandomGroup {
    public static Map<String, RandomGroup> groups = new LinkedHashMap<String, RandomGroup>();
    private List<Entry> units = new ArrayList<Entry>();
    private final Random rng = new Random();
    public int cost;
    public int count = 1;
    public float total;
    public String name;
    public String description = "";

    public List<ItemStack> getRandomLoot() {
        RandomUnit[] tmp = new RandomUnit[this.count];
        int i = 0;
        block0: while (i < this.count) {
            float r = this.rng.nextFloat() * this.total;
            for (Entry e : this.units) {
                if (!(r <= e.chance)) continue;
                tmp[i] = e.unit;
                ++i;
                continue block0;
            }
        }
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        for (RandomUnit u : tmp) {
            for (ItemStack is : u.getLoot()) {
                list.add(is);
            }
        }
        return list;
    }

    public static void read() {
        ArrayList<RandomGroup> groups = new ArrayList<RandomGroup>();
        File root = new File("vShop/randomGroups/");
        if (root.isDirectory()) {
            RandomGroup g = new RandomGroup();
            for (File f : root.listFiles()) {
                g.name = f.getName();
                if (!g.name.endsWith(".txt")) continue;
                g.description = g.name = g.name.substring(0, g.name.length() - 4);
                g.total = 0.0f;
                System.out.println("Trying to read group " + g.name);
                Scanner in = null;
                try {
                    in = new Scanner(f, "UTF-8");
                    String[] temp = in.nextLine().split(" ");
                    g.cost = Integer.parseInt(temp[0]);
                    try {
                        g.count = Integer.parseInt(temp[1]);
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                    boolean description = false;
                    while (in.hasNextLine()) {
                        String line = in.nextLine();
                        if (line.isEmpty()) {
                            description = true;
                            continue;
                        }
                        if (!description) {
                            String[] s = line.split(" ", 2);
                            if (s.length != 2) continue;
                            try {
                                float chance = Float.parseFloat(s[0]);
                                if (chance < 0.0f || chance > 100.0f) {
                                    System.err.println("Group " + g.name + " chance 0.0 - 100.0: " + s[0]);
                                    continue;
                                }
                                RandomUnit u = RandomUnit.get(s[1]);
                                if (u != null) {
                                    System.out.println("Group " + g.name + " unit " + u.name() + " " + chance + "%");
                                    g.units.add(new Entry(u, chance + g.total));
                                    g.total += chance;
                                    continue;
                                }
                                System.out.println("Group " + g.name + " unit invalid: " + s[1]);
                            }
                            catch (Throwable t) {
                                System.err.println("Group " + g.name + " chance invalid: " + s[0]);
                            }
                            continue;
                        }
                        g.description = g.description + "\n" + line;
                    }
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
                try {
                    in.close();
                    System.out.println("Group " + g.name + " loaded");
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                if (g.units.isEmpty()) continue;
                groups.add(g);
                g = new RandomGroup();
            }
        }
        Collections.sort(groups, new Comparator<RandomGroup>(){

            @Override
            public int compare(RandomGroup o1, RandomGroup o2) {
                return o1.cost - o2.cost;
            }
        });
        RandomGroup.groups.clear();
        for (RandomGroup g : groups) {
            System.out.println(g.cost + " " + g.name);
            RandomGroup.groups.put(g.name, g);
        }
    }

    private static class Entry {
        private final RandomUnit unit;
        private final float chance;

        private Entry(RandomUnit unit, float chance) {
            this.unit = unit;
            this.chance = chance;
        }
    }
}

