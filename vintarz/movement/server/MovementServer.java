/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  api.player.server.IServerPlayerAPI
 *  api.player.server.ServerPlayerAPI
 *  cpw.mods.fml.common.IPlayerTracker
 *  cpw.mods.fml.common.network.IPacketHandler
 *  cpw.mods.fml.common.network.NetworkRegistry
 *  cpw.mods.fml.common.network.Player
 *  cpw.mods.fml.common.registry.GameRegistry
 *  cpw.mods.fml.relauncher.Side
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.network.INetworkManager
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.Packet250CustomPayload
 *  net.minecraft.server.MinecraftServer
 *  net.minecraftforge.common.IExtendedEntityProperties
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.ForgeSubscribe
 *  net.minecraftforge.event.entity.EntityEvent$EntityConstructing
 *  net.vintarz.movement.MovementData
 *  net.vintarz.movement.MovementUtils
 *  org.apache.commons.math3.util.FastMath
 *  vintarz.core.VRP
 */
package vintarz.movement.server;

import api.player.server.IServerPlayerAPI;
import api.player.server.ServerPlayerAPI;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import net.vintarz.movement.MovementUtils;
import org.apache.commons.math3.util.FastMath;
import vintarz.core.VRP;
import vintarz.movement.server.MovementData;
import vintarz.movement.server.MovementEEP;

public class MovementServer
implements IPacketHandler,
IPlayerTracker {
    private static final Set<String> online = new HashSet<String>();

    public static void init() {
        MovementServer instance = new MovementServer();
        MinecraftForge.EVENT_BUS.register((Object)instance);
        GameRegistry.registerPlayerTracker((IPlayerTracker)instance);
        ServerPlayerAPI.register((String)"vtzmovement", MovementData.class);
        NetworkRegistry.instance().registerChannel((IPacketHandler)instance, "M", Side.SERVER);
    }

    public static boolean isReallySprinting(EntityPlayer p) {
        MovementData d = MovementServer.getMoveData(p);
        return d != null && d.sprinting >= 0.5f;
    }

    @ForgeSubscribe
    public void playerConstruct(EntityEvent.EntityConstructing ev) {
        if (ev.entity instanceof EntityPlayerMP) {
            ev.entity.registerExtendedProperties("M", (IExtendedEntityProperties)new MovementEEP());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Lifted jumps to return sites
     */
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player plr) {
        float f;
        MovementData d;
        boolean rotated;
        boolean moved;
        EntityPlayerMP p;
        block35: {
            block34: {
                block33: {
                    p = (EntityPlayerMP)plr;
                    moved = packet.data != null && (packet.data.length == 13 || packet.data.length == 21);
                    rotated = packet.data != null && (packet.data.length == 9 || packet.data.length == 21);
                    d = MovementServer.getMoveData((EntityPlayer)p);
                    if (d == null) {
                        MovementServer.kick(p);
                        return;
                    }
                    if (Double.isNaN(p.u) || Double.isNaN(p.v) || Double.isNaN(p.w) || Float.isNaN(p.A) || Float.isNaN(p.B)) {
                        p.a(0.0, 100.0, 0.0, 0.0f, 0.0f);
                        MovementServer.kick(p, "\u0422\u044b \u0437\u0430\u0431\u0430\u0433\u0430\u043b\u0441\u044f. \u041f\u043e\u0441\u043b\u0435 \u043f\u0435\u0440\u0435\u0437\u0430\u0445\u043e\u0434\u0430 \u043e\u043a\u0430\u0436\u0435\u0448\u044c\u0441\u044f \u043d\u0430 \u0441\u0435\u0439\u0444\u0437\u043e\u043d\u0435.");
                        return;
                    }
                    if (!d.teleported) break block35;
                    if (moved && !rotated && packet.data[0] == 0) break block33;
                    if (!d.teleported) return;
                    if (++d.teleportTicks < 20) return;
                    p.setPositionAndUpdate(p.u, p.v, p.w);
                    return;
                }
                VRP in = new VRP(packet);
                float posX = in.readFloat();
                float posY = in.readFloat();
                float posZ = in.readFloat();
                if (!(p.ag() ? !d.ltcas(posX, posY, posZ) : (float)p.u != posX || (float)p.E.minY != posY || (float)p.w != posZ)) break block34;
                if (!d.teleported) return;
                if (++d.teleportTicks < 20) return;
                p.setPositionAndUpdate(p.u, p.v, p.w);
                return;
            }
            try {
                block36: {
                    break block36;
                    catch (IOException in) {
                        // empty catch block
                    }
                }
                d.teleported = false;
                p.z = 0.0;
                p.y = 0.0;
                p.x = 0.0;
                p.F = false;
                d.sprinting = 0.0f;
                d.tick();
                return;
            }
            finally {
                if (d.teleported && ++d.teleportTicks >= 20) {
                    p.setPositionAndUpdate(p.u, p.v, p.w);
                }
            }
        }
        if (packet.data == null || packet.data.length == 0) {
            MovementUtils.movePlayer((EntityPlayer)p, (net.vintarz.movement.MovementData)d, (float)0.0f, (float)0.0f, (boolean)false, (float)p.A);
            d.tick();
            return;
        }
        if (!moved && !rotated && packet.data.length != 1) {
            MovementServer.kick(p);
            return;
        }
        VRP in = new VRP(packet);
        byte input = in.type;
        boolean fwd = (input & 1) > 0;
        boolean back = (input & 2) > 0;
        boolean left = (input & 4) > 0;
        boolean right = (input & 8) > 0;
        boolean jump = (input & 0x10) > 0;
        boolean sprinting = (input & 0x20) > 0;
        boolean sneaking = (input & 0x40) > 0;
        boolean crawling = (input & 0x80) > 0;
        int forward = 0;
        int strafe = 0;
        if (fwd) {
            ++forward;
        }
        if (back) {
            --forward;
        }
        if (left) {
            ++strafe;
        }
        if (right) {
            --strafe;
        }
        if (p.ai() != sprinting) {
            p.c(sprinting);
        }
        if (p.ah() != sneaking) {
            p.b(sneaking);
        }
        float oldYaw = p.A;
        if (rotated) {
            try {
                float rotationYaw = in.readFloat();
                float rotationPitch = in.readFloat();
                if (!Float.isFinite(rotationYaw) || !Float.isFinite(rotationPitch)) {
                    MovementServer.kick(p);
                    return;
                }
                p.A = rotationYaw;
                p.B = rotationPitch;
            }
            catch (IOException rotationYaw) {
                // empty catch block
            }
        }
        if (p.ag()) {
            p.bf = forward;
            p.be = strafe;
            d.tick();
            return;
        }
        if (MovementUtils.isPlayerCrawling((EntityPlayer)p) != crawling && (!crawling || p.F || MovementUtils.canClimb((Entity)p, (double)0.5))) {
            boolean allow;
            boolean bl = allow = !p.F || d.canToggleCrawling();
            if (p.F) {
                d.afterToggleCrawling();
            }
            if (!allow || !MovementUtils.togglePlayerCrawling((EntityPlayer)p, (!p.F || d.motionY < -0.2 ? 1 : 0) != 0)) {
                p.setPositionAndUpdate(p.u, p.v, p.w);
                return;
            }
            d.sliding = p.F && (double)d.sprinting > 0.5 && crawling ? Math.min(d.sprinting, 0.75f) : 0.0f;
        }
        if (p.F && d.sprinting > 0.0f && crawling && d.sliding > 0.0f && !p.H()) {
            float speed = d.sliding * 0.25f;
            double f4 = FastMath.sin((double)((double)p.A * Math.PI / 180.0));
            double f5 = FastMath.cos((double)((double)p.A * Math.PI / 180.0));
            d.motionX -= (double)speed * f4;
            d.motionZ += (double)speed * f5;
            if ((p.managedPosX -= 40) < 0) {
                p.managedPosX = 0;
            }
        }
        p.U = p.r = p.u;
        p.V = p.s = p.v;
        p.W = p.t = p.w;
        double pX = p.u;
        double pZ = p.w;
        p.x = d.motionX;
        p.y = d.motionY;
        p.z = d.motionZ;
        MovementUtils.movePlayer((EntityPlayer)p, (net.vintarz.movement.MovementData)d, (float)strafe, (float)forward, (boolean)jump, (float)oldYaw);
        d.motionX = p.x;
        d.motionY = p.y;
        d.motionZ = p.z;
        d.crawlingToggleDistance = (float)((double)d.crawlingToggleDistance - FastMath.sqrt((double)((pX -= p.u) * pX + (pZ -= p.w) * pZ)));
        if (f < 0.0f) {
            d.crawlingToggleDistance = 0.0f;
        }
        float posX = (float)p.u;
        float posY = (float)p.v;
        float posZ = (float)p.w;
        if (moved) {
            try {
                posX = in.readFloat();
                posY = in.readFloat();
                posZ = in.readFloat();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        if ((float)p.u != posX || (float)p.E.minY != posY || (float)p.w != posZ) {
            p.setPositionAndUpdate(p.u, p.v, p.w);
            return;
        }
        if (d.crawlingToggleTimeout > 0) {
            --d.crawlingToggleTimeout;
        }
        d.tick();
        p.bf = forward;
        p.be = strafe;
    }

    public static void onTeleport(EntityPlayerMP player) {
        try {
            MovementData d = MovementServer.getMoveData((EntityPlayer)player);
            d.onTeleport();
            if (player.ag() && MovementUtils.isPlayerCrawling((EntityPlayer)player)) {
                MovementUtils.togglePlayerCrawling((EntityPlayer)player, (boolean)false, (boolean)true);
            }
            if (online.contains(player.bu) && MovementUtils.isPlayerCrawling((EntityPlayer)player) && d.canToggleCrawling()) {
                MovementUtils.togglePlayerCrawling((EntityPlayer)player);
            }
            MovementServer.sendCrawlingState(player);
        }
        catch (NullPointerException nullPointerException) {
            // empty catch block
        }
    }

    public static void kick(EntityPlayerMP player) {
        MovementServer.kick(player, "\u041d\u0435-\u0430");
    }

    public static void kick(EntityPlayerMP player, String reason) {
        if (MinecraftServer.getServer().isSinglePlayer()) {
            throw new RuntimeException();
        }
        player.playerNetServerHandler.kickPlayerFromServer(reason);
    }

    private static void sendCrawlingState(EntityPlayerMP player) {
        boolean crawl = MovementUtils.isPlayerCrawling((EntityPlayer)player);
        player.playerNetServerHandler.sendPacketToPlayer((Packet)new Packet250CustomPayload("M", new byte[]{(byte)(crawl ? 1 : 0)}));
    }

    public void onPlayerLogin(EntityPlayer player) {
        online.add(player.username);
        MovementServer.sendCrawlingState((EntityPlayerMP)player);
    }

    public void onPlayerLogout(EntityPlayer player) {
        online.remove(player.username);
    }

    public void onPlayerChangedDimension(EntityPlayer player) {
        MovementServer.sendCrawlingState((EntityPlayerMP)player);
    }

    public void onPlayerRespawn(EntityPlayer player) {
        MovementServer.sendCrawlingState((EntityPlayerMP)player);
    }

    private static MovementData getMoveData(EntityPlayer player) {
        return (MovementData)ServerPlayerAPI.getServerPlayerBase((IServerPlayerAPI)((IServerPlayerAPI)player), (String)"vtzmovement");
    }
}

