/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.common.ITickHandler
 *  cpw.mods.fml.common.TickType
 *  cpw.mods.fml.common.registry.TickRegistry
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.EntityClientPlayerMP
 *  net.minecraft.client.renderer.entity.RenderManager
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.MovingObjectPosition
 *  net.minecraft.util.Vec3
 *  net.minecraftforge.client.event.RenderLivingEvent$Pre
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.ForgeSubscribe
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingUpdateEvent
 *  org.lwjgl.opengl.GL11
 */
package hcsmod.server;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.lwjgl.opengl.GL11;

@SideOnly(value=Side.CLIENT)
public class ClientSideDebug
implements ITickHandler {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean hit_box = false;
    private static boolean hit_key = false;
    private static boolean was_c_pressed = false;
    private static Entity hit_tgt = null;

    public ClientSideDebug() {
        MinecraftForge.EVENT_BUS.register((Object)this);
        TickRegistry.registerTickHandler((ITickHandler)this, (Side)Side.CLIENT);
    }

    @ForgeSubscribe
    public void $(RenderLivingEvent.Pre ev) {
        EntityClientPlayerMP p = Minecraft.getMinecraft().thePlayer;
        if (ev.entity == p) {
            return;
        }
        if (hit_box) {
            AxisAlignedBB b = ev.entity.E;
            GL11.glPushMatrix();
            GL11.glTranslated((double)(-RenderManager.renderPosX), (double)(-RenderManager.renderPosY), (double)(-RenderManager.renderPosZ));
            GL11.glDisable((int)3553);
            GL11.glEnable((int)3042);
            GL11.glBlendFunc((int)770, (int)771);
            GL11.glDisable((int)3008);
            GL11.glLineWidth((float)5.0f);
            if (hit_tgt == ev.entity) {
                GL11.glColor4f((float)0.0f, (float)1.0f, (float)0.0f, (float)1.0f);
            } else {
                GL11.glColor4f((float)1.0f, (float)0.0f, (float)0.0f, (float)1.0f);
            }
            GL11.glLineWidth((float)5.0f);
            GL11.glBegin((int)1);
            GL11.glVertex3d((double)b.maxX, (double)b.maxY, (double)b.maxZ);
            GL11.glVertex3d((double)b.maxX, (double)b.minY, (double)b.maxZ);
            GL11.glVertex3d((double)b.minX, (double)b.maxY, (double)b.minZ);
            GL11.glVertex3d((double)b.minX, (double)b.minY, (double)b.minZ);
            GL11.glVertex3d((double)b.maxX, (double)b.maxY, (double)b.minZ);
            GL11.glVertex3d((double)b.maxX, (double)b.minY, (double)b.minZ);
            GL11.glVertex3d((double)b.minX, (double)b.maxY, (double)b.maxZ);
            GL11.glVertex3d((double)b.minX, (double)b.minY, (double)b.maxZ);
            GL11.glVertex3d((double)b.maxX, (double)b.maxY, (double)b.maxZ);
            GL11.glVertex3d((double)b.maxX, (double)b.maxY, (double)b.minZ);
            GL11.glVertex3d((double)b.maxX, (double)b.maxY, (double)b.maxZ);
            GL11.glVertex3d((double)b.minX, (double)b.maxY, (double)b.maxZ);
            GL11.glVertex3d((double)b.minX, (double)b.maxY, (double)b.maxZ);
            GL11.glVertex3d((double)b.minX, (double)b.maxY, (double)b.minZ);
            GL11.glVertex3d((double)b.minX, (double)b.maxY, (double)b.minZ);
            GL11.glVertex3d((double)b.maxX, (double)b.maxY, (double)b.minZ);
            GL11.glVertex3d((double)b.maxX, (double)b.minY, (double)b.maxZ);
            GL11.glVertex3d((double)b.maxX, (double)b.minY, (double)b.minZ);
            GL11.glVertex3d((double)b.maxX, (double)b.minY, (double)b.maxZ);
            GL11.glVertex3d((double)b.minX, (double)b.minY, (double)b.maxZ);
            GL11.glVertex3d((double)b.minX, (double)b.minY, (double)b.maxZ);
            GL11.glVertex3d((double)b.minX, (double)b.minY, (double)b.minZ);
            GL11.glVertex3d((double)b.minX, (double)b.minY, (double)b.minZ);
            GL11.glVertex3d((double)b.maxX, (double)b.minY, (double)b.minZ);
            GL11.glEnd();
            GL11.glLineWidth((float)2.0f);
            GL11.glBegin((int)1);
            GL11.glVertex3d((double)b.maxX, (double)b.maxY, (double)b.maxZ);
            GL11.glVertex3d((double)b.maxX, (double)b.minY, (double)b.minZ);
            GL11.glVertex3d((double)b.maxX, (double)b.maxY, (double)b.maxZ);
            GL11.glVertex3d((double)b.minX, (double)b.minY, (double)b.maxZ);
            GL11.glVertex3d((double)b.maxX, (double)b.minY, (double)b.maxZ);
            GL11.glVertex3d((double)b.maxX, (double)b.maxY, (double)b.minZ);
            GL11.glVertex3d((double)b.maxX, (double)b.minY, (double)b.maxZ);
            GL11.glVertex3d((double)b.minX, (double)b.maxY, (double)b.maxZ);
            GL11.glVertex3d((double)b.maxX, (double)b.maxY, (double)b.minZ);
            GL11.glVertex3d((double)b.minX, (double)b.minY, (double)b.minZ);
            GL11.glVertex3d((double)b.maxX, (double)b.minY, (double)b.minZ);
            GL11.glVertex3d((double)b.minX, (double)b.maxY, (double)b.minZ);
            GL11.glVertex3d((double)b.minX, (double)b.maxY, (double)b.maxZ);
            GL11.glVertex3d((double)b.minX, (double)b.minY, (double)b.minZ);
            GL11.glVertex3d((double)b.minX, (double)b.minY, (double)b.maxZ);
            GL11.glVertex3d((double)b.minX, (double)b.maxY, (double)b.minZ);
            GL11.glVertex3d((double)b.maxX, (double)b.maxY, (double)b.minZ);
            GL11.glVertex3d((double)b.minX, (double)b.maxY, (double)b.maxZ);
            GL11.glVertex3d((double)b.maxX, (double)b.maxY, (double)b.maxZ);
            GL11.glVertex3d((double)b.minX, (double)b.maxY, (double)b.minZ);
            GL11.glVertex3d((double)b.maxX, (double)b.minY, (double)b.minZ);
            GL11.glVertex3d((double)b.minX, (double)b.minY, (double)b.maxZ);
            GL11.glVertex3d((double)b.maxX, (double)b.minY, (double)b.maxZ);
            GL11.glVertex3d((double)b.minX, (double)b.minY, (double)b.minZ);
            GL11.glEnd();
            GL11.glColor3f((float)1.0f, (float)1.0f, (float)1.0f);
            GL11.glEnable((int)3008);
            GL11.glDisable((int)3042);
            GL11.glEnable((int)3553);
            GL11.glPopMatrix();
        }
    }

    @ForgeSubscribe
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent ev) {
    }

    public void tickStart(EnumSet<TickType> type, Object ... tickData) {
    }

    public void tickEnd(EnumSet<TickType> type, Object ... tickData) {
        if (ClientSideDebug.mc.theWorld == null || ClientSideDebug.mc.thePlayer == null) {
            return;
        }
        List list = ClientSideDebug.mc.theWorld.e;
        double d2 = 16.0;
        Vec3 pos = ClientSideDebug.mc.thePlayer.l(1.0f);
        Vec3 look = ClientSideDebug.mc.thePlayer.aa();
        look.xCoord *= 16.0;
        look.yCoord *= 16.0;
        look.zCoord *= 16.0;
        look = look.addVector(pos.xCoord, pos.yCoord, pos.zCoord);
        hit_tgt = null;
        for (int i = 0; i < list.size(); ++i) {
            double d3;
            Entity entity = (Entity)list.get(i);
            if (!entity.canBeCollidedWith()) continue;
            float f2 = entity.getCollisionBorderSize();
            AxisAlignedBB axisalignedbb = entity.boundingBox.expand((double)f2, (double)f2, (double)f2);
            MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(pos, look);
            if (axisalignedbb.isVecInside(pos)) {
                if (!(0.0 < d2) && d2 != 0.0) continue;
                hit_tgt = entity;
                d2 = 0.0;
                continue;
            }
            if (movingobjectposition == null || !((d3 = pos.distanceTo(movingobjectposition.hitVec)) < d2) && d2 != 0.0) continue;
            if (entity == ClientSideDebug.mc.renderViewEntity.o && !entity.canRiderInteract()) {
                if (d2 != 0.0) continue;
                hit_tgt = entity;
                continue;
            }
            hit_tgt = entity;
            d2 = d3;
        }
    }

    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.CLIENT);
    }

    public String getLabel() {
        return "hcsDebugTHclient";
    }
}

