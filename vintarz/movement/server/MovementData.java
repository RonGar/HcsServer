/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  api.player.server.ServerPlayerAPI
 *  api.player.server.ServerPlayerBase
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.Packet10Flying
 *  net.vintarz.movement.MovementData
 *  net.vintarz.movement.MovementFakePacket
 */
package vintarz.movement.server;

import api.player.server.ServerPlayerAPI;
import api.player.server.ServerPlayerBase;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet10Flying;
import net.vintarz.movement.MovementFakePacket;
import vintarz.movement.server.MovementServer;

public class MovementData
extends ServerPlayerBase
implements net.vintarz.movement.MovementData {
    public static final boolean AWH_ENABLE = Boolean.parseBoolean(System.getProperty("vz.awh.enable", "true"));
    private static final Packet10Flying update = new Packet10Flying();
    private static boolean allow;
    public float sprinting;
    public boolean teleported;
    private float nextStepDistance;
    int crawlingToggleTimeout = 0;
    float crawlingToggleDistance = 0.0f;
    public float sliding;
    public int teleportTicks;
    private double tpX;
    private double tpY;
    private double tpZ;
    public double motionX;
    public double motionY;
    public double motionZ;

    public MovementData(ServerPlayerAPI var1) {
        super(var1);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tick() {
        try {
            allow = true;
            MovementData.update.stance = this.player.E.minY;
            MovementData.update.xPosition = this.player.u;
            MovementData.update.yPosition = this.player.v;
            MovementData.update.zPosition = this.player.w;
            if (this.player.ag()) {
                this.player.playerNetServerHandler.handleFlying(update);
                return;
            }
            double motionX = this.player.x;
            double motionY = this.player.y;
            double motionZ = this.player.z;
            double minX = this.player.E.minX;
            double minY = this.player.E.minY;
            double minZ = this.player.E.minZ;
            double maxX = this.player.E.maxX;
            double maxY = this.player.E.maxY;
            double maxZ = this.player.E.maxZ;
            double posX = this.player.u;
            double posY = this.player.v;
            double posZ = this.player.w;
            double lastX = this.player.U;
            double lastY = this.player.V;
            double lastZ = this.player.W;
            boolean onGround = this.player.F;
            this.player.playerNetServerHandler.handleFlying(update);
            this.player.F = onGround;
            this.player.U = this.player.r = lastX;
            this.player.V = this.player.s = lastY;
            this.player.W = this.player.t = lastZ;
            this.player.u = posX;
            this.player.v = posY;
            this.player.w = posZ;
            this.player.E.minX = minX;
            this.player.E.minY = minY;
            this.player.E.minZ = minZ;
            this.player.E.maxX = maxX;
            this.player.E.maxY = maxY;
            this.player.E.maxZ = maxZ;
            this.player.x = motionX;
            this.player.y = motionY;
            this.player.z = motionZ;
        }
        finally {
            allow = false;
            if (AWH_ENABLE) {
                this.player.playerNetServerHandler.sendPacketToPlayer((Packet)new MovementFakePacket(this.player));
            }
        }
    }

    private boolean disallow() {
        return !allow;
    }

    public void moveEntityWithHeading(float strafe, float forward) {
        if (this.disallow()) {
            MovementServer.kick(this.player);
        }
    }

    public void moveFlying(float strafe, float forward, float speed) {
        if (this.disallow()) {
            MovementServer.kick(this.player);
        }
    }

    public void moveEntity(double x, double y, double z) {
    }

    public void onTeleport() {
        this.teleported = true;
        this.teleportTicks = 0;
        this.tpX = this.player.u;
        this.tpY = this.player.v;
        this.tpZ = this.player.w;
        this.motionZ = 0.0;
        this.motionY = 0.0;
        this.motionX = 0.0;
        this.afterToggleCrawling();
    }

    public boolean canToggleCrawling() {
        return this.crawlingToggleDistance == 0.0f || this.crawlingToggleTimeout == 0;
    }

    public void afterToggleCrawling() {
        this.crawlingToggleTimeout = 6;
        this.crawlingToggleDistance = 1.5f;
    }

    public float modSprinting() {
        return this.sprinting;
    }

    public void modSprinting(float modSprinting) {
        this.sprinting = modSprinting;
    }

    public float nextStepDistance() {
        return this.nextStepDistance;
    }

    public void nextStepDistance(float nextStepDistance) {
        this.nextStepDistance = nextStepDistance;
    }

    public boolean ltcas(double posX, double posY, double posZ) {
        if (posX == (double)((float)this.tpX) && posY == (double)((float)this.tpY) && posZ == (double)((float)this.tpZ)) {
            this.player.u = this.tpX;
            this.player.v = this.tpY;
            this.player.w = this.tpZ;
            return true;
        }
        return false;
    }
}

