/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  co.uk.flansmods.common.InfoType
 *  co.uk.flansmods.common.guns.EntityBullet
 *  co.uk.flansmods.common.guns.ItemBullet
 *  co.uk.flansmods.common.guns.ItemGun
 *  co.uk.flansmods.common.network.PacketPlaySound
 *  co.uk.flansmods.vintarz.EntityShootFX
 *  co.uk.flansmods.vintarz.Util
 *  hcsmod.common.zombie.ZombieGroup
 *  hcsmod.entity.EntityPalatka
 *  hcsmod.entity.EntityZombieDayZ
 *  hcsmod.entity.IZombieAttackAI
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.ai.RandomPositionGenerator
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.ItemStack
 *  net.minecraft.pathfinding.PathNavigate
 *  net.minecraft.pathfinding.PathPoint
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.Vec3
 *  net.minecraft.util.Vec3Pool
 */
package hcsmod.server.zombie;

import co.uk.flansmods.common.InfoType;
import co.uk.flansmods.common.guns.EntityBullet;
import co.uk.flansmods.common.guns.ItemBullet;
import co.uk.flansmods.common.guns.ItemGun;
import co.uk.flansmods.common.network.PacketPlaySound;
import co.uk.flansmods.vintarz.EntityShootFX;
import co.uk.flansmods.vintarz.Util;
import hcsmod.common.zombie.ZombieGroup;
import hcsmod.entity.EntityPalatka;
import hcsmod.entity.EntityZombieDayZ;
import hcsmod.entity.IZombieAttackAI;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.zombie.DelayedPathFind;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3Pool;

public class ZombieAttackAi
extends IZombieAttackAI {
    private static final int PATH_FIND_COOLDOWN = 10;
    private static final int STUCK_LIMIT = 10;
    private final EntityZombieDayZ entityHost;
    private boolean hasTarget = false;
    private EntityLivingBase attackTarget;
    private double targetX;
    private double targetY;
    private double targetZ;
    private final DelayedPathFind delayedPathFind;
    private int pathFindCooldown = 0;
    private boolean pathChanged = false;
    private boolean isRetreating = false;
    private int stuckCounter = 0;
    private double prevPosX;
    private double prevPosY;
    private double prevPosZ;
    private int attackTick;
    private int currentBullets;
    private int ticksToBurst;
    private int ticksToShoot;

    public ZombieAttackAi(EntityZombieDayZ par1EntityCreature, double par2) {
        this.entityHost = par1EntityCreature;
        this.a(3);
        this.delayedPathFind = new DelayedPathFind(par1EntityCreature.k(), par2);
    }

    public void entityTick() {
        if (this.entityHost.ac % 2 == 1) {
            return;
        }
        if (!this.entityHost.canAttackTarget(this.entityHost.m()) || !this.entityHost.o((Entity)this.entityHost.m())) {
            this.entityHost.d(null);
        }
        this.find_target();
        this.attackTarget = this.entityHost.m();
        if (this.attackTarget != null) {
            Vec3 targetVec = this.targetPos(this.entityHost.q.getWorldVec3Pool(), this.attackTarget);
            this.targetX = targetVec.xCoord;
            this.targetY = targetVec.yCoord;
            this.targetZ = targetVec.zCoord;
        }
    }

    public Vec3 targetPos(Vec3Pool vec3Pool, EntityLivingBase target) {
        double dX = target.u - this.entityHost.u;
        double dZ = target.w - this.entityHost.w;
        double hDst = Math.sqrt(dX * dX + dZ * dZ);
        dX /= hDst;
        dZ /= hDst;
        double multiplier = (double)(-target.O) * 0.666;
        return vec3Pool.getVecFromPool(target.u + (dX *= multiplier), target.v + (double)target.getEyeHeight() - 0.2, target.w + (dZ *= multiplier));
    }

    public boolean setWalkTarget(double x, double y, double z) {
        if (this.isRetreating) {
            return true;
        }
        double followRange = this.entityHost.a(SharedMonsterAttributes.followRange).getAttributeValue();
        if (this.entityHost.e(x, y, z) > followRange * followRange) {
            return false;
        }
        PathNavigate navigator = this.entityHost.k();
        if (navigator.noPath()) {
            this.queuePathFind(x, y, z);
            return true;
        }
        PathPoint targetPoint = navigator.getPath().getFinalPathPoint();
        double dX = x - (double)targetPoint.xCoord;
        double dY = y - (double)targetPoint.yCoord;
        double dZ = z - (double)targetPoint.zCoord;
        if (dX * dX + dY * dY + dZ * dZ < 1.0) {
            return true;
        }
        this.queuePathFind(x, y, z);
        return true;
    }

    private void queuePathFind(double x, double y, double z) {
        this.delayedPathFind.x = x;
        this.delayedPathFind.y = y;
        this.delayedPathFind.z = z;
        this.queuePathFind();
    }

    private void queuePathFind() {
        this.hasTarget = true;
        if (this.pathFindCooldown > 0) {
            this.pathChanged = true;
            return;
        }
        this.pathChanged = false;
        this.delayedPathFind.queue();
        this.pathFindCooldown = 10;
    }

    public boolean hasTarget() {
        return this.hasTarget;
    }

    public boolean hasPath() {
        if (!this.delayedPathFind.hasPath()) {
            this.stuckCounter = 0;
            return false;
        }
        if (Math.abs(this.prevPosX - this.entityHost.u) < 0.0625 && Math.abs(this.prevPosZ - this.entityHost.w) < 0.0625 && Math.abs(this.prevPosY - this.entityHost.v) < 0.0625) {
            if (++this.stuckCounter > 10) {
                this.stuckCounter = 0;
                return false;
            }
        } else {
            this.stuckCounter = 0;
        }
        this.prevPosX = this.entityHost.u;
        this.prevPosY = this.entityHost.v;
        this.prevPosZ = this.entityHost.w;
        return true;
    }

    public boolean a() {
        if (this.attackTarget != null) {
            this.isRetreating = false;
            if (!this.setWalkTarget(this.attackTarget.u, this.attackTarget.E.minY, this.attackTarget.w)) {
                this.attackTarget = null;
                this.entityHost.d(null);
            }
        }
        return this.hasTarget;
    }

    public boolean b() {
        if (!this.a()) {
            return false;
        }
        if (!(this.attackTarget != null || this.delayedPathFind.isQueued() || this.pathChanged || this.hasPath())) {
            Vec3 vec3;
            if (!this.isRetreating && Util.isFlansWeapon((ItemStack)this.entityHost.aZ()) && (vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom((EntityCreature)this.entityHost, (int)this.entityHost.zombieGroup.walkBackDistanceHorVert[0], (int)this.entityHost.zombieGroup.walkBackDistanceHorVert[1], (Vec3)this.entityHost.q.getWorldVec3Pool().getVecFromPool(this.targetX, this.targetY, this.targetZ))) != null) {
                this.queuePathFind(vec3.xCoord, vec3.yCoord, vec3.zCoord);
                this.isRetreating = true;
                return true;
            }
            this.isRetreating = false;
            this.hasTarget = false;
            return false;
        }
        return true;
    }

    public void d() {
        this.entityHost.k().clearPathEntity();
        this.hasTarget = false;
    }

    public void e() {
        boolean inRange;
        if (this.delayedPathFind.isQueued()) {
            this.pathFindCooldown = 10;
        } else if (this.pathFindCooldown > 0) {
            --this.pathFindCooldown;
        } else if (this.pathChanged) {
            this.queuePathFind();
        }
        this.entityHost.h().setLookPosition(this.delayedPathFind.x, this.delayedPathFind.y + 1.5, this.delayedPathFind.z, 180.0f, 180.0f);
        this.attackTick = Math.max(this.attackTick - 1, 0);
        PathNavigate navigate = this.entityHost.k();
        boolean isShooter = Util.isFlansWeapon((ItemStack)this.entityHost.aZ());
        boolean bl = inRange = isShooter && Math.sqrt((this.entityHost.u - this.targetX) * (this.entityHost.u - this.targetX) + (this.entityHost.v - this.targetY) * (this.entityHost.v - this.targetY) + (this.entityHost.w - this.targetZ) * (this.entityHost.w - this.targetZ)) < (double)(this.entityHost.zombieGroup.shootRange - 2);
        if (!isShooter || !inRange || this.attackTarget == null) {
            this.delayedPathFind.upload();
        } else {
            navigate.setPath(null, 0.0);
        }
        if (this.entityHost.zombieGroup.event) {
            for (Object o : this.entityHost.q.getEntitiesWithinAABB(EntityPalatka.class, this.entityHost.E.expand(1.0, 1.0, 1.0))) {
                if (!(o instanceof EntityPalatka)) continue;
                EntityPalatka e = (EntityPalatka)o;
                if (e.M) continue;
                e.x();
                this.entityHost.aV();
            }
        }
        if (this.attackTarget == null) {
            return;
        }
        if (this.canAttack((Entity)this.attackTarget, 0.25, 1.0)) {
            if (this.attackTick <= 0) {
                this.attackTick = 20;
                this.entityHost.aV();
                this.entityHost.m((Entity)this.attackTarget);
            }
        } else if (Util.isFlansWeapon((ItemStack)this.entityHost.aZ())) {
            double dist = Math.sqrt((this.entityHost.u - this.targetX) * (this.entityHost.u - this.targetX) + (this.entityHost.v - this.targetY) * (this.entityHost.v - this.targetY) + (this.entityHost.w - this.targetZ) * (this.entityHost.w - this.targetZ));
            this.rangeAttack((float)dist);
        }
    }

    private boolean canAttack(Entity target, double distance, double up) {
        AxisAlignedBB aabb = AxisAlignedBB.getAABBPool().getAABB(this.entityHost.E.minX - distance, this.entityHost.E.minY, this.entityHost.E.minZ - distance, this.entityHost.E.maxX + distance, this.entityHost.E.maxY + up, this.entityHost.E.maxZ + distance);
        return aabb.intersectsWith(target.boundingBox);
    }

    public void rangeAttack(float distance) {
        this.entityHost.h().setLookPosition(this.targetX, this.targetY, this.targetZ, 180.0f, 180.0f);
        this.entityHost.h().onUpdateLook();
        this.entityHost.h().setLookPosition(this.targetX, this.targetY, this.targetZ, 180.0f, 180.0f);
        ZombieGroup zombieGroup = this.entityHost.zombieGroup;
        Random rand = this.entityHost.rand();
        if (distance <= (float)zombieGroup.shootRange) {
            if (this.ticksToBurst <= 0) {
                if (this.currentBullets > 0) {
                    if (this.ticksToShoot <= 0) {
                        ItemGun itemGun = (ItemGun)this.entityHost.aZ().getItem();
                        ItemStack bulletStack = itemGun.getBulletItemStack(this.entityHost.aZ(), 0);
                        MinecraftServer.getServer().getConfigurationManager().sendToAllNear(this.entityHost.u, this.entityHost.v, this.entityHost.w, 64.0, this.entityHost.ar, PacketPlaySound.buildSoundPacket((double)this.entityHost.u, (double)this.entityHost.v, (double)this.entityHost.w, (String)itemGun.type.shootSound, (boolean)itemGun.type.distortSound, (boolean)false));
                        EntityBullet entityBullet = ((ItemBullet)bulletStack.getItem()).getEntity(this.entityHost, zombieGroup.shootSpread, (float)zombieGroup.shootDamage / 600.0f, (float)zombieGroup.bulletSpeed, false, 0.0f, (InfoType)itemGun.type);
                        this.entityHost.q.spawnEntityInWorld((Entity)new EntityShootFX(entityBullet, (EntityLivingBase)this.entityHost));
                        entityBullet.doAttack();
                        --this.currentBullets;
                        this.ticksToShoot = rand.nextInt(zombieGroup.cooldownBetweenShoot[1] - zombieGroup.cooldownBetweenShoot[0] + 1) + zombieGroup.cooldownBetweenShoot[0];
                    } else {
                        --this.ticksToShoot;
                    }
                } else {
                    this.ticksToBurst = rand.nextInt(zombieGroup.cooldownBetweenBurst[1] - zombieGroup.cooldownBetweenBurst[0] + 1) + zombieGroup.cooldownBetweenBurst[0];
                    this.currentBullets = rand.nextInt(zombieGroup.bulletsInBurst[1] - zombieGroup.bulletsInBurst[0] + 1) + zombieGroup.bulletsInBurst[0];
                }
            } else {
                --this.ticksToBurst;
            }
        }
    }

    public void find_target() {
        double prevDistanceSq = this.entityHost.m() != null ? this.entityHost.m().e((Entity)this.entityHost) - 4.0 : Double.MAX_VALUE;
        double distanceStep = 1.0;
        if (this.entityHost.m() == null || this.entityHost.m().M) {
            long time = this.entityHost.q.getWorldInfo().getWorldTime();
            boolean night = time > 13800L && time < 22200L && !this.entityHost.zombieGroup.event;
            for (Object o : this.entityHost.q.playerEntities) {
                double hearDistance;
                EntityPlayer p;
                double distanceSq;
                if (!(o instanceof EntityPlayer) || (distanceSq = (p = (EntityPlayer)o).e((Entity)this.entityHost)) >= prevDistanceSq) continue;
                int lvl = ExtendedPlayer.server((EntityPlayer)p).visibility;
                if (!this.entityHost.isDeaf && distanceSq < (hearDistance = (double)this.entityHost.zombieGroup.detectDistances_b[lvl]) * hearDistance && this.entityHost.canAttackTarget((EntityLivingBase)p)) {
                    if (this.entityHost.o((Entity)p)) {
                        this.entityHost.d((EntityLivingBase)p);
                    } else {
                        this.setWalkTarget(p.u, p.E.minY, p.w);
                    }
                    prevDistanceSq = distanceSq - 1.0;
                    continue;
                }
                if (this.entityHost.isBlind) continue;
                int n = night ? 1 : 4;
                double sightDistance = this.entityHost.zombieGroup.detectDistances_f[Math.min(lvl, n)];
                if (!(distanceSq < sightDistance * sightDistance)) continue;
                float p_rot = 360.0f - (float)(Math.atan2(p.u - this.entityHost.u, p.w - this.entityHost.w) / Math.PI) * 180.0f;
                if (!this.hasTarget && (double)EntityZombieDayZ.angularDistance((float)p_rot, (float)this.entityHost.A) > 75.0 || !this.entityHost.canAttackTarget((EntityLivingBase)p) || !this.entityHost.o((Entity)p)) continue;
                this.entityHost.d((EntityLivingBase)p);
                prevDistanceSq = distanceSq - 1.0;
            }
        }
    }
}

