/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.reflect.TypeToken
 *  cpw.mods.fml.relauncher.FMLLaunchHandler
 *  jv
 *  net.minecraft.command.IEntitySelector
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.network.TcpConnection
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.Packet62LevelSound
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.ChatMessageComponent
 *  net.minecraft.util.Vec3
 */
package hcsmod.server.vote;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import hcsmod.server.SPacketHandler;
import hcsmod.server.vote.VoteConfig;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.LambdaMetafactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.TcpConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet62LevelSound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Vec3;
import vintarz.core.server.VCoreServer;

public class VoteGift {
    private static Random rnd = new SecureRandom();
    private static final String INTERNAL_PREFIX = "VoteGift-";
    private static final String HINT_ID = "VoteGift";
    private static final String CONFIG_FILE = "voteGift";
    private static VoteConfig config = new VoteConfig();
    private final VoteBalance balance = new VoteBalance();
    private boolean requestingBalance;
    private boolean requestingReward;
    private long nextTimeUpdate = System.currentTimeMillis();
    private long nextBalanceUpdate = System.currentTimeMillis();
    private long nextClaimRequest = 0L;
    private long tokenIdempotent = -1L;
    private int time;
    private double posX;
    private double posZ;
    private AxisAlignedBB[] movePath = new AxisAlignedBB[0];
    private long lastMove;
    private boolean afk;

    public static boolean active() {
        return !VoteGift.config.saveName.isEmpty();
    }

    public static void readConfig() {
        Gson gson = new Gson();
        try (InputStreamReader in = new InputStreamReader((InputStream)new FileInputStream("databases/voteGift.json"), StandardCharsets.UTF_8);){
            config = (VoteConfig)gson.fromJson((Reader)in, VoteConfig.class);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(NBTTagCompound playerDat) {
        if (!VoteGift.active()) {
            return;
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("time", this.time);
        tag.setLong("nextClaimRequest", this.nextClaimRequest);
        tag.setLong("tokenIndependent", this.tokenIdempotent);
        playerDat.setCompoundTag(INTERNAL_PREFIX + VoteGift.config.saveName, tag);
    }

    public void save(NBTTagCompound playerDat) {
        if (!VoteGift.active()) {
            return;
        }
        NBTTagCompound tag = playerDat.getCompoundTag(INTERNAL_PREFIX + VoteGift.config.saveName);
        this.time = tag.getInteger("time");
        this.nextClaimRequest = tag.getLong("nextClaimRequest");
        this.tokenIdempotent = tag.getLong("tokenIndependent");
    }

    public void tick(EntityPlayerMP player) {
        if (!VoteGift.active()) {
            this.time = 0;
            return;
        }
        long now = System.currentTimeMillis();
        if (!this.requestingBalance && now > this.nextBalanceUpdate) {
            this.requestingBalance = true;
            this.nextBalanceUpdate = now + 10000L;
            VCoreServer.asyncExecutor.submit((Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$tick$0(jv ), ()V)((VoteGift)this, (jv)player));
        }
        if (this.balance.available == 0) {
            return;
        }
        boolean moved = false;
        if (Math.abs(player.u - this.posX) > VoteGift.config.afkMove || Math.abs(player.w - this.posZ) > VoteGift.config.afkMove) {
            this.posX = player.u;
            this.posZ = player.w;
            if (this.movePath.length != VoteGift.config.afkPath) {
                this.movePath = Arrays.copyOf(this.movePath, VoteGift.config.afkPath);
            }
            if (!this.inMovePath(player.q.getWorldVec3Pool().getVecFromPool(player.u, player.v, player.w)) || this.hasPlayersNearby(player)) {
                moved = true;
            }
            if (this.movePath.length > 0) {
                AxisAlignedBB last = this.movePath[this.movePath.length - 1];
                System.arraycopy(this.movePath, 0, this.movePath, 1, this.movePath.length - 1);
                if (last == null) {
                    last = AxisAlignedBB.getBoundingBox((double)0.0, (double)0.0, (double)0.0, (double)0.0, (double)0.0, (double)0.0);
                }
                this.movePath[0] = last;
                last.minX = player.u - VoteGift.config.afkMove;
                last.minY = player.v - VoteGift.config.afkMove;
                last.minZ = player.w - VoteGift.config.afkMove;
                last.maxX = player.u + VoteGift.config.afkMove;
                last.maxY = player.v + VoteGift.config.afkMove;
                last.maxZ = player.w + VoteGift.config.afkMove;
            }
        }
        if (moved) {
            this.lastMove = now;
            if (this.afk) {
                this.afk = false;
            }
        }
        if (this.nextTimeUpdate < now - (long)VoteGift.config.afkSeconds * 1000L) {
            this.nextTimeUpdate = now;
        }
        if (now > this.nextTimeUpdate) {
            this.nextTimeUpdate += 1000L;
            if (!this.afk && this.lastMove < now - (long)VoteGift.config.afkSeconds * 1000L && !this.hasPlayersNearby(player)) {
                this.afk = true;
                this.time -= VoteGift.config.afkSeconds;
            }
            String inZoneMessage = null;
            for (VoteConfig.Zone zone : VoteGift.config.disableZones) {
                if (!(player.u >= zone.minX) || !(player.v >= zone.minY) || !(player.w >= zone.minZ) || !(player.u <= zone.maxX) || !(player.v <= zone.maxY) || !(player.w <= zone.maxZ)) continue;
                inZoneMessage = zone.inZoneWarning;
                break;
            }
            if (player.playerNetServerHandler.netManager instanceof TcpConnection) {
                if (inZoneMessage != null) {
                    this.sendHint(player, inZoneMessage);
                } else if (!this.afk) {
                    ++this.time;
                    this.sendHint(player, "");
                } else {
                    this.sendHint(player, "\u0412 \u0410\u0424\u041a \u0432\u0440\u0435\u043c\u044f \u043d\u0435 \u0443\u0447\u0438\u0442\u044b\u0432\u0430\u0435\u0442\u0441\u044f!");
                }
            }
            if (this.time >= VoteGift.config.claimTime && now > this.nextClaimRequest && !this.requestingReward) {
                this.requestingReward = true;
                if (this.tokenIdempotent == -1L) {
                    this.genIdempotentToken();
                }
                VCoreServer.asyncExecutor.submit((Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$tick$1(jv ), ()V)((VoteGift)this, (jv)player));
            }
        }
    }

    private boolean hasPlayersNearby(EntityPlayerMP player) {
        List playersNearby = player.q.getEntitiesWithinAABBExcludingEntity((Entity)player, player.E.expand(16.0, 4.0, 16.0), new IEntitySelector(){

            public boolean a(Entity entity) {
                return entity instanceof EntityPlayerMP;
            }
        });
        return !playersNearby.isEmpty();
    }

    private boolean inMovePath(Vec3 location) {
        for (AxisAlignedBB pathBox : this.movePath) {
            if (pathBox == null || !pathBox.isVecInside(location)) continue;
            return true;
        }
        return false;
    }

    private void genIdempotentToken() {
        this.tokenIdempotent = rnd.nextLong();
    }

    private void sendHint(EntityPlayerMP entityPlayer, String extra) {
        String tmp = Math.max(0, (int)Math.ceil((float)(VoteGift.config.claimTime - this.time) / 60.0f)) + "\u043c\u0438\u043d. \u0434\u043e \u043d\u0430\u0433\u0440\u0430\u0434\u044b \u0437\u0430 \u0433\u043e\u043b\u043e\u0441\u043e\u0432\u0430\u043d\u0438\u0435 [F5]";
        if (!extra.isEmpty()) {
            tmp = tmp + "\n" + extra;
        }
        SPacketHandler.sendHint((EntityPlayer)entityPlayer, HINT_ID, tmp, 127);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private /* synthetic */ void lambda$tick$1(EntityPlayerMP player) {
        boolean ipRestricted = false;
        boolean success = false;
        int payOut = 0;
        int available = 0;
        int bonus = 0;
        Gson gson = new Gson();
        byte[] reqBody = new Gson().toJson((Object)new RewardRequest(player, this.tokenIdempotent)).getBytes(StandardCharsets.UTF_8);
        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(VoteGift.config.backend + "/reward").openConnection();
            try {
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.write(reqBody);
                out.flush();
                out.close();
                conn.connect();
                int resCode = conn.getResponseCode();
                if (resCode == 403) {
                    ipRestricted = true;
                    success = true;
                } else if (resCode == 200) {
                    Map res = (Map)gson.fromJson((Reader)new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8), new TypeToken<HashMap<String, Integer>>(){}.getType());
                    payOut = (Integer)res.get("payOut");
                    available = (Integer)res.get("available");
                    bonus = (Integer)res.get("bonus");
                    success = true;
                }
            }
            finally {
                conn.disconnect();
            }
            boolean finSuccess = success;
            int finalPayOut = payOut;
            int finalAvailable = available;
            int finalBonus = bonus;
            boolean finalIpRestricted = ipRestricted;
            VCoreServer.syncQueue.add(new Runnable(finSuccess, finalIpRestricted, player, finalPayOut, finalAvailable, finalBonus){
                final /* synthetic */ boolean val$finSuccess;
                final /* synthetic */ boolean val$finalIpRestricted;
                final /* synthetic */ EntityPlayerMP val$player;
                final /* synthetic */ int val$finalPayOut;
                final /* synthetic */ int val$finalAvailable;
                final /* synthetic */ int val$finalBonus;
                {
                    this.val$finSuccess = bl;
                    this.val$finalIpRestricted = bl2;
                    this.val$player = entityPlayerMP;
                    this.val$finalPayOut = n;
                    this.val$finalAvailable = n2;
                    this.val$finalBonus = n3;
                }

                @Override
                public void run() {
                    if (this.val$finSuccess) {
                        if (this.val$finalIpRestricted) {
                            this.val$player.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u00a7c\u041f\u043e\u043b\u0443\u0447\u0435\u043d\u0438\u0435 \u043d\u0430\u0433\u0440\u0430\u0434\u044b \u0441 IP, \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u043e\u0433\u043e \u0434\u043b\u044f \u043d\u0430\u043a\u0440\u0443\u0442\u043a\u0438 \u0438\u043b\u0438 \u0447\u0438\u0442\u0435\u0440\u043e\u043c, \u0437\u0430\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043d\u043e \u043d\u0430 \u0441\u0443\u0442\u043a\u0438."));
                            Packet62LevelSound pt = new Packet62LevelSound("random.classic_hurt", this.val$player.u, this.val$player.v, this.val$player.w, 1.0f, 1.0f);
                            this.val$player.playerNetServerHandler.sendPacketToPlayer((Packet)pt);
                        } else {
                            this.val$player.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u00a73\u041d\u0430\u0433\u0440\u0430\u0434\u0430 \u0437\u0430 \u0433\u043e\u043b\u043e\u0441\u043e\u0432\u0430\u043d\u0438\u0435: \u041f\u043e\u043b\u0443\u0447\u0435\u043d\u043e \u043c\u043e\u043d\u0435\u0442: " + this.val$finalPayOut)));
                            Packet62LevelSound pt = new Packet62LevelSound("random.levelup", this.val$player.u, this.val$player.v, this.val$player.w, 1.0f, 1.0f);
                            this.val$player.playerNetServerHandler.sendPacketToPlayer((Packet)pt);
                        }
                        VoteGift.this.time = VoteGift.this.time - config.claimTime;
                        if (VoteGift.this.time < 0) {
                            VoteGift.this.time = 0;
                        }
                        ((VoteGift)VoteGift.this).balance.available = this.val$finalAvailable;
                        ((VoteGift)VoteGift.this).balance.bonus = this.val$finalBonus;
                        SPacketHandler.sendVoteData((EntityPlayer)this.val$player, ((VoteGift)VoteGift.this).balance.available, ((VoteGift)VoteGift.this).balance.bonus);
                    }
                    VoteGift.this.requestingReward = false;
                    VoteGift.this.nextClaimRequest = System.currentTimeMillis() + 3000L;
                    VoteGift.this.genIdempotentToken();
                }
            });
        }
        catch (Exception e) {
            try {
                e.printStackTrace();
                boolean finSuccess = success;
                int finalPayOut = payOut;
                int finalAvailable = available;
                int finalBonus = bonus;
                boolean finalIpRestricted = ipRestricted;
                VCoreServer.syncQueue.add(new /* invalid duplicate definition of identical inner class */);
            }
            catch (Throwable throwable) {
                boolean finSuccess = success;
                int finalPayOut = payOut;
                int finalAvailable = available;
                int finalBonus = bonus;
                boolean finalIpRestricted = ipRestricted;
                VCoreServer.syncQueue.add(new /* invalid duplicate definition of identical inner class */);
                throw throwable;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private /* synthetic */ void lambda$tick$0(EntityPlayerMP player) {
        int available = 0;
        int bonus = 0;
        Gson gson = new Gson();
        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(VoteGift.config.backend + "/cash/get/" + player.c_()).openConnection();
            try {
                conn.setUseCaches(false);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.connect();
                int resCode = conn.getResponseCode();
                if (resCode == 200) {
                    Map res = (Map)gson.fromJson((Reader)new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8), new TypeToken<HashMap<String, Integer>>(){}.getType());
                    available = (Integer)res.get("available");
                    bonus = (Integer)res.get("bonus");
                }
            }
            finally {
                conn.disconnect();
            }
            int finalCash = available;
            int finalBonusCash = bonus;
            VCoreServer.syncQueue.add(new Runnable(finalCash, finalBonusCash, player){
                final /* synthetic */ int val$finalCash;
                final /* synthetic */ int val$finalBonusCash;
                final /* synthetic */ EntityPlayerMP val$player;
                {
                    this.val$finalCash = n;
                    this.val$finalBonusCash = n2;
                    this.val$player = entityPlayerMP;
                }

                @Override
                public void run() {
                    ((VoteGift)VoteGift.this).balance.available = this.val$finalCash;
                    ((VoteGift)VoteGift.this).balance.bonus = this.val$finalBonusCash;
                    SPacketHandler.sendVoteData((EntityPlayer)this.val$player, ((VoteGift)VoteGift.this).balance.available, ((VoteGift)VoteGift.this).balance.bonus);
                    VoteGift.this.requestingBalance = false;
                }
            });
        }
        catch (IOException e) {
            try {
                if (!FMLLaunchHandler.side().isClient()) {
                    e.printStackTrace();
                }
                int finalCash = available;
                int finalBonusCash = bonus;
                VCoreServer.syncQueue.add(new /* invalid duplicate definition of identical inner class */);
            }
            catch (Throwable throwable) {
                int finalCash = available;
                int finalBonusCash = bonus;
                VCoreServer.syncQueue.add(new /* invalid duplicate definition of identical inner class */);
                throw throwable;
            }
        }
    }

    private static class VoteBalance {
        int available;
        int bonus;

        private VoteBalance() {
        }
    }

    private static class RewardRequest {
        String name;
        long idempotentToken;
        int x;
        int y;
        int z;
        String serverName;
        String ip;
        long time;

        public RewardRequest(EntityPlayerMP entityPlayerMP, long idempotentToken) {
            String server = MinecraftServer.getServer().getMOTD();
            long time = System.currentTimeMillis() / 1000L;
            this.name = entityPlayerMP.c_();
            this.idempotentToken = idempotentToken;
            this.x = (int)entityPlayerMP.u;
            this.y = (int)entityPlayerMP.v;
            this.z = (int)entityPlayerMP.w;
            this.serverName = server;
            this.ip = entityPlayerMP.getPlayerIP();
            this.time = time;
        }
    }
}

