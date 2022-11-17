/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  co.uk.flansmods.common.FlansModPlayerData
 *  co.uk.flansmods.common.FlansModPlayerHandler
 *  co.uk.flansmods.common.guns.EntityBullet
 *  co.uk.flansmods.common.guns.ItemGun
 *  cpw.mods.fml.common.network.IPacketHandler
 *  cpw.mods.fml.common.network.Player
 *  hcsmod.HcsInteract
 *  hcsmod.HcsMod
 *  hcsmod.common.WorldMarker
 *  hcsmod.common.zombie.IndoorChunkInfo
 *  hcsmod.common.zombie.IndoorLocation
 *  hcsmod.common.zombie.OutdoorLocation
 *  hcsmod.common.zombie.SpawnZone
 *  hcsmod.common.zombie.ZombieGroup
 *  hcsmod.cunches.IVehicle
 *  hcsmod.entity.EntityCorpse
 *  hcsmod.entity.EntityHouseCommon
 *  hcsmod.entity.EntityPalatka
 *  hcsmod.flashlight.Flashlight
 *  hcsmod.player.ContainerExtended
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockDoor
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.inventory.ContainerChest
 *  net.minecraft.inventory.IInventory
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.INetworkManager
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.Packet250CustomPayload
 *  net.minecraft.network.packet.Packet53BlockChange
 *  net.minecraft.network.packet.Packet61DoorChange
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.ChatMessageComponent
 *  net.minecraft.world.IBlockAccess
 *  net.minecraft.world.World
 *  net.minecraftforge.event.Event$Result
 *  net.minecraftforge.event.ForgeEventFactory
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$Action
 *  vintarz.core.VRP
 *  vintarz.core.VSP
 */
package hcsmod.server;

import co.uk.flansmods.common.FlansModPlayerData;
import co.uk.flansmods.common.FlansModPlayerHandler;
import co.uk.flansmods.common.guns.EntityBullet;
import co.uk.flansmods.common.guns.ItemGun;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import hcsmod.HcsInteract;
import hcsmod.HcsMod;
import hcsmod.clans.server.ClansServer;
import hcsmod.common.WorldMarker;
import hcsmod.common.zombie.IndoorChunkInfo;
import hcsmod.common.zombie.IndoorLocation;
import hcsmod.common.zombie.OutdoorLocation;
import hcsmod.common.zombie.SpawnZone;
import hcsmod.common.zombie.ZombieGroup;
import hcsmod.cunches.IVehicle;
import hcsmod.entity.EntityCorpse;
import hcsmod.entity.EntityHouseCommon;
import hcsmod.entity.EntityPalatka;
import hcsmod.flashlight.Flashlight;
import hcsmod.player.ContainerExtended;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.ChemProtectSuitData;
import hcsmod.server.EntityHouseServer;
import hcsmod.server.ExtendedStorage;
import hcsmod.server.FlansVehicleHelper;
import hcsmod.server.HcsServer;
import hcsmod.server.ItemPickupUtil;
import hcsmod.server.Location;
import hcsmod.server.MapMarkersServer;
import hcsmod.server.RandomSpawn;
import hcsmod.server.airdrop.AirdropSystem;
import hcsmod.server.event.EventSystem;
import hcsmod.server.storage.InvLoadCallback;
import hcsmod.server.storage.LocationData;
import hcsmod.server.storage.StorageGroup;
import hcsmod.server.storage.StorageInventory;
import hcsmod.server.storage.StorageUtils;
import hcsmod.server.zones.PvpSystem;
import hcsmod.server.zones.PvpZoneData;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet53BlockChange;
import net.minecraft.network.packet.Packet61DoorChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import vintarz.core.VRP;
import vintarz.core.VSP;

public class SPacketHandler
implements IPacketHandler {
    public void onPacketData(INetworkManager mgr, Packet250CustomPayload pt, Player plr) {
        VRP in;
        block136: {
            byte type;
            EntityPlayerMP p;
            block135: {
                p = (EntityPlayerMP)plr;
                if (!p.T()) {
                    return;
                }
                in = new VRP(pt);
                type = in.type;
                if (type == 0) {
                    this.handleOpenInventory((EntityPlayer)p);
                } else if (type == 1) {
                    HcsServer.toggleFlashLight((EntityPlayer)p);
                    VSP os = new VSP(19, "HCSMOD");
                    try {
                        os.writeInt(Flashlight.getFlashlightTime((boolean)false));
                        os.send((EntityPlayer)p);
                    }
                    catch (IOException iOException) {}
                } else if (type == 2) {
                    try {
                        Entity w;
                        int eiID = in.readInt();
                        if (eiID == -1) {
                            ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
                            ItemStack is = ep.inventory.inventoryStacks[2];
                            if (is != null) {
                                is.getItem().onItemRightClick(is, p.q, (EntityPlayer)p);
                                ep.inventory.inventoryStacks[2] = null;
                            }
                        }
                        if ((w = p.q.getEntityByID(eiID)) == null || w.isDead) break block135;
                        if (w instanceof EntityCorpse && w.getDistanceSq(p.u, p.v + (double)p.getEyeHeight(), p.w) <= 9.0) {
                            EntityCorpse corpse = (EntityCorpse)w;
                            p.displayGUIChest((IInventory)corpse);
                        } else if (w instanceof IVehicle) {
                            IVehicle v = (IVehicle)w;
                            if (p.bp instanceof ContainerChest) {
                                ContainerChest cc = (ContainerChest)p.bp;
                                if (cc.lowerChestInventory instanceof EntityHouseServer) {
                                    EntityHouseServer entityHouseServer = (EntityHouseServer)cc.lowerChestInventory;
                                    if (!w.isDead && !v.hasDriver() && (double)w.getDistanceToEntity((Entity)entityHouseServer) < 24.0) {
                                        entityHouseServer.storeVehicle(v, (EntityPlayer)p);
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (type == 3) {
                    try {
                        p.closeScreen();
                        int eiID = in.readInt();
                        World w = p.q;
                        if (w.getEntityByID(eiID) instanceof EntityHouseServer) {
                            EntityHouseServer ei = (EntityHouseServer)w.getEntityByID(eiID);
                            ei.checkUserAndUse((EntityPlayer)p, in.readUTF());
                        }
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (type == 4) {
                    if (p.bp instanceof ContainerChest) {
                        ContainerChest cc = (ContainerChest)p.bp;
                        if (cc.lowerChestInventory instanceof EntityHouseServer) {
                            EntityHouseServer h = (EntityHouseServer)cc.lowerChestInventory;
                            try {
                                h.spawnVehicle(in.readInt(), (EntityPlayer)p);
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (type == 5) {
                    try {
                        String spawnpoint = in.readUTF();
                        if (HcsServer.isHarxcoreServer && spawnpoint.isEmpty()) {
                            p.playerNetServerHandler.kickPlayerFromServer("\u0421\u043b\u0443\u0447\u0430\u0439\u043d\u044b\u0439 \u0441\u043f\u0430\u0443\u043d \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d \u043d\u0430 DayZ Hardcore!");
                            return;
                        }
                        if (!(p.u * p.u + (p.v - 1.0) * (p.v - 1.0) + p.w * p.w <= 25.0)) break block135;
                        ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
                        RandomSpawn rs = RandomSpawn.get(spawnpoint);
                        if (rs != null) {
                            Long time = (Long)ep.spawnCooldowns.get(spawnpoint);
                            if (time != null && time <= System.currentTimeMillis()) {
                                time = null;
                                ep.spawnCooldowns.remove(spawnpoint);
                            }
                            if (time == null) {
                                rs.respawnPlayer((EntityPlayer)p);
                                if (HcsServer.hcsConfig.respawnPvPImmune > 0) {
                                    ExtendedStorage.get((ExtendedPlayer)ep).pvePlayer.setTemporaryPvPImmune((EntityPlayer)p, HcsServer.hcsConfig.respawnPvPImmune);
                                }
                            }
                        }
                    }
                    catch (IOException spawnpoint) {}
                } else if (type == 6) {
                    try {
                        int eid = in.readInt();
                        boolean shift = in.readBoolean();
                        Entity e = p.q.getEntityByID(eid);
                        if (e != null && !e.isDead && p.bn.getItemStack() == null && e instanceof EntityItem && p.E.expand(3.5, 1.5, 3.5).intersectsWith(e.boundingBox)) {
                            EntityItem ei = (EntityItem)e;
                            if (!shift) {
                                p.bn.setItemStack(ei.getEntityItem());
                                ei.x();
                                boolean bl = p.playerInventoryBeingManipulated;
                                p.playerInventoryBeingManipulated = false;
                                p.updateHeldItem();
                                p.playerInventoryBeingManipulated = bl;
                            } else {
                                ItemPickupUtil.pickupItem(ei, (EntityPlayer)p);
                                p.bp.detectAndSendChanges();
                            }
                        }
                    }
                    catch (IOException eid) {
                        // empty catch block
                    }
                }
            }
            if (type == 7) {
                try {
                    ClansServer.handleClansMessage(p, (DataInputStream)in);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (type == 8) {
                try {
                    int action = in.readUnsignedByte();
                    if (action == 0) {
                        if (!(p.bp instanceof ContainerExtended)) {
                            p.bp = new ContainerExtended((EntityPlayer)p, ExtendedPlayer.server((EntityPlayer)p).inventory);
                        }
                    } else if (action == 1) {
                        p.bp = p.bo;
                    }
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            } else if (type == 9) {
                if (p.o instanceof EntityHouseServer) {
                    EntityHouseServer house = (EntityHouseServer)p.o;
                    boolean enable = !house.isStoveEnabled();
                    house.setStoveEnabled(enable);
                    if (enable && !house.isStoveCharged()) {
                        house.checkupTime = System.currentTimeMillis() - 1L;
                    }
                }
            } else if (type == 11) {
                ItemStack stack = p.by();
                FlansModPlayerData fmpd = FlansModPlayerHandler.getPlayerData((EntityPlayer)p);
                if (fmpd != null && fmpd.shootTime == 0 && stack != null && stack.getItem() instanceof ItemGun) {
                    ((ItemGun)stack.getItem()).unload(stack, (EntityPlayer)p);
                }
            } else if (type == 12) {
                byte id = 0;
                try {
                    id = in.readByte();
                }
                catch (IOException fmpd) {
                    // empty catch block
                }
                long time = System.currentTimeMillis();
                ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
                if (id != 0 && ep.logoutTime == 0L) {
                    ep.logoutTime = time + 20000L;
                    ep.logoutId = id;
                    ep.logoutX = p.u;
                    ep.logoutY = p.v;
                    ep.logoutZ = p.w;
                    ep.logoutP = p.B;
                    ep.logoutYw = p.A;
                    ep.logoutH = p.P;
                } else if (id == 0 || ep.logoutId != id || ep.logoutX != p.u || ep.logoutY != p.v || ep.logoutZ != p.w || ep.logoutP != p.B || ep.logoutYw != p.A || ep.logoutH != p.P) {
                    ep.logoutTime = 0L;
                } else if (ep.logoutTime <= time) {
                    p.playerNetServerHandler.kickPlayerFromServer(null);
                } else {
                    VSP vSP = new VSP(13, "HCSMOD");
                    try {
                        vSP.writeByte((int)ep.logoutId);
                        vSP.writeByte((int)((ep.logoutTime - time) / 1000L) + 1);
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                    vSP.send((EntityPlayer)p);
                }
            } else if (type == 15) {
                if (p.o != null) {
                    return;
                }
                try {
                    short blockX = in.readShort();
                    int blockY = in.readUnsignedByte();
                    short blockZ = in.readShort();
                    byte blockF = in.readByte();
                    double d = (double)blockX - p.u;
                    double y = (double)blockY - (p.v + (double)p.getEyeHeight());
                    double z = (double)blockZ - p.w;
                    Block block = Block.blocksList[p.q.getBlockId((int)blockX, blockY, (int)blockZ)];
                    if (!(d * d + y * y + z * z <= 16.0) || !HcsInteract.$((Block)block)) break block136;
                    HcsInteract.$ = true;
                    PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract((EntityPlayer)p, (PlayerInteractEvent.Action)PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, (int)blockX, (int)blockY, (int)blockZ, (int)blockF);
                    HcsInteract.$ = false;
                    if (event.isCanceled()) {
                        p.playerNetServerHandler.sendPacketToPlayer((Packet)new Packet53BlockChange((int)blockX, blockY, (int)blockZ, p.q));
                        if (block instanceof BlockDoor) {
                            if (Block.blocksList[p.q.getBlockId((int)blockX, blockY + 1, (int)blockZ)] instanceof BlockDoor) {
                                p.playerNetServerHandler.sendPacketToPlayer((Packet)new Packet53BlockChange((int)blockX, blockY + 1, (int)blockZ, p.q));
                            }
                            if (Block.blocksList[p.q.getBlockId((int)blockX, blockY - 1, (int)blockZ)] instanceof BlockDoor) {
                                p.playerNetServerHandler.sendPacketToPlayer((Packet)new Packet53BlockChange((int)blockX, blockY - 1, (int)blockZ, p.q));
                            }
                        }
                    } else if (!event.isCanceled() && event.useBlock != Event.Result.DENY) {
                        if (block.getClass() == BlockDoor.class) {
                            int i1 = ((BlockDoor)block).getFullMetadata((IBlockAccess)p.q, (int)blockX, blockY, (int)blockZ);
                            int j1 = i1 & 7;
                            j1 ^= 4;
                            if ((i1 & 8) == 0) {
                                p.q.setBlockMetadataWithNotify((int)blockX, blockY, (int)blockZ, j1, 2);
                            } else {
                                p.q.setBlockMetadataWithNotify((int)blockX, blockY - 1, (int)blockZ, j1, 2);
                            }
                            MinecraftServer.getServer().getConfigurationManager().sendToAllNearExcept((EntityPlayer)p, (double)blockX, (double)blockY, (double)blockZ, 64.0, p.q.provider.dimensionId, (Packet)new Packet61DoorChange(1003, (int)blockX, blockY, (int)blockZ, 0, false));
                        } else {
                            block.onBlockActivated(p.q, (int)blockX, blockY, (int)blockZ, (EntityPlayer)p, (int)blockF, (float)blockX, (float)blockY, (float)blockZ);
                        }
                        return;
                    }
                }
                catch (IOException blockX) {}
            } else if (type == 16) {
                try {
                    double size;
                    Entity entity = p.q.getEntityByID(in.readInt());
                    if (p.o != null && (p.o != entity || !(entity instanceof EntityPalatka) && !(entity instanceof EntityHouseCommon))) break block136;
                    double x = (entity.boundingBox.minX + entity.boundingBox.maxX) / 2.0;
                    double y = (entity.boundingBox.minY + entity.boundingBox.maxY) / 2.0;
                    double z = (entity.boundingBox.minZ + entity.boundingBox.maxZ) / 2.0;
                    if ((x -= p.u) * x + (y -= p.v + (double)p.getEyeHeight()) * y + (z -= p.w) * z <= (size = (double)(entity.width * entity.width + entity.height * entity.height + 9.0f)) && HcsInteract.$((Entity)entity)) {
                        HcsInteract.$ = true;
                        p.p(entity);
                        HcsInteract.$ = false;
                    }
                }
                catch (IOException entity) {}
            } else if (type == 17) {
                try {
                    HcsServer.meleeAttack(p, in.readUnsignedByte());
                }
                catch (IOException entity) {}
            } else if (type == 18) {
                FlansVehicleHelper.handleControl(p, in);
            } else if (type == 19) {
                if (!HcsServer.storageEnabled) {
                    p.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u00a7\u0441\u041b\u0438\u0447\u043d\u043e\u0435 \u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435 \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u043e \u043d\u0430 \u044d\u0442\u043e\u043c \u0441\u0435\u0440\u0432\u0435\u0440\u0435"));
                    p.closeScreen();
                    return;
                }
                ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
                StorageGroup sg = HcsServer.getPlayerStorageGroup(p.c_());
                if (sg.OpenLocations.size() != 0) {
                    boolean flag = false;
                    for (LocationData locationData : sg.OpenLocations) {
                        if (!(p.u >= (double)locationData.x1) || !(p.u <= (double)locationData.x2) || !(p.v >= (double)locationData.y1) || !(p.v <= (double)locationData.y2) || !(p.w >= (double)locationData.z1) || !(p.w <= (double)locationData.z2)) continue;
                        flag = true;
                        break;
                    }
                    if (!flag) {
                        p.sendChatToPlayer(ChatMessageComponent.createFromText((String)sg.LocationMessage));
                        return;
                    }
                }
                boolean inHouse = p.o instanceof EntityHouseServer;
                StorageUtils.checkInventoryChange((EntityPlayer)p);
                PvpZoneData pvpZone = PvpSystem.isPlayerInside((EntityPlayer)p);
                int n = pvpZone == null ? sg.cooldown : Math.max(sg.cooldown, pvpZone.storagePvPDelay);
                int walkDelay = pvpZone == null ? sg.walkDelay : Math.max(sg.walkDelay, pvpZone.storageWalkDelay);
                int inventoryDelay = pvpZone == null ? sg.inventoryDelay : Math.max(sg.inventoryDelay, pvpZone.storageInventoryDelay);
                int messageId = -1;
                long time = 0L;
                if (ExtendedStorage.get((ExtendedPlayer)ep).lastPVPtime + (long)n * 1000L >= System.currentTimeMillis()) {
                    time = ExtendedStorage.get((ExtendedPlayer)ep).lastPVPtime + (long)n * 1000L - System.currentTimeMillis();
                    messageId = 0;
                }
                if (ExtendedStorage.get((ExtendedPlayer)ep).lastWalkTime + (long)walkDelay * 1000L - System.currentTimeMillis() > time) {
                    time = ExtendedStorage.get((ExtendedPlayer)ep).lastWalkTime + (long)walkDelay * 1000L - System.currentTimeMillis();
                    messageId = 1;
                }
                if (ExtendedStorage.get((ExtendedPlayer)ep).lastInventoryChangeTime + (long)inventoryDelay * 1000L - System.currentTimeMillis() > time) {
                    time = ExtendedStorage.get((ExtendedPlayer)ep).lastInventoryChangeTime + (long)inventoryDelay * 1000L - System.currentTimeMillis();
                    messageId = 2;
                }
                if (!inHouse && messageId != -1) {
                    p.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0414\u043e \u043e\u0442\u043a\u0440\u044b\u0442\u0438\u044f \u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0430 " + (time + 1000L) / 1000L + "c. " + StorageUtils.chatMessages[messageId])));
                    if (pvpZone != null) {
                        p.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0412\u0440\u0435\u043c\u044f \u0443\u0432\u0435\u043b\u0438\u0447\u0435\u043d\u043e \u0432 \u043b\u043e\u043a\u0430\u0446\u0438\u0438: " + pvpZone.name)));
                    }
                    return;
                }
                if (EventSystem.config.enabled) {
                    for (HashMap<String, EventSystem.Event> events : EventSystem.config.events) {
                        for (EventSystem.Event event : events.values()) {
                            if (!(p.u >= (double)event.damageZone[0]) || !(p.v >= (double)event.damageZone[1]) || !(p.w >= (double)event.damageZone[2]) || !(p.u <= (double)event.damageZone[3]) || !(p.v <= (double)event.damageZone[4]) || !(p.w <= (double)event.damageZone[5])) continue;
                            p.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u00a7\u0441\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435 \u043b\u0438\u0447\u043d\u043e\u0433\u043e \u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0430 \u043d\u0430 \u0442\u0435\u0440\u0440\u0438\u0442\u043e\u0440\u0438\u0438 \u0438\u0432\u0435\u043d\u0442\u0430 \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043e!"));
                            return;
                        }
                    }
                }
                HcsServer.customStorage.loadInventory(p.c_(), sg, true, new InvLoadCallback(){

                    @Override
                    public void loadingDone(StorageInventory inv) {
                        if (p.bp != null) {
                            p.closeScreen();
                        }
                        inv.openBy((EntityPlayer)p);
                    }
                });
            } else if (type == 20) {
                try {
                    if (!EventSystem.config.enabled) {
                        return;
                    }
                    ExtendedPlayer extendedPlayer = ExtendedPlayer.server((EntityPlayer)p);
                    if (System.currentTimeMillis() < extendedPlayer.lastEventReq + 700L) {
                        return;
                    }
                    extendedPlayer.lastEventReq = System.currentTimeMillis();
                    VSP os = new VSP(31, "HCSMOD");
                    os.writeByte(EventSystem.currentEvents.size());
                    for (EventSystem.CurrentEventData currentEvent : EventSystem.currentEvents) {
                        os.writeByte(currentEvent.state.ordinal());
                        if (currentEvent.state == EventSystem.CurrentEventData.State.RUNNING) {
                            os.writeUTF(currentEvent.eventName);
                            os.writeByte(currentEvent.waveId);
                            os.writeByte(currentEvent.eventData.waves.size());
                            os.writeShort(currentEvent.kills);
                            os.writeShort(currentEvent.eventData.waves.get((int)currentEvent.waveId).deathsToNextWave);
                            continue;
                        }
                        if (currentEvent.state != EventSystem.CurrentEventData.State.WAITING) continue;
                        os.writeUTF(currentEvent.eventName);
                        os.writeShort((int)((long)((int)(currentEvent.timeToStart - System.currentTimeMillis())) / 1000L));
                    }
                    int size = 0;
                    for (HashMap<String, EventSystem.Event> hashMap : EventSystem.config.events) {
                        size += hashMap.size();
                    }
                    os.writeByte(size - EventSystem.currentEvents.size());
                    for (HashMap<String, Object> hashMap : EventSystem.finishedEvents) {
                        for (String eventName : hashMap.keySet()) {
                            EventSystem.FinishedEventData finishedEventData = (EventSystem.FinishedEventData)hashMap.get(eventName);
                            os.writeByte(finishedEventData.state.ordinal());
                            os.writeUTF(eventName);
                            if (finishedEventData.state != EventSystem.FinishedEventData.State.WAITING) continue;
                            os.writeShort((int)((long)((int)(finishedEventData.damageStartTime - System.currentTimeMillis())) / 1000L));
                        }
                    }
                    os.send((EntityPlayer)p);
                }
                catch (Throwable extendedPlayer) {}
            } else if (type == 21) {
                try {
                    Long uniqueId = in.readLong();
                    AirdropSystem.Airdrop airdrop = AirdropSystem.airdrops.get(uniqueId);
                    double x = p.u - (double)airdrop.location.x;
                    double d = p.v - (double)airdrop.location.y;
                    double z = p.w - (double)airdrop.location.z;
                    if (x * x + d * d + z * z > 36.0 || airdrop.state != AirdropSystem.Airdrop.State.LOOT) {
                        return;
                    }
                    AirdropSystem.capture(airdrop, p.c_());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (type == 22) {
                ExtendedPlayer extendedPlayer = ExtendedPlayer.server((EntityPlayer)p);
                ExtendedStorage extendedStorage = ExtendedStorage.get(extendedPlayer);
                if (extendedStorage.lastPvPZoneUpdateRequest + (long)PvpSystem.UPDATE_DELAY > System.currentTimeMillis()) {
                    return;
                }
                PvpSystem.sendUpdate((EntityPlayer)p);
                extendedStorage.lastPvPZoneUpdateRequest = System.currentTimeMillis();
            }
        }
        try {
            in.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private void handleOpenInventory(EntityPlayer p) {
        p.openGui((Object)HcsMod.INSTANCE, 0, p.q, (int)p.u, (int)p.v, (int)p.w);
    }

    public static void broadcastExtendedData(EntityPlayer p, ExtendedPlayer ep) {
        VSP os = new VSP(0, "HCSMOD");
        try {
            Packet.writeItemStack((ItemStack)ep.inventory.inventoryStacks[0], (DataOutput)os);
            Packet.writeItemStack((ItemStack)ep.inventory.inventoryStacks[1], (DataOutput)os);
            Packet.writeItemStack((ItemStack)ep.inventory.inventoryStacks[2], (DataOutput)os);
            Packet.writeItemStack((ItemStack)ep.inventory.inventoryStacks[3], (DataOutput)os);
            Packet.writeItemStack((ItemStack)ep.inventory.inventoryStacks[4], (DataOutput)os);
            Packet.writeItemStack((ItemStack)ep.inventory.inventoryStacks[5], (DataOutput)os);
            os.writeUTF(p.username);
            os.writeByte(ep.shieldCharge == 600 ? 1 : (ep.shieldCharge < 0 ? 2 : 0));
            os.sendAll();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static void sendExtendedInventoryToPlayer(EntityPlayer p, ExtendedPlayer ep, EntityPlayer SENDTO) {
        VSP os = new VSP(0, "HCSMOD");
        try {
            Packet.writeItemStack((ItemStack)ep.inventory.inventoryStacks[0], (DataOutput)os);
            Packet.writeItemStack((ItemStack)ep.inventory.inventoryStacks[1], (DataOutput)os);
            Packet.writeItemStack((ItemStack)ep.inventory.inventoryStacks[2], (DataOutput)os);
            Packet.writeItemStack((ItemStack)ep.inventory.inventoryStacks[3], (DataOutput)os);
            Packet.writeItemStack((ItemStack)ep.inventory.inventoryStacks[4], (DataOutput)os);
            Packet.writeItemStack((ItemStack)ep.inventory.inventoryStacks[5], (DataOutput)os);
            os.writeUTF(p.username);
            os.writeByte(ep.shieldCharge == 600 ? 1 : (ep.shieldCharge < 0 ? 2 : 0));
            os.send(SENDTO);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static void sendExtendedArmorToPlayer(EntityPlayer player, ExtendedStorage storage) {
        VSP os = new VSP(18, "HCSMOD");
        try {
            os.writeByte(storage.customHelmet);
            os.writeByte(storage.customArmor);
            os.writeByte(storage.customLegs);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        os.send(player);
    }

    public static void sendHint(EntityPlayer p, String id, String hint, int time) {
        time = Math.min(time, 255);
        try {
            byte[] data;
            boolean flag = false;
            if (time > 0 && hint != null && !hint.isEmpty()) {
                id = id + "\n" + hint;
                flag = true;
            }
            if ((data = id.getBytes("UTF-8")).length > 255) {
                return;
            }
            VSP os = new VSP(12, "HCSMOD");
            os.writeByte(data.length);
            os.write(data);
            if (flag) {
                os.writeByte(time);
            }
            os.send(p);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static void sendHitboxExtend(EntityPlayer p) {
        try {
            VSP os = new VSP(20, "HCSMOD");
            os.writeFloat(EntityBullet.HITBOX_EXTEND);
            os.send(p);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static void sendOverrideDimension(EntityPlayer p) {
        try {
            VSP os = new VSP(22, "HCSMOD");
            os.writeByte((int)((byte)HcsServer.overrideDimensionId));
            os.send(p);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void sendMapMarkers(EntityPlayer player) {
        VSP os = new VSP(30, "HCSMOD");
        try {
            os.writeByte(MapMarkersServer.markerGroups.size());
            for (String groupId : MapMarkersServer.markerGroups.keySet()) {
                MapMarkersServer.MarkerGroup group = MapMarkersServer.markerGroups.get(groupId);
                os.writeUTF(groupId);
                os.writeUTF(group.groupName);
                os.writeUTF(group.iconName);
                os.writeByte(group.description.length);
                for (String s : group.description) {
                    os.writeUTF(s);
                }
                os.writeByte(group.iconSize);
                os.writeByte(group.iconShiftX);
                os.writeByte(group.iconShiftY);
                os.writeShort(group.markers.size());
                for (MapMarkersServer.MarkerData marker : group.markers) {
                    os.writeShort(marker.x);
                    os.writeShort(marker.z);
                    os.writeByte(marker.shiftX);
                    os.writeByte(marker.shiftY);
                    os.writeByte(marker.description.length);
                    for (String s : marker.description) {
                        os.writeUTF(s);
                    }
                }
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        if (player != null) {
            os.send(player);
        } else {
            os.sendAll();
        }
    }

    public static void sendZombieSpawnInfo(EntityPlayer p, SpawnZone zone) {
        VSP os = new VSP(41, "HCSMOD");
        try {
            os.writeShort(zone.indoorLocations.size());
            for (IndoorLocation indoorLocation : zone.indoorLocations) {
                os.writeShort(indoorLocation.x1);
                os.writeShort(indoorLocation.z1);
                os.writeShort(indoorLocation.x2);
                os.writeShort(indoorLocation.z2);
                os.writeShort(indoorLocation.y);
            }
            os.writeShort(zone.outdoorLocations.size());
            for (OutdoorLocation outdoorLocation : zone.outdoorLocations) {
                os.writeShort(outdoorLocation.x);
                os.writeShort(outdoorLocation.z);
                os.writeShort(outdoorLocation.r);
            }
            os.writeShort(zone.indoorChunks.keySet().size());
            for (Integer chunk : zone.indoorChunks.keySet()) {
                os.writeInt(chunk.intValue());
                os.writeShort(((ArrayList)zone.indoorChunks.get(chunk)).size());
                for (IndoorChunkInfo info : (ArrayList)zone.indoorChunks.get(chunk)) {
                    os.writeShort(info.y);
                }
            }
            os.writeShort(zone.outdoorChunks.size());
            for (Integer chunk : zone.outdoorChunks) {
                os.writeInt(chunk.intValue());
            }
            os.send(p);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static void sendAirdropZombieSpawnInfo(EntityPlayer p, ZombieGroup zombieGroup) {
        VSP os = new VSP(41, "HCSMOD");
        try {
            os.writeShort(0);
            os.writeShort(0);
            os.writeShort(0);
            os.writeShort(zombieGroup.outdoorChunks.size());
            for (Integer chunk : zombieGroup.outdoorChunks) {
                os.writeInt(chunk.intValue());
            }
            os.send(p);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static void clearZombieSpawnInfo(EntityPlayer p) {
        VSP os = new VSP(45, "HCSMOD");
        os.send(p);
    }

    public static void sendCPSData(EntityPlayer p) {
        VSP os = new VSP(47, "HCSMOD");
        try {
            os.writeByte(HcsServer.CPSData.size());
            for (int id : HcsServer.CPSData.keySet()) {
                ChemProtectSuitData data = HcsServer.CPSData.get(id);
                os.writeShort(id);
                os.writeInt(data.lifeTimeTicks);
                os.writeInt(data.timeToBreakSeconds);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if (p != null) {
            os.send(p);
        } else {
            os.sendAll();
        }
    }

    public static void sendDamageAngle(EntityPlayer p, float angle, float damage) {
        VSP os = new VSP(52, "HCSMOD");
        try {
            os.writeFloat(angle);
            os.writeFloat(damage);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        os.send(p);
    }

    public static void sendSound(EntityPlayer p, int x, int y, int z, int radius, String soundName) {
        try {
            VSP os = new VSP(48, "HCSMOD");
            os.writeInt(x);
            os.writeInt(y);
            os.writeInt(z);
            os.writeUTF(soundName);
            os.sendAllInRange(p.q, radius, (double)x, (double)y, (double)z);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendVoteData(EntityPlayer p, int available, int bonus) {
        try {
            VSP os = new VSP(49, "HCSMOD");
            os.writeShort(available);
            os.writeShort(bonus);
            os.send(p);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendRandomSpawnLocations(EntityPlayer p, ArrayList<Location> locs) {
        try {
            VSP os = new VSP(29, "HCSMOD");
            if (locs == null) {
                os.writeShort(0);
            } else {
                os.writeShort(locs.size());
                for (Location loc : locs) {
                    os.writeShort(loc.X);
                    os.writeShort(loc.Y);
                    os.writeShort(loc.Z);
                }
            }
            os.send(p);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static Packet250CustomPayload genWorldMarkersPacket() {
        try {
            VSP os = new VSP(60, "HCSMOD");
            if (HcsServer.hcsConfig != null && HcsServer.hcsConfig.worldMarkers != null) {
                for (WorldMarker worldMarker : HcsServer.hcsConfig.worldMarkers) {
                    os.writeFloat(worldMarker.x);
                    os.writeFloat(worldMarker.y);
                    os.writeFloat(worldMarker.z);
                    os.writeFloat(worldMarker.radius);
                    os.writeByte(worldMarker.lines.length);
                    for (String line : worldMarker.lines) {
                        byte[] data = line.getBytes(StandardCharsets.UTF_8);
                        os.writeByte(data.length);
                        os.write(data);
                    }
                }
            }
            return os.genPacket();
        }
        catch (Exception exception) {
            return null;
        }
    }
}

