/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.world.World
 *  net.minecraftforge.common.IExtendedEntityProperties
 *  net.vintarz.movement.MovementUtils
 */
package vintarz.movement.server;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.vintarz.movement.MovementUtils;

public class MovementEEP
implements IExtendedEntityProperties {
    private EntityPlayerMP player;

    public void init(Entity entity, World world) {
        this.player = (EntityPlayerMP)entity;
    }

    public void saveNBTData(NBTTagCompound compound) {
        compound.setBoolean("crawling", MovementUtils.isPlayerCrawling((EntityPlayer)this.player));
    }

    public void loadNBTData(NBTTagCompound compound) {
        if (compound.getBoolean("crawling") != MovementUtils.isPlayerCrawling((EntityPlayer)this.player)) {
            MovementUtils.togglePlayerCrawling((EntityPlayer)this.player);
        }
    }
}

