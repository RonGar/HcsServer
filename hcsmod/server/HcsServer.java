/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  co.uk.flansmods.common.FlansModPlayerData
 *  co.uk.flansmods.common.FlansModPlayerHandler
 *  co.uk.flansmods.common.guns.EntityBullet
 *  co.uk.flansmods.common.guns.ItemBullet
 *  co.uk.flansmods.common.guns.ItemGun
 *  co.uk.flansmods.common.network.PacketFlak
 *  co.uk.flansmods.vintarz.BulletSpread
 *  co.uk.flansmods.vintarz.GunTypeMod
 *  co.uk.flansmods.vintarz.PlayerRayTraceResult
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.google.gson.reflect.TypeToken
 *  cpw.mods.fml.common.IPlayerTracker
 *  cpw.mods.fml.common.ITickHandler
 *  cpw.mods.fml.common.event.FMLServerAboutToStartEvent
 *  cpw.mods.fml.common.event.FMLServerStartedEvent
 *  cpw.mods.fml.common.event.FMLServerStartingEvent
 *  cpw.mods.fml.common.event.FMLServerStoppingEvent
 *  cpw.mods.fml.common.network.IPacketHandler
 *  cpw.mods.fml.common.network.NetworkRegistry
 *  cpw.mods.fml.common.network.PacketDispatcher
 *  cpw.mods.fml.common.registry.EntityRegistry
 *  cpw.mods.fml.common.registry.GameRegistry
 *  cpw.mods.fml.common.registry.TickRegistry
 *  cpw.mods.fml.relauncher.FMLLaunchHandler
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  hcsmod.HCS
 *  hcsmod.HCS$EnumEntity
 *  hcsmod.HcsMod
 *  hcsmod.common.map.MapConfig
 *  hcsmod.common.zombie.IndoorChunkInfo
 *  hcsmod.common.zombie.IndoorLocation
 *  hcsmod.common.zombie.OutdoorLocation
 *  hcsmod.common.zombie.SpawnZone
 *  hcsmod.common.zombie.ZombieGroup
 *  hcsmod.entity.EntityPalatka
 *  hcsmod.entity.EntityZombieDayZ
 *  hcsmod.entity.IZombieAttackAI
 *  hcsmod.entity.IZombieWanderAI
 *  hcsmod.flashlight.Flashlight
 *  hcsmod.items.ItemPalatka
 *  hcsmod.player.ExtendedPlayer
 *  hcsplatfom.PlatformBridge
 *  net.minecraft.block.Block
 *  net.minecraft.block.material.Material
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiMultiplayer
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.gui.GuiSelectWorld
 *  net.minecraft.command.ICommand
 *  net.minecraft.command.ServerCommandManager
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.crafting.CraftingManager
 *  net.minecraft.item.crafting.IRecipe
 *  net.minecraft.item.crafting.ShapedRecipes
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.network.NetLoginHandler
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.Packet250CustomPayload
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.ChatMessageComponent
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.MathHelper
 *  net.minecraft.util.MovingObjectPosition
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.World
 *  net.minecraft.world.WorldServer
 *  net.minecraft.world.chunk.Chunk
 *  net.minecraftforge.common.ForgeHooks
 *  net.minecraftforge.common.MinecraftForge
 *  net.vintarz.movement.Movement
 *  org.lwjgl.input.Keyboard
 *  vac.VAC
 *  vintarz.core.VCore
 *  vintarz.core.VSP
 */
package hcsmod.server;

import co.uk.flansmods.common.FlansModPlayerData;
import co.uk.flansmods.common.FlansModPlayerHandler;
import co.uk.flansmods.common.guns.EntityBullet;
import co.uk.flansmods.common.guns.ItemBullet;
import co.uk.flansmods.common.guns.ItemGun;
import co.uk.flansmods.common.network.PacketFlak;
import co.uk.flansmods.vintarz.BulletSpread;
import co.uk.flansmods.vintarz.GunTypeMod;
import co.uk.flansmods.vintarz.PlayerRayTraceResult;
import co.uk.flansmods.vintarz.server.LagCompensator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extendedDmgSrc.ExtendedDamageSource;
import hcsmod.HCS;
import hcsmod.HcsMod;
import hcsmod.clans.server.ClansCommand;
import hcsmod.clans.server.ClansServer;
import hcsmod.common.map.MapConfig;
import hcsmod.common.zombie.IndoorChunkInfo;
import hcsmod.common.zombie.IndoorLocation;
import hcsmod.common.zombie.OutdoorLocation;
import hcsmod.common.zombie.SpawnZone;
import hcsmod.common.zombie.ZombieGroup;
import hcsmod.entity.EntityPalatka;
import hcsmod.entity.EntityZombieDayZ;
import hcsmod.entity.IZombieAttackAI;
import hcsmod.entity.IZombieWanderAI;
import hcsmod.flashlight.Flashlight;
import hcsmod.items.ItemPalatka;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.ChemProtectSuitData;
import hcsmod.server.ClientSideDebug;
import hcsmod.server.EntityHouseServer;
import hcsmod.server.ExtendedStorage;
import hcsmod.server.HCSCommand;
import hcsmod.server.HCSConfig;
import hcsmod.server.HarxCoreArmor;
import hcsmod.server.HcsTrigger;
import hcsmod.server.HouseGetCommand;
import hcsmod.server.HousePutCommand;
import hcsmod.server.ItemPickupUtil;
import hcsmod.server.LimitedSizeByteArrayOutputStream;
import hcsmod.server.MapCommand;
import hcsmod.server.MapMarkersServer;
import hcsmod.server.NotifyCommand;
import hcsmod.server.PlayGift;
import hcsmod.server.PlayGiftAPI;
import hcsmod.server.RandomSpawn;
import hcsmod.server.RandomSpawnCommand;
import hcsmod.server.RepairRecipe;
import hcsmod.server.SEventHandler;
import hcsmod.server.SPacketHandler;
import hcsmod.server.SPlayerTracker;
import hcsmod.server.STickHandler;
import hcsmod.server.SpawnZonesCommand;
import hcsmod.server.ZombiesCommand;
import hcsmod.server.airdrop.ASEventHandler;
import hcsmod.server.airdrop.AirdropSystem;
import hcsmod.server.api.HcsServerAPI;
import hcsmod.server.event.ESEventHandler;
import hcsmod.server.event.EventSystem;
import hcsmod.server.storage.LocationData;
import hcsmod.server.storage.StorageCommand;
import hcsmod.server.storage.StorageGroup;
import hcsmod.server.storage.StorageIO;
import hcsmod.server.vote.VoteGift;
import hcsmod.server.zombie.ZombieAttackAi;
import hcsmod.server.zombie.ZombieSpawner;
import hcsmod.server.zombie.ZombieWanderAi;
import hcsmod.server.zones.PveSystem;
import hcsmod.server.zones.PveZonesCommand;
import hcsmod.server.zones.PvpSystem;
import hcsmod.server.zones.PvpZonesCommand;
import hcsmod.server.zones.regions.PveRegion;
import hcsplatfom.PlatformBridge;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.command.ICommand;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.vintarz.movement.Movement;
import org.lwjgl.input.Keyboard;
import vac.VAC;
import vintarz.core.VCore;
import vintarz.core.VSP;
import vintarz.core.server.VCoreServer;

public class HcsServer {
    private static final ThreadLocal<LimitedSizeByteArrayOutputStream> cache = new ThreadLocal();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Set<String> reported = new HashSet<String>();
    public static final int FLASHLIGHT_BATTERY_SECONDS = 1800;
    private static final Random rand = new Random();
    public static boolean isPVPserver;
    public static boolean isLiteserver;
    public static boolean isHarxcoreServer;
    public static MapConfig mapServerConfig;
    private static boolean aprilFool;
    public static int overrideDimensionId;
    private static boolean isFirstLoad;
    private static String[] allowedAircraft;
    public static int[] bannedItems;
    public static PveSystem pveSystem;
    public static LagCompensator lagCompensator;
    public static Map<String, ZombieGroup> zombieGroups;
    public static Map<String, SpawnZone> spawnZones;
    public static boolean storageEnabled;
    public static final StorageIO customStorage;
    public static HashMap<String, StorageGroup> storageGroups;
    public static final List<HcsTrigger> triggers;
    public static HashMap<Integer, ChemProtectSuitData> CPSData;
    public static HCSConfig hcsConfig;
    public static Packet250CustomPayload worldMarkersPacket;
    public static long tickStart;

    public static void reloadBannedItems() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        try (Scanner in = new Scanner(new File("vz_banned_items.txt"));){
            while (in.hasNextLine()) {
                list.add(Integer.parseUnsignedInt(in.nextLine().split(" ", 2)[0]));
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        bannedItems = HcsServer.intListToArray(list);
        System.out.println("Banned items: " + bannedItems.length);
    }

    public static void aprilFool(boolean aprilFool) {
        HcsServer.aprilFool = aprilFool;
        EntityBullet.FOOL = aprilFool;
        Movement.CJS = aprilFool;
        ItemGun.CHS = aprilFool;
        VSP os = new VSP(17, "HCSMOD");
        try {
            os.writeBoolean(aprilFool);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        os.sendAll();
    }

    public static void reloadCPSData() {
        HcsServer.readCPSData();
        SPacketHandler.sendCPSData(null);
    }

    public static void readHCSConfig() {
        hcsConfig = null;
        Gson gson = new Gson();
        try (InputStreamReader in = new InputStreamReader((InputStream)new FileInputStream("hcsConfig/HCSConfig.json"), StandardCharsets.UTF_8);){
            hcsConfig = (HCSConfig)gson.fromJson((Reader)in, HCSConfig.class);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        WorldServer w = MinecraftServer.getServer().worldServerForDimension(0);
        w.N().setSpawnPosition(HcsServer.hcsConfig.spawnLocation.X, HcsServer.hcsConfig.spawnLocation.Y, HcsServer.hcsConfig.spawnLocation.Z);
        worldMarkersPacket = SPacketHandler.genWorldMarkersPacket();
        if (worldMarkersPacket != null) {
            PacketDispatcher.sendPacketToAllPlayers((Packet)worldMarkersPacket);
        }
    }

    public static void readCPSData() {
        CPSData.clear();
        Gson gson = new Gson();
        try (InputStreamReader in = new InputStreamReader((InputStream)new FileInputStream("hcsConfig/CPSData.json"), StandardCharsets.UTF_8);){
            CPSData = (HashMap)gson.fromJson((Reader)in, new TypeToken<HashMap<Integer, ChemProtectSuitData>>(){}.getType());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readPveZones() {
        HcsServer.pveSystem.regions.clear();
        Gson gson = new Gson();
        try (InputStreamReader in = new InputStreamReader((InputStream)new FileInputStream("hcsConfig/pveZones.json"), StandardCharsets.UTF_8);){
            HcsServer.pveSystem.regions = (Map)gson.fromJson((Reader)in, new TypeToken<HashMap<String, PveRegion>>(){}.getType());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readMapConfig() {
        try (InputStreamReader in = new InputStreamReader((InputStream)new FileInputStream("hcsConfig/map/map.json"), StandardCharsets.UTF_8);){
            Gson gson = new Gson();
            mapServerConfig = (MapConfig)gson.fromJson((Reader)in, MapConfig.class);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void readStorageConfig() {
        JsonObject config = null;
        try (InputStreamReader in = new InputStreamReader((InputStream)new FileInputStream("storage.json"), StandardCharsets.UTF_8);){
            config = (JsonObject)new JsonParser().parse((Reader)in);
            storageEnabled = true;
        }
        catch (IOException e) {
            storageEnabled = false;
        }
        if (config != null && config.has("groups")) {
            JsonArray arr = config.getAsJsonArray("groups");
            for (int i = 0; i < arr.size(); ++i) {
                JsonObject jo = arr.get(i).getAsJsonObject();
                if (jo.has("OpenLocations")) {
                    JsonArray ol = jo.getAsJsonArray("OpenLocations");
                    ArrayList<LocationData> OpenLocations = new ArrayList<LocationData>();
                    for (JsonElement e : ol) {
                        OpenLocations.add(new LocationData(e.getAsJsonArray().get(0).getAsInt(), e.getAsJsonArray().get(1).getAsInt(), e.getAsJsonArray().get(2).getAsInt(), e.getAsJsonArray().get(3).getAsInt(), e.getAsJsonArray().get(4).getAsInt(), e.getAsJsonArray().get(5).getAsInt()));
                    }
                    StorageGroup sg = new StorageGroup(jo.getAsJsonPrimitive("slots").getAsInt(), jo.getAsJsonPrimitive("cooldown").getAsInt(), jo.getAsJsonPrimitive("level").getAsInt(), jo.getAsJsonPrimitive("walkDelay").getAsInt(), jo.getAsJsonPrimitive("inventoryDelay").getAsInt(), OpenLocations, jo.getAsJsonPrimitive("LocationMessage").getAsString());
                    storageGroups.put(jo.getAsJsonPrimitive("name").getAsString(), sg);
                    continue;
                }
                StorageGroup sg = new StorageGroup(jo.getAsJsonPrimitive("slots").getAsInt(), jo.getAsJsonPrimitive("cooldown").getAsInt(), jo.getAsJsonPrimitive("level").getAsInt(), jo.getAsJsonPrimitive("walkDelay").getAsInt(), jo.getAsJsonPrimitive("inventoryDelay").getAsInt());
                storageGroups.put(jo.getAsJsonPrimitive("name").getAsString(), sg);
            }
        }
    }

    public static void readSpawnZones() {
        spawnZones.clear();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        try (InputStreamReader in = new InputStreamReader((InputStream)new FileInputStream("hcsConfig/zombies/spawnZones.json"), StandardCharsets.UTF_8);){
            spawnZones = (Map)gson.fromJson((Reader)in, new TypeToken<HashMap<String, SpawnZone>>(){}.getType());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        HcsServer.computeSpawnZones();
    }

    public static void computeSpawnZones() {
        for (String name : spawnZones.keySet()) {
            SpawnZone zone = spawnZones.get(name);
            zone.outdoorChunks.clear();
            zone.indoorChunks.clear();
            for (OutdoorLocation outdoorLocation : HcsServer.spawnZones.get((Object)name).outdoorLocations) {
                int X = MathHelper.floor_double((double)((double)outdoorLocation.x / 16.0));
                int Z = MathHelper.floor_double((double)((double)outdoorLocation.z / 16.0));
                for (int i = -outdoorLocation.r; i <= outdoorLocation.r; ++i) {
                    for (int j = -outdoorLocation.r; j <= outdoorLocation.r; ++j) {
                        if (!(Math.sqrt(i * i + j * j) <= (double)outdoorLocation.r)) continue;
                        int combined = X + i << 16 | Z + j & 0xFFFF;
                        zone.outdoorChunks.add(combined);
                    }
                }
            }
            for (IndoorLocation indoorLocation : HcsServer.spawnZones.get((Object)name).indoorLocations) {
                for (int i = MathHelper.floor_double((double)((double)indoorLocation.x1 / 16.0)); i <= MathHelper.floor_double((double)((double)indoorLocation.x2 / 16.0)); ++i) {
                    for (int j = MathHelper.floor_double((double)((double)indoorLocation.z1 / 16.0)); j <= MathHelper.floor_double((double)((double)indoorLocation.z2 / 16.0)); ++j) {
                        int combined = i << 16 | j & 0xFFFF;
                        int absoluteX = i * 16;
                        int absoluteZ = j * 16;
                        int shiftX1 = HcsServer.calculateShiftMin(absoluteX, indoorLocation.x1);
                        int shiftZ1 = HcsServer.calculateShiftMin(absoluteZ, indoorLocation.z1);
                        int shiftX2 = HcsServer.calculateShiftMax(absoluteX + 15, indoorLocation.x2);
                        int shiftZ2 = HcsServer.calculateShiftMax(absoluteZ + 15, indoorLocation.z2);
                        IndoorChunkInfo info = new IndoorChunkInfo(indoorLocation.y, shiftX1, shiftZ1, shiftX2, shiftZ2);
                        if (zone.indoorChunks.containsKey(combined)) {
                            ((ArrayList)zone.indoorChunks.get(combined)).add(info);
                            continue;
                        }
                        ArrayList<IndoorChunkInfo> yCoords = new ArrayList<IndoorChunkInfo>();
                        yCoords.add(info);
                        zone.indoorChunks.put(combined, yCoords);
                    }
                }
            }
        }
    }

    public static void readZombieConfig() {
        zombieGroups.clear();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        try (InputStreamReader in = new InputStreamReader((InputStream)new FileInputStream("hcsConfig/zombies/config.json"), StandardCharsets.UTF_8);){
            zombieGroups = (Map)gson.fromJson((Reader)in, new TypeToken<HashMap<String, ZombieGroup>>(){}.getType());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        for (String zombieGroupKey : zombieGroups.keySet()) {
            ZombieGroup zombieGroup = zombieGroups.get(zombieGroupKey);
            zombieGroup.zombieGroupKey = zombieGroupKey;
        }
        HcsServer.addZonesToZombieGroup();
    }

    public static void addZonesToZombieGroup() {
        ZombieSpawner.zombieSpawnCooldowns.clear();
        for (ZombieGroup zombieGroup : zombieGroups.values()) {
            HashMap tmp = new HashMap();
            ZombieSpawner.zombieSpawnCooldowns.put(zombieGroup, tmp);
            for (String zoneName : zombieGroup.spawnZones) {
                if (!spawnZones.containsKey(zoneName)) continue;
                SpawnZone zone = spawnZones.get(zoneName);
                zombieGroup.outdoorChunks.addAll(zone.outdoorChunks);
                zombieGroup.indoorChunks.putAll(zone.indoorChunks);
            }
        }
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

    public static StorageGroup getPlayerStorageGroup(String playerName) {
        StorageGroup sg = storageGroups.get("default");
        for (Map.Entry<String, StorageGroup> m : storageGroups.entrySet()) {
            if (!HcsServer.hasPermission(playerName, "hcs.storage." + m.getKey()) || m.getValue().level <= sg.level) continue;
            sg = m.getValue();
        }
        return sg;
    }

    public static boolean aprilFool() {
        return aprilFool;
    }

    private static void registerHouseCraftingRecipes() {
        ItemStack stone = new ItemStack(Block.stone);
        GameRegistry.addShapelessRecipe((ItemStack)new ItemStack(Block.bedrock), (Object[])new Object[]{stone, stone, stone, stone, stone, stone, stone, stone, stone});
        GameRegistry.addShapedRecipe((ItemStack)new ItemStack(HCS.Palatka.itemID, 1, 5), (Object[])new Object[]{"SPS", "WGW", "BBB", Character.valueOf('S'), new ItemStack(Block.stairsWoodOak), Character.valueOf('P'), new ItemStack(Block.planks), Character.valueOf('W'), new ItemStack(Block.wood), Character.valueOf('G'), new ItemStack(Block.glass), Character.valueOf('B'), new ItemStack(Block.bedrock)});
    }

    public static boolean playerHasData(String username, String data) {
        HashSet list = (HashSet)PlatformBridge.PLAYER_FLAGS.get(username);
        return list != null && list.contains(data);
    }

    public static void init() {
        try {
            HcsServer.$();
        }
        catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    private static void $() {
        if (FMLLaunchHandler.side().isClient()) {
            new ClientSideDebug();
        }
        try {
            NetLoginHandler.CUSTOM_HOSTNAME = "HCS DayZ 1";
            GameRegistry.registerPlayerTracker((IPlayerTracker)new SPlayerTracker());
            TickRegistry.registerTickHandler((ITickHandler)new STickHandler(), (Side)Side.SERVER);
            MinecraftForge.EVENT_BUS.register((Object)new SEventHandler());
            MinecraftForge.EVENT_BUS.register((Object)new ESEventHandler());
            MinecraftForge.EVENT_BUS.register((Object)new ASEventHandler());
            MinecraftForge.EVENT_BUS.register((Object)new ItemPickupUtil());
            NetworkRegistry.instance().registerChannel((IPacketHandler)new SPacketHandler(), "HCSMOD", Side.SERVER);
            EntityRegistry.registerModEntity(EntityHouseServer.class, (String)"ENTITYHouseSWAG1488", (int)HCS.EnumEntity.HOUSE.ordinal(), (Object)HcsMod.INSTANCE, (int)64, (int)Integer.MAX_VALUE, (boolean)false);
            Iterator i = CraftingManager.getInstance().getRecipeList().iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (!(o instanceof ShapedRecipes)) continue;
                ShapedRecipes sr = (ShapedRecipes)o;
                if (sr.recipeOutputItemID != Block.blockIron.blockID) continue;
                i.remove();
            }
            GameRegistry.addRecipe((IRecipe)RepairRecipe.instance);
            HcsServerAPI.playGift = new PlayGiftAPI();
            PlayGift.reloadConfig();
            VoteGift.readConfig();
        }
        catch (Throwable t) {
            t.printStackTrace();
            Runtime.getRuntime().halt(0);
        }
    }

    public static void $(FMLServerStartingEvent event) {
        MinecraftServer srv = event.getServer();
        srv.onTickBegin.add(new Runnable(){

            @Override
            public void run() {
                tickStart = System.currentTimeMillis();
            }
        });
        srv.onTickEnd.add(new Runnable(){

            @Override
            public void run() {
                PlatformBridge.timings.report("ServerTick", System.currentTimeMillis() - tickStart);
            }
        });
        isPVPserver = srv.getMOTD().toLowerCase().contains("pvp");
        isLiteserver = srv.getMOTD().toLowerCase().contains("lite");
        isHarxcoreServer = srv.getMOTD().toLowerCase().contains("hardcore");
        HCS.pvpServer = isPVPserver;
        EntityBullet.HITBOX_EXTEND = Float.parseFloat(System.getProperty("vz.hitbox.extend", "0.2"));
        if (!isPVPserver && srv.isDedicatedServer()) {
            HcsServer.registerHouseCraftingRecipes();
        }
        ((ServerCommandManager)srv.getCommandManager()).a((ICommand)new HCSCommand());
        ((ServerCommandManager)srv.getCommandManager()).a((ICommand)new StorageCommand());
        ((ServerCommandManager)srv.getCommandManager()).a((ICommand)new HouseGetCommand());
        ((ServerCommandManager)srv.getCommandManager()).a((ICommand)new HousePutCommand());
        ((ServerCommandManager)srv.getCommandManager()).a((ICommand)new NotifyCommand());
        ((ServerCommandManager)srv.getCommandManager()).a((ICommand)new ClansCommand());
        ((ServerCommandManager)srv.getCommandManager()).a((ICommand)new ZombiesCommand());
        ((ServerCommandManager)srv.getCommandManager()).a((ICommand)new SpawnZonesCommand());
        ((ServerCommandManager)srv.getCommandManager()).a((ICommand)new PveZonesCommand());
        ((ServerCommandManager)srv.getCommandManager()).a((ICommand)new MapCommand());
        ((ServerCommandManager)srv.getCommandManager()).a((ICommand)new RandomSpawnCommand());
        ((ServerCommandManager)srv.getCommandManager()).a((ICommand)new PvpZonesCommand());
        RandomSpawn.readSpawns();
        HarxCoreArmor.serverStarting();
        if (isFirstLoad) {
            isFirstLoad = false;
            if (isHarxcoreServer) {
                Movement.stepSoundCallback.add(new Consumer<EntityPlayer>(){

                    @Override
                    public void accept(EntityPlayer p) {
                        if (!p.q.isRemote && HCS.isHardcoreServer()) {
                            if (ExtendedStorage.get((ExtendedPlayer)ExtendedPlayer.server((EntityPlayer)p)).hardFeetDamage) {
                                p.attackEntityFrom(DamageSource.fall, 0.021666666f);
                            }
                            if (p.inventory.armorInventory[0] != null && p.inventory.armorInventory[0].itemID == 309) {
                                p.q.playSoundAtEntity((Entity)p, "random.anvil_land", 0.1f, 0.5f);
                            }
                        }
                    }
                });
            }
        }
        HcsServer.readStorageConfig();
        HcsServer.readSpawnZones();
        HcsServer.readZombieConfig();
        HcsServer.readMapConfig();
        HcsServer.readPveZones();
        HcsServer.readCPSData();
        HcsServer.readHCSConfig();
        MapMarkersServer.readMapMarkers();
        EventSystem.readConfig();
        AirdropSystem.readConfig();
        PvpSystem.readConfig();
    }

    public static void $(FMLServerStartedEvent event) {
        HcsServer.reloadTriggers();
    }

    public static void serverStopping(FMLServerStoppingEvent event) {
        for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (!(o instanceof Entity)) continue;
            Entity e = (Entity)o;
            e.mountEntity(null);
        }
        customStorage.shutdown();
    }

    public static void tickFlashlight(ItemStack is) {
        int currentTime;
        int charge = is.getTagCompound().getInteger("F");
        if (charge != 0 && (currentTime = Flashlight.getFlashlightTime((boolean)false)) >= charge) {
            is.getTagCompound().removeTag("F");
        }
    }

    public static boolean toggleFlashLight(EntityPlayer p) {
        if (HcsServer.toggleFlashLight(p.getCurrentEquippedItem(), p)) {
            return true;
        }
        ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
        return HcsServer.toggleFlashLight(ep.inventory.inventoryStacks[4], p);
    }

    public static boolean toggleFlashLight(ItemStack is, EntityPlayer p) {
        if (is != null && is.getItem() == Flashlight.flashlight) {
            NBTTagCompound tag = is.getTagCompound();
            if (tag == null) {
                tag = new NBTTagCompound();
                is.setTagCompound(tag);
            }
            int currentTime = Flashlight.getFlashlightTime((boolean)false);
            int charge = tag.getInteger("Charge");
            if (charge > 0) {
                tag.removeTag("Charge");
                tag.removeTag("on");
                charge = MathHelper.floor_float((float)((float)charge / 20.0f));
                if (tag.getBoolean("on")) {
                    tag.setInteger("F", charge + currentTime);
                } else {
                    tag.setShort("f", (short)charge);
                }
            } else {
                charge = tag.getInteger("F");
                if (charge != 0) {
                    tag.removeTag("F");
                    if ((charge -= currentTime) > 0) {
                        tag.setShort("f", (short)charge);
                    }
                } else {
                    charge = tag.getShort("f");
                    if (charge == 0) {
                        if (p.inventory.hasItem(7456)) {
                            p.inventory.consumeInventoryItem(7456);
                            tag.setShort("f", (short)1800);
                        }
                    } else {
                        tag.removeTag("f");
                        tag.setInteger("F", charge + currentTime);
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static void palatkaRightClick(EntityPlayer p, World w, ItemStack is) {
        if (w.isRemote) {
            return;
        }
        if (isPVPserver) {
            p.a(ChatMessageComponent.createFromText((String)"\u00a7c\u0414\u043e\u043c\u0430 \u0438 \u043f\u0430\u043b\u0430\u0442\u043a\u0438 \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u044b \u043d\u0430 PvP \u0441\u0435\u0440\u0432\u0435\u0440\u0435."));
            is.stackSize = 0;
            return;
        }
        Vec3 vec3 = w.getWorldVec3Pool().getVecFromPool(p.u, p.v + (double)p.getEyeHeight(), p.w);
        Vec3 lookVec = p.aa();
        Vec3 vec31 = w.getWorldVec3Pool().getVecFromPool(p.u + lookVec.xCoord * 6.0, p.v + (double)p.getEyeHeight() + lookVec.yCoord * 6.0, p.w + lookVec.zCoord * 6.0);
        MovingObjectPosition b = w.rayTraceBlocks_do_do(vec3, vec31, false, true);
        if (b != null && b.sideHit == 1) {
            Object e;
            if (is.getItemDamage() == 0) {
                e = new EntityPalatka(p, b.hitVec.xCoord, b.hitVec.yCoord, b.hitVec.zCoord, p.A);
            } else {
                long time = -1L;
                boolean free = false;
                switch (is.getItemDamage()) {
                    case 1: {
                        time = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(3L, TimeUnit.DAYS);
                        break;
                    }
                    case 2: {
                        time = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(7L, TimeUnit.DAYS);
                        break;
                    }
                    case 3: {
                        time = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(30L, TimeUnit.DAYS);
                        break;
                    }
                    case 4: {
                        time = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(90L, TimeUnit.DAYS);
                        break;
                    }
                    case 5: {
                        time = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(2L, TimeUnit.DAYS);
                        free = true;
                    }
                }
                e = new EntityHouseServer(p, b.hitVec.xCoord, b.hitVec.yCoord, b.hitVec.zCoord, (float)((int)((p.A - 45.0f) / 90.0f)) * 90.0f + 45.0f, time, free);
            }
            if (!ItemPalatka.checkPlacement((Entity)e, (EntityPlayer)p)) {
                return;
            }
            w.spawnEntityInWorld((Entity)e);
            --is.stackSize;
        }
    }

    public static void tickPalatka(EntityPalatka e) {
        if (e.oops instanceof EntityLivingBase && e.n == null) {
            EntityLivingBase elb = (EntityLivingBase)e.oops;
            elb.setPositionAndUpdate(e.playerX, e.playerY, e.playerZ);
        }
        e.oops = e.n;
    }

    public static void meleeAttack(EntityPlayerMP p, int lcTick) {
        ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
        if (ep.hitCooldown != 0) {
            return;
        }
        Vec3 look = p.aa();
        double attackRange = p.bG.isCreativeMode ? 48.0 : 1.5;
        look.xCoord *= attackRange;
        look.yCoord *= attackRange;
        look.zCoord *= attackRange;
        Vec3 vecStart = p.q.getWorldVec3Pool().getVecFromPool(p.u, p.v + (double)p.getEyeHeight(), p.w);
        Vec3 vecEnd = p.q.getWorldVec3Pool().getVecFromPool(p.u + look.xCoord, p.v + (double)p.getEyeHeight() + look.yCoord, p.w + look.zCoord);
        MovingObjectPosition movingobjectposition = p.q.rayTraceBlocks_do_do(vecStart, vecEnd, false, true);
        vecStart = p.q.getWorldVec3Pool().getVecFromPool(p.u, p.v + (double)p.getEyeHeight(), p.w);
        PlayerRayTraceResult hitInfo = lagCompensator.rayTracePlayersForTick(p, vecStart, vecEnd = movingobjectposition != null ? movingobjectposition.hitVec : p.q.getWorldVec3Pool().getVecFromPool(p.u + look.xCoord, p.v + (double)p.getEyeHeight() + look.yCoord, p.w + look.zCoord), lcTick);
        if (hitInfo != null && hitInfo.hit.entityHit != null) {
            vecEnd = hitInfo.hit.hitVec;
            ExtendedDamageSource.hitBodyPart = LagCompensator.getHitBodyPart(hitInfo, (Entity)p);
            PacketDispatcher.sendPacketToAllAround((double)vecEnd.xCoord, (double)vecEnd.yCoord, (double)vecEnd.zCoord, (double)128.0, (int)p.q.getWorldInfo().getVanillaDimension(), (Packet)PacketFlak.buildFlakPacket((double)vecEnd.xCoord, (double)vecEnd.yCoord, (double)vecEnd.zCoord, (int)3, (String)"tilecrack_35_14", (double)(look.xCoord / (attackRange *= 10.0)), (double)(look.yCoord / attackRange), (double)(look.zCoord / attackRange)));
            VAC.safeAttackEntity((EntityPlayerMP)p, (Entity)hitInfo.hit.entityHit);
            ExtendedDamageSource.reset();
        }
    }

    public static void onTeleport(EntityPlayerMP p) {
        ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
        ep.hitCooldown = -1;
        ExtendedStorage.get(ep).resetPosition((EntityPlayer)p);
    }

    public static String zombieLoot(EntityPlayer player, Entity zombie) {
        ExtendedStorage.zombieKill(player, zombie);
        return ForgeHooks.getTotalArmorValue((EntityPlayer)player) >= 15 ? "zombieelite" : "zombieloot";
    }

    public static void initExtendedPlayer(ExtendedPlayer ep, Entity entity, World world) {
        ExtendedStorage.get(ep).init(entity, world);
    }

    public static int[] intListToArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < array.length; ++i) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static boolean isStartLoot(ItemStack is) {
        NBTTagCompound tag = is.stackTagCompound;
        return tag != null && tag.hasKey("DAYZSTARTLOOT");
    }

    public static boolean isBannedItem(ItemStack is) {
        for (int id : bannedItems) {
            if (id != is.itemID) continue;
            return true;
        }
        return false;
    }

    public static boolean isHardcoreServer() {
        return isHarxcoreServer;
    }

    public static boolean isAllowedAircraft(String vehName) {
        if (VCore.isSinglePlayer()) {
            return true;
        }
        int hash = vehName.hashCode();
        for (String s : allowedAircraft) {
            if (s.hashCode() != hash || !s.equals(vehName)) continue;
            return true;
        }
        return false;
    }

    @SideOnly(value=Side.CLIENT)
    public static void button3(Object o) {
        if (Keyboard.isKeyDown((int)42) && ("VinTarZ".equals(Minecraft.getMinecraft().getSession().getUsername()) || "vladru".equals(Minecraft.getMinecraft().getSession().getUsername()) || "Tristana".equals(Minecraft.getMinecraft().getSession().getUsername())) && System.getProperty("fml.coreMods.load") != null) {
            if (Keyboard.isKeyDown((int)29) && Keyboard.isKeyDown((int)56)) {
                Minecraft.getMinecraft().displayGuiScreen((GuiScreen)new GuiMultiplayer((GuiScreen)o));
            } else {
                Minecraft.getMinecraft().displayGuiScreen((GuiScreen)new GuiSelectWorld((GuiScreen)o));
            }
        }
    }

    public static void shoot(ItemGun item, ItemStack stack, World world, EntityPlayerMP player, long playerseed, int lcTick) {
        FlansModPlayerData data = FlansModPlayerHandler.getPlayerData((EntityPlayer)player);
        ExtendedStorage storage = ExtendedStorage.get(ExtendedPlayer.server((EntityPlayer)player));
        if (storage.prevGunSeed >= playerseed) {
            storage.reusingGunSeed = true;
            if (storage.prevGunSeed > playerseed) {
                HcsServer.report(player.c_(), "gun seed reuse: less");
            } else {
                HcsServer.report(player.c_(), "gun seed reuse: equals");
            }
            return;
        }
        storage.prevGunSeed = playerseed;
        if (!data.spreadTicked) {
            data.spreadTicked = true;
            BulletSpread.tickWeaponSpread((EntityPlayer)player, (FlansModPlayerData)data);
        }
        if (data.shootClickDelay == 0) {
            long seed = data.seed ^ playerseed;
            GunTypeMod overrideGun = null;
			//lmao nice fix xCharon No Spread | vk.com/xcharon
            boolean cheater = storage.reusingGunSeed || HcsServer.playerHasData(player.c_(), "AimbotSpread");
            EntityBullet.bulletRng.setSeed(cheater ? System.nanoTime() : seed);
            if (cheater && !pveSystem.isPvPDisabled(storage.pvePlayer)) {
                double maxDistance;
                double distance = maxDistance = 128.0;
                double eye = player.getEyeHeight();
                double posX = player.u;
                double posY = player.v;
                double posZ = player.w;
                Vec3 vecStart = player.q.getWorldVec3Pool().getVecFromPool(posX, posY + eye, posZ);
                Vec3 vecEnd = player.aa();
                vecEnd.xCoord *= distance;
                vecEnd.yCoord *= distance;
                vecEnd.zCoord *= distance;
                vecEnd.xCoord += vecStart.xCoord;
                vecEnd.yCoord += vecStart.yCoord;
                vecEnd.zCoord += vecStart.zCoord;
                MovingObjectPosition blockHit = HcsServer.rayTraceBlocks(world, vecStart, vecEnd);
                vecStart = player.q.getWorldVec3Pool().getVecFromPool(posX, posY + eye, posZ);
                if (blockHit != null) {
                    distance = vecStart.distanceTo(blockHit.hitVec);
                }
                vecEnd = player.aa();
                vecEnd.xCoord *= distance;
                vecEnd.yCoord *= distance;
                vecEnd.zCoord *= distance;
                vecEnd.xCoord += vecStart.xCoord;
                vecEnd.yCoord += vecStart.yCoord;
                vecEnd.zCoord += vecStart.zCoord;
                PlayerRayTraceResult hitInfo = lagCompensator.rayTracePlayersForTick(player, vecStart, vecEnd, lcTick);
                if (hitInfo != null && hitInfo.hit.entityHit instanceof EntityPlayerMP) {
                    boolean targetShooting;
                    distance = vecStart.distanceTo(hitInfo.hit.hitVec);
                    boolean continuous = (double)data.weaponSpread >= 0.0011111111111111111;
                    seed = System.nanoTime();
                    if ("VinTarZ".equals(player.c_())) {
                        SPacketHandler.sendHint((EntityPlayer)player, "aimbotSpread", "AimbotSpread: " + distance, 10);
                    }
                    EntityPlayer target = (EntityPlayer)hitInfo.hit.entityHit;
                    ExtendedStorage targetStorage = ExtendedStorage.get(ExtendedPlayer.server((EntityPlayer)target));
                    boolean bl = targetShooting = targetStorage.shootTime > 0;
                    if (targetShooting || continuous) {
                        overrideGun = new GunTypeMod(item.type);
                        overrideGun.damage = (float)((double)overrideGun.damage * (targetShooting ? 0.5 : 0.5 + 0.5 * (double)EntityBullet.bulletRng.nextFloat()));
                        if (data.overrideGuns == null) {
                            data.overrideGuns = new HashMap();
                        }
                        data.overrideGuns.put(item.type, overrideGun);
                    }
                }
            }
            if (data.shootTime <= 0) {
                int bulletID;
                ItemStack bulletStack = null;
                for (bulletID = 0; bulletID < item.type.numAmmoItemsInGun; ++bulletID) {
                    ItemStack checkingStack = item.getBulletItemStack(stack, bulletID);
                    if (checkingStack == null || checkingStack.getItem() == null || checkingStack.getItemDamage() >= checkingStack.getMaxDamage()) continue;
                    bulletStack = checkingStack;
                    break;
                }
                ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
                if (bulletStack == null || !(bulletStack.getItem() instanceof ItemBullet)) {
                    SPacketHandler.sendHint((EntityPlayer)player, "ndrld1", "\u00a7c\u041d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u043e \u043f\u0435\u0440\u0435\u0437\u0430\u0440\u044f\u0434\u0438\u0442\u044c \u043e\u0440\u0443\u0436\u0438\u0435\n\u00a7c\u043d\u0430\u0436\u043c\u0438\u0442\u0435 R", 40);
                } else if (stack.getItemDamage() + 1 < stack.getMaxDamage()) {
                    item.shoot(stack, world, bulletStack, (EntityPlayer)player, lcTick, seed);
                    storage.shootTime = data.shootTime + 1;
                    if (!data.lockAmmo) {
                        bulletStack.setItemDamage(bulletStack.getItemDamage() + 1);
                        item.setBulletItemStack(stack, bulletStack, bulletID);
                    }
                } else {
                    SPacketHandler.sendHint((EntityPlayer)player, "gnbr", "\u00a7c\u041e\u0440\u0443\u0436\u0438\u0435 \u0441\u043b\u043e\u043c\u0430\u043d\u043e, \u043f\u043e\u0447\u0438\u043d\u0438\u0442\u0435 \u0435\u0433\u043e", 40);
                }
            }
            if (overrideGun != null) {
                data.overrideGuns.remove((Object)item.type);
            }
        }
    }

    public static boolean doAttack(EntityBullet entityBullet) {
        if (entityBullet.owner instanceof EntityPlayerMP) {
            return !HcsServer.isInMCHEntity(entityBullet.owner);
        }
        return true;
    }

    public static boolean isInMCHEntity(Entity e) {
        Entity entity = e.ridingEntity;
        if (entity != null) {
            String entityString = EntityList.getEntityString((Entity)entity);
            return entityString != null && entityString.startsWith("mcheli.MCH.E.");
        }
        return false;
    }

    public static boolean healCooldown(EntityPlayer entityplayer) {
        if (entityplayer instanceof EntityPlayerMP) {
            ExtendedStorage storage = ExtendedStorage.get(ExtendedPlayer.server((EntityPlayer)entityplayer));
            if (storage.flansHealCooldown > 0) {
                return false;
            }
            storage.flansHealCooldown = 10;
            return true;
        }
        return false;
    }

    public static NBTTagCompound saveItemToNBT(ItemStack is, boolean save) {
        if (HcsServer.isBannedItem(is)) {
            return null;
        }
        NBTTagCompound tag = is.writeToNBT(new NBTTagCompound());
        if (!save && is.stackTagCompound != null) {
            tag.getCompoundTag("tag").removeTag("pages");
        }
        return tag;
    }

    public static MovingObjectPosition rayTraceBlocks(World worldObj, Vec3 from, Vec3 to) {
        if (!(Double.isNaN(from.xCoord) || Double.isNaN(from.yCoord) || Double.isNaN(from.zCoord))) {
            if (!(Double.isNaN(to.xCoord) || Double.isNaN(to.yCoord) || Double.isNaN(to.zCoord))) {
                int i = MathHelper.floor_double((double)to.xCoord);
                int j = MathHelper.floor_double((double)to.yCoord);
                int k = MathHelper.floor_double((double)to.zCoord);
                int l = MathHelper.floor_double((double)from.xCoord);
                int i1 = MathHelper.floor_double((double)from.yCoord);
                int j1 = MathHelper.floor_double((double)from.zCoord);
                int k1 = 200;
                while (k1-- >= 0) {
                    MovingObjectPosition movingobjectposition;
                    int b0;
                    if (Double.isNaN(from.xCoord) || Double.isNaN(from.yCoord) || Double.isNaN(from.zCoord)) {
                        return null;
                    }
                    if (l == i && i1 == j && j1 == k) {
                        return null;
                    }
                    boolean flag2 = true;
                    boolean flag3 = true;
                    boolean flag4 = true;
                    double d0 = 999.0;
                    double d1 = 999.0;
                    double d2 = 999.0;
                    if (i > l) {
                        d0 = (double)l + 1.0;
                    } else if (i < l) {
                        d0 = (double)l + 0.0;
                    } else {
                        flag2 = false;
                    }
                    if (j > i1) {
                        d1 = (double)i1 + 1.0;
                    } else if (j < i1) {
                        d1 = (double)i1 + 0.0;
                    } else {
                        flag3 = false;
                    }
                    if (k > j1) {
                        d2 = (double)j1 + 1.0;
                    } else if (k < j1) {
                        d2 = (double)j1 + 0.0;
                    } else {
                        flag4 = false;
                    }
                    double d3 = 999.0;
                    double d4 = 999.0;
                    double d5 = 999.0;
                    double d6 = to.xCoord - from.xCoord;
                    double d7 = to.yCoord - from.yCoord;
                    double d8 = to.zCoord - from.zCoord;
                    if (flag2) {
                        d3 = (d0 - from.xCoord) / d6;
                    }
                    if (flag3) {
                        d4 = (d1 - from.yCoord) / d7;
                    }
                    if (flag4) {
                        d5 = (d2 - from.zCoord) / d8;
                    }
                    boolean flag5 = false;
                    if (d3 < d4 && d3 < d5) {
                        b0 = i > l ? 4 : 5;
                        from.xCoord = d0;
                        from.yCoord += d7 * d3;
                        from.zCoord += d8 * d3;
                    } else if (d4 < d5) {
                        b0 = j > i1 ? 0 : 1;
                        from.xCoord += d6 * d4;
                        from.yCoord = d1;
                        from.zCoord += d8 * d4;
                    } else {
                        b0 = k > j1 ? 2 : 3;
                        from.xCoord += d6 * d5;
                        from.yCoord += d7 * d5;
                        from.zCoord = d2;
                    }
                    Vec3 vec32 = worldObj.getWorldVec3Pool().getVecFromPool(from.xCoord, from.yCoord, from.zCoord);
                    vec32.xCoord = MathHelper.floor_double((double)from.xCoord);
                    l = (int)vec32.xCoord;
                    if (b0 == 5) {
                        --l;
                        vec32.xCoord += 1.0;
                    }
                    vec32.yCoord = MathHelper.floor_double((double)from.yCoord);
                    i1 = (int)vec32.yCoord;
                    if (b0 == 1) {
                        --i1;
                        vec32.yCoord += 1.0;
                    }
                    vec32.zCoord = MathHelper.floor_double((double)from.zCoord);
                    j1 = (int)vec32.zCoord;
                    if (b0 == 3) {
                        --j1;
                        vec32.zCoord += 1.0;
                    }
                    int i2 = worldObj.getBlockId(l, i1, j1);
                    int j2 = worldObj.getBlockMetadata(l, i1, j1);
                    Block block = Block.blocksList[i2];
                    if (block == null) continue;
                    Material material = block.blockMaterial;
                    if (!block.isCollidable() || !material.isLiquid() && (material == Material.leaves || material == Material.web || material == Material.plants || material == Material.vine || material == Material.circuits) || EntityBullet.getDamageModifierForBlock((Block)block) != 0.0f || block.getCollisionBoundingBoxFromPool(worldObj, l, i1, j1) == null && !block.canCollideCheck(j2, true) || (movingobjectposition = block.collisionRayTrace(worldObj, l, i1, j1, from, to)) == null) continue;
                    movingobjectposition.hitInfo = block;
                    return movingobjectposition;
                }
                return null;
            }
            return null;
        }
        return null;
    }

    public static boolean hasPermission(String playerName, String permission) {
        return PlatformBridge.playerPermissions.hasPermission(playerName, permission);
    }

    public static void report(String player, String reason) {
        final String entry = player + " " + reason;
        if (reported.add(entry)) {
            VCoreServer.asyncExecutor.submit(new Runnable(){

                @Override
                public void run() {
                    try (BufferedWriter out = new BufferedWriter(new FileWriter(new File("retards.txt"), true));){
                        out.write(sdf.format(GregorianCalendar.getInstance().getTime()) + " " + entry);
                        out.newLine();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static void clearReported() {
        reported.clear();
    }

    public static void assignJuggernaut(String playerName, ItemStack is) {
        if (is.itemID >= HCS.JAG[0].cv && is.itemID <= HCS.JAG[3].cv) {
            NBTTagCompound nbt;
            if (is.getTagCompound() == null) {
                is.setTagCompound(new NBTTagCompound("jugger"));
            }
            if (!(nbt = is.getTagCompound()).hasKey("juggerOwner")) {
                nbt.setString("juggerOwner", playerName.toLowerCase());
            }
        }
    }

    public static void serverAboutToStart(FMLServerAboutToStartEvent event) {
        ClansServer.serverAboutToStart(event);
    }

    public static void reloadTriggers() {
        triggers.clear();
        File triggerRoot = new File("hcsConfig/triggers");
        if (!triggerRoot.isDirectory()) {
            return;
        }
        File[] triggerFiles = triggerRoot.listFiles();
        if (triggerFiles == null) {
            return;
        }
        for (File triggerFile : triggerFiles) {
            try {
                triggers.add(new HcsTrigger(Files.readAllLines(triggerFile.toPath(), StandardCharsets.UTF_8)));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static IZombieAttackAI newZombieAttackAI(EntityZombieDayZ zombie) {
        return new ZombieAttackAi(zombie, 1.0);
    }

    public static IZombieWanderAI newZombieWanderAI(EntityZombieDayZ zombie) {
        return new ZombieWanderAi(zombie, 0.5);
    }

    public static void setZombieWalkPoint(EntityPlayer p) {
        Chunk c = p.q.getChunkFromBlockCoords(MathHelper.floor_double((double)p.u), MathHelper.floor_double((double)p.w));
        for (ZombieGroup zombieGroup : zombieGroups.values()) {
            int radius = zombieGroup.detectShootChunkDist;
            for (int cx = c.xPosition - radius; cx <= c.xPosition + radius; ++cx) {
                for (int cz = c.zPosition - radius; cz <= c.zPosition + radius; ++cz) {
                    Chunk chunk = p.q.getChunkFromChunkCoords(cx, cz);
                    for (List es : chunk.entityLists) {
                        for (Object o : es) {
                            if (!(o instanceof EntityZombieDayZ)) continue;
                            EntityZombieDayZ e = (EntityZombieDayZ)o;
                            if (e.attackAi == null || zombieGroup != e.zombieGroup || e.isDeafShoot) continue;
                            e.attackAi.setWalkTarget(p.u, p.E.minY, p.w);
                        }
                    }
                }
            }
        }
    }

    static {
        mapServerConfig = new MapConfig();
        overrideDimensionId = 0;
        isFirstLoad = true;
        allowedAircraft = new String[]{"bell206l", "bell47g", "bell47gf", "robinson_r44", "robinson_r44f", "mh-6", "fl282", "sh-3", "ch47", "mh-60g", "mh-53e", "an2", "cessna172", "macchi_m33", "macchi_mc72", "p180", "t-4"};
        pveSystem = new PveSystem();
        lagCompensator = new LagCompensator(10);
        zombieGroups = new HashMap<String, ZombieGroup>();
        spawnZones = new HashMap<String, SpawnZone>();
        storageEnabled = false;
        customStorage = new StorageIO(new File("storage"));
        storageGroups = new HashMap();
        triggers = new ArrayList<HcsTrigger>();
        CPSData = new HashMap();
        hcsConfig = new HCSConfig();
        tickStart = 0L;
        HcsServer.reloadBannedItems();
    }
}

