/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  mcheli.aircraft.MCH_EntityAircraft
 *  mcheli.helicopter.MCH_EntityHeli
 *  mcheli.plane.MCP_EntityPlane
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.MathHelper
 *  vintarz.core.VSP
 */
package hcsmod.server.zones;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import hcsmod.server.HcsServer;
import hcsmod.server.SPacketHandler;
import hcsmod.server.zones.PvpZoneData;
import hcsmod.server.zones.regions.PvpRegion;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import mcheli.aircraft.MCH_EntityAircraft;
import mcheli.helicopter.MCH_EntityHeli;
import mcheli.plane.MCP_EntityPlane;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import vintarz.core.VSP;

public class PvpSystem {
    public static Map<String, PvpZoneData> config = new HashMap<String, PvpZoneData>();
    public static Map<Integer, ArrayList<PvpRegion>> data = new HashMap<Integer, ArrayList<PvpRegion>>();
    public static int countAirZones = 0;
    public static int UPDATE_DELAY = 750;

    public static void reloadConfig() {
        PvpSystem.readConfig();
        PvpSystem.sendData(null);
    }

    public static void readConfig() {
        config.clear();
        data.clear();
        countAirZones = 0;
        Gson gson = new GsonBuilder().create();
        try (InputStreamReader in = new InputStreamReader((InputStream)new FileInputStream("hcsConfig/pvpZones.json"), StandardCharsets.UTF_8);){
            config = (Map)gson.fromJson((Reader)in, new TypeToken<Map<String, PvpZoneData>>(){}.getType());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        for (PvpZoneData pvpZoneData : config.values()) {
            if (pvpZoneData.isAirZone) {
                pvpZoneData.airNames = new ArrayList();
                ++countAirZones;
            }
            for (int i = MathHelper.floor_double((double)(pvpZoneData.region.minX / 16.0)); i <= MathHelper.floor_double((double)(pvpZoneData.region.maxX / 16.0)); ++i) {
                for (int j = MathHelper.floor_double((double)(pvpZoneData.region.minZ / 16.0)); j <= MathHelper.floor_double((double)(pvpZoneData.region.maxZ / 16.0)); ++j) {
                    int combined = PvpSystem.getCombinedChunk(i, j);
                    int absoluteX = i * 16;
                    int absoluteZ = j * 16;
                    int shiftMinX = PvpSystem.calculateShiftMin(absoluteX, MathHelper.floor_double((double)pvpZoneData.region.minX));
                    int shiftMinZ = PvpSystem.calculateShiftMin(absoluteZ, MathHelper.floor_double((double)pvpZoneData.region.minZ));
                    int shiftMaxX = PvpSystem.calculateShiftMax(absoluteX + 15, MathHelper.floor_double((double)pvpZoneData.region.maxX));
                    int shiftMaxZ = PvpSystem.calculateShiftMax(absoluteZ + 15, MathHelper.floor_double((double)pvpZoneData.region.maxZ));
                    PvpRegion pvpRegion = new PvpRegion(pvpZoneData, i, j, shiftMinX, pvpZoneData.region.minY, shiftMinZ, shiftMaxX, pvpZoneData.region.maxY, shiftMaxZ);
                    if (data.containsKey(combined)) {
                        data.get(combined).add(pvpRegion);
                        continue;
                    }
                    ArrayList<PvpRegion> regions = new ArrayList<PvpRegion>();
                    regions.add(pvpRegion);
                    data.put(combined, regions);
                }
            }
        }
    }

    public static PvpZoneData isPlayerInside(EntityPlayer ep) {
        int chunkZ;
        int chunkX = MathHelper.floor_double((double)(ep.u / 16.0));
        int combined = PvpSystem.getCombinedChunk(chunkX, chunkZ = MathHelper.floor_double((double)(ep.w / 16.0)));
        if (!data.containsKey(combined)) {
            return null;
        }
        ArrayList<PvpRegion> regions = data.get(combined);
        for (PvpRegion region : regions) {
            if (!region.inside(ep.u, ep.v, ep.w)) continue;
            return region.pvpZoneData;
        }
        return null;
    }

    public static void tick() {
        for (PvpZoneData pvpZone : config.values()) {
            if (!pvpZone.isAirZone) continue;
            pvpZone.airCount = 0;
            pvpZone.playerCount = 0;
            if (pvpZone.airNames == null) {
                pvpZone.airNames = new ArrayList();
                continue;
            }
            pvpZone.airNames.clear();
        }
        for (PvpZoneData o : MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).playerEntityList) {
            EntityPlayer p;
            PvpZoneData pvpZone;
            if (!(o instanceof EntityPlayer) || (pvpZone = PvpSystem.isPlayerInside(p = (EntityPlayer)o)) == null) continue;
            SPacketHandler.sendHint(p, "pvpZoneName", "\u041b\u043e\u043a\u0430\u0446\u0438\u044f: " + pvpZone.name, 10);
            if (!pvpZone.isAirZone) continue;
            ++pvpZone.playerCount;
            if (!(p.o instanceof MCH_EntityAircraft)) continue;
            ++pvpZone.airCount;
            if (p.o instanceof MCH_EntityHeli) {
                pvpZone.airNames.add(((MCH_EntityHeli)p.o).getHeliInfo().displayName);
                continue;
            }
            if (!(p.o instanceof MCP_EntityPlane)) continue;
            pvpZone.airNames.add(((MCP_EntityPlane)p.o).getPlaneInfo().displayName);
        }
    }

    public static void sendData(EntityPlayer p) {
        VSP os = new VSP(53, "HCSMOD");
        try {
            os.writeByte(countAirZones);
            for (String name : config.keySet()) {
                PvpZoneData pvpZone = config.get(name);
                if (!pvpZone.isAirZone) continue;
                os.writeUTF(name);
                os.writeShort(pvpZone.markerX);
                os.writeShort(pvpZone.markerZ);
                os.writeUTF(pvpZone.countNoPermText);
                os.writeUTF(pvpZone.listNoPermText);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if (p != null) {
            os.send(p);
        } else {
            os.sendAll();
        }
    }

    public static void sendUpdate(EntityPlayer p) {
        VSP os = new VSP(54, "HCSMOD");
        try {
            os.writeByte(countAirZones);
            for (String name : config.keySet()) {
                PvpZoneData pvpZone = config.get(name);
                if (!pvpZone.isAirZone) continue;
                os.writeUTF(name);
                boolean hasPermToCount = HcsServer.hasPermission(p.getCommandSenderName(), pvpZone.countPermission);
                boolean hasPermToList = HcsServer.hasPermission(p.getCommandSenderName(), pvpZone.listPermission);
                byte perms = 0;
                if (hasPermToCount) {
                    perms = (byte)(perms | true ? 1 : 0);
                }
                if (hasPermToList) {
                    perms = (byte)(perms | 2);
                }
                os.writeByte((int)perms);
                if (hasPermToCount) {
                    os.writeShort(pvpZone.airCount);
                    os.writeShort(pvpZone.playerCount);
                } else {
                    os.writeBoolean(pvpZone.airCount > 0);
                }
                if (!hasPermToList) continue;
                os.writeShort(pvpZone.airNames.size());
                for (String airName : pvpZone.airNames) {
                    os.writeUTF(airName);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if (p != null) {
            os.send(p);
        } else {
            os.sendAll();
        }
    }

    private static int getCombinedChunk(int x, int z) {
        return x << 16 | z & 0xFFFF;
    }

    private static int getChunkX(int combined) {
        return (short)(combined >> 16);
    }

    private static int getChunkZ(int combined) {
        return (short)combined;
    }

    private static int calculateShiftMin(int absolute, int indoor) {
        if (indoor <= absolute) {
            return 0;
        }
        return Math.abs(indoor - absolute);
    }

    private static int calculateShiftMax(int absolute, int indoor) {
        if (indoor >= absolute) {
            return 15;
        }
        return 15 - Math.abs(absolute - indoor);
    }
}

