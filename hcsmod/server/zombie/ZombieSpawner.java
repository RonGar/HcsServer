/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.common.zombie.IndoorChunkInfo
 *  hcsmod.common.zombie.ZombieGroup
 *  hcsmod.entity.EntityZombieDayZ
 *  js
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.util.MathHelper
 *  net.minecraft.world.ChunkPosition
 *  net.minecraft.world.World
 *  net.minecraft.world.WorldServer
 *  net.minecraft.world.chunk.Chunk
 */
package hcsmod.server.zombie;

import hcsmod.common.zombie.IndoorChunkInfo;
import hcsmod.common.zombie.ZombieGroup;
import hcsmod.entity.EntityZombieDayZ;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

public final class ZombieSpawner {
    public static HashMap<ZombieGroup, HashMap<Integer, Long>> zombieSpawnCooldowns = new HashMap();
    private static final HashSet<Integer> outdoorChunksForSpawning = new HashSet();
    private static final HashMap<Integer, ArrayList<IndoorChunkInfo>> indoorChunksForSpawning = new HashMap();
    private static final HashSet<Integer> loadedToSpawnChunks = new HashSet();
    private static final Random rand = new Random();

    private static long getRandomTime(ZombieGroup zombieGroup) {
        int time = zombieGroup.spawnInChunkCooldownTick / 20;
        int fourth = time / 4;
        int r = rand.nextInt(1 + fourth * 2);
        return (long)(time - fourth + r) * 1000L;
    }

    private static boolean isChunkNotExist(int combined, WorldServer worldServer) {
        short x = (short)(combined >> 16);
        short z = (short)combined;
        return !worldServer.L().chunkExists((int)x, (int)z);
    }

    private static ChunkPosition getRandomSpawningPointInChunk(World par0World, int par1, int par2) {
        Chunk chunk = par0World.getChunkFromChunkCoords(par1, par2);
        int x = par1 * 16;
        int z = par2 * 16;
        int shiftX = par0World.rand.nextInt(16);
        int shiftZ = par0World.rand.nextInt(16);
        int y = chunk.getHeightValue(shiftX, shiftZ);
        return new ChunkPosition(x + shiftX, y, z + shiftZ);
    }

    public static void zombieSpawner(WorldServer worldServer, ZombieGroup zombieGroup) {
        if (worldServer.I) {
            return;
        }
        indoorChunksForSpawning.clear();
        outdoorChunksForSpawning.clear();
        loadedToSpawnChunks.clear();
        if (zombieGroup.outdoorChunks.size() <= 0 && zombieGroup.indoorChunks.size() <= 0 && zombieGroup.spawnBiomes.size() <= 0) {
            return;
        }
        HashMap<Integer, Long> chunksCooldown = zombieSpawnCooldowns.get((Object)zombieGroup);
        chunksCooldown.values().removeIf(aLong -> aLong <= System.currentTimeMillis());
        chunksCooldown.keySet().removeIf((Predicate<Integer>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Z, lambda$zombieSpawner$1(js java.lang.Integer ), (Ljava/lang/Integer;)Z)((js)worldServer));
        for (int i = 0; i < worldServer.h.size(); ++i) {
            EntityPlayer entityplayer = (EntityPlayer)worldServer.h.get(i);
            int chunkX = MathHelper.floor_double((double)(entityplayer.u / 16.0));
            int chunkZ = MathHelper.floor_double((double)(entityplayer.w / 16.0));
            int chunkRadius = 4;
            for (int shiftX = -chunkRadius; shiftX <= chunkRadius; ++shiftX) {
                for (int shiftZ = -chunkRadius; shiftZ <= chunkRadius; ++shiftZ) {
                    int shiftedX = shiftX + chunkX;
                    int shiftedZ = shiftZ + chunkZ;
                    int combinedXZ = shiftedX << 16 | shiftedZ & 0xFFFF;
                    if (ZombieSpawner.isChunkNotExist(combinedXZ, worldServer)) continue;
                    loadedToSpawnChunks.add(combinedXZ);
                    if (chunksCooldown.get(combinedXZ) != null || indoorChunksForSpawning.containsKey(combinedXZ) || outdoorChunksForSpawning.contains(combinedXZ)) continue;
                    Chunk chunk = worldServer.e(shiftedX, shiftedZ);
                    boolean hasBiome = false;
                    Iterator iterator = zombieGroup.spawnBiomes.iterator();
                    block3: while (iterator.hasNext()) {
                        byte id = (Byte)iterator.next();
                        for (byte j : chunk.getBiomeArray()) {
                            if (id != j) continue;
                            hasBiome = true;
                            continue block3;
                        }
                    }
                    if (zombieGroup.indoorChunks != null && zombieGroup.indoorChunks.containsKey(combinedXZ)) {
                        indoorChunksForSpawning.put(combinedXZ, (ArrayList<IndoorChunkInfo>)zombieGroup.indoorChunks.get(combinedXZ));
                    }
                    if (hasBiome) {
                        outdoorChunksForSpawning.add(combinedXZ);
                        continue;
                    }
                    if (zombieGroup.outdoorChunks == null || !zombieGroup.outdoorChunks.contains(combinedXZ)) continue;
                    outdoorChunksForSpawning.add(combinedXZ);
                }
            }
        }
        ZombieSpawner.spawnIndoorZombie(worldServer, zombieGroup);
        ZombieSpawner.spawnOutdoorZombie(worldServer, zombieGroup);
    }

    private static void spawnIndoorZombie(WorldServer worldServer, ZombieGroup zombieGroup) {
        ArrayList<Integer> tmp = new ArrayList<Integer>(indoorChunksForSpawning.keySet());
        Collections.shuffle(tmp);
        Iterator<Integer> iterator = tmp.iterator();
        while (iterator.hasNext()) {
            int combined = iterator.next();
            short chunkX = (short)(combined >> 16);
            short chunkZ = (short)combined;
            ArrayList<IndoorChunkInfo> indoorChunkPlaces = indoorChunksForSpawning.get(chunkX << 16 | chunkZ & 0xFFFF);
            Collections.shuffle(indoorChunkPlaces);
            block1: for (IndoorChunkInfo chunkInfo : indoorChunkPlaces) {
                if (ZombieSpawner.spawnLimitExceeded(worldServer, zombieGroup, combined)) break;
                for (int attempt = 0; attempt < 8; ++attempt) {
                    float sz;
                    float sy;
                    float sx;
                    int shiftZ;
                    int z1;
                    boolean isValidPlaceForCustomSpawn;
                    int x = chunkX * 16;
                    int z = chunkZ * 16;
                    int shiftX = worldServer.s.nextInt(Math.max(1, chunkInfo.x2 - chunkInfo.x1)) + chunkInfo.x1;
                    int x1 = x + shiftX;
                    boolean bl = isValidPlaceForCustomSpawn = !worldServer.u(x1, chunkInfo.y, z1 = z + (shiftZ = worldServer.s.nextInt(Math.max(1, chunkInfo.z2 - chunkInfo.z1)) + chunkInfo.z1)) && !worldServer.u(x1, chunkInfo.y + 1, z1);
                    if (!isValidPlaceForCustomSpawn || worldServer.a((double)(sx = (float)x1 + 0.5f), (double)(sy = (float)chunkInfo.y), (double)(sz = (float)z1 + 0.5f), (double)zombieGroup.disableSpawnAroundPlayerRadius) != null) continue;
                    EntityZombieDayZ entityliving = new EntityZombieDayZ((World)worldServer, zombieGroup);
                    entityliving.b((double)sx, (double)sy, (double)sz, worldServer.s.nextFloat() * 360.0f, 0.0f);
                    worldServer.d((Entity)entityliving);
                    continue block1;
                }
            }
            iterator.remove();
        }
    }

    private static void spawnOutdoorZombie(WorldServer worldServer, ZombieGroup zombieGroup) {
        ArrayList<Integer> tmp = new ArrayList<Integer>(outdoorChunksForSpawning);
        Collections.shuffle(tmp);
        Iterator<Integer> iterator = tmp.iterator();
        while (iterator.hasNext()) {
            int combined = iterator.next();
            short chunkX = (short)(combined >> 16);
            short chunkZ = (short)combined;
            if (ZombieSpawner.spawnLimitExceeded(worldServer, zombieGroup, combined)) continue;
            for (int attempt = 0; attempt < 8; ++attempt) {
                float sz;
                float sy;
                float sx;
                boolean isValidPlaceForCustomSpawn;
                ChunkPosition chunkposition = ZombieSpawner.getRandomSpawningPointInChunk((World)worldServer, chunkX, chunkZ);
                int x1 = chunkposition.x;
                int y1 = chunkposition.y;
                int z1 = chunkposition.z;
                boolean bl = isValidPlaceForCustomSpawn = !worldServer.u(x1, y1, z1) && !worldServer.g(x1, y1, z1).isLiquid() && !worldServer.u(x1, y1 + 1, z1);
                if (!isValidPlaceForCustomSpawn || worldServer.a((double)(sx = (float)x1 + 0.5f), (double)(sy = (float)y1), (double)(sz = (float)z1 + 0.5f), (double)zombieGroup.disableSpawnAroundPlayerRadius) != null) continue;
                int x = MathHelper.floor_double((double)sx);
                int z = MathHelper.floor_double((double)sz);
                int y = MathHelper.floor_double((double)sy);
                boolean flag = false;
                for (int m = -2; m < 0; ++m) {
                    if (worldServer.a(x, y + m, z) == 35 && (worldServer.h(x, y + m - 1, z) == 3 || worldServer.h(x, y + m - 1, z) == 13 || worldServer.h(x, y + m - 1, z) == 1) || worldServer.a(x, y + m, z) == 1 && (worldServer.h(x, y + m - 1, z) == 3 || worldServer.h(x, y + m - 1, z) == 13)) {
                        flag = true;
                        break;
                    }
                    if (worldServer.a(x, y + m, z) == 2) {
                        flag = true;
                        break;
                    }
                    if (worldServer.a(x, y + m, z) == 79 && worldServer.a(x, y + m + 1, z) == 0 && worldServer.a(x, y + m + 2, z) == 0) {
                        flag = true;
                        break;
                    }
                    if (worldServer.a(x, y + m, z) == 80 && worldServer.a(x, y + m + 1, z) == 0 && worldServer.a(x, y + m + 2, z) == 0) {
                        flag = true;
                        break;
                    }
                    if (worldServer.a(x, y + m, z) != 1 || worldServer.a(x, y + m + 1, z) != 0 || worldServer.a(x, y + m + 2, z) != 0 || worldServer.a(x, y + m - 1, z) != 3) continue;
                    flag = true;
                    break;
                }
                if (!flag) continue;
                EntityZombieDayZ entityliving = new EntityZombieDayZ((World)worldServer, zombieGroup);
                entityliving.b((double)sx, (double)sy, (double)sz, worldServer.s.nextFloat() * 360.0f, 0.0f);
                worldServer.d((Entity)entityliving);
                break;
            }
            iterator.remove();
        }
    }

    private static int getMaxZombieCount(int legalChunks, int countCheckRadius, int maxCount) {
        int chunksInRadius = (int)(Math.floor(Math.PI * (double)(countCheckRadius * countCheckRadius)) + 1.0);
        return (int)Math.ceil(Math.min((float)maxCount * ((float)legalChunks / (float)chunksInRadius), (float)maxCount));
    }

    private static boolean spawnLimitExceeded(WorldServer worldServer, ZombieGroup zombieGroup, int combined) {
        short chunkX = (short)(combined >> 16);
        short chunkZ = (short)combined;
        int nearZombieCount = 0;
        int legalChunksCount = 0;
        for (int shiftX = -zombieGroup.countCheckRadius; shiftX <= zombieGroup.countCheckRadius; ++shiftX) {
            for (int shiftZ = -zombieGroup.countCheckRadius; shiftZ <= zombieGroup.countCheckRadius; ++shiftZ) {
                int shiftedX = shiftX + chunkX;
                int shiftedZ = shiftZ + chunkZ;
                int chunkCheckCombined = shiftedX << 16 | shiftedZ & 0xFFFF;
                Chunk chunk = worldServer.e(shiftedX, shiftedZ);
                for (int k = 0; k < chunk.entityLists.length; ++k) {
                    for (Object o : chunk.entityLists[k]) {
                        Entity entity = (Entity)o;
                        if (!(entity instanceof EntityZombieDayZ)) continue;
                        EntityZombieDayZ ezd = (EntityZombieDayZ)entity;
                        if (ezd.zombieGroup != zombieGroup) continue;
                        ++nearZombieCount;
                    }
                }
                if ((zombieGroup.outdoorChunks.contains(chunkCheckCombined) || zombieGroup.indoorChunks.containsKey(chunkCheckCombined)) && loadedToSpawnChunks.contains(chunkCheckCombined)) {
                    ++legalChunksCount;
                    continue;
                }
                Iterator iterator = zombieGroup.spawnBiomes.iterator();
                block4: while (iterator.hasNext()) {
                    byte id = (Byte)iterator.next();
                    for (byte j : chunk.getBiomeArray()) {
                        if (id != j) continue;
                        ++legalChunksCount;
                        continue block4;
                    }
                }
            }
        }
        if (nearZombieCount >= ZombieSpawner.getMaxZombieCount(legalChunksCount, zombieGroup.countCheckRadius, zombieGroup.maxCount)) {
            zombieSpawnCooldowns.get((Object)zombieGroup).put(combined, System.currentTimeMillis() + ZombieSpawner.getRandomTime(zombieGroup));
            return true;
        }
        return false;
    }

    private static /* synthetic */ boolean lambda$zombieSpawner$1(WorldServer worldServer, Integer coords) {
        return ZombieSpawner.isChunkNotExist(coords, worldServer);
    }
}

