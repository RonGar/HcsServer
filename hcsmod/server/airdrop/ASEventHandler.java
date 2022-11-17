/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.common.zombie.ZombieGroup
 *  hcsmod.entity.EntityZombieDayZ
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraftforge.event.ForgeSubscribe
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 */
package hcsmod.server.airdrop;

import hcsmod.common.zombie.ZombieGroup;
import hcsmod.entity.EntityZombieDayZ;
import hcsmod.server.airdrop.AirdropSystem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class ASEventHandler {
    @ForgeSubscribe
    public void checkEventZombieDead(LivingDeathEvent event) {
        if (!AirdropSystem.config.enabled) {
            return;
        }
        if (event.source.getEntity() instanceof EntityPlayer && event.entityLiving instanceof EntityZombieDayZ) {
            ZombieGroup zombieGroup = ((EntityZombieDayZ)event.entityLiving).zombieGroup;
            if (zombieGroup.airdropUniqueId != -1L && AirdropSystem.airdrops.containsKey(zombieGroup.airdropUniqueId)) {
                AirdropSystem.Airdrop airdrop = AirdropSystem.airdrops.get(zombieGroup.airdropUniqueId);
                if (airdrop.type.checkGroups.contains(zombieGroup.parentZombieGroupName)) {
                    ++airdrop.kills;
                }
            }
        }
    }
}

