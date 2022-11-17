/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  co.uk.flansmods.common.guns.EntityBullet
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.util.AxisAlignedBB
 */
package co.uk.flansmods.vintarz.server;

import co.uk.flansmods.common.guns.EntityBullet;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;

public class LagEntry {
    public final AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox((double)0.0, (double)0.0, (double)0.0, (double)0.0, (double)0.0, (double)0.0);
    public float rotation = 0.0f;
    public EntityLivingBase entity;

    public void update(EntityLivingBase e) {
        this.aabb.minX = e.E.minX - (double)EntityBullet.HITBOX_EXTEND;
        this.aabb.minY = e.E.minY - (double)EntityBullet.HITBOX_EXTEND;
        this.aabb.minZ = e.E.minZ - (double)EntityBullet.HITBOX_EXTEND;
        this.aabb.maxX = e.E.maxX + (double)EntityBullet.HITBOX_EXTEND;
        this.aabb.maxY = e.E.maxY + (double)EntityBullet.HITBOX_EXTEND;
        this.aabb.maxZ = e.E.maxZ + (double)EntityBullet.HITBOX_EXTEND;
        this.rotation = e.A;
        this.entity = e;
    }
}

