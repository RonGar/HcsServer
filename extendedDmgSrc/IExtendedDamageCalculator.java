/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayerMP
 */
package extendedDmgSrc;

import net.minecraft.entity.player.EntityPlayerMP;

public interface IExtendedDamageCalculator {
    public float damageHead(EntityPlayerMP var1, float var2);

    public float damageBody(EntityPlayerMP var1, float var2);

    public float damageLegs(EntityPlayerMP var1, float var2);

    public float damageUnspecified(EntityPlayerMP var1, float var2);
}

