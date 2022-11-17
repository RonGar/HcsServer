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
package hcsmod.server.event;

import hcsmod.common.zombie.ZombieGroup;
import hcsmod.entity.EntityZombieDayZ;
import hcsmod.server.event.EventSystem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class ESEventHandler {
    @ForgeSubscribe
    public void checkEventZombieDead(LivingDeathEvent event) {
        if (!EventSystem.config.enabled) {
            return;
        }
        if (event.source.getEntity() instanceof EntityPlayer && event.entityLiving instanceof EntityZombieDayZ) {
            ZombieGroup zombieGroup = ((EntityZombieDayZ)event.entityLiving).zombieGroup;
            if (zombieGroup.eventId != -1 && EventSystem.currentEvents.get(zombieGroup.eventId) != null) {
                EventSystem.CurrentEventData currentEventData = EventSystem.currentEvents.get(zombieGroup.eventId);
                if (currentEventData.eventName.equals(zombieGroup.eventName) && currentEventData.eventData.waves.get((int)currentEventData.waveId).zombieGroupsDeathCheck.contains(zombieGroup.parentZombieGroupName)) {
                    ++currentEventData.kills;
                }
            }
        }
    }
}

