/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  hcsmod.common.HCSUtils
 *  hcsmod.common.zombie.SpawnZone
 *  hcsmod.common.zombie.ZombieGroup
 *  hcsmod.entity.EntityZombieDayZ
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.ChatMessageComponent
 *  net.minecraft.util.DamageSource
 */
package hcsmod.server.event;

import com.google.gson.Gson;
import hcsmod.common.HCSUtils;
import hcsmod.common.zombie.SpawnZone;
import hcsmod.common.zombie.ZombieGroup;
import hcsmod.entity.EntityZombieDayZ;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.HcsServer;
import hcsmod.server.HcsTrigger;
import hcsmod.server.SPacketHandler;
import hcsmod.server.zombie.ZombieSpawner;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.DamageSource;

public class EventSystem {
    public static Config config = new Config();
    public static int groupSize = 0;
    public static final List<CurrentEventData> currentEvents = new ArrayList<CurrentEventData>();
    public static final List<HashMap<String, FinishedEventData>> finishedEvents = new ArrayList<HashMap<String, FinishedEventData>>();
    private static final Random rnd = new Random();

    public static void reloadConfig() {
        groupSize = EventSystem.config.events.size();
        for (int id = 0; id < groupSize; ++id) {
            CurrentEventData currentEvent = currentEvents.get(id);
            for (Event.Wave wave : currentEvent.eventData.waves) {
                for (String[] zombieGroupData : wave.addZombieGroups) {
                    HcsServer.zombieGroups.remove(EventSystem.getEventZombieGroupKey(id, currentEvent.eventName, zombieGroupData[0]));
                }
            }
            for (Event.Wave o : MinecraftServer.getServer().getEntityWorld().loadedEntityList) {
                if (!(o instanceof EntityZombieDayZ)) continue;
                EntityZombieDayZ entityZombieDayZ = (EntityZombieDayZ)o;
                ZombieGroup zombieGroup = entityZombieDayZ.zombieGroup;
                if (zombieGroup.eventName == null || zombieGroup.eventId != id || !zombieGroup.eventName.equals(currentEvent.eventName)) continue;
                entityZombieDayZ.x();
            }
        }
        currentEvents.clear();
        EventSystem.readConfig();
    }

    public static void readConfig() {
        int id;
        currentEvents.clear();
        finishedEvents.clear();
        Gson gson = new Gson();
        try {
            InputStreamReader in = new InputStreamReader((InputStream)new FileInputStream("hcsConfig/event.json"), StandardCharsets.UTF_8);
            Object object = null;
            try {
                config = (Config)gson.fromJson((Reader)in, Config.class);
                groupSize = EventSystem.config.events.size();
            }
            catch (Throwable throwable) {
                object = throwable;
                throw throwable;
            }
            finally {
                if (in != null) {
                    if (object != null) {
                        try {
                            ((Reader)in).close();
                        }
                        catch (Throwable throwable) {
                            ((Throwable)object).addSuppressed(throwable);
                        }
                    } else {
                        ((Reader)in).close();
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        HashSet<String> tmp = new HashSet<String>();
        for (HashMap<String, Event> data : EventSystem.config.events) {
            for (Event event : data.values()) {
                for (Event.Wave wave : event.waves) {
                    if (wave.eventTriggers == null) continue;
                    for (Event.EventTrigger eventTrigger : wave.eventTriggers) {
                        AxisAlignedBB box = AxisAlignedBB.getBoundingBox((double)eventTrigger.box.minX, (double)eventTrigger.box.minY, (double)eventTrigger.box.minZ, (double)eventTrigger.box.maxX, (double)eventTrigger.box.maxY, (double)eventTrigger.box.maxZ);
                        HcsTrigger hcsTrigger = new HcsTrigger(box, eventTrigger.commands);
                        wave.triggers.add(hcsTrigger);
                    }
                }
            }
        }
        for (id = 0; id < groupSize; ++id) {
            CurrentEventData currentEventData = new CurrentEventData(id, "");
            tmp.add(currentEventData.eventName);
            currentEvents.add(currentEventData);
        }
        for (id = 0; id < groupSize; ++id) {
            finishedEvents.add(new HashMap());
            for (String eventName : EventSystem.config.events.get(id).keySet()) {
                if (tmp.contains(eventName)) continue;
                finishedEvents.get(id).put(eventName, new FinishedEventData(eventName, EventSystem.config.events.get(id).get(eventName)));
            }
        }
    }

    public static void tick() {
        int id;
        if (!EventSystem.config.enabled) {
            return;
        }
        for (id = 0; id < groupSize; ++id) {
            CurrentEventData currentEvent = currentEvents.get(id);
            if (currentEvent.state == CurrentEventData.State.WAITING) {
                EntityPlayer ep;
                if (MinecraftServer.getServer().getTickCounter() % 5 == 0) {
                    for (Object o : MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).playerEntityList) {
                        if (!(o instanceof EntityPlayer)) continue;
                        ep = (EntityPlayer)o;
                        if (!(Math.pow(ep.u - currentEvent.eventData.x, 2.0) + Math.pow(ep.w - currentEvent.eventData.z, 2.0) <= Math.pow(currentEvent.eventData.radius, 2.0))) continue;
                        if (!EventSystem.config.disableDamage && System.currentTimeMillis() >= currentEvent.nextSirenTime) {
                            SPacketHandler.sendSound(ep, (int)currentEvent.eventData.x, (int)ep.v, (int)currentEvent.eventData.z, currentEvent.eventData.radius, EventSystem.config.soundName);
                            currentEvent.nextSirenTime = System.currentTimeMillis() + EventSystem.config.sirenCooldownMilliseconds;
                        }
                        int time = (int)(currentEvent.timeToStart - System.currentTimeMillis());
                        SPacketHandler.sendHint(ep, "evName", currentEvent.eventData.displayName, 10);
                        if (ep.u >= (double)currentEvent.eventData.damageZone[0] && ep.v >= (double)currentEvent.eventData.damageZone[1] && ep.w >= (double)currentEvent.eventData.damageZone[2] && ep.u <= (double)currentEvent.eventData.damageZone[3] && ep.v <= (double)currentEvent.eventData.damageZone[4] && ep.w <= (double)currentEvent.eventData.damageZone[5]) {
                            if (System.currentTimeMillis() > currentEvent.timeToDamage) {
                                if (!EventSystem.config.disableDamage && MinecraftServer.getServer().getTickCounter() % 20 == 0) {
                                    ep.attackEntityFrom(DamageSource.outOfWorld, currentEvent.eventData.inactiveEventDamage);
                                }
                                SPacketHandler.sendHint(ep, "evDanger", "\u041e\u043f\u0430\u0441\u043d\u043e! \u0412\u044b \u0432 \u0437\u043e\u043d\u0435 \u043f\u043e\u0440\u0430\u0436\u0435\u043d\u0438\u044f \u0438 \u043f\u043e\u043b\u0443\u0447\u0430\u0435\u0442\u0435 \u0443\u0440\u043e\u043d! \u0414\u0430\u0436\u0435 \u0432 \u043f\u0440\u043e\u0442\u0438\u0432\u043e\u0433\u0430\u0437\u0435!", 10);
                            } else {
                                int n = (int)(currentEvent.timeToDamage - System.currentTimeMillis());
                                SPacketHandler.sendHint(ep, "evTimeDamageStart", "\u0414\u043e \u043d\u0430\u0447\u0430\u043b\u0430 \u043f\u043e\u043b\u0443\u0447\u0435\u043d\u0438\u044f \u0443\u0440\u043e\u043d\u0430 " + HCSUtils.timerText((int)(n / 1000)), 10);
                            }
                        } else {
                            SPacketHandler.sendHint(ep, "evDanger", "\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435! \u0414\u043e \u043f\u043e\u044f\u0432\u043b\u0435\u043d\u0438\u044f \u043f\u0435\u0440\u0432\u043e\u0439 \u0432\u043e\u043b\u043d\u044b \u0442\u0435\u0440\u0440\u0438\u0442\u043e\u0440\u0438\u044f \u0437\u0430\u0440\u0430\u0436\u0435\u043d\u0430!\n\u0423\u0440\u043e\u043d \u043f\u0440\u043e\u0445\u043e\u0434\u0438\u0442 \u0434\u0430\u0436\u0435 \u0432 \u043f\u0440\u043e\u0442\u0438\u0432\u043e\u0433\u0430\u0437\u0435!", 10);
                        }
                        SPacketHandler.sendHint(ep, "evTimeStart", "\u0414\u043e \u043d\u0430\u0447\u0430\u043b\u0430 \u043f\u0435\u0440\u0432\u043e\u0439 \u0432\u043e\u043b\u043d\u044b " + HCSUtils.timerText((int)(time / 1000)), 10);
                    }
                }
                if (System.currentTimeMillis() < currentEvent.timeToStart) continue;
                EventSystem.addEventZombieGroup(id, currentEvent, currentEvent.eventData.waves.get(currentEvent.waveId));
                currentEvent.state = CurrentEventData.State.RUNNING;
                for (String groupName : currentEvent.eventData.waves.get((int)currentEvent.waveId).zombieGroupsDeathCheck) {
                    ZombieGroup zombieGroup = HcsServer.zombieGroups.get(EventSystem.getEventZombieGroupKey(id, currentEvent.eventName, groupName));
                    zombieGroup.lootGroup = currentEvent.eventData.waves.get((int)currentEvent.waveId).addLootGroupToActiveZombies;
                }
                for (Object o : MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).playerEntityList) {
                    if (!(o instanceof EntityPlayer)) continue;
                    ep = (EntityPlayer)o;
                    ep.a(ChatMessageComponent.createFromText((String)("\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435 \"\u00a7e" + currentEvent.eventData.displayName + " \u00a7f\u043f\u043e\u0434\u0432\u0435\u0440\u0433\u043b\u0430\u0441\u044c \u043d\u0430\u043f\u0430\u0434\u0435\u043d\u0438\u044e \u0437\u043e\u043c\u0431\u0438. \u041e\u0442\u043c\u0435\u0447\u0435\u043d\u043e \u043d\u0430 \u043a\u0430\u0440\u0442\u0435.")));
                }
                continue;
            }
            if (currentEvent.state == CurrentEventData.State.RUNNING) {
                Object wave = currentEvent.eventData.waves.get(currentEvent.waveId);
                if (System.currentTimeMillis() >= currentEvent.endTime && currentEvent.endTime != 0L) {
                    currentEvent.state = CurrentEventData.State.FINISH;
                }
                if (MinecraftServer.getServer().getTickCounter() % 5 == 0) {
                    for (Object o : MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).playerEntityList) {
                        if (!(o instanceof EntityPlayer)) continue;
                        EntityPlayer ep = (EntityPlayer)o;
                        if (!(Math.pow(ep.u - currentEvent.eventData.x, 2.0) + Math.pow(ep.w - currentEvent.eventData.z, 2.0) <= Math.pow(currentEvent.eventData.radius, 2.0))) continue;
                        SPacketHandler.sendHint(ep, "evName", currentEvent.eventData.displayName, 10);
                        SPacketHandler.sendHint(ep, "evWave", "\u0412\u043e\u043b\u043d\u0430 " + (currentEvent.waveId + 1) + "/" + currentEvent.eventData.waves.size(), 10);
                        SPacketHandler.sendHint(ep, "evKills", ((Event.Wave)wave).text + " " + currentEvent.kills + "/" + ((Event.Wave)wave).deathsToNextWave, 10);
                        if (currentEvent.endTime >= System.currentTimeMillis()) {
                            int n = (int)(currentEvent.endTime - System.currentTimeMillis());
                            SPacketHandler.sendHint(ep, "evTimeEnd", "\u0412\u0440\u0435\u043c\u044f \u043d\u0430 \u0437\u0430\u0447\u0438\u0441\u0442\u043a\u0443 \u0432\u043e\u043b\u043d\u044b " + HCSUtils.timerText((int)(n / 1000)), 10);
                        }
                        if (!(ep.u >= (double)currentEvent.eventData.damageZone[0]) || !(ep.v >= (double)currentEvent.eventData.damageZone[1]) || !(ep.w >= (double)currentEvent.eventData.damageZone[2]) || !(ep.u <= (double)currentEvent.eventData.damageZone[3]) || !(ep.v <= (double)currentEvent.eventData.damageZone[4]) || !(ep.w <= (double)currentEvent.eventData.damageZone[5])) continue;
                        ExtendedPlayer extendedPlayer = ExtendedPlayer.server((EntityPlayer)ep);
                        if (!EventSystem.config.disableDamage && MinecraftServer.getServer().getTickCounter() % 20 == 0) {
                            boolean needDamage = false;
                            if (extendedPlayer.inventory.inventoryStacks[1] == null) {
                                needDamage = true;
                            } else {
                                NBTTagCompound tag = extendedPlayer.inventory.inventoryStacks[1].getTagCompound();
                                if (tag != null && tag.getLong("breakTime") < System.currentTimeMillis()) {
                                    needDamage = true;
                                }
                            }
                            if (needDamage) {
                                ep.attackEntityFrom(DamageSource.outOfWorld, currentEvent.eventData.activeEventDamage);
                            }
                        }
                        if (extendedPlayer.inventory.inventoryStacks[1] != null) continue;
                        SPacketHandler.sendHint(ep, "evDanger", "\u0413\u0430\u0437\u043e\u0432\u043e\u0435 \u0437\u0430\u0440\u0430\u0436\u0435\u043d\u0438\u0435! \u0411\u0435\u0437 \u043f\u0440\u043e\u0442\u0438\u0432\u043e\u0433\u0430\u0437\u0430 \u0432\u044b \u043f\u043e\u043b\u0443\u0447\u0430\u0435\u0442\u0435 \u0443\u0440\u043e\u043d!\n\u041f\u0440\u043e\u0442\u0438\u0432\u043e\u0433\u0430\u0437 \u043c\u043e\u0436\u043d\u043e \u043a\u0443\u043f\u0438\u0442\u044c \u043d\u0430 \u0441\u0435\u0439\u0444\u0437\u043e\u043d\u0435.", 15);
                    }
                }
                if (((Event.Wave)wave).triggers != null) {
                    for (HcsTrigger hcsTrigger : currentEvent.eventData.waves.get((int)currentEvent.waveId).triggers) {
                        if (hcsTrigger == null) continue;
                        hcsTrigger.tick();
                    }
                }
                if (currentEvent.kills < currentEvent.eventData.waves.get((int)currentEvent.waveId).deathsToNextWave) continue;
                if (currentEvent.waveId >= currentEvent.eventData.waves.size() - 1) {
                    currentEvent.state = CurrentEventData.State.FINISH;
                    continue;
                }
                ++currentEvent.waveId;
                wave = currentEvent.eventData.waves.get(currentEvent.waveId);
                currentEvent.kills = 0;
                for (Object o : MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).playerEntityList) {
                    if (!(o instanceof EntityPlayer)) continue;
                    EntityPlayer ep = (EntityPlayer)o;
                    if (!(Math.pow(ep.u - currentEvent.eventData.x, 2.0) + Math.pow(ep.w - currentEvent.eventData.z, 2.0) <= Math.pow(currentEvent.eventData.radius, 2.0))) continue;
                    ep.a(ChatMessageComponent.createFromText((String)("\u00a7a\u0412\u043e\u043b\u043d\u0430 " + (currentEvent.waveId + 1) + "/" + currentEvent.eventData.waves.size())));
                }
                for (ZombieGroup zombieGroup : HcsServer.zombieGroups.values()) {
                    if (zombieGroup.eventId != id || !zombieGroup.eventName.equals(currentEvent.eventName)) continue;
                    zombieGroup.lootGroup = "none";
                }
                for (Iterator<Object> iterator : currentEvent.eventData.waves.get((int)currentEvent.waveId).clearSpawnCooldownGroups) {
                    ZombieGroup zombieGroup = HcsServer.zombieGroups.get(EventSystem.getEventZombieGroupKey(id, currentEvent.eventName, iterator));
                    if (!ZombieSpawner.zombieSpawnCooldowns.containsKey((Object)zombieGroup)) continue;
                    ZombieSpawner.zombieSpawnCooldowns.get((Object)zombieGroup).clear();
                }
                for (Iterator<Object> iterator : currentEvent.eventData.waves.get((int)currentEvent.waveId).disableSpawnGroups) {
                    HcsServer.zombieGroups.remove(EventSystem.getEventZombieGroupKey(id, currentEvent.eventName, iterator));
                }
                for (Iterator<Object> iterator : currentEvent.eventData.waves.get((int)currentEvent.waveId).removeZombieGroups) {
                    for (Object o : MinecraftServer.getServer().getEntityWorld().loadedEntityList) {
                        if (!(o instanceof EntityZombieDayZ)) continue;
                        EntityZombieDayZ entityZombieDayZ = (EntityZombieDayZ)o;
                        ZombieGroup zombieGroup = entityZombieDayZ.zombieGroup;
                        if (zombieGroup.eventId != id || !zombieGroup.eventName.equals(currentEvent.eventName) || !zombieGroup.parentZombieGroupName.equals(iterator)) continue;
                        entityZombieDayZ.x();
                    }
                }
                EventSystem.addEventZombieGroup(id, currentEvent, currentEvent.eventData.waves.get(currentEvent.waveId));
                for (String groupName : currentEvent.eventData.waves.get((int)currentEvent.waveId).zombieGroupsDeathCheck) {
                    ZombieGroup zombieGroup = HcsServer.zombieGroups.get(EventSystem.getEventZombieGroupKey(id, currentEvent.eventName, groupName));
                    zombieGroup.lootGroup = currentEvent.eventData.waves.get((int)currentEvent.waveId).addLootGroupToActiveZombies;
                }
                if (((Event.Wave)wave).timeToEventEndSeconds <= 0) continue;
                currentEvent.endTime = System.currentTimeMillis() + (long)((Event.Wave)wave).timeToEventEndSeconds * 1000L;
                continue;
            }
            if (currentEvent.state != CurrentEventData.State.FINISH) continue;
            for (Object o : MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).playerEntityList) {
                if (!(o instanceof EntityPlayer)) continue;
                EntityPlayer ep = (EntityPlayer)o;
                if (System.currentTimeMillis() >= currentEvent.endTime && currentEvent.endTime != 0L) {
                    ep.a(ChatMessageComponent.createFromText((String)("\"\u00a7e" + currentEvent.eventData.displayName + "\u00a7f, \u0432\u0440\u0435\u043c\u044f \u043d\u0430 \u0437\u0430\u0447\u0438\u0441\u0442\u043a\u0443 \u0438\u0441\u0442\u0435\u043a\u043b\u043e")));
                    continue;
                }
                ep.a(ChatMessageComponent.createFromText((String)("\"\u00a7e" + currentEvent.eventData.displayName + " \u00a7f\u0437\u0430\u0447\u0438\u0449\u0435\u043d\u0430")));
            }
            for (Event.Wave wave : currentEvent.eventData.waves) {
                for (String[] zombieGroupData : wave.addZombieGroups) {
                    HcsServer.zombieGroups.remove(EventSystem.getEventZombieGroupKey(id, currentEvent.eventName, zombieGroupData[0]));
                }
            }
            for (Event.Wave o : MinecraftServer.getServer().getEntityWorld().loadedEntityList) {
                if (!(o instanceof EntityZombieDayZ)) continue;
                EntityZombieDayZ entityZombieDayZ = (EntityZombieDayZ)o;
                ZombieGroup zombieGroup = entityZombieDayZ.zombieGroup;
                if (zombieGroup.eventId != id || !zombieGroup.eventName.equals(currentEvent.eventName)) continue;
                entityZombieDayZ.x();
            }
            finishedEvents.get(id).put(currentEvent.eventName, new FinishedEventData(currentEvent.eventName, currentEvent.eventData));
            CurrentEventData newEvent = new CurrentEventData(id, currentEvent.eventName);
            currentEvents.set(id, newEvent);
            finishedEvents.get(id).remove(newEvent.eventName);
            int time = (int)(newEvent.timeToStart - System.currentTimeMillis());
            for (Object o : MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).playerEntityList) {
                if (!(o instanceof EntityPlayer)) continue;
                ((EntityPlayer)o).a(ChatMessageComponent.createFromText((String)("\u00a77[\u00a7c\u0418\u043d\u0444\u043e\u0440\u043c\u0430\u0442\u043e\u0440\u00a77] \u00a7f\u0427\u0435\u0440\u0435\u0437 " + HCSUtils.timerText((int)(time / 1000)) + " \u0432 \"\u00a7e" + newEvent.eventData.displayName + " \u043f\u0440\u0438\u0431\u0443\u0434\u0435\u0442 \u0442\u043e\u043b\u043f\u0430 \u0432\u043e\u043e\u0440\u0443\u0436\u0451\u043d\u043d\u044b\u0445 \u0437\u043e\u043c\u0431\u0438. \u041e\u0442\u043c\u0435\u0447\u0435\u043d\u043e \u043d\u0430 \u043a\u0430\u0440\u0442\u0435.")));
            }
            currentEvents.set(id, newEvent);
        }
        for (id = 0; id < groupSize; ++id) {
            for (String eventName : finishedEvents.get(id).keySet()) {
                FinishedEventData finishedEventData = finishedEvents.get(id).get(eventName);
                if (finishedEventData.state == FinishedEventData.State.WAITING) {
                    if (System.currentTimeMillis() <= finishedEventData.damageStartTime) {
                        if (MinecraftServer.getServer().getTickCounter() % 5 != 0) continue;
                        for (Object o : MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).playerEntityList) {
                            if (!(o instanceof EntityPlayer)) continue;
                            EntityPlayer entityPlayer = (EntityPlayer)o;
                            if (!(Math.pow(entityPlayer.u - finishedEventData.eventData.x, 2.0) + Math.pow(entityPlayer.w - finishedEventData.eventData.z, 2.0) <= Math.pow(finishedEventData.eventData.radius, 2.0))) continue;
                            if (!EventSystem.config.disableDamage && System.currentTimeMillis() >= finishedEventData.nextSirenTime) {
                                SPacketHandler.sendSound(entityPlayer, (int)finishedEventData.eventData.x, (int)entityPlayer.v, (int)finishedEventData.eventData.z, finishedEventData.eventData.radius, EventSystem.config.soundName);
                                finishedEventData.nextSirenTime = System.currentTimeMillis() + EventSystem.config.sirenCooldownMilliseconds;
                            }
                            SPacketHandler.sendHint(entityPlayer, "evEnd", "\u0422\u043e\u0447\u043a\u0430 \"" + finishedEventData.eventData.displayName + "\" \u0437\u0430\u0447\u0438\u0449\u0435\u043d\u0430.\n\u0418\u0449\u0438\u0442\u0435 \u043d\u043e\u0432\u044b\u0435 \u043d\u0430 \u043a\u0430\u0440\u0442\u0435.", 10);
                            int time = (int)(finishedEventData.damageStartTime - System.currentTimeMillis());
                            SPacketHandler.sendHint(entityPlayer, "evTimeDamage", "\u0417\u0430\u0440\u0430\u0436\u0435\u043d\u0438\u0435 \u044d\u0442\u043e\u0433\u043e \u043f\u0435\u0440\u0438\u043c\u0435\u0442\u0440\u0430 \u0447\u0435\u0440\u0435\u0437 " + HCSUtils.timerText((int)(time / 1000)), 10);
                        }
                        continue;
                    }
                    finishedEventData.state = FinishedEventData.State.DAMAGE;
                    continue;
                }
                if (finishedEventData.state != FinishedEventData.State.DAMAGE || MinecraftServer.getServer().getTickCounter() % 10 != 0) continue;
                for (Object o : MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).playerEntityList) {
                    if (!(o instanceof EntityPlayer)) continue;
                    EntityPlayer entityPlayer = (EntityPlayer)o;
                    if (!(Math.pow(entityPlayer.u - finishedEventData.eventData.x, 2.0) + Math.pow(entityPlayer.w - finishedEventData.eventData.z, 2.0) <= Math.pow(finishedEventData.eventData.radius, 2.0))) continue;
                    if (!EventSystem.config.disableDamage && System.currentTimeMillis() >= finishedEventData.nextSirenTime) {
                        SPacketHandler.sendSound(entityPlayer, (int)finishedEventData.eventData.x, (int)entityPlayer.v, (int)finishedEventData.eventData.z, finishedEventData.eventData.radius, EventSystem.config.soundName);
                        finishedEventData.nextSirenTime = System.currentTimeMillis() + EventSystem.config.sirenCooldownMilliseconds;
                    }
                    SPacketHandler.sendHint(entityPlayer, "evName", "\u0418\u0432\u0435\u043d\u0442 \"" + finishedEventData.eventData.displayName + "\" \u043d\u0435 \u0430\u043a\u0442\u0438\u0432\u0435\u043d.", 15);
                    if (entityPlayer.u >= (double)finishedEventData.eventData.damageZone[0] && entityPlayer.v >= (double)finishedEventData.eventData.damageZone[1] && entityPlayer.w >= (double)finishedEventData.eventData.damageZone[2] && entityPlayer.u <= (double)finishedEventData.eventData.damageZone[3] && entityPlayer.v <= (double)finishedEventData.eventData.damageZone[4] && entityPlayer.w <= (double)finishedEventData.eventData.damageZone[5]) {
                        if (!EventSystem.config.disableDamage && MinecraftServer.getServer().getTickCounter() % 20 == 0) {
                            entityPlayer.attackEntityFrom(DamageSource.outOfWorld, finishedEventData.eventData.inactiveEventDamage);
                        }
                        SPacketHandler.sendHint(entityPlayer, "evDanger", "\u041e\u043f\u0430\u0441\u043d\u043e\u0441\u0442\u044c! \u0412\u044b \u0432 \u0437\u043e\u043d\u0435 \u0437\u0430\u0440\u0430\u0436\u0435\u043d\u0438\u044f \u0438 \u043f\u043e\u043b\u0443\u0447\u0430\u0435\u0442\u0435 \u0443\u0440\u043e\u043d! \u0414\u0430\u0436\u0435 \u0432 \u043f\u0440\u043e\u0442\u0438\u0432\u043e\u0433\u0430\u0437\u0435!", 15);
                        continue;
                    }
                    SPacketHandler.sendHint(entityPlayer, "evDanger", "\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435! \u041f\u0435\u0440\u0438\u043c\u0435\u0442\u0440 \u0438\u0432\u0435\u043d\u0442\u0430 \u0437\u0430\u0440\u0430\u0436\u0435\u043d, \u043d\u0435 \u043f\u043e\u0441\u0435\u0449\u0430\u0439\u0442\u0435 \u0435\u0433\u043e!\n\u0423\u0440\u043e\u043d \u0431\u0443\u0434\u0435\u0442 \u0434\u0430\u0436\u0435 \u0441\u043a\u0432\u043e\u0437\u044c \u043f\u0440\u043e\u0442\u0438\u0432\u043e\u0433\u0430\u0437!", 15);
                }
            }
        }
    }

    private static void addEventZombieGroup(int id, CurrentEventData currentEvent, Event.Wave wave) {
        for (String[] zombieGroupData : wave.addZombieGroups) {
            ZombieGroup eventZombieGroup = new ZombieGroup(HcsServer.zombieGroups.get(zombieGroupData[0]));
            eventZombieGroup.spawnZones.addAll(Arrays.asList(Arrays.copyOfRange(zombieGroupData, 1, zombieGroupData.length)));
            eventZombieGroup.eventId = id;
            eventZombieGroup.eventName = currentEvent.eventName;
            eventZombieGroup.parentZombieGroupName = zombieGroupData[0];
            HashMap tmp = new HashMap();
            ZombieSpawner.zombieSpawnCooldowns.put(eventZombieGroup, tmp);
            for (String zoneName : eventZombieGroup.spawnZones) {
                if (!HcsServer.spawnZones.containsKey(zoneName)) continue;
                SpawnZone zone = HcsServer.spawnZones.get(zoneName);
                eventZombieGroup.outdoorChunks.addAll(zone.outdoorChunks);
                eventZombieGroup.indoorChunks.putAll(zone.indoorChunks);
            }
            HcsServer.zombieGroups.put(EventSystem.getEventZombieGroupKey(id, currentEvent.eventName, zombieGroupData[0]), eventZombieGroup);
        }
    }

    private static String getRandomEventExclude(int id, String exclude) {
        ArrayList<String> data = new ArrayList<String>();
        for (String name : EventSystem.config.events.get(id).keySet()) {
            if (EventSystem.config.events.get(id).size() == 1) {
                return name;
            }
            if (name.equals(exclude)) continue;
            data.add(name);
        }
        return (String)data.get(rnd.nextInt(data.size()));
    }

    private static String getEventZombieGroupKey(int id, String event, String groupName) {
        return id + "_" + event + "_" + groupName;
    }

    public static class FinishedEventData {
        public State state;
        public long damageStartTime;
        public Event eventData;
        public String eventName;
        public long nextSirenTime;

        public FinishedEventData(String eventName, Event eventData) {
            this.eventName = eventName;
            this.eventData = eventData;
            this.state = State.WAITING;
            this.damageStartTime = System.currentTimeMillis() + (long)eventData.damageStartTime * 1000L;
        }

        public static enum State {
            WAITING,
            DAMAGE;

        }
    }

    public static class CurrentEventData {
        public State state = State.WAITING;
        public String eventName;
        public Event eventData;
        public int kills;
        public int waveId;
        public long timeToStart;
        public long timeToDamage;
        public long nextSirenTime;
        public long endTime;

        public CurrentEventData(int id, String oldEventName) {
            this.eventName = EventSystem.getRandomEventExclude(id, oldEventName);
            this.eventData = EventSystem.config.events.get(id).get(this.eventName);
            this.timeToStart = System.currentTimeMillis() + (long)(rnd.nextInt(this.eventData.timeToEventStart[1] - this.eventData.timeToEventStart[0] + 1) + this.eventData.timeToEventStart[0]) * 1000L;
            this.timeToDamage = System.currentTimeMillis() + (long)this.eventData.damageStartTime * 1000L;
            this.kills = 0;
            this.waveId = 0;
        }

        public static enum State {
            WAITING,
            RUNNING,
            FINISH;

        }
    }

    public static class Event {
        int[] timeToEventStart;
        double x;
        double z;
        int radius;
        float activeEventDamage;
        float inactiveEventDamage;
        public int[] damageZone;
        public String displayName;
        public List<Wave> waves = new ArrayList<Wave>();
        public int damageStartTime;

        public static class Box {
            public double minX;
            public double minY;
            public double minZ;
            public double maxX;
            public double maxY;
            public double maxZ;
        }

        public static class EventTrigger {
            Box box;
            String[] commands;
        }

        public static class Wave {
            String text;
            ArrayList<String[]> addZombieGroups = new ArrayList();
            HashSet<String> zombieGroupsDeathCheck = new HashSet();
            String addLootGroupToActiveZombies;
            public int deathsToNextWave;
            ArrayList<HcsTrigger> triggers = new ArrayList();
            EventTrigger[] eventTriggers = null;
            String[] disableSpawnGroups = new String[0];
            String[] removeZombieGroups = new String[0];
            String[] clearSpawnCooldownGroups = new String[0];
            int timeToEventEndSeconds;
        }
    }

    public static class Config {
        public boolean enabled;
        public ArrayList<HashMap<String, Event>> events = new ArrayList();
        boolean disableDamage;
        long sirenCooldownMilliseconds;
        String soundName;
    }
}

