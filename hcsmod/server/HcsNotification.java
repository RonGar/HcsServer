/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.server.MinecraftServer
 */
package hcsmod.server;

import hcsmod.server.SPacketHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class HcsNotification {
    private static final long SEND_INTERVAL = 10000L;
    public static final List<HcsNotification> notifications = new ArrayList<HcsNotification>();
    private static long nextSecond;
    public final String id;
    public final String text;
    public final long timeout;
    private final String networkID;
    private long nextUpdate;

    public HcsNotification(String id, String text) {
        this(id, text, 0);
    }

    public HcsNotification(String id, String text, int timeout) {
        this.id = id;
        this.text = text.replace('#', '\u00a7');
        this.timeout = timeout > 0 ? System.currentTimeMillis() + (long)timeout * 1000L : 0L;
        this.networkID = "$" + this.id;
    }

    public void send() {
        for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (!(o instanceof EntityPlayerMP)) continue;
            this.send((EntityPlayerMP)o);
        }
    }

    public void send(EntityPlayerMP p) {
        SPacketHandler.sendHint((EntityPlayer)p, this.networkID, this.text, 255);
    }

    public void remove() {
        for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (!(o instanceof EntityPlayerMP)) continue;
            SPacketHandler.sendHint((EntityPlayer)((EntityPlayerMP)o), this.networkID, null, 0);
        }
    }

    public static void add(HcsNotification notification) {
        Iterator<HcsNotification> i = notifications.iterator();
        while (i.hasNext()) {
            HcsNotification n = i.next();
            if (!Objects.equals(notification.id, n.id)) continue;
            i.remove();
        }
        notifications.add(notification);
        notification.send();
    }

    public static void remove(String id) {
        Iterator<HcsNotification> i = notifications.iterator();
        while (i.hasNext()) {
            HcsNotification n = i.next();
            if (!Objects.equals(id, n.id)) continue;
            i.remove();
            n.remove();
        }
    }

    static void serverTick() {
        long time = System.currentTimeMillis();
        if (time >= nextSecond) {
            nextSecond = time + 1000L;
            Iterator<HcsNotification> i = notifications.iterator();
            while (i.hasNext()) {
                HcsNotification notification = i.next();
                if (notification.timeout != 0L && time > notification.timeout) {
                    notification.remove();
                    i.remove();
                    continue;
                }
                if (time <= notification.nextUpdate) continue;
                notification.nextUpdate = time + 10000L;
                notification.send();
            }
        }
    }

    static void playerLogin(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP)player;
            for (HcsNotification notification : notifications) {
                notification.send(p);
            }
        }
    }
}

