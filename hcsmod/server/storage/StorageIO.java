/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompressedStreamTools
 *  net.minecraft.nbt.NBTTagCompound
 */
package hcsmod.server.storage;

import hcsmod.server.storage.InvLoadCallback;
import hcsmod.server.storage.StorageGroup;
import hcsmod.server.storage.StorageInventory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import vintarz.core.server.VCoreServer;

public class StorageIO {
    private final File dataFolder;
    private final Map<String, StorageInventory> loadedInventories = new HashMap<String, StorageInventory>();

    public StorageIO(File dataFolder) {
        this.dataFolder = dataFolder;
        boolean rez = dataFolder.mkdir();
        if (rez) {
            System.out.println("storage \u0441\u043e\u0437\u0434\u0430\u043d");
        } else {
            System.out.println("storage \u043d\u0435 \u0441\u043e\u0437\u0434\u0430\u043d");
        }
    }

    public void loadInventory(final String playerName, StorageGroup storageGroup, final boolean create, final InvLoadCallback callback) {
        StorageInventory existing = this.loadedInventories.get(playerName);
        if (existing != null) {
            if (existing.loadCallbacks.isEmpty()) {
                callback.loadingDone(existing);
                if (!existing.isActive()) {
                    this.unload(existing);
                }
            } else {
                existing.loadCallbacks.add(callback);
            }
            return;
        }
        final StorageInventory inv = new StorageInventory(playerName, storageGroup);
        this.loadedInventories.put(playerName, inv);
        inv.loadCallbacks.add(callback);
        StorageIO.ioExecutor().submit(new Runnable(){

            @Override
            public void run() {
                NBTTagCompound tag;
                File file = StorageIO.this.playerFile(playerName);
                if (!create && !file.isFile()) {
                    VCoreServer.syncQueue.offer(new Runnable(){

                        @Override
                        public void run() {
                            callback.loadingFailed("\u041d\u0435\u0442 \u0442\u0430\u043a\u043e\u0433\u043e \u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0430!");
                        }
                    });
                    return;
                }
                try {
                    tag = CompressedStreamTools.read((File)file);
                }
                catch (IOException e) {
                    tag = null;
                }
                if (tag == null) {
                    tag = new NBTTagCompound();
                }
                inv.load(tag);
                VCoreServer.syncQueue.offer(new Runnable(){

                    @Override
                    public void run() {
                        for (InvLoadCallback r : inv.loadCallbacks) {
                            r.loadingDone(inv);
                        }
                        inv.loadCallbacks.clear();
                        if (!inv.isActive()) {
                            StorageIO.this.unload(inv);
                        }
                    }
                });
            }
        });
    }

    public void unload(final StorageInventory inv) {
        final NBTTagCompound tag = new NBTTagCompound();
        inv.save(tag);
        Runnable r = new Runnable(){

            @Override
            public void run() {
                File file = StorageIO.this.playerFile(inv.playerName);
                try {
                    StorageIO.safeWrite(tag, file);
                }
                catch (Throwable e) {
                    VCoreServer.syncQueue.offer(new Runnable(){

                        @Override
                        public void run() {
                            throw new RuntimeException("Failed to save custom storage data", e);
                        }
                    });
                }
                VCoreServer.syncQueue.offer(new Runnable(){

                    @Override
                    public void run() {
                        if (!inv.isActive()) {
                            StorageIO.this.loadedInventories.remove(inv.playerName);
                        }
                    }
                });
            }
        };
        try {
            StorageIO.ioExecutor().submit(r);
        }
        catch (RejectedExecutionException e) {
            r.run();
        }
    }

    public void shutdown() {
        StorageInventory[] invs;
        for (StorageInventory inv : invs = this.loadedInventories.values().toArray(new StorageInventory[0])) {
            inv.forceClose();
            this.unload(inv);
        }
    }

    public void tick() {
        this.loadedInventories.values().forEach(inv -> {
            if (!inv.isActive()) {
                this.unload((StorageInventory)inv);
            }
        });
    }

    private File playerFile(String playerName) {
        return new File(this.dataFolder, playerName + ".dat");
    }

    public static void safeWrite(NBTTagCompound par0NBTTagCompound, File par1File) throws IOException {
        File file2 = new File(par1File.getAbsolutePath() + "_tmp");
        if (file2.exists()) {
            file2.delete();
        }
        CompressedStreamTools.write((NBTTagCompound)par0NBTTagCompound, (File)file2);
        if (par1File.exists()) {
            par1File.delete();
        }
        if (par1File.exists()) {
            throw new IOException("Failed to delete " + par1File);
        }
        file2.renameTo(par1File);
    }

    private static ExecutorService ioExecutor() {
        return VCoreServer.executorService("StorageIO", Executors::newSingleThreadExecutor);
    }
}

