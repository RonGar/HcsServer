/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  co.uk.flansmods.common.guns.ContainerGunModTable
 *  cpw.mods.fml.common.ITickHandler
 *  cpw.mods.fml.common.TickType
 *  cpw.mods.fml.common.network.PacketDispatcher
 *  cpw.mods.fml.relauncher.FMLLaunchHandler
 *  cpw.mods.fml.relauncher.Side
 *  hcsmod.HCS
 *  hcsmod.common.zombie.ZombieGroup
 *  hcsmod.cunches.IItemVehicle
 *  hcsmod.effects.Effect
 *  hcsmod.entity.EntityCorpse
 *  hcsmod.entity.EntityKoster
 *  hcsmod.entity.EntityZombieDayZ
 *  hcsmod.items.ItemChemProtectSuit
 *  hcsmod.player.ContainerExtended
 *  hcsmod.player.ExtendedPlayer
 *  hcsplatfom.PlatformBridge
 *  mcheli.aircraft.MCH_EntityAircraft
 *  net.minecraft.command.IEntitySelector
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.entity.player.InventoryPlayer
 *  net.minecraft.inventory.Container
 *  net.minecraft.inventory.ContainerChest
 *  net.minecraft.inventory.ContainerWorkbench
 *  net.minecraft.inventory.IInventory
 *  net.minecraft.inventory.InventoryLargeChest
 *  net.minecraft.inventory.Slot
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.Packet4UpdateTime
 *  net.minecraft.network.packet.Packet62LevelSound
 *  net.minecraft.potion.Potion
 *  net.minecraft.potion.PotionEffect
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.tileentity.TileEntityChest
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.MathHelper
 *  net.minecraft.world.World
 *  net.minecraft.world.WorldServer
 *  net.minecraft.world.WorldServerMulti
 *  org.lwjgl.input.Keyboard
 *  vintarz.core.VSP
 */
package hcsmod.server;

import co.uk.flansmods.common.guns.ContainerGunModTable;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;
import hcsmod.HCS;
import hcsmod.clans.server.ClansServer;
import hcsmod.common.zombie.ZombieGroup;
import hcsmod.cunches.IItemVehicle;
import hcsmod.effects.Effect;
import hcsmod.entity.EntityCorpse;
import hcsmod.entity.EntityKoster;
import hcsmod.entity.EntityZombieDayZ;
import hcsmod.items.ItemChemProtectSuit;
import hcsmod.player.ContainerExtended;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.DelayedTask;
import hcsmod.server.ExtendedStorage;
import hcsmod.server.HCSCommand;
import hcsmod.server.HcsNotification;
import hcsmod.server.HcsServer;
import hcsmod.server.HcsTrigger;
import hcsmod.server.HouseGetCommand;
import hcsmod.server.JuggerRadar;
import hcsmod.server.RandomSpawn;
import hcsmod.server.SPacketHandler;
import hcsmod.server.SPlayerTracker;
import hcsmod.server.airdrop.AirdropSystem;
import hcsmod.server.event.EventSystem;
import hcsmod.server.storage.StorageUtils;
import hcsmod.server.zombie.ZombieSpawner;
import hcsmod.server.zones.PvpSystem;
import hcsplatfom.PlatformBridge;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import mcheli.aircraft.MCH_EntityAircraft;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet4UpdateTime;
import net.minecraft.network.packet.Packet62LevelSound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import org.lwjgl.input.Keyboard;
import vintarz.core.VSP;
import vintarz.ingamestore.server.ContainerShop;

public class STickHandler
implements ITickHandler {
    private static final boolean ENABLE_TEMPERATURE = Boolean.parseBoolean(System.getProperty("vz.usetemperature", "false"));
    private static final boolean LIMIT_WORLD = Boolean.parseBoolean(System.getProperty("vz.limitworld", "true"));
    private static long nextReportCleanup;
    public static final int LIMIT_MAX_X;
    public static final int LIMIT_MAX_Z;
    public static final int LIMIT_MIN_X;
    public static final int LIMIT_MIN_Z;
    static long SLEEP;
    static Field plrMPinitialInvulnerability;
    private static final Class[] allowedInventories;

    private static void unstackItems(EntityPlayer p) {
        ItemStack[] inv = p.inventory.mainInventory;
        for (int i = 0; i < inv.length; ++i) {
            if (inv[i] == null || inv[i].stackSize <= inv[i].getMaxStackSize()) continue;
            ItemStack is = inv[i].copy();
            is.stackSize -= inv[i].getMaxStackSize();
            inv[i].stackSize = inv[i].getMaxStackSize();
            if (p.inventory.addItemStackToInventory(is)) continue;
            p.dropPlayerItem(is);
        }
        if (p.openContainer instanceof ContainerChest) {
            ContainerChest c = (ContainerChest)p.openContainer;
            IInventory inv2 = c.getLowerChestInventory();
            for (int i = 0; i < inv2.getSizeInventory(); ++i) {
                ItemStack is = inv2.getStackInSlot(i);
                if (is == null || is.stackSize <= is.getMaxStackSize()) continue;
                ItemStack drop = is.copy();
                drop.stackSize -= is.getMaxStackSize();
                is.stackSize = is.getMaxStackSize();
                if (p.inventory.addItemStackToInventory(drop)) continue;
                p.dropPlayerItem(drop);
            }
        }
    }

    private static boolean tickBackpackAndHasJuggernaut(EntityPlayer p) {
        ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
        ItemStack is = ep.inventory.a(5);
        if (is != null && is.itemID != HCS.PNV.itemID) {
            p.dropPlayerItem(is.copy());
            ep.inventory.a(5, null);
        }
        if ((is = ep.inventory.a(4)) != null && is.itemID != 8267) {
            p.dropPlayerItem(is.copy());
            ep.inventory.a(4, null);
        }
        if ((is = ep.inventory.a(3)) != null && is.itemID != HCS.min.itemID && is.itemID != HCS.mid.itemID && is.itemID != HCS.max.itemID) {
            p.dropPlayerItem(is.copy());
            ep.inventory.a(3, null);
        }
        if ((is = ep.inventory.a(2)) != null && is.itemID != HCS.raincoat.itemID) {
            p.dropPlayerItem(is.copy());
            ep.inventory.a(2, null);
        }
        if ((is = ep.inventory.a(1)) != null && !(is.getItem() instanceof ItemChemProtectSuit)) {
            p.dropPlayerItem(is.copy());
            ep.inventory.a(1, null);
        }
        if ((is = ep.inventory.a(0)) != null && is.itemID != HCS.warmclothes.itemID) {
            p.dropPlayerItem(is.copy());
            ep.inventory.a(0, null);
        }
        if ((is = ep.inventory.a(1)) != null && is.getItem() instanceof ItemChemProtectSuit) {
            ((ItemChemProtectSuit)is.getItem()).tickInSlot(ep.inventory, is);
            if (p.ac % 10 == 0) {
                VSP os = new VSP(46, "HCSMOD");
                try {
                    Packet.writeItemStack((ItemStack)ep.inventory.inventoryStacks[1], (DataOutput)os);
                    os.send(p);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
        }
        if (!p.capabilities.isCreativeMode && ep.inventory.inventoryStacks[3] == null) {
            for (int i = 0; i < 9; ++i) {
                is = p.inventory.mainInventory[i];
                if (is == null || is.itemID != HCS.min.itemID && is.itemID != HCS.mid.itemID && is.itemID != HCS.max.itemID) continue;
                ep.inventory.inventoryStacks[3] = new ItemStack(is.itemID, 1, 0);
                p.inventory.mainInventory[i] = null;
                break;
            }
        }
        int backpacklvl = HCS.getBackpackLVL((EntityPlayer)p, (Side)Side.SERVER);
        for (int i = 0; i < 4; ++i) {
            for (int k = 0; k < 9; ++k) {
                int slot = k + i * 9;
                is = p.inventory.mainInventory[slot];
                if (is == null) continue;
                if (HcsServer.isBannedItem(is)) {
                    p.inventory.mainInventory[slot] = null;
                    continue;
                }
                if (!p.capabilities.isCreativeMode && i > backpacklvl) {
                    STickHandler.tryMoveItemOrDrop(p, slot, backpacklvl);
                    continue;
                }
                if (p.capabilities.isCreativeMode || !(is.getItem() instanceof IItemVehicle) || !(p.inventoryContainer instanceof ContainerExtended)) continue;
                if (p.o != null || !((IItemVehicle)is.getItem()).spawnAndSetDriver(p, is) || p.o != null && p.o.isDead) {
                    if (p.o != null) {
                        p.mountEntity(null);
                    }
                    p.dropPlayerItem(is);
                }
                p.inventory.mainInventory[slot] = null;
            }
        }
        int juglvl = 0;
        for (int i = 0; i < 4; ++i) {
            NBTTagCompound nbt;
            is = p.inventory.armorInventory[i];
            if (is == null || is.itemID < HCS.JAG[0].cv || is.itemID > HCS.JAG[3].cv) continue;
            if (is.getTagCompound() == null) {
                is.setTagCompound(new NBTTagCompound("jugger"));
            }
            if ((nbt = is.getTagCompound()).hasKey("juggerOwner")) {
                String owner = nbt.getString("juggerOwner");
                if (p.username.equalsIgnoreCase(owner)) {
                    ++juglvl;
                    continue;
                }
                if (MinecraftServer.getServer().getTickCounter() % 60 != 0) continue;
                int damage = is.getItemDamage();
                if (++damage > is.getMaxDamage()) {
                    p.inventory.armorInventory[i] = null;
                    continue;
                }
                is.setItemDamage(damage);
                continue;
            }
            nbt.setString("juggerOwner", p.username.toLowerCase());
        }
        boolean hasShield = false;
        int lastShieldChargeSend = (int)(ep.shieldCharge > 0 ? (float)ep.shieldCharge * 127.0f / 600.0f : (ep.shieldCharge < 0 ? (float)ep.shieldCharge * 127.0f / 20.0f : 0.0f));
        if (juglvl == 4) {
            if (p.q.getTotalWorldTime() % 40L == 0L) {
                p.f(0.05f);
            }
            if (++ep.shieldCharge > 600) {
                ep.shieldCharge = 600;
                hasShield = true;
            }
        } else {
            ep.shieldCharge = 0;
        }
        int currentShieldChargeSend = (int)(ep.shieldCharge > 0 ? (float)ep.shieldCharge * 127.0f / 600.0f : (ep.shieldCharge < 0 ? (float)ep.shieldCharge * 127.0f / 20.0f : 0.0f));
        boolean shieldEquals = currentShieldChargeSend == lastShieldChargeSend;
        boolean allowCheck = false;
        ExtendedStorage es = ExtendedStorage.get(ep);
        if (++es.ticksSinceLastExtendedSync >= 5) {
            allowCheck = true;
        }
        if (!shieldEquals || allowCheck) {
            boolean equals = shieldEquals;
            for (int i = 0; i < ep.inventory.inventoryStacks.length; ++i) {
                if (ItemStack.areItemStacksEqual((ItemStack)ep.inventory.inventoryStacks[i], (ItemStack)ep.previousItems[i])) continue;
                ep.previousItems[i] = ep.inventory.inventoryStacks[i] == null ? null : ep.inventory.inventoryStacks[i].copy();
                equals = false;
            }
            if (!equals) {
                SPacketHandler.broadcastExtendedData(p, ep);
                es.ticksSinceLastExtendedSync = 0;
            }
        }
        if (!shieldEquals) {
            try {
                VSP os = new VSP(8, "HCSMOD");
                os.writeShort(currentShieldChargeSend);
                os.send(p);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return hasShield;
    }

    private static void tryMoveItemOrDrop(EntityPlayer p, int invalidSlotId, int backpacklvl) {
        ItemStack is = p.inventory.mainInventory[invalidSlotId];
        for (int i = 0; i < 4; ++i) {
            for (int k = 0; k < 9; ++k) {
                int slotId = k + i * 9;
                if (i > backpacklvl || p.inventory.mainInventory[slotId] != null) continue;
                p.inventory.mainInventory[slotId] = is;
                p.inventory.mainInventory[invalidSlotId] = null;
                return;
            }
        }
        p.dropPlayerItem(is);
        p.inventory.mainInventory[invalidSlotId] = null;
    }

    private static void tickThirstAndFood(EntityPlayer p) {
        VSP os2;
        ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
        if (p.capabilities.isCreativeMode || HcsServer.isPVPserver) {
            if (ep.thirst != 0 || ep.hunger != 0) {
                ep.thirst = 0;
                ep.hunger = 0;
                try {
                    os2 = new VSP(2, "HCSMOD");
                    os2.writeInt(0);
                    os2.writeInt(0);
                    os2.send(p);
                }
                catch (Exception os2) {}
            }
        } else {
            ep.thirst += 2;
            if (!p.ag()) {
                if (((EntityPlayerMP)p).bf != 0.0f || ((EntityPlayerMP)p).be != 0.0f) {
                    ++ep.thirst;
                }
                if (p.ai()) {
                    ep.thirst += 3;
                }
                if (p.an) {
                    ep.thirst += 3;
                }
            }
            ++ep.hunger;
            if (!p.ag()) {
                if (((EntityPlayerMP)p).bf != 0.0f || ((EntityPlayerMP)p).be != 0.0f) {
                    ep.hunger += 2;
                }
                if (p.ai()) {
                    ep.hunger += 3;
                }
                if (p.an) {
                    ep.hunger += 3;
                }
                if (p.au) {
                    ep.hunger += 3;
                }
            }
        }
        p.getFoodStats().addStats(20 - (int)((double)ep.hunger * 20.0 / 78000.0) - p.getFoodStats().getFoodLevel(), 0.0f);
        if (ep.hunger >= 78000) {
            ep.hunger = 78000;
            if (p.ac % 602 == 0) {
                p.attackEntityFrom(DamageSource.starve, 0.8333333f);
            }
        }
        if (ep.thirst >= 78000) {
            p.c(new PotionEffect(Potion.weakness.id, 5, 1));
            ep.thirst = 78000;
            if (p.ac % 602 == 0) {
                p.attackEntityFrom(DamageSource.starve, 0.8333333f);
            }
        }
        if (MinecraftServer.getServer().getTickCounter() % 20 == 0) {
            try {
                os2 = new VSP(2, "HCSMOD");
                os2.writeInt(ep.thirst);
                os2.writeInt(ep.hunger);
                os2.send(p);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    static void tickWorldTime(World w) {
        if (FMLLaunchHandler.side().isClient() && Keyboard.isKeyDown((int)62)) {
            w.setWorldTime(w.getWorldTime() + 100L);
            PacketDispatcher.sendPacketToAllInDimension((Packet)new Packet4UpdateTime(w.getTotalWorldTime(), w.getWorldTime(), w.getGameRules().getGameRuleBooleanValue("doDaylightCycle")), (int)w.getWorldInfo().getVanillaDimension());
        }
    }

    public void tickStart(EnumSet<TickType> type, Object ... tickData) {
        if (type.contains((Object)TickType.WORLD)) {
            WorldServer w = (WorldServer)tickData[0];
            if (w instanceof WorldServerMulti) {
                return;
            }
            for (ZombieGroup zg : HcsServer.zombieGroups.values()) {
                if ((long)MinecraftServer.getServer().getTickCounter() < zg.lastSpawnTryTick + (long)zg.spawnTriesDelayTick) continue;
                zg.lastSpawnTryTick = MinecraftServer.getServer().getTickCounter();
                ZombieSpawner.zombieSpawner(w, zg);
            }
        }
    }

    public void tickEnd(EnumSet<TickType> type, Object ... tickData) {
        if (type.contains((Object)TickType.PLAYER)) {
            boolean inSafezone;
            EntityPlayer p = (EntityPlayer)tickData[0];
            if (p instanceof EntityPlayerMP) {
                EntityPlayerMP entityPlayerMP = (EntityPlayerMP)p;
                if (entityPlayerMP.needUpdateViewDistance && entityPlayerMP.nextViewDistanceUpdate <= System.currentTimeMillis()) {
                    entityPlayerMP.updateRenderDistance();
                }
            }
            p.inventoryContainer.detectAndSendChanges();
            if (!p.capabilities.isCreativeMode) {
                try {
                    plrMPinitialInvulnerability.setInt((Object)p, 0);
                }
                catch (Throwable entityPlayerMP) {
                    // empty catch block
                }
            }
            ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
            if (ep.zombieCheckCountDist > 0) {
                int zombieAllCount = 0;
                int zombieCountInSpawnChunks = 0;
                for (Object o : p.q.loadedEntityList) {
                    if (!(o instanceof EntityZombieDayZ)) continue;
                    EntityZombieDayZ ez = (EntityZombieDayZ)o;
                    double dx = ez.u - p.u;
                    double dy = ez.v - p.v;
                    double dz = ez.w - p.w;
                    if (!(dx * dx + dy * dy + dz * dz <= (double)(ep.zombieCheckCountDist * ep.zombieCheckCountDist))) continue;
                    ++zombieAllCount;
                    int ezChunkX = ez.aj;
                    int ezChunkZ = ez.al;
                    int combinedXZ = ezChunkX << 16 | ezChunkZ & 0xFFFF;
                    if (!ez.zombieGroup.indoorChunks.containsKey(combinedXZ) && !ez.zombieGroup.outdoorChunks.contains(combinedXZ)) continue;
                    ++zombieCountInSpawnChunks;
                }
                SPacketHandler.sendHint(p, "zcc", "Zombies(" + ep.zombieCheckCountDist + "): Legal:" + zombieCountInSpawnChunks + " Illegal:" + (zombieAllCount - zombieCountInSpawnChunks) + " All:" + zombieAllCount, 10);
            }
            ExtendedStorage.tick(p, ep);
            if (--ep.hitCooldown < 0) {
                ep.hitCooldown = 0;
            }
            if (Math.abs(p.ac - ep.lastRadarUpdate) > 6) {
                ep.lastRadarUpdate = p.ac;
                JuggerRadar.detect(p);
            }
            if (ep.damageTimeout + 2000L < System.currentTimeMillis()) {
                ep.damageAmmount = 0.0f;
                ep.damageTimeout = 0L;
                ep.damageStart = 0L;
            }
            STickHandler.unstackItems(p);
            boolean hasJugger = STickHandler.tickBackpackAndHasJuggernaut(p);
            this.removeOpenContainerProhibitedItems(p);
            boolean onFoot = p.o == null;
            boolean bl = inSafezone = p.u > -147.0 && p.u < 163.0 && p.w > -156.0 && p.w < 153.0 && p.v > 10.0;
            if (onFoot && !inSafezone && (p.bf != 0.0f || p.be != 0.0f)) {
                ExtendedStorage.get((ExtendedPlayer)ep).lastWalkTime = System.currentTimeMillis();
            }
            if (ENABLE_TEMPERATURE) {
                this.tickTemperature(p, ep, false, hasJugger);
            }
            p.experienceLevel = 32767;
            p.experienceTotal = 32767;
            p.experience = Float.MAX_VALUE;
            if (ep.regen) {
                ep.visibility = 0;
                if (p.ac % 4 == 0) {
                    p.f(1.0f);
                }
                p.k(Effect.bleeding.c());
                p.k(Potion.moveSlowdown.id);
            } else {
                STickHandler.tickThirstAndFood(p);
                ep.visibility = ExtendedPlayer.server((EntityPlayer)p).detectibility.update(p);
            }
            if (ep.healing > 0.0f && p.ac % 5 == 0) {
                if (ep.pauseHeal == 0 || ep.prevHealing == 0.0f) {
                    float f;
                    float prevh = ep.healing;
                    float ammount = 1.5f;
                    ep.healing -= ammount;
                    if (f < 0.0f) {
                        ammount = prevh;
                        ep.healing = 0.0f;
                    }
                    p.f(ammount);
                }
                if (--ep.pauseHeal < 0) {
                    ep.pauseHeal = 0;
                }
            }
            if (ep.healing != ep.prevHealing) {
                ep.prevHealing = ep.healing;
                try {
                    VSP os = new VSP(1, "HCSMOD");
                    os.writeByte(2);
                    os.writeByte((int)(ep.healing / 20.0f * 100.0f));
                    os.send(p);
                }
                catch (Exception os) {
                    // empty catch block
                }
            }
            if (p.u * p.u + (p.v - 1.0) * (p.v - 1.0) + p.w * p.w <= 25.0 && (p.ac % 10 == 0 || ep.firstSpawnTick)) {
                try {
                    long current = System.currentTimeMillis();
                    VSP os = new VSP(7, "HCSMOD");
                    os.writeUTF(HcsServer.hcsConfig.spawnGUITopBtnName);
                    os.writeByte(RandomSpawn.spawns.size());
                    for (RandomSpawn rs : RandomSpawn.spawns.values()) {
                        Long time = (Long)ep.spawnCooldowns.get(rs.name);
                        String sendName = rs.name.replace("#", "\u00a7");
                        os.writeUTF(sendName);
                        os.writeShort((int)rs.posX);
                        os.writeShort((int)rs.posZ);
                        if (time != null && time > current) {
                            os.writeShort((int)((time - current) / 1000L));
                            continue;
                        }
                        os.writeShort(0);
                    }
                    os.send(p);
                    ep.firstSpawnTick = false;
                }
                catch (IOException current) {
                    // empty catch block
                }
            }
            if (ep.placeHouse) {
                if (p.getCurrentEquippedItem() != null) {
                    ep.placeHouse = false;
                    HouseGetCommand.cancelled(p);
                }
            } else if (ep.hasHousesForTransfer && p.ac % 200 == 0) {
                ep.hasHousesForTransfer(p);
            }
            p.inventoryContainer.detectAndSendChanges();
            ep.prevX = p.u;
            ep.prevY = p.v;
            ep.prevZ = p.w;
            if (p.ac % 30 == 0 && HcsServer.playerHasData(p.username, "cheater")) {
                Packet62LevelSound pt = new Packet62LevelSound("mob.ckicken.say", p.u, p.v, p.w, 1.0f, 1.0f);
                for (Object o : p.q.playerEntities) {
                    EntityPlayerMP target;
                    if (!(o instanceof EntityPlayerMP) || !(p.e((Entity)(target = (EntityPlayerMP)o)) < 4096.0) || HcsServer.playerHasData(target.bu, "cheater")) continue;
                    target.playerNetServerHandler.sendPacketToPlayer((Packet)pt);
                }
            }
            StorageUtils.checkInventoryChange(p);
        } else if (type.contains((Object)TickType.WORLD)) {
            World w = (World)tickData[0];
            STickHandler.tickWorldTime(w);
            if (w.getWorldInfo().isRaining() && w.getWorldInfo().getRainTime() > 6000) {
                w.getWorldInfo().setRainTime(w.rand.nextInt(3600) + 2400);
            }
            if (LIMIT_WORLD) {
                for (Object o : w.loadedEntityList) {
                    Entity e = (Entity)o;
                    boolean flag = false;
                    if (e.posX > (double)LIMIT_MAX_X) {
                        e.posX = LIMIT_MAX_X;
                        flag = true;
                    }
                    if (e.posX < (double)LIMIT_MIN_X) {
                        e.posX = LIMIT_MIN_X;
                        flag = true;
                    }
                    if (e.posZ > (double)LIMIT_MAX_Z) {
                        e.posZ = LIMIT_MAX_Z;
                        flag = true;
                    }
                    if (e.posZ < (double)LIMIT_MIN_Z) {
                        e.posZ = LIMIT_MIN_Z;
                        flag = true;
                    }
                    if (e.posY > 256.0) {
                        e.posY = 256.0;
                        flag = true;
                    }
                    if (!flag) continue;
                    if (e instanceof EntityLivingBase) {
                        ((EntityLivingBase)e).setPositionAndUpdate(e.posX, e.posY, e.posZ);
                        continue;
                    }
                    e.setPosition(e.posX, e.posY, e.posZ);
                }
            }
        } else if (type.contains((Object)TickType.SERVER)) {
            long now;
            long time = System.currentTimeMillis();
            if (time > nextReportCleanup) {
                nextReportCleanup = time + 1000L;
                HcsServer.clearReported();
            }
            HcsNotification.serverTick();
            ClansServer.tick();
            HcsServer.triggers.forEach(HcsTrigger::tick);
            HcsServer.customStorage.tick();
            EventSystem.tick();
            AirdropSystem.tick();
            if (HCSCommand.forcedCrash != null) {
                throw HCSCommand.forcedCrash;
            }
            if (SLEEP > 0L) {
                try {
                    Thread.sleep(SLEEP);
                }
                catch (InterruptedException o) {
                    // empty catch block
                }
            }
            for (Object o : MinecraftServer.getServerConfigurationManager((MinecraftServer)MinecraftServer.getServer()).playerEntityList) {
                if (!(o instanceof EntityPlayer)) continue;
                EntityPlayer ep = (EntityPlayer)o;
                for (HashMap<String, EventSystem.Event> data : EventSystem.config.events) {
                    for (EventSystem.Event eventData : data.values()) {
                        if (!(ep.u >= (double)eventData.damageZone[0]) || !(ep.v >= (double)eventData.damageZone[1]) || !(ep.w >= (double)eventData.damageZone[2]) || !(ep.u <= (double)eventData.damageZone[3]) || !(ep.v <= (double)eventData.damageZone[4]) || !(ep.w <= (double)eventData.damageZone[5]) || !(ep.o instanceof MCH_EntityAircraft)) continue;
                        ep.o.setPosition(ep.o.posX, 200.0, ep.o.posZ);
                    }
                }
            }
            long start = System.currentTimeMillis();
            long maxTime = Math.max(HcsServer.tickStart + 50L, start + 10L);
            while (DelayedTask.runDelayedTask() && (now = System.currentTimeMillis()) < maxTime) {
            }
            PlatformBridge.timings.report("DelayedTasksExecution", System.currentTimeMillis() - start);
            PvpSystem.tick();
        }
    }

    private void removeOpenContainerProhibitedItems(EntityPlayer p) {
        if (p.openContainer != p.inventoryContainer) {
            for (Slot slot : p.openContainer.inventorySlots) {
                ItemStack is = slot.getStack();
                if (is == null) continue;
                if (HcsServer.isStartLoot(is) && !(slot.inventory instanceof InventoryPlayer) || is.getItem() instanceof IItemVehicle || HcsServer.isBannedItem(is) || is.isItemStackDamageable() && is.getItemDamage() > is.getMaxDamage()) {
                    slot.putStack(null);
                    continue;
                }
                SPlayerTracker.turnFlashlightOff(is);
            }
        }
    }

    private boolean isAllowedContainer(Container openContainer) {
        return openContainer instanceof ContainerShop || openContainer instanceof ContainerWorkbench || openContainer instanceof ContainerGunModTable || openContainer instanceof ContainerChest && this.isAllowedInventory(((ContainerChest)openContainer).getLowerChestInventory());
    }

    private boolean isAllowedInventory(IInventory chestInventory) {
        for (Class invClass : allowedInventories) {
            if (invClass != chestInventory.getClass()) continue;
            return true;
        }
        return false;
    }

    private void tickTemperature(EntityPlayer p, ExtendedPlayer ep, boolean hasHeatPack, boolean hasJuggernaut) {
        float overheat;
        float environment;
        int lastTemperature = (int)(ep.temperature * 1024.0f);
        if (p.v < 5.0 || p.capabilities.isCreativeMode || p.u * p.u + p.w * p.w <= 6400.0) {
            ep.temperature = 25.0f;
            environment = 25.0f;
            overheat = 0.0f;
        } else {
            float daytime = HCS.daytime((World)p.q);
            float mod = 0.05f;
            environment = 15.0f + daytime * 10.0f;
            float internalHeat = hasHeatPack ? 0.3f : 0.1f;
            overheat = STickHandler.calculateValueBetween(ep.temperature, 15.0f, 20.0f, 40.0f, 45.0f);
            float cooling = 0.1f;
            if (overheat > 0.0f) {
                cooling += 0.1f * overheat;
            }
            if (overheat < 0.0f) {
                cooling += 0.05f * overheat;
            }
            float conductivity = 0.02f;
            if (p.ag() || hasJuggernaut) {
                environment = 25.0f;
                conductivity = 0.03f;
            } else {
                float height = STickHandler.calculateValueBetween((float)p.v, 45.0f, 60.0f, 70.0f, 110.0f);
                if (height > 0.0f) {
                    environment -= 8.0f * height;
                }
                if (height < 0.0f) {
                    height = -height;
                    environment = 1.0f * height + (1.0f - height) * environment;
                }
                if (p.H()) {
                    environment = Math.min(environment, 10.0f + 5.0f * daytime);
                    conductivity = 0.03f;
                } else {
                    float armor = 0.0f;
                    for (int i = 0; i < 4; ++i) {
                        if (p.inventory.armorInventory[i] == null) continue;
                        armor += 1.0f;
                    }
                    ItemStack is = ep.inventory.a(0);
                    if (is != null && is.itemID == HCS.warmclothes.itemID) {
                        armor += 3.8f;
                    }
                    if (p.ai()) {
                        internalHeat = p.q.isRaining() ? (internalHeat += 0.3f) : (internalHeat += 0.16f);
                    } else if (p.bf != 0.0f || p.be != 0.0f) {
                        internalHeat += 0.1f;
                    }
                    conductivity -= armor * 0.0025f;
                    if (p.q.isRaining()) {
                        environment -= 5.0f;
                        int x = MathHelper.floor_double((double)p.u);
                        int z = MathHelper.floor_double((double)p.w);
                        boolean under_rain = true;
                        is = ep.inventory.a(2);
                        if (is == null || is.itemID != HCS.raincoat.itemID) {
                            for (int y = MathHelper.floor_double((double)p.v); y < 256; ++y) {
                                if (p.q.getBlockId(x, y, z) == 0) continue;
                                under_rain = false;
                            }
                        } else if (is.itemID == HCS.raincoat.itemID) {
                            under_rain = false;
                        }
                        if (under_rain) {
                            conductivity = 0.03f;
                        }
                    }
                    if (!p.af()) {
                        double radius = 5.0;
                        double temperature = 35.0;
                        List kosters = p.q.getEntitiesWithinAABBExcludingEntity((Entity)p, p.E.expand(radius, 5.0, radius), new IEntitySelector(){

                            public boolean a(Entity entity) {
                                return entity instanceof EntityKoster;
                            }
                        });
                        for (EntityKoster koster : kosters) {
                            if (!koster.isActive()) continue;
                            double range = koster.d((Entity)p);
                            if (range < 0.9) {
                                p.d(10);
                            }
                            environment = (float)((double)environment + Math.max(0.0, (radius - range) / radius * temperature));
                        }
                    }
                }
            }
            ep.temperature += (environment - ep.temperature) * conductivity * mod + internalHeat * mod - cooling * mod;
            if (!p.af() && overheat > 0.0f) {
                if (HcsServer.isHarxcoreServer) {
                    ExtendedStorage storage = ExtendedStorage.get(ep);
                    if ((double)overheat >= 0.5) {
                        int n;
                        ++storage.temperatureTicks;
                        if ((float)n >= 180.0f - 160.0f * overheat) {
                            p.attackEntityFrom(DamageSource.outOfWorld, 0.061666667f);
                            storage.temperatureTicks = 0;
                        }
                    }
                    ep.thirst += (int)(10.0f * overheat);
                } else {
                    ep.thirst += (int)(15.0f * overheat);
                }
            } else if (overheat < 0.0f) {
                if (HcsServer.isHarxcoreServer) {
                    ExtendedStorage storage = ExtendedStorage.get(ep);
                    if ((double)overheat <= -0.5) {
                        int n;
                        ++storage.temperatureTicks;
                        if ((float)n >= 180.0f - 160.0f * -overheat) {
                            p.attackEntityFrom(DamageSource.outOfWorld, 0.061666667f);
                            storage.temperatureTicks = 0;
                            ep.hunger += MathHelper.floor_float((float)(-3.0f * overheat)) * 2;
                        }
                    }
                } else {
                    ep.hunger += (int)(-10.0f * overheat);
                }
            }
        }
        int temperature = Math.max(0, Math.min(65535, (int)(ep.temperature * 1024.0f)));
        if (temperature != lastTemperature) {
            try {
                VSP os = new VSP(11, "HCSMOD");
                os.writeShort(temperature);
                os.writeShort((int)(environment * 1024.0f));
                os.writeByte((int)((double)overheat * 127.0));
                os.send(p);
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    private static float calculateValueBetween(float value, float min, float low, float high, float max) {
        float cold = -1.0f;
        float normal = 0.0f;
        float hot = 1.0f;
        if (value <= min) {
            return cold;
        }
        if (value >= max) {
            return hot;
        }
        if (value >= min && value <= low) {
            return cold + (normal - cold) * (value -= min) / (low -= min);
        }
        if (value >= high && value <= max) {
            return normal + (hot - normal) * (value -= high) / (max -= high);
        }
        return normal;
    }

    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.PLAYER, TickType.WORLD, TickType.SERVER);
    }

    public String getLabel() {
        return "HCS Server TH";
    }

    static {
        String[] limits = System.getProperty("vz.limitworld.region", "4410,4410,-4091,-4095").split(",");
        int x0 = Integer.parseInt(limits[0]);
        int z0 = Integer.parseInt(limits[1]);
        int x1 = Integer.parseInt(limits[2]);
        int z1 = Integer.parseInt(limits[3]);
        LIMIT_MAX_X = Math.max(x0, x1);
        LIMIT_MAX_Z = Math.max(z0, z1);
        LIMIT_MIN_X = Math.min(x0, x1);
        LIMIT_MIN_Z = Math.min(z0, z1);
        HCS.setWorldBorders((float)LIMIT_MIN_X, (float)LIMIT_MAX_X, (float)LIMIT_MIN_Z, (float)LIMIT_MAX_Z);
        for (String s : new String[]{"initialInvulnerability", "field_71145_cl", "bT"}) {
            try {
                Field f = EntityPlayerMP.class.getDeclaredField(s);
                if (f.getType() == Integer.TYPE) {
                    f.setAccessible(true);
                }
                plrMPinitialInvulnerability = f;
                break;
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
        allowedInventories = new Class[]{EntityCorpse.class, TileEntityChest.class, InventoryLargeChest.class};
    }
}

