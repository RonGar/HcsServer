/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.nbt.NBTTagCompound
 */
package hcsmod.server;

import hcsmod.server.SPacketHandler;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class PlayGift {
    private static final String INTERNAL_PREFIX = "PlayGift-";
    private static final String CONFIG_FILE = "play-gift.properties";
    private static String SAVE_NAME = "";
    private static int CLAIM_TIME = 900;
    private static String GIFT_MESSAGE = "\u0418\u0433\u0440\u0430\u0439 \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435 \u0438 \u0447\u0435\u0440\u0435\u0437 15 \u043c\u0438\u043d\u0443\u0442 \u043f\u043e\u043b\u0443\u0447\u0438\u0448\u044c \u043f\u043e\u0434\u0430\u0440\u043e\u043a!";
    private static String CLAIM_MESSAGE = "\u0417\u0430\u0431\u0435\u0440\u0438 \u043f\u043e\u0434\u0430\u0440\u043e\u043a: /kit 2021";
    private static double AFK_MOVE = 2.0;
    private static int AFK_SECONDS = 5;
    private static String AFK_WARNING = "\u041f\u043e\u043a\u0430 \u0441\u0442\u043e\u0438\u0448\u044c \u0410\u0424\u041a, \u0432\u0440\u0435\u043c\u044f \u0438\u0433\u0440\u044b \u043d\u0435 \u0443\u0447\u0438\u0442\u044b\u0432\u0430\u0435\u0442\u0441\u044f!";
    private int time;
    private boolean claimed;
    private double posX;
    private double posZ;
    private long lastMove;
    private boolean afk;
    private long nextUpdate;

    public static boolean reloadConfig() {
        Properties properties = PlayGift.readConfig();
        if (properties == null) {
            return false;
        }
        PlayGift.loadConfig(properties);
        return true;
    }

    public static boolean setConfigProperty(String key, String value) {
        Properties properties = PlayGift.readConfig();
        if (properties == null) {
            return false;
        }
        if ("-".equals(value)) {
            value = "";
        }
        properties.setProperty(key, value);
        PlayGift.loadConfig(properties);
        try (OutputStreamWriter w = new OutputStreamWriter((OutputStream)new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8);){
            properties.store(w, "");
        }
        catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean active() {
        return !SAVE_NAME.isEmpty();
    }

    private static Properties readConfig() {
        Properties props = new Properties();
        try (InputStreamReader is = new InputStreamReader((InputStream)new FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8);){
            props.load(is);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return props;
    }

    private static void loadConfig(Properties props) {
        SAVE_NAME = props.getProperty("save-name", SAVE_NAME);
        CLAIM_TIME = Integer.parseUnsignedInt(props.getProperty("claim-in-seconds", String.valueOf(CLAIM_TIME)));
        GIFT_MESSAGE = props.getProperty("gift-message", GIFT_MESSAGE);
        CLAIM_MESSAGE = props.getProperty("claim-message", CLAIM_MESSAGE);
        AFK_MOVE = Double.parseDouble(props.getProperty("afk-move-blocks", String.valueOf(AFK_MOVE)));
        AFK_SECONDS = Integer.parseUnsignedInt(props.getProperty("afk-seconds", String.valueOf(AFK_SECONDS)));
        AFK_WARNING = props.getProperty("afk-warning", AFK_WARNING);
    }

    public void load(NBTTagCompound playerDat) {
        if (!PlayGift.active()) {
            return;
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("claimed", this.claimed);
        tag.setInteger("time", this.time);
        tag.setDouble("X", this.posX);
        tag.setDouble("Z", this.posZ);
        tag.setLong("lastMove", this.lastMove);
        tag.setBoolean("afk", this.afk);
        playerDat.setCompoundTag(INTERNAL_PREFIX + SAVE_NAME, tag);
    }

    public void save(NBTTagCompound playerDat) {
        if (!PlayGift.active()) {
            return;
        }
        NBTTagCompound tag = playerDat.getCompoundTag(INTERNAL_PREFIX + SAVE_NAME);
        this.claimed = tag.getBoolean("claimed");
        this.time = tag.getInteger("time");
        this.posX = tag.getDouble("X");
        this.posZ = tag.getDouble("Z");
        this.lastMove = tag.getLong("lastMove");
        this.afk = tag.getBoolean("afk");
    }

    public void tick(EntityPlayer player) {
        if (!PlayGift.active()) {
            this.time = 0;
            this.claimed = false;
            return;
        }
        if (this.claimed) {
            return;
        }
        boolean moved = false;
        if (Math.abs(player.u - this.posX) > AFK_MOVE) {
            this.posX = player.u;
            moved = true;
        }
        if (Math.abs(player.w - this.posZ) > AFK_MOVE) {
            this.posZ = player.w;
            moved = true;
        }
        long now = System.currentTimeMillis();
        if (moved) {
            this.lastMove = now;
            if (this.afk) {
                this.afk = false;
                SPacketHandler.sendHint(player, "PlayGift-AFK", "", 0);
            }
        }
        if (!this.afk && this.lastMove < now - (long)AFK_SECONDS * 1000L) {
            this.afk = true;
            this.time -= AFK_SECONDS;
        }
        if (this.nextUpdate < now - (long)AFK_SECONDS * 1000L) {
            this.nextUpdate = now;
        }
        if (now > this.nextUpdate) {
            this.nextUpdate += 1000L;
            if (!this.canClaim()) {
                SPacketHandler.sendHint(player, "PlayGift-MSG", GIFT_MESSAGE + "\n\u041c\u0438\u043d\u0443\u0442 \u043e\u0442\u044b\u0433\u0440\u0430\u043d\u043e: " + this.time / 60, 127);
                if (!this.afk) {
                    ++this.time;
                } else {
                    SPacketHandler.sendHint(player, "PlayGift-AFK", AFK_WARNING, 127);
                }
            } else {
                SPacketHandler.sendHint(player, "PlayGift-MSG", CLAIM_MESSAGE, 127);
                SPacketHandler.sendHint(player, "PlayGift-AFK", "", 0);
            }
        }
    }

    private boolean canClaim() {
        return !this.claimed && this.time >= CLAIM_TIME;
    }

    public boolean claim(EntityPlayer player) {
        if (!PlayGift.active() || !this.canClaim()) {
            return false;
        }
        this.claimed = true;
        SPacketHandler.sendHint(player, "PlayGift-MSG", "", 0);
        SPacketHandler.sendHint(player, "PlayGift-AFK", "", 0);
        return true;
    }
}

