/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.entity.EntityZombieDayZ
 *  hcsmod.entity.IZombieWanderAI
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.ai.RandomPositionGenerator
 *  net.minecraft.util.Vec3
 */
package hcsmod.server.zombie;

import hcsmod.entity.EntityZombieDayZ;
import hcsmod.entity.IZombieWanderAI;
import hcsmod.server.zombie.DelayedPathFind;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.Vec3;

public class ZombieWanderAi
extends IZombieWanderAI {
    private EntityZombieDayZ entity;
    private double spawnX;
    private double spawnY;
    private double spawnZ;
    private double xPosition;
    private double yPosition;
    private double zPosition;
    private long nextMoveTime;
    private final DelayedPathFind delayedPathFind;

    public ZombieWanderAi(EntityZombieDayZ par1EntityCreature, double par2) {
        this.entity = par1EntityCreature;
        this.delayedPathFind = new DelayedPathFind(this.entity.k(), par2);
        this.a(1);
    }

    public void applyCooldown() {
        this.nextMoveTime = System.currentTimeMillis() + (long)this.entity.aD().nextInt(this.entity.zombieGroup.walkCooldown[1] - this.entity.zombieGroup.walkCooldown[0] + 1) + (long)this.entity.zombieGroup.walkCooldown[0];
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean a() {
        if (this.spawnY == 0.0 && this.spawnX == 0.0 && this.spawnZ == 0.0 && this.entity.u != 0.0 && this.entity.v != 0.0 && this.entity.w != 0.0) {
            this.spawnX = this.entity.u;
            this.spawnY = this.entity.v;
            this.spawnZ = this.entity.w;
        }
        if (System.currentTimeMillis() >= this.nextMoveTime && this.entity.zombieGroup != null) {
            double posX = this.entity.u;
            double posY = this.entity.v;
            double posZ = this.entity.w;
            try {
                Vec3 vec3;
                if (this.entity.zombieGroup.lockToSpawn) {
                    this.entity.u = this.spawnX;
                    this.entity.v = this.spawnY;
                    this.entity.w = this.spawnZ;
                }
                if ((vec3 = RandomPositionGenerator.findRandomTarget((EntityCreature)this.entity, (int)this.entity.zombieGroup.wanderAreaHorVert[0], (int)this.entity.zombieGroup.wanderAreaHorVert[1])) == null) {
                    boolean bl = false;
                    return bl;
                }
                this.xPosition = vec3.xCoord;
                this.yPosition = vec3.yCoord;
                this.zPosition = vec3.zCoord;
                boolean bl = true;
                return bl;
            }
            finally {
                this.entity.u = posX;
                this.entity.v = posY;
                this.entity.w = posZ;
            }
        }
        return false;
    }

    public void d() {
        this.applyCooldown();
    }

    public boolean b() {
        return this.delayedPathFind.isQueued() || this.delayedPathFind.hasPath();
    }

    public void c() {
        this.delayedPathFind.x = this.xPosition;
        this.delayedPathFind.y = this.yPosition;
        this.delayedPathFind.z = this.zPosition;
        this.delayedPathFind.queue();
    }

    public void e() {
        this.delayedPathFind.upload();
    }

    public boolean respawn() {
        if (this.spawnY == 0.0 && this.spawnX == 0.0 && this.spawnZ == 0.0) {
            return false;
        }
        this.entity.k().clearPathEntity();
        this.entity.a(this.spawnX, this.spawnY, this.spawnZ);
        return true;
    }
}

