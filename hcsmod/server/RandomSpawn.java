/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NBTBase
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.nbt.NBTTagList
 *  net.minecraft.nbt.NBTTagString
 *  net.minecraft.server.MinecraftServer
 */
package hcsmod.server;

import hcsmod.player.ExtendedPlayer;
import hcsmod.server.HcsServer;
import hcsmod.server.Location;
import hcsmod.server.SPacketHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;

public class RandomSpawn {
    public static final Map<String, RandomSpawn> spawns = new HashMap<String, RandomSpawn>();
    private static RandomSpawn def;
    private static Random random;
    public final double posX;
    public final double posZ;
    public final double radius;
    public ArrayList<Location> locations;
    public final long cooldown;
    public String name;

    public static void readSpawns() {
        spawns.clear();
        def = new RandomSpawn(MinecraftServer.getServer().getFile("SpawnLocations.txt"), null);
        File root = MinecraftServer.getServer().getFile("SpawnLocations");
        for (File f : root.listFiles()) {
            String name = f.getName();
            if (name.length() <= 5 || !name.endsWith(".txt")) continue;
            name = name.substring(0, name.length() - 4);
            spawns.put(name, new RandomSpawn(f, name));
        }
    }

    public static RandomSpawn get(String point) {
        if (point == null || point.isEmpty()) {
            return def;
        }
        RandomSpawn rs = spawns.get(point);
        return rs;
    }

    private RandomSpawn(File f, String s) {
        ArrayList<Location> loc = new ArrayList<Location>();
        double posX = 0.0;
        double posZ = 0.0;
        double radius = 0.0;
        long cooldown = 0L;
        try {
            String[] str;
            Scanner in = new Scanner(f);
            if (s != null) {
                str = in.nextLine().split(" ");
                posX = Integer.parseInt(str[0]);
                posZ = Integer.parseInt(str[1]);
                try {
                    radius = Integer.parseInt(str[2]);
                    cooldown = Integer.parseInt(str[3]);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            while (in.hasNextLine()) {
                str = in.nextLine().split(" ");
                try {
                    int x = Integer.parseInt(str[0]);
                    int y = Integer.parseInt(str[1]);
                    int z = Integer.parseInt(str[2]);
                    loc.add(new Location(x, y, z));
                }
                catch (Exception exception) {}
            }
            in.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.locations = new ArrayList(loc);
        this.posX = posX;
        this.posZ = posZ;
        this.radius = radius;
        this.cooldown = cooldown;
        this.name = s;
    }

    public void respawnPlayer(EntityPlayer p) {
        Collections.shuffle(this.locations);
        if (this.locations.size() > 0) {
            Location locOnMaxDistance = null;
            int maxDistance = -1;
            for (Location l : this.locations) {
                EntityPlayer ep = p.q.getClosestPlayer((double)l.X, (double)l.Y, (double)l.Z, 64.0);
                if (ep == null) {
                    this.internalSpawn(p, l);
                    return;
                }
                double dX = ep.u - (double)l.X;
                double dY = ep.v - (double)l.Y;
                double dZ = ep.w - (double)l.Z;
                int distance = (int)Math.sqrt(dX * dX + dY * dY + dZ * dZ);
                if (distance <= maxDistance) continue;
                locOnMaxDistance = l;
                maxDistance = distance;
            }
            if (locOnMaxDistance == null) {
                locOnMaxDistance = this.locations.get(random.nextInt(this.locations.size()));
            }
            this.internalSpawn(p, locOnMaxDistance);
        }
    }

    private void internalSpawn(EntityPlayer p, Location loc) {
        p.A = p.aD().nextFloat() * 360.0f - 180.0f;
        p.a((double)((float)loc.X + 0.5f), (double)loc.Y, (double)((float)loc.Z + 0.5f));
        ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
        SPacketHandler.broadcastExtendedData(p, ep);
        if (HcsServer.hcsConfig.refreshProvisions) {
            ep.feed(0, 0);
            ep.water(0, 0);
        }
        RandomSpawn.addStartLoot(p);
        ep.firstSpawnTick = true;
    }

    private static void addStartLoot(EntityPlayer p, int slot, Item item) {
        p.inventory.mainInventory[slot] = RandomSpawn.createStartLoot(item);
    }

    public static void addStartLoot(EntityPlayer p) {
        for (int slotId = 0; slotId < HcsServer.hcsConfig.spawnLoot.size(); ++slotId) {
            RandomSpawn.addStartLoot(p, slotId, Item.itemsList[HcsServer.hcsConfig.spawnLoot.get(slotId)]);
        }
    }

    public static ItemStack createStartLoot(Item item) {
        NBTTagList lore = new NBTTagList("Lore");
        lore.appendTag((NBTBase)new NBTTagString(null, "\u0421\u0442\u0430\u0440\u0442\u043e\u0432\u044b\u0439 \u043b\u0443\u0442. \u041d\u0435 \u0432\u044b\u0431\u0440\u0430\u0441\u044b\u0432\u0430\u0435\u0442\u0441\u044f."));
        lore.appendTag((NBTBase)new NBTTagString(null, "Start loot. Cannot be dropped."));
        NBTTagCompound display = new NBTTagCompound("display");
        display.setTag("Lore", (NBTBase)lore);
        NBTTagCompound nbt = new NBTTagCompound("tag");
        nbt.setTag("display", (NBTBase)display);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("DAYZSTARTLOOT", true);
        tag.setTag("display", (NBTBase)display);
        ItemStack is = new ItemStack(item);
        is.stackTagCompound = tag;
        return is;
    }

    static {
        random = new Random();
    }
}

