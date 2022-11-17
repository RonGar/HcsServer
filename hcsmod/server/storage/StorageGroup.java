/*
 * Decompiled with CFR 0.150.
 */
package hcsmod.server.storage;

import hcsmod.server.storage.LocationData;
import java.util.ArrayList;
import java.util.List;

public class StorageGroup {
    public static final StorageGroup MAX = new StorageGroup(5, 0, 0, 0, 0);
    public int level;
    public List<LocationData> OpenLocations = new ArrayList<LocationData>();
    public String LocationMessage;
    public int slots;
    public int cooldown;
    public int walkDelay;
    public int inventoryDelay;

    public StorageGroup(int slots, int cooldown, int level, int walkDelay, int inventoryDelay) {
        this.level = level;
        this.slots = slots;
        this.cooldown = cooldown;
        this.walkDelay = walkDelay;
        this.inventoryDelay = inventoryDelay;
        if (level > 5) {
            throw new IllegalArgumentException("slots must be <= 5");
        }
    }

    public StorageGroup(int slots, int cooldown, int level, int walkDelay, int inventoryDelay, List<LocationData> OpenLocations, String LocationMessage) {
        this.level = level;
        this.slots = slots;
        this.cooldown = cooldown;
        this.walkDelay = walkDelay;
        this.inventoryDelay = inventoryDelay;
        this.OpenLocations = OpenLocations;
        this.LocationMessage = LocationMessage;
        if (level > 5) {
            throw new IllegalArgumentException("slots must be <= 5");
        }
    }
}

