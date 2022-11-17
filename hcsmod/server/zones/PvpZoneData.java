/*
 * Decompiled with CFR 0.150.
 */
package hcsmod.server.zones;

import hcsmod.server.zones.regions.Region;
import java.util.ArrayList;

public class PvpZoneData {
    public String name;
    public Region region;
    public int storagePvPDelay;
    public int storageWalkDelay;
    public int storageInventoryDelay;
    public boolean isAirZone = false;
    public ArrayList<String> airNames = null;
    public int airCount;
    public int playerCount;
    public int markerX;
    public int markerZ;
    public String countPermission;
    public String listPermission;
    public String countNoPermText;
    public String listNoPermText;

    public PvpZoneData(Region region, String name) {
        this.region = region;
        this.name = name;
    }
}

