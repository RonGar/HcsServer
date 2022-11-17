/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.common.Line
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NBTBase
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.MathHelper
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.World
 *  net.minecraftforge.common.IExtendedEntityProperties
 *  vintarz.core.VSP
 */
package hcsmod.server;

import hcsmod.clans.server.ClansStore;
import hcsmod.common.Line;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.HarxCoreArmor;
import hcsmod.server.HcsServer;
import hcsmod.server.PlayGift;
import hcsmod.server.SPacketHandler;
import hcsmod.server.vote.VoteGift;
import hcsmod.server.zones.PveSystem;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import vintarz.core.VSP;

public class ExtendedStorage
implements IExtendedEntityProperties {
    public static final int INITIAL_DROP_DELAY_TICKS = 36000;
    public static final long INACTIVE_DELAY_MS = 1200000L;
    public static final int ITEMS_PER_DAY = 10;
    public long lastPVPtime;
    public long lastWalkTime;
    public long lastInventoryChangeTime;
    public Map<Integer, InventoryMap> playerInventoryMap = new HashMap<Integer, InventoryMap>();
    public long lastClanTeleport;
    public long lastPvPZoneUpdateRequest;
    private static final Random rng = new Random();
    public int ticksSinceLastExtendedSync;
    public int flansHealCooldown;
    public int shootTime;
    public boolean vehicleUpdated;
    public long dayReset;
    public int dayItems;
    public long lastActive;
    public int dropDelay = ExtendedStorage.initialDropDelay();
    public boolean allowDistanceReset;
    public float posX;
    public float posZ;
    public int customHelmet;
    public int customArmor;
    public int customLegs;
    public int hardFeetValue = 0;
    public int hardFeetDisplay = 0;
    public int hardFeetSend = 0;
    public boolean hardFeetDamage;
    public int hardFeetTicks;
    public int temperatureTicks;
    public long prevGunSeed;
    public boolean reusingGunSeed;
    public final PlayGift playGift = new PlayGift();
    public final VoteGift voteGift = new VoteGift();
    public final ClansStore clansStore = new ClansStore();
    private AxisAlignedBB selectionBox;
    public final PveSystem.Player pvePlayer = new PveSystem.Player();
    public final List<Line[]> lagCompDebug = new LinkedList<Line[]>();
    public int lagCompMaxDebug = 0;

    public static ExtendedStorage get(ExtendedPlayer ep) {
        if (!(ep.serverStorage instanceof ExtendedStorage)) {
            ep.serverStorage = new ExtendedStorage();
        }
        return (ExtendedStorage)ep.serverStorage;
    }

    private static int initialDropDelay() {
        return 36000 + rng.nextInt(600) * 20;
    }

    public static void tick(EntityPlayer p, ExtendedPlayer ep) {
        ExtendedStorage.get(ep).tick(p);
    }

    public static void zombieKill(EntityPlayer player, Entity zombie) {
        ExtendedStorage l = ExtendedStorage.get(ExtendedPlayer.server((EntityPlayer)player));
        if (l.canDropIron()) {
            l.droppedItem(player);
            zombie.entityDropItem(new ItemStack(Item.ingotIron), 0.5f);
        }
    }

    private boolean canDropIron() {
        return this.dayItems < 10 && this.dropDelay == 0;
    }

    private void droppedItem(EntityPlayer p) {
        ++this.dayItems;
        this.dropDelay = ExtendedStorage.initialDropDelay();
        this.resetPosition(p);
    }

    public void resetPosition(EntityPlayer p) {
        this.posX = (float)p.u;
        this.posZ = (float)p.w;
    }

    private void tick(EntityPlayer p) {
        HcsServer.pveSystem.tick(p, this.pvePlayer);
        this.clansStore.tick(p);
        this.vehicleUpdated = false;
        if (this.shootTime > 0) {
            --this.shootTime;
        }
        if (this.flansHealCooldown > 0) {
            --this.flansHealCooldown;
        }
        if (HcsServer.isHarxcoreServer) {
            int display;
            if (p.F) {
                if (p.ai()) {
                    ItemStack is = p.inventory.armorInventory[0];
                    ++this.hardFeetTicks;
                    if (is != null) {
                        int ticksBetweenDamage = 200;
                        if (is.itemID == 7734) {
                            this.hardFeetValue += 5;
                        } else if (is.itemID == 7750 || is.itemID == 301) {
                            this.hardFeetValue += 2;
                        } else if (is.itemID != 7746 && is.itemID != 7738 && is.itemID != 7742) {
                            this.hardFeetValue += 15;
                            ticksBetweenDamage = 5;
                        }
                        if (this.hardFeetTicks > ticksBetweenDamage) {
                            this.hardFeetTicks = 0;
                            p.inventory.armorInventory[0].damageItem(1, (EntityLivingBase)p);
                            if (p.inventory.armorInventory[0].stackSize == 0) {
                                p.inventory.armorInventory[0] = null;
                            }
                        }
                    } else {
                        this.hardFeetValue += 10;
                    }
                } else {
                    this.hardFeetValue = p.bf != 0.0f || p.be != 0.0f ? (this.hardFeetValue -= this.hardFeetValue <= 500 || p.ah() ? 25 : 15) : (this.hardFeetValue -= 50);
                }
            }
            if (this.hardFeetValue > 5000) {
                this.hardFeetValue = 5000;
                this.hardFeetDamage = true;
                this.hardFeetSend = 0;
            }
            if (this.hardFeetValue < 0) {
                this.hardFeetValue = 0;
            }
            if ((display = this.hardFeetValue / 500) == 0 && this.hardFeetDamage) {
                this.hardFeetDamage = false;
                this.hardFeetSend = 0;
            }
            if (this.hardFeetDisplay != display) {
                this.hardFeetDisplay = display;
                this.hardFeetSend = 0;
            }
            if (--this.hardFeetSend <= 0) {
                this.hardFeetSend = 200;
                if (display == 0) {
                    SPacketHandler.sendHint(p, "HF", "", 0);
                } else if (this.hardFeetDamage) {
                    SPacketHandler.sendHint(p, "HF", "\u00a7e\u041d\u043e\u0433\u0438 \u0443\u0441\u0442\u0430\u043b\u0438: " + display + "/10\n\u00a7e\u041e\u0421\u0422\u0410\u041d\u041e\u0412\u0418\u0421\u042c, \u0431\u043e\u043b\u0438\u0442!", 255);
                } else if (p.inventory.armorInventory[0] == null) {
                    SPacketHandler.sendHint(p, "HF", "\u041d\u043e\u0433\u0438 \u0443\u0441\u0442\u0430\u043b\u0438: " + display + "/10\n\u041e\u0431\u0443\u0439 \u0431\u043e\u0442\u0438\u043d\u043a\u0438, \u0431\u043e\u0441\u044f\u043a!", 255);
                } else {
                    SPacketHandler.sendHint(p, "HF", "\u041d\u043e\u0433\u0438 \u0443\u0441\u0442\u0430\u043b\u0438: " + display + "/10", 255);
                }
            }
            int value = HarxCoreArmor.getValue(p.inventory.armorInventory[3]);
            boolean updated = false;
            if (this.customHelmet != value) {
                this.customHelmet = value;
                updated = true;
            }
            if (this.customArmor != (value = HarxCoreArmor.getValue(p.inventory.armorInventory[2]))) {
                this.customArmor = value;
                updated = true;
            }
            if (this.customLegs != (value = HarxCoreArmor.getValue(p.inventory.armorInventory[1]))) {
                this.customLegs = value;
                updated = true;
            }
            if (updated) {
                SPacketHandler.sendExtendedArmorToPlayer(p, this);
            }
            return;
        }
        this.lastActive = System.currentTimeMillis();
        if (this.lastActive > this.dayReset) {
            this.dayReset = this.lastActive + (long)((20.0f + rng.nextFloat() * 4.0f) * 3600.0f * 1000.0f);
            this.dayItems = 0;
        }
        if (this.allowDistanceReset) {
            if (p.o == null) {
                float x = (float)p.u - this.posX;
                float z = (float)p.w - this.posZ;
                if (MathHelper.sqrt_float((float)(x * x + z * z)) > 1500.0f) {
                    this.dropDelay = 0;
                }
            } else {
                this.resetPosition(p);
            }
        }
        if (this.dropDelay > 0) {
            --this.dropDelay;
        } else if (!this.allowDistanceReset) {
            this.allowDistanceReset = true;
        }
        this.playGift.tick(p);
        this.voteGift.tick((EntityPlayerMP)p);
    }

    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound ironLoot = new NBTTagCompound("ironLoot");
        compound.setTag("ironLoot", (NBTBase)ironLoot);
        ironLoot.setLong("dayReset", this.dayReset);
        ironLoot.setByte("dayItems", (byte)this.dayItems);
        ironLoot.setLong("lastActive", this.lastActive);
        ironLoot.setShort("dropDelay", (short)this.dropDelay);
        if (this.temperatureTicks > 0) {
            compound.setShort("hardFeetValue", (short)this.hardFeetValue);
        }
        if (this.hardFeetDamage) {
            compound.setBoolean("hardFeetDamage", true);
        }
        if (this.temperatureTicks > 0) {
            compound.setShort("temperatureTicks", (short)this.temperatureTicks);
        }
        if (this.allowDistanceReset) {
            ironLoot.setBoolean("allowDistanceReset", true);
        }
        compound.setLong("lastDamageToPlayer", this.lastPVPtime);
        compound.setLong("lastWalkTime", this.lastWalkTime);
        compound.setLong("lastClanTeleport", this.lastClanTeleport);
        this.playGift.load(compound);
        this.voteGift.load(compound);
    }

    public void loadNBTData(NBTTagCompound compound) {
        NBTTagCompound ironLoot = compound.getCompoundTag("ironLoot");
        this.dayReset = ironLoot.getLong("dayReset");
        this.dayItems = ironLoot.getByte("dayItems") & 0xFF;
        this.lastActive = ironLoot.getLong("lastActive");
        if (this.lastActive > System.currentTimeMillis() - 1200000L) {
            this.dropDelay = ironLoot.getShort("dropDelay") & 0xFFFF;
            this.allowDistanceReset = ironLoot.getBoolean("allowDistanceReset");
        }
        this.hardFeetDamage = compound.getBoolean("hardFeetDamage");
        this.hardFeetValue = compound.getShort("hardFeetValue") & 0xFFFF;
        this.temperatureTicks = compound.getShort("temperatureTicks") & 0xFFFF;
        this.lastPVPtime = compound.getLong("lastDamageToPlayer");
        this.lastWalkTime = compound.getLong("lastWalkTime");
        this.lastClanTeleport = compound.getLong("lastClanTeleport");
        this.playGift.save(compound);
        this.voteGift.save(compound);
    }

    public void init(Entity entity, World world) {
    }

    public void removeSelectionBox(EntityPlayer p) {
        this.selectionBox = null;
        this.sendSelectionBox(p);
    }

    public void setOrUpdateSelection(EntityPlayer p, Vec3 one, Vec3 two) {
        if (this.selectionBox == null) {
            if (one == null && two == null) {
                throw new NullPointerException("Only 1 point can be null!");
            }
            if (one == null) {
                one = two;
            } else if (two == null) {
                two = one;
            }
            this.selectionBox = AxisAlignedBB.getBoundingBox((double)one.xCoord, (double)one.yCoord, (double)one.zCoord, (double)two.xCoord, (double)two.yCoord, (double)two.zCoord);
        } else {
            if (one != null) {
                this.selectionBox.minX = one.xCoord;
                this.selectionBox.minY = one.yCoord;
                this.selectionBox.minZ = one.zCoord;
            }
            if (two != null) {
                this.selectionBox.maxX = two.xCoord;
                this.selectionBox.maxY = two.yCoord;
                this.selectionBox.maxZ = two.zCoord;
            }
        }
        this.sendSelectionBox(p);
    }

    public AxisAlignedBB getSelectionBox() {
        if (this.selectionBox == null) {
            return null;
        }
        return AxisAlignedBB.getBoundingBox((double)Math.min(this.selectionBox.minX, this.selectionBox.maxX), (double)Math.min(this.selectionBox.minY, this.selectionBox.maxY), (double)Math.min(this.selectionBox.minZ, this.selectionBox.maxZ), (double)Math.max(this.selectionBox.minX, this.selectionBox.maxX), (double)Math.max(this.selectionBox.minY, this.selectionBox.maxY), (double)Math.max(this.selectionBox.minZ, this.selectionBox.maxZ));
    }

    private void sendSelectionBox(EntityPlayer p) {
        VSP os = new VSP(23, "HCSMOD");
        if (this.selectionBox != null) {
            AxisAlignedBB selectionBox = this.getSelectionBox();
            try {
                os.writeDouble(selectionBox.minX);
                os.writeDouble(selectionBox.minY);
                os.writeDouble(selectionBox.minZ);
                os.writeDouble(selectionBox.maxX);
                os.writeDouble(selectionBox.maxY);
                os.writeDouble(selectionBox.maxZ);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        os.send(p);
    }

    public void sendDebug(int id, EntityPlayer player) {
        if (id < 0 || id >= this.lagCompDebug.size()) {
            VSP os = new VSP(127, "HCSMOD");
            os.send(player);
            return;
        }
        VSP os = new VSP(127, "HCSMOD");
        try {
            for (Line line : this.lagCompDebug.get(id)) {
                os.writeInt(line.color);
                os.writeDouble(line.x1);
                os.writeDouble(line.y1);
                os.writeDouble(line.z1);
                os.writeDouble(line.x2);
                os.writeDouble(line.y2);
                os.writeDouble(line.z2);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        os.send(player);
    }

    public void addLagCompDebug(Line[] debugLines) {
        this.lagCompDebug.add(debugLines);
        this.applyLagCompDebugLimit();
    }

    public void applyLagCompDebugLimit() {
        while (this.lagCompDebug.size() > this.lagCompMaxDebug) {
            this.lagCompDebug.remove(0);
        }
    }

    public static class InventoryMap {
        public int old;
        public int current;

        public InventoryMap(int old, int current) {
            this.old = old;
            this.current = current;
        }
    }
}

