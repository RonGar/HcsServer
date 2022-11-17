/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.util.MathHelper
 *  net.minecraft.world.World
 */
package co.uk.flansmods.vintarz.server;

import co.uk.flansmods.vintarz.server.LagEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class LagSnapshot {
    private static final Deque<List<LagEntry>> listPool = new ArrayDeque<List<LagEntry>>();
    public final Map<Long, List<LagEntry>> entries = new HashMap<Long, List<LagEntry>>();
    public int tick;

    public void capture(int tick, World world) {
        this.tick = tick;
        this.reset(false);
        for (Object o : world.loadedEntityList) {
            if (!(o instanceof EntityLivingBase)) continue;
            LagEntry entry = new LagEntry();
            entry.update((EntityLivingBase)o);
            int minChunkX = MathHelper.floor_double((double)entry.aabb.minX) >> 4;
            int maxChunkX = MathHelper.floor_double((double)entry.aabb.maxX) >> 4;
            int minChunkZ = MathHelper.floor_double((double)entry.aabb.minZ) >> 4;
            int maxChunkZ = MathHelper.floor_double((double)entry.aabb.maxZ) >> 4;
            for (int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ) {
                    Long chunkKey = this.chunkKey(chunkX, chunkZ);
                    List<LagEntry> list = this.entries.get(chunkKey);
                    if (list == null) {
                        list = listPool.isEmpty() ? new ArrayList<LagEntry>() : listPool.removeLast();
                        this.entries.put(chunkKey, list);
                    }
                    list.add(entry);
                }
            }
        }
    }

    public void addEntries(List<LagEntry> to, double minX, double minZ, double maxX, double maxZ) {
        int minChunkX = MathHelper.floor_double((double)minX) >> 4;
        int maxChunkX = MathHelper.floor_double((double)maxX) >> 4;
        int minChunkZ = MathHelper.floor_double((double)minZ) >> 4;
        int maxChunkZ = MathHelper.floor_double((double)maxZ) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ) {
                List<LagEntry> list = this.entries.get(this.chunkKey(chunkX, chunkZ));
                if (list == null) continue;
                to.addAll(list);
            }
        }
    }

    public long chunkKey(int chunkX, int chunkZ) {
        return (long)chunkX & 0xFFFFFFFFL | ((long)chunkZ & 0xFFFFFFFFL) << 32;
    }

    public void reset(boolean force) {
        if (!force) {
            Collection<List<LagEntry>> lists = this.entries.values();
            lists.forEach(List::clear);
            listPool.addAll(lists);
        }
        this.entries.clear();
    }
}

