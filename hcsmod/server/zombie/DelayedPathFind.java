/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.pathfinding.PathEntity
 *  net.minecraft.pathfinding.PathNavigate
 */
package hcsmod.server.zombie;

import hcsmod.server.DelayedTask;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;

public class DelayedPathFind
extends DelayedTask {
    public final PathNavigate navigate;
    public final double speedMultiplier;
    public double x;
    public double y;
    public double z;
    private PathEntity path = null;

    public DelayedPathFind(PathNavigate navigate, double speedMultiplier) {
        this.navigate = navigate;
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    public void run() {
        this.navigate.setBreakDoors(true);
        this.navigate.setEnterDoors(true);
        this.path = this.navigate.getPathToXYZ(this.x, this.y, this.z);
    }

    public boolean hasPath() {
        return this.path != null && !this.path.isFinished();
    }

    public void upload() {
        if (this.navigate.getPath() != this.path) {
            this.navigate.setPath(this.path, this.speedMultiplier);
            this.path = this.navigate.getPath();
        }
    }
}

