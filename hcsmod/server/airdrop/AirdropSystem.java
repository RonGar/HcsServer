/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  cpw.mods.fml.common.network.PacketDispatcher
 *  hcsmod.common.HCSUtils
 *  hcsmod.common.zombie.ZombieGroup
 *  hcsmod.entity.EntityZombieDayZ
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.Packet62LevelSound
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.ChatMessageComponent
 *  net.minecraft.util.MathHelper
 *  vintarz.core.VSP
 */
package hcsmod.server.airdrop;

import com.google.gson.Gson;
import cpw.mods.fml.common.network.PacketDispatcher;
import hcsmod.common.HCSUtils;
import hcsmod.common.zombie.ZombieGroup;
import hcsmod.entity.EntityZombieDayZ;
import hcsmod.server.HcsServer;
import hcsmod.server.MapCommand;
import hcsmod.server.SPacketHandler;
import hcsmod.server.zombie.ZombieSpawner;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet62LevelSound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.MathHelper;
import vintarz.core.VSP;

public class AirdropSystem {
    public static Random rnd = new Random();
    public static Config config = new Config();
    public static HashMap<Long, Airdrop> airdrops = new HashMap();
    public static HashMap<String, Spawn> spawns = new HashMap();
    public static Map<String, List<AirdropLocation>> availableLocations = new HashMap<String, List<AirdropLocation>>();

    private static String getAirdropZombieGroupKey(Airdrop airdrop, String zombieGroupName) {
        return airdrop.typeName + "_" + zombieGroupName + "_" + airdrop.uniqueID;
    }

    public static void reloadConfig() {
        for (Airdrop airdrop : airdrops.values()) {
            AirdropSystem.sendUpdate(Airdrop.State.FINISHED, false, airdrop, null);
        }
        for (Airdrop airdrop : airdrops.values()) {
            for (ZombieSpawnData data : airdrop.type.addZombieGroups) {
                HcsServer.zombieGroups.remove(AirdropSystem.getAirdropZombieGroupKey(airdrop, data.zombieGroup));
            }
            AirdropSystem.removeAllCustomZombies(airdrop);
        }
        airdrops.clear();
        availableLocations.clear();
        spawns.clear();
        AirdropSystem.readConfig();
        for (String playerName : MapCommand.airdropSpawnList.keySet()) {
            HashSet<String> regions = MapCommand.airdropSpawnList.get(playerName);
            for (String region : regions) {
                if (AirdropSystem.config.regions.containsKey(region)) {
                    List<AirdropLocation> airdropLocations = AirdropSystem.config.regions.get(region);
                    MapCommand.sendMapDataToClient(true, "airdropSpawn_" + region, airdropLocations, (EntityPlayer)MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).getPlayerForUsername(playerName));
                    continue;
                }
                MapCommand.sendMapDataToClient(false, "airdropSpawn_" + region, null, (EntityPlayer)MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).getPlayerForUsername(playerName));
            }
        }
    }

    public static void readConfig() {
        Gson gson = new Gson();
        try (InputStreamReader in = new InputStreamReader((InputStream)new FileInputStream("hcsConfig/airdrops.json"), StandardCharsets.UTF_8);){
            config = (Config)gson.fromJson((Reader)in, Config.class);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        for (String region : AirdropSystem.config.regions.keySet()) {
            availableLocations.put(region, AirdropSystem.config.regions.get(region));
        }
        for (String spawnTypeName : AirdropSystem.config.spawnTypes.keySet()) {
            Config.SpawnType spawnType = AirdropSystem.config.spawnTypes.get(spawnTypeName);
            Spawn spawn = new Spawn(spawnTypeName, spawnType);
            for (AirdropSpawnData spawnData : spawnType.spawnList.values()) {
                spawn.typesAllWeight += spawnData.weight;
            }
            spawns.put(spawnTypeName, spawn);
        }
    }

    private static Airdrop createNewAirdrop(Spawn spawn) {
        List<AirdropLocation> locations;
        if (availableLocations.size() == 0) {
            return null;
        }
        String typeName = null;
        float r = rnd.nextFloat() * spawn.typesAllWeight;
        float countWeight = 0.0f;
        for (String spawnDataName : spawn.type.spawnList.keySet()) {
            AirdropSpawnData spawnData = spawn.type.spawnList.get(spawnDataName);
            if (!((countWeight += spawnData.weight) >= r)) continue;
            typeName = spawnDataName;
            break;
        }
        if (typeName != null && (locations = availableLocations.get(spawn.type.spawnList.get(typeName).region)).size() > 0) {
            AirdropLocation location = null;
            for (int attempt = 0; attempt < 16; ++attempt) {
                int locationId = rnd.nextInt(locations.size());
                AirdropLocation tmp = locations.get(locationId);
                if (MinecraftServer.getServer().worldServerForDimension(0).a((double)tmp.x, (double)tmp.y, (double)tmp.z, (double)AirdropSystem.config.broadcastRadius) != null) continue;
                location = tmp;
                locations.remove(locationId);
                break;
            }
            if (location == null) {
                return null;
            }
            Airdrop airdrop = new Airdrop(typeName, spawn, location);
            if (airdrop.state == Airdrop.State.RUNNING) {
                AirdropSystem.addAirdropZombieGroup(airdrop);
            }
            for (Object o : MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).playerEntityList) {
                if (!(o instanceof EntityPlayer)) continue;
                ((EntityPlayer)o).a(ChatMessageComponent.createFromText((String)"\u00a7c\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435! \u00a7a\u041d\u0430 \u043a\u0430\u0440\u0442\u0435 \u043e\u0442\u043c\u0435\u0447\u0435\u043d \u043d\u043e\u0432\u044b\u0439 \u0430\u0440\u043c\u0435\u0439\u0441\u043a\u0438\u0439 \u0433\u0440\u0443\u0437"));
            }
            AirdropSystem.sendUpdate(airdrop.state, true, airdrop, null);
            ++spawn.count;
            return airdrop;
        }
        return null;
    }

    private static void addAirdropZombieGroup(Airdrop airdrop) {
        for (ZombieSpawnData data : airdrop.type.addZombieGroups) {
            ZombieGroup airdropZombieGroup = new ZombieGroup(HcsServer.zombieGroups.get(data.zombieGroup));
            airdropZombieGroup.parentZombieGroupName = data.zombieGroup;
            airdropZombieGroup.airdropUniqueId = airdrop.uniqueID;
            HashMap tmp = new HashMap();
            ZombieSpawner.zombieSpawnCooldowns.put(airdropZombieGroup, tmp);
            int X = MathHelper.floor_double((double)((double)airdrop.location.x / 16.0));
            int Z = MathHelper.floor_double((double)((double)airdrop.location.z / 16.0));
            HashSet<Integer> outdoorChunks = new HashSet<Integer>();
            int radius = data.chunkRadius;
            for (int i = -radius; i <= radius; ++i) {
                for (int j = -radius; j <= radius; ++j) {
                    if (!(Math.sqrt(i * i + j * j) <= (double)radius)) continue;
                    int combined = X + i << 16 | Z + j & 0xFFFF;
                    outdoorChunks.add(combined);
                }
            }
            airdropZombieGroup.outdoorChunks.addAll(outdoorChunks);
            if (airdrop.type.checkGroups.contains(data.zombieGroup)) {
                airdropZombieGroup.lootGroup = airdrop.type.activeZombieLootGroup;
            }
            if (airdrop.type.checkGroups.contains(airdropZombieGroup.parentZombieGroupName)) {
                airdropZombieGroup.name = "\u00a7c" + airdropZombieGroup.name;
            }
            HcsServer.zombieGroups.put(AirdropSystem.getAirdropZombieGroupKey(airdrop, data.zombieGroup), airdropZombieGroup);
        }
    }

    public static void tick() {
        if (!AirdropSystem.config.enabled) {
            return;
        }
        long now = System.currentTimeMillis();
        for (Spawn spawn : spawns.values()) {
            if (spawn.count >= spawn.type.maxCount || now < spawn.nextAirdropSpawn) continue;
            Airdrop airdrop = AirdropSystem.createNewAirdrop(spawn);
            if (airdrop != null) {
                airdrops.put(airdrop.uniqueID, airdrop);
                spawn.nextAirdropSpawn = System.currentTimeMillis() + (long)(rnd.nextInt(spawn.type.timeInterval[1] - spawn.type.timeInterval[0] + 1) + spawn.type.timeInterval[0]) * 1000L;
                continue;
            }
            spawn.nextAirdropSpawn = System.currentTimeMillis() + 15000L;
        }
        for (Airdrop airdrop : airdrops.values()) {
            int time;
            EntityPlayer ep;
            if (airdrop.state == Airdrop.State.WAITING) {
                if (MinecraftServer.getServer().getTickCounter() % 5 == 0) {
                    for (Object o : MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).playerEntityList) {
                        if (!(o instanceof EntityPlayer)) continue;
                        ep = (EntityPlayer)o;
                        if (!(Math.pow(ep.u - (double)airdrop.location.x, 2.0) + Math.pow(ep.w - (double)airdrop.location.z, 2.0) <= Math.pow(AirdropSystem.config.broadcastRadius, 2.0))) continue;
                        time = (int)(airdrop.runTime - System.currentTimeMillis());
                        SPacketHandler.sendHint(ep, "airdInfo", "\u0427\u0435\u0440\u0435\u0437 " + HCSUtils.timerText((int)((int)((long)time / 1000L))) + "\n\u0437\u0434\u0435\u0441\u044c \u043f\u043e\u044f\u0432\u0438\u0442\u0441\u044f \u0430\u0440\u043c\u0435\u0439\u0441\u043a\u0438\u0439 \u0433\u0440\u0443\u0437 \u0438 \u0431\u043e\u0442\u044b!", 10);
                    }
                }
                if (System.currentTimeMillis() < airdrop.runTime) continue;
                airdrop.state = Airdrop.State.RUNNING;
                AirdropSystem.addAirdropZombieGroup(airdrop);
                AirdropSystem.sendUpdate(airdrop.state, false, airdrop, null);
                continue;
            }
            if (airdrop.state == Airdrop.State.RUNNING) {
                if (System.currentTimeMillis() >= airdrop.endTime) {
                    AirdropSystem.removeAllCustomZombies(airdrop);
                    AirdropSystem.finish(airdrop);
                    continue;
                }
                if (MinecraftServer.getServer().getTickCounter() % 5 == 0) {
                    for (Object o : MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).playerEntityList) {
                        if (!(o instanceof EntityPlayer)) continue;
                        ep = (EntityPlayer)o;
                        if (!(Math.pow(ep.u - (double)airdrop.location.x, 2.0) + Math.pow(ep.w - (double)airdrop.location.z, 2.0) <= Math.pow(AirdropSystem.config.broadcastRadius, 2.0))) continue;
                        time = (int)(airdrop.endTime - System.currentTimeMillis());
                        SPacketHandler.sendHint(ep, "airdInfo", "\u0412\u044b \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0440\u044f\u0434\u043e\u043c \u0441 \u0430\u0440\u043c\u0435\u0439\u0441\u043a\u0438\u043c \u0433\u0440\u0443\u0437\u043e\u043c\n\u0412\u0440\u0435\u043c\u044f \u0434\u043e \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u0438\u044f " + HCSUtils.timerText((int)((int)((long)time / 1000L))) + "\n\u0423\u0431\u0435\u0439\u0442\u0435 \u0431\u043e\u0442\u043e\u0432 \u0441 \u043a\u0440\u0430\u0441\u043d\u044b\u043c \u043d\u0438\u043a\u043e\u043c " + airdrop.kills + "/" + airdrop.type.goal + ",\n\u0447\u0442\u043e\u0431\u044b \u0437\u0430\u0431\u0440\u0430\u0442\u044c \u0432\u0435\u0449\u0438", 10);
                    }
                }
                if (airdrop.kills < airdrop.type.goal) continue;
                AirdropSystem.revertZombieNames(airdrop);
                airdrop.state = Airdrop.State.LOOT;
                AirdropSystem.sendUpdate(airdrop.state, false, airdrop, null);
                Packet62LevelSound pt = new Packet62LevelSound("random.chestopen", (double)airdrop.location.x, (double)airdrop.location.y, (double)airdrop.location.z, 4.0f, 1.0f);
                PacketDispatcher.sendPacketToAllAround((double)airdrop.location.x, (double)airdrop.location.y, (double)airdrop.location.z, (double)(AirdropSystem.config.broadcastRadius + 16), (int)0, (Packet)pt);
                continue;
            }
            if (airdrop.state != Airdrop.State.LOOT || MinecraftServer.getServer().getTickCounter() % 5 != 0) continue;
            for (Object o : MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).playerEntityList) {
                if (!(o instanceof EntityPlayer)) continue;
                ep = (EntityPlayer)o;
                if (!(Math.pow(ep.u - (double)airdrop.location.x, 2.0) + Math.pow(ep.w - (double)airdrop.location.z, 2.0) <= Math.pow(AirdropSystem.config.broadcastRadius, 2.0))) continue;
                SPacketHandler.sendHint(ep, "airdName", "\u0412\u044b \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0440\u044f\u0434\u043e\u043c \u0441 \u0430\u0440\u043c\u0435\u0439\u0441\u043a\u0438\u043c \u0433\u0440\u0443\u0437\u043e\u043c", 10);
                SPacketHandler.sendHint(ep, "airdInfo", "\u0417\u0430\u0431\u0435\u0440\u0438\u0442\u0435 \u043b\u0443\u0442 \u0438\u0437 \u0441\u0443\u043d\u0434\u0443\u043a\u0430", 10);
            }
        }
        airdrops.values().removeIf(airdrop1 -> airdrop1.state == Airdrop.State.FINISHED);
    }

    private static void revertZombieNames(Airdrop airdrop) {
        for (ZombieSpawnData data : airdrop.type.addZombieGroups) {
            ZombieGroup parentZombieGroup = HcsServer.zombieGroups.get(data.zombieGroup);
            ZombieGroup customZombieGroup = HcsServer.zombieGroups.get(AirdropSystem.getAirdropZombieGroupKey(airdrop, data.zombieGroup));
            customZombieGroup.name = parentZombieGroup.name;
            HashMap<Integer, Long> spawnCooldowns = ZombieSpawner.zombieSpawnCooldowns.get((Object)customZombieGroup);
            spawnCooldowns.replaceAll((c, v) -> System.currentTimeMillis() + (long)(customZombieGroup.spawnInChunkCooldownTick / 20) * 1000L);
        }
        for (ZombieSpawnData o : MinecraftServer.getServer().getEntityWorld().loadedEntityList) {
            if (!(o instanceof EntityZombieDayZ)) continue;
            EntityZombieDayZ entityZombieDayZ = (EntityZombieDayZ)o;
            ZombieGroup zombieGroup = entityZombieDayZ.zombieGroup;
            if (zombieGroup.airdropUniqueId == -1L || zombieGroup.airdropUniqueId != airdrop.uniqueID) continue;
            entityZombieDayZ.updateName();
        }
    }

    public static void capture(Airdrop airdrop, String playerName) {
        if (EntityZombieDayZ.randomDropCallback != null && !airdrop.type.lootGroup.isEmpty()) {
            EntityZombieDayZ.randomDropCallback.dropZombieLoot((double)airdrop.location.x, (double)airdrop.location.y, (double)airdrop.location.z, playerName, airdrop.type.lootGroup);
        }
        AirdropSystem.resetZombieGroupToParent(airdrop);
        AirdropSystem.finish(airdrop);
    }

    private static void finish(Airdrop airdrop) {
        for (ZombieSpawnData data : airdrop.type.addZombieGroups) {
            HcsServer.zombieGroups.remove(AirdropSystem.getAirdropZombieGroupKey(airdrop, data.zombieGroup));
        }
        airdrop.state = Airdrop.State.FINISHED;
        AirdropSystem.sendUpdate(airdrop.state, false, airdrop, null);
        List<AirdropLocation> locations = availableLocations.get(airdrop.spawn.type.spawnList.get((Object)airdrop.typeName).region);
        locations.add(airdrop.location);
        airdrop.spawn.nextAirdropSpawn = System.currentTimeMillis() + (long)(rnd.nextInt(airdrop.spawn.type.timeInterval[1] - airdrop.spawn.type.timeInterval[0] + 1) + airdrop.spawn.type.timeInterval[0]) * 1000L;
        --airdrop.spawn.count;
    }

    private static void resetZombieGroupToParent(Airdrop airdrop) {
        block0: for (Object o : MinecraftServer.getServer().getEntityWorld().loadedEntityList) {
            if (!(o instanceof EntityZombieDayZ)) continue;
            EntityZombieDayZ entityZombieDayZ = (EntityZombieDayZ)o;
            ZombieGroup zombieGroup = entityZombieDayZ.zombieGroup;
            if (zombieGroup.airdropUniqueId == -1L || zombieGroup.airdropUniqueId != airdrop.uniqueID) continue;
            for (ZombieSpawnData data : airdrop.type.addZombieGroups) {
                if (!zombieGroup.parentZombieGroupName.equals(data.zombieGroup)) continue;
                entityZombieDayZ.zombieGroup = HcsServer.zombieGroups.get(data.zombieGroup);
                continue block0;
            }
        }
    }

    private static void removeAllCustomZombies(Airdrop airdrop) {
        for (Object o : MinecraftServer.getServer().getEntityWorld().loadedEntityList) {
            if (!(o instanceof EntityZombieDayZ)) continue;
            EntityZombieDayZ entityZombieDayZ = (EntityZombieDayZ)o;
            ZombieGroup zombieGroup = entityZombieDayZ.zombieGroup;
            if (zombieGroup.airdropUniqueId == -1L || zombieGroup.airdropUniqueId != airdrop.uniqueID) continue;
            entityZombieDayZ.x();
        }
    }

    public static void sendUpdate(Airdrop.State state, boolean sendAllData, Airdrop airdrop, EntityPlayer entityPlayer) {
        VSP os = new VSP(50, "HCSMOD");
        try {
            os.writeByte(state.ordinal());
            os.writeLong(airdrop.uniqueID);
            os.writeBoolean(sendAllData);
            if (sendAllData) {
                os.writeUTF(airdrop.type.displayName);
                os.writeInt(airdrop.location.x);
                os.writeInt(airdrop.location.y);
                os.writeInt(airdrop.location.z);
                os.writeLong(airdrop.endTime);
                if (state == Airdrop.State.WAITING) {
                    os.writeLong(airdrop.runTime);
                }
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        if (entityPlayer == null) {
            os.sendAll();
        } else {
            os.send(entityPlayer);
        }
    }

    public static class ZombieSpawnData {
        String zombieGroup;
        int chunkRadius;
    }

    public static class AirdropLocation {
        public int x;
        public int y;
        public int z;
    }

    public static class AirdropSpawnData {
        float weight;
        String region;
    }

    public static class Config {
        public boolean enabled;
        int broadcastRadius;
        public HashMap<String, List<AirdropLocation>> regions;
        HashMap<String, SpawnType> spawnTypes;
        HashMap<String, AirdropType> airdropTypes;

        public static class SpawnType {
            int maxCount;
            int[] timeInterval;
            int timeToRun;
            HashMap<String, AirdropSpawnData> spawnList;
        }

        public static class AirdropType {
            String displayName;
            int timeToEventEndSeconds;
            ArrayList<ZombieSpawnData> addZombieGroups;
            HashSet<String> checkGroups;
            int goal;
            String lootGroup;
            String activeZombieLootGroup;
        }
    }

    public static class Spawn {
        public String typeName;
        public Config.SpawnType type;
        public float typesAllWeight;
        public long nextAirdropSpawn;
        public int count;

        public Spawn(String typeName, Config.SpawnType type) {
            this.typeName = typeName;
            this.type = type;
        }
    }

    public static class Airdrop {
        String typeName;
        long uniqueID = System.nanoTime();
        public State state;
        public Config.AirdropType type;
        public AirdropLocation location;
        Spawn spawn;
        int kills;
        long runTime;
        long endTime;

        public Airdrop(String typeName, Spawn spawn, AirdropLocation location) {
            this.typeName = typeName;
            this.location = location;
            this.spawn = spawn;
            this.type = AirdropSystem.config.airdropTypes.get(typeName);
            this.endTime = System.currentTimeMillis() + (long)this.type.timeToEventEndSeconds * 1000L + (long)this.spawn.type.timeToRun * 1000L;
            if (spawn.type.timeToRun > 0) {
                this.state = State.WAITING;
                this.runTime = System.currentTimeMillis() + (long)this.spawn.type.timeToRun * 1000L;
            } else {
                this.state = State.RUNNING;
            }
            this.kills = 0;
        }

        public static enum State {
            WAITING,
            RUNNING,
            LOOT,
            FINISHED;

        }
    }
}

