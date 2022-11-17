/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.common.ITickHandler
 *  cpw.mods.fml.common.TickType
 *  cpw.mods.fml.common.event.FMLServerAboutToStartEvent
 *  cpw.mods.fml.common.event.FMLServerStoppingEvent
 *  cpw.mods.fml.common.registry.TickRegistry
 *  cpw.mods.fml.relauncher.Side
 *  org.apache.commons.dbcp2.BasicDataSource
 */
package vintarz.core.server;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.commons.dbcp2.BasicDataSource;

public class VCoreServer
implements ITickHandler {
    public static ExecutorService asyncExecutor;
    private static Map<String, ExecutorService> executorServices;
    public static final Queue<Runnable> syncQueue;

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static Properties initDatabaseConnectionPool(BasicDataSource ds, File config) {
        try (FileInputStream in = new FileInputStream(config);){
            Properties props = new Properties();
            props.load(in);
            ds.setDriverClassName(props.getProperty("driverClass"));
            ds.setUrl(props.getProperty("connectionString"));
            ds.setUsername(props.getProperty("username"));
            ds.setPassword(props.getProperty("password"));
            ds.setTimeBetweenEvictionRunsMillis(5000L);
            ds.setMinIdle(2);
            ds.setMaxIdle(Runtime.getRuntime().availableProcessors());
            Properties properties = props;
            return properties;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ExecutorService executorService(String name, Supplier<ExecutorService> init) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name must not be empty");
        }
        return executorServices.computeIfAbsent(name, s -> (ExecutorService)init.get());
    }

    public static void serverStart(FMLServerAboutToStartEvent event) {
        asyncExecutor = Executors.newWorkStealingPool();
        executorServices = new HashMap<String, ExecutorService>();
        executorServices.put("", asyncExecutor);
    }

    public static void serverStop(FMLServerStoppingEvent event) {
        executorServices.values().forEach(ExecutorService::shutdown);
        executorServices.forEach((name, executor) -> {
            try {
                do {
                    System.out.println("Waiting for VCoreServer ExecutorService to finish: " + name);
                } while (!executor.awaitTermination(1L, TimeUnit.SECONDS));
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        asyncExecutor = null;
        executorServices = null;
        VCoreServer.processSyncTasks();
    }

    private static void processSyncTasks() {
        Runnable r;
        while ((r = syncQueue.poll()) != null) {
            r.run();
        }
    }

    public void tickStart(EnumSet<TickType> type, Object ... tickData) {
        VCoreServer.processSyncTasks();
    }

    public void tickEnd(EnumSet<TickType> type, Object ... tickData) {
    }

    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.SERVER);
    }

    public String getLabel() {
        return "VCoreServer TH";
    }

    static {
        syncQueue = new LinkedBlockingQueue<Runnable>();
        TickRegistry.registerTickHandler((ITickHandler)new VCoreServer(), (Side)Side.SERVER);
    }
}

