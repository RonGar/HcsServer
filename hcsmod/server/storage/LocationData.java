/*
 * Decompiled with CFR 0.150.
 */
package hcsmod.server.storage;

public class LocationData {
    public int x1;
    public int y1;
    public int z1;
    public int x2;
    public int y2;
    public int z2;

    public LocationData(int x1, int y1, int z1, int x2, int y2, int z2) {
        if (x1 <= x2) {
            this.x1 = x1;
            this.x2 = x2;
        } else {
            this.x1 = x2;
            this.x2 = x1;
        }
        if (z1 <= z2) {
            this.z1 = z1;
            this.z2 = z2;
        } else {
            this.z1 = z2;
            this.z2 = z1;
        }
        if (y1 <= y2) {
            this.y1 = y1;
            this.y2 = y2;
        } else {
            this.y1 = y2;
            this.y2 = y1;
        }
    }
}

