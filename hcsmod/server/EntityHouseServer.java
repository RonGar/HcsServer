/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  co.uk.flansmods.common.driveables.EntityDriveable
 *  cpw.mods.fml.common.registry.IEntityAdditionalSpawnData
 *  hcsmod.HCS
 *  hcsmod.cunches.IItemVehicle
 *  hcsmod.cunches.IVehicle
 *  hcsmod.effects.Effect
 *  hcsmod.entity.EntityHouseCommon
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.inventory.IInventory
 *  net.minecraft.item.ItemArmor
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NBTBase
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.nbt.NBTTagList
 *  net.minecraft.nbt.NBTTagString
 *  net.minecraft.potion.Potion
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.ChatMessageComponent
 *  net.minecraft.util.DamageSource
 *  net.minecraft.world.World
 *  vintarz.core.VSP
 */
package hcsmod.server;

import co.uk.flansmods.common.driveables.EntityDriveable;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import hcsmod.HCS;
import hcsmod.cunches.IItemVehicle;
import hcsmod.cunches.IVehicle;
import hcsmod.effects.Effect;
import hcsmod.entity.EntityHouseCommon;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.HcsServer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import vintarz.core.VSP;

public class EntityHouseServer
extends EntityHouseCommon
implements IInventory,
IEntityAdditionalSpawnData {
    private static final boolean FREEZE_HOUSES = Boolean.parseBoolean(System.getProperty("vz.hcs.house.freeze", "false"));
    private static final boolean ENABLE_UPDATE = Boolean.parseBoolean(System.getProperty("vz.hcs.house.update", "true"));
    private static List<double[]> wrongPlaces = new ArrayList<double[]>();
    public double playerX;
    public double playerY;
    public double playerZ;
    private static final ChatMessageComponent VEH_DESTROYED;
    public String lastUser = "unknown";
    public String placedBy = "";
    String password = "";
    public long checkupTime = 0L;
    int hitNum;
    int hitTime;
    boolean free;
    List<String> users = new ArrayList<String>();
    private int opened;
    NBTTagList storedVehicles = new NBTTagList();
    int vehTicks;
    private Entity wasRiddenBy;
    int checkupDebug = 0;
    public byte[] saveDebug;
    public ItemStack[] inventory = new ItemStack[54];

    public static boolean wrongPlace(double x, double z) {
        for (double[] tmp : wrongPlaces) {
            double len;
            double dz;
            double dx = tmp[0] - x;
            if (!(dx * dx + (dz = tmp[1] - z) * dz <= (len = tmp[2] + 150.0) * len)) continue;
            return true;
        }
        return false;
    }

    public EntityHouseServer(World w) {
        super(w);
        if (HcsServer.isPVPserver) {
            this.M = true;
        }
    }

    public EntityHouseServer(EntityPlayer p, double X, double Y, double Z, float rotation, long removeTime, boolean free) {
        this(p.q);
        this.placedBy = p.username;
        this.a(X, Y, Z, rotation, 0.0f);
        this.removeTime = removeTime;
        this.free = free;
    }

    void setStoveEnabled(boolean value) {
        byte prev = this.v().getWatchableObjectByte(7);
        this.v().updateObject(7, (Object)((byte)(prev & 2 | (value ? 1 : 0))));
    }

    private void setStoveCharged(boolean value) {
        byte prev = this.v().getWatchableObjectByte(7);
        this.v().updateObject(7, (Object)((byte)(prev & 1 | (value ? 2 : 0))));
    }

    protected void a(NBTTagCompound nbttagcompound) {
        NBTTagCompound nbttagcompound1;
        int i;
        this.free = nbttagcompound.hasKey("isFree");
        this.lastUser = nbttagcompound.getString("lastUser");
        this.playerX = nbttagcompound.getDouble("playerX");
        this.playerY = nbttagcompound.getDouble("playerY");
        this.playerZ = nbttagcompound.getDouble("playerZ");
        this.removeTime = nbttagcompound.getLong("removeTime");
        this.placedBy = nbttagcompound.getString("placedBy");
        this.checkupTime = nbttagcompound.getLong("checkupTime1");
        this.setStoveEnabled(nbttagcompound.getBoolean("stoveEnabled"));
        this.setStoveCharged(nbttagcompound.getBoolean("stoveCharged"));
        if (nbttagcompound.hasKey("password")) {
            this.password = nbttagcompound.getString("password");
        }
        NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
        this.inventory = new ItemStack[this.j_()];
        for (i = 0; i < nbttaglist.tagCount(); ++i) {
            nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 0xFF;
            if (j >= this.inventory.length) continue;
            this.inventory[j] = ItemStack.loadItemStackFromNBT((NBTTagCompound)nbttagcompound1);
            if (this.inventory[j] == null || !(this.inventory[j].getItem() instanceof IItemVehicle) && !HcsServer.isBannedItem(this.inventory[j])) continue;
            this.inventory[j] = null;
        }
        nbttaglist = nbttagcompound.getTagList("authorized");
        this.users.clear();
        if (nbttaglist != null) {
            for (i = 0; i < nbttaglist.tagCount(); ++i) {
                nbttagcompound1 = (NBTTagString)nbttaglist.tagAt(i);
                this.users.add(nbttagcompound1.data);
            }
        }
        if ((nbttaglist = nbttagcompound.getTagList("saved_vehs")) != null) {
            this.storedVehicles = nbttaglist;
        }
        if (HcsServer.isHarxcoreServer) {
            NBTTagList storedVehicles = this.storedVehicles;
            this.storedVehicles = new NBTTagList();
            for (int $ = 0; $ < storedVehicles.tagCount(); ++$) {
                NBTTagCompound tag = (NBTTagCompound)storedVehicles.tagAt($);
                String aircraftType = tag.getString("TypeName");
                if (!aircraftType.isEmpty() && !HcsServer.isAllowedAircraft(aircraftType)) continue;
                this.storedVehicles.appendTag((NBTBase)tag);
            }
        }
        this.validateStoredVehicles();
        this.rebuildStoredData();
        if (nbttagcompound.hasKey("HouseSaveDebug")) {
            this.saveDebug = nbttagcompound.getByteArray("HouseSaveDebug");
        }
    }

    protected void b(NBTTagCompound nbttagcompound) {
        if (this.free) {
            nbttagcompound.setBoolean("isFree", true);
        }
        nbttagcompound.setString("lastUser", this.lastUser);
        nbttagcompound.setDouble("playerX", this.playerX);
        nbttagcompound.setDouble("playerY", this.playerY);
        nbttagcompound.setDouble("playerZ", this.playerZ);
        nbttagcompound.setLong("removeTime", this.removeTime);
        nbttagcompound.setString("placedBy", this.placedBy);
        nbttagcompound.setLong("checkupTime1", this.checkupTime);
        nbttagcompound.setBoolean("stoveEnabled", this.isStoveEnabled());
        nbttagcompound.setBoolean("stoveCharged", this.isStoveCharged());
        if (!this.password.isEmpty()) {
            nbttagcompound.setString("password", this.password);
        }
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < this.inventory.length; ++i) {
            if (this.inventory[i] == null || HcsServer.isBannedItem(this.inventory[i])) continue;
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Slot", (byte)i);
            this.inventory[i].writeToNBT(nbttagcompound1);
            nbttaglist.appendTag((NBTBase)nbttagcompound1);
        }
        nbttagcompound.setTag("Items", (NBTBase)nbttaglist);
        nbttaglist = new NBTTagList();
        for (String str : this.users) {
            NBTTagString nbttagcompound1 = new NBTTagString(str, str);
            nbttaglist.appendTag((NBTBase)nbttagcompound1);
        }
        nbttagcompound.setTag("authorized", (NBTBase)nbttaglist);
        this.validateStoredVehicles();
        nbttagcompound.setTag("saved_vehs", (NBTBase)this.storedVehicles);
        if (this.saveDebug != null) {
            nbttagcompound.setByteArray("HouseSaveDebug", this.saveDebug);
        }
    }

    public AxisAlignedBB E() {
        return this.E;
    }

    public boolean a(DamageSource par1DamageSource, float par2) {
        if (!this.q.isRemote && !this.M && par1DamageSource.getEntity() != null && par1DamageSource.getEntity() instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP)par1DamageSource.getEntity();
            if (p == this.n && p.bu.equals(this.placedBy)) {
                this.sendOpenGui((EntityPlayer)p);
            } else if (p.bG.isCreativeMode) {
                this.hitTime = 10;
                ++this.hitNum;
                if (this.hitNum < 5) {
                    return true;
                }
                if (this.n == null) {
                    for (int i = 0; i < this.j_(); ++i) {
                        ItemStack itemstack = this.a(i);
                        if (itemstack == null) continue;
                        float f = this.ab.nextFloat() * 0.8f + 0.1f;
                        float f1 = this.ab.nextFloat() * 0.8f + 0.1f;
                        float f2 = this.ab.nextFloat() * 0.8f + 0.1f;
                        EntityItem entityitem = new EntityItem(this.q, this.u + (double)f, this.v + (double)f1, this.w + (double)f2, itemstack);
                        float f3 = 0.05f;
                        entityitem.x = (float)this.ab.nextGaussian() * f3;
                        entityitem.y = (float)this.ab.nextGaussian() * f3 + 0.2f;
                        entityitem.z = (float)this.ab.nextGaussian() * f3;
                        this.q.spawnEntityInWorld((Entity)entityitem);
                    }
                }
                this.x();
                return true;
            }
        }
        return false;
    }

    public boolean c(EntityPlayer p) {
        if (this.q.isRemote || !p.T()) {
            return false;
        }
        ItemStack is = p.getCurrentEquippedItem();
        if (is != null && is.itemID == 510) {
            p.a(ChatMessageComponent.createFromText((String)("Owner(\"" + this.placedBy + "\"); LastUser(\"" + this.lastUser + "\"); Authorized:")));
            StringBuilder sb = new StringBuilder();
            for (String s : this.users) {
                if (sb.length() + s.length() > 250) {
                    p.a(ChatMessageComponent.createFromText((String)sb.toString()));
                    sb.setLength(0);
                }
                sb.append(s);
                sb.append(' ');
            }
            p.a(ChatMessageComponent.createFromText((String)sb.toString()));
            return true;
        }
        if (this.v().getWatchableObjectByte(8) > 0) {
            this.checkInv();
            p.displayGUIChest((IInventory)this);
            return false;
        }
        if (is != null && is.itemID == HCS.Palatka.itemID) {
            switch (is.getItemDamage()) {
                case 1: {
                    this.removeTime += TimeUnit.MILLISECONDS.convert(3L, TimeUnit.DAYS);
                    break;
                }
                case 2: {
                    this.removeTime += TimeUnit.MILLISECONDS.convert(7L, TimeUnit.DAYS);
                    break;
                }
                case 3: {
                    this.removeTime += TimeUnit.MILLISECONDS.convert(30L, TimeUnit.DAYS);
                    break;
                }
                case 4: {
                    this.removeTime += TimeUnit.MILLISECONDS.convert(90L, TimeUnit.DAYS);
                    break;
                }
                case 5: {
                    if (!this.free) break;
                    this.removeTime += TimeUnit.MILLISECONDS.convert(2L, TimeUnit.DAYS);
                }
            }
            --is.stackSize;
            return true;
        }
        if (p.capabilities.isCreativeMode || this.placedBy.equals(p.username) || this.users.contains(p.username)) {
            this.use(p);
        } else {
            this.sendOpenGui(p);
        }
        return false;
    }

    public void checkUserAndUse(EntityPlayer p, String password) {
        if (p == this.n && p.username.equals(this.placedBy)) {
            this.password = password;
            this.users.clear();
        } else if (!this.password.isEmpty() && this.password.equals(password)) {
            this.users.add(p.username);
            this.use(p);
        }
    }

    private void use(EntityPlayer p) {
        if (FREEZE_HOUSES) {
            p.a(ChatMessageComponent.createFromText((String)"\u0414\u043e\u043c\u0430 \u0437\u0430\u043c\u043e\u0440\u043e\u0436\u0435\u043d\u044b. \u0412\u0437\u0430\u0438\u043c\u043e\u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435 \u043d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e."));
            return;
        }
        boolean flag = p.capabilities.isCreativeMode;
        if (p == this.n) {
            this.checkInv();
            p.displayGUIChest((IInventory)this);
        } else if (p.ah() && (this.n != null || this.placedBy.equals(p.username) || flag)) {
            this.checkInv();
            p.displayGUIChest((IInventory)this);
        } else if (this.n == null && (this.placedBy.equals(p.username) || flag)) {
            if (this.checkPvpAccess(p)) {
                p.b(false);
                this.playerX = p.u;
                this.playerY = p.v;
                this.playerZ = p.w;
                p.mountEntity((Entity)this);
            }
        } else {
            return;
        }
        if (!p.capabilities.isCreativeMode) {
            this.lastUser = p.username;
        }
    }

    private boolean checkPvpAccess(EntityPlayer p) {
        long time = ExtendedPlayer.server((EntityPlayer)p).housePvpTimeout - System.currentTimeMillis();
        if (time > 0L) {
            p.a(ChatMessageComponent.createFromText((String)("\u0422\u044b \u0443\u0447\u0430\u0441\u0442\u0432\u043e\u0432\u0430\u043b \u0432 \u043f\u0432\u043f, \u0432\u0445\u043e\u0434 \u0432 \u0434\u043e\u043c \u0437\u0430\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043d \u043d\u0430 " + time / 1000L + "\u0441.")));
            return false;
        }
        return true;
    }

    private void checkInv() {
        for (int i = 0; i < this.inventory.length; ++i) {
            if (this.inventory[i] == null || this.inventory[i].stackSize <= this.inventory[i].getMaxStackSize()) continue;
            this.inventory[i].stackSize = this.inventory[i].getMaxStackSize();
        }
    }

    public boolean L() {
        return !this.M;
    }

    public void l_() {
        if (this.removeTime == -1L) {
            if (!this.q.isRemote) {
                this.x();
            }
            return;
        }
        if (ENABLE_UPDATE) {
            if (this.removeTime < System.currentTimeMillis()) {
                this.x();
                return;
            }
            long currentTime = System.currentTimeMillis();
            if (this.ac % 20 == 0) {
                this.v().updateObject(8, (Object)((byte)(HcsServer.playerHasData(this.placedBy, "cheater") ? 1 : 0)));
            }
            if (this.wasRiddenBy != null && this.n == null && this.wasRiddenBy instanceof EntityLivingBase) {
                EntityLivingBase elb = (EntityLivingBase)this.wasRiddenBy;
                elb.setPositionAndUpdate(this.playerX, this.playerY, this.playerZ);
            }
            this.wasRiddenBy = this.n;
            if (this.vehTicks > 0) {
                --this.vehTicks;
            }
            if (this.hitNum > 0 && --this.hitTime < 0) {
                this.hitTime = 0;
                this.hitNum = 0;
            }
            if (!(this.checkupDebug <= 0 || this.n instanceof EntityPlayer && ((EntityPlayer)this.n).capabilities.isCreativeMode)) {
                this.checkupDebug = 0;
            }
            if (this.checkupTime <= 0L) {
                this.checkupTime = currentTime;
            }
            while (this.checkupTime <= currentTime) {
                this.checkupTime += 3600000L;
                if (HcsServer.isLiteserver) continue;
                this.setStoveCharged(false);
                if (this.isStoveEnabled()) {
                    for (int i = 0; i < this.inventory.length; ++i) {
                        if (this.inventory[i] == null || this.inventory[i].itemID != 17) continue;
                        if (--this.inventory[i].stackSize <= 0) {
                            this.inventory[i] = null;
                        }
                        this.setStoveCharged(true);
                        break;
                    }
                }
                if (this.isStoveCharged()) continue;
                for (int i = 0; i < this.inventory.length; ++i) {
                    if (this.inventory[i] == null || !this.inventory[i].getItem().isDamageable() || !(this.inventory[i].getItem() instanceof ItemArmor)) continue;
                    int dmg = this.inventory[i].getItemDamage() + 1;
                    this.inventory[i].setItemDamage(dmg);
                    if (dmg <= this.inventory[i].getMaxDamage() || --this.inventory[i].stackSize > 0) continue;
                    this.inventory[i] = null;
                }
                NBTTagList storedVehicles = this.storedVehicles;
                this.storedVehicles = new NBTTagList();
                for (int $ = 0; $ < storedVehicles.tagCount(); ++$) {
                    NBTBase base = storedVehicles.tagAt($);
                    IVehicle o = this.nbt2veh(base);
                    if (o instanceof EntityDriveable) {
                        EntityDriveable veh = (EntityDriveable)o;
                        ItemStack[] inventory = veh.driveableData.cargo;
                        for (int i = 0; i < inventory.length; ++i) {
                            if (inventory[i] == null || !inventory[i].getItem().isDamageable() || !(inventory[i].getItem() instanceof ItemArmor)) continue;
                            int dmg = inventory[i].getItemDamage() + 2;
                            inventory[i].setItemDamage(dmg);
                            if (dmg <= inventory[i].getMaxDamage() || --inventory[i].stackSize > 0) continue;
                            inventory[i] = null;
                        }
                        this.storedVehicles.appendTag((NBTBase)this.veh2nbt(o));
                        continue;
                    }
                    this.storedVehicles.appendTag(base);
                }
            }
        }
        if (this.n != null && this.n instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)this.n;
            if (this.removeTime > -1L && !this.q.isRemote && this.ac % 5 == 0) {
                p.f(0.05f);
            }
            if (p.b((Potion)Effect.bleeding) != null) {
                p.b((Potion)Effect.bleeding).duration -= 9;
            }
            if (p.b(Potion.moveSlowdown) != null) {
                p.b((Potion)Potion.moveSlowdown).duration -= 9;
            }
        }
        super.l_();
    }

    public int j_() {
        return this.inventory.length;
    }

    public ItemStack a(int i) {
        return this.inventory[i];
    }

    public ItemStack a(int par1, int par2) {
        if (this.inventory[par1] != null) {
            if (this.inventory[par1].stackSize <= par2) {
                ItemStack itemstack = this.inventory[par1];
                this.inventory[par1] = null;
                return itemstack;
            }
            ItemStack itemstack = this.inventory[par1].splitStack(par2);
            if (this.inventory[par1].stackSize == 0) {
                this.inventory[par1] = null;
            }
            return itemstack;
        }
        return null;
    }

    public ItemStack a_(int i) {
        if (this.inventory[i] != null) {
            ItemStack itemstack = this.inventory[i];
            this.inventory[i] = null;
            return itemstack;
        }
        return null;
    }

    public void a(int par1, ItemStack par2ItemStack) {
        this.inventory[par1] = par2ItemStack;
        if (par2ItemStack != null && par2ItemStack.stackSize > this.d()) {
            par2ItemStack.stackSize = this.d();
        }
    }

    public String b() {
        if (this.removeTime > -1L) {
            if (this.v().getWatchableObjectByte(8) > 0) {
                return "VhS" + this.k + ":\u0414\u043e\u043c \u0447\u0438\u0442\u0435\u0440\u0430 " + this.placedBy;
            }
            if (this.free) {
                return "VhS" + this.k + ":\u0425\u0430\u043b\u044f\u0432\u0430 " + TimeUnit.DAYS.convert(this.removeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS) + "\u0434 " + TimeUnit.HOURS.convert(this.removeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS) % 24L + "\u0447";
            }
            return "VhS" + this.k + ":\u0414\u043e\u043c " + TimeUnit.DAYS.convert(this.removeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS) + "\u0434 " + TimeUnit.HOURS.convert(this.removeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS) % 24L + "\u0447";
        }
        return "HouseNonsense.";
    }

    public boolean c() {
        return false;
    }

    public int d() {
        return 64;
    }

    public void e() {
    }

    public boolean a(EntityPlayer entityplayer) {
        return !this.M && entityplayer.e((Entity)this) <= 36.0;
    }

    public void k_() {
        ++this.opened;
    }

    public void g() {
        if (this.opened > 0) {
            --this.opened;
        }
    }

    public boolean b(int i, ItemStack itemstack) {
        return true;
    }

    private void sendOpenGui(EntityPlayer p) {
        try {
            VSP os = new VSP(5, "HCSMOD");
            os.writeInt(this.k);
            os.send(p);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public String[] listSavedVehs() {
        return this.v().getWatchableObjectString(6).split("\n");
    }

    public void validateStoredVehicles() {
        for (int i = 0; i < this.storedVehicles.tagCount(); ++i) {
            NBTBase base = this.storedVehicles.tagAt(i);
            if (!(base instanceof NBTTagCompound)) continue;
            NBTTagCompound vehicle = (NBTTagCompound)base;
            Iterator iterator = vehicle.getTags().iterator();
            while (iterator.hasNext()) {
                ItemStack is;
                NBTTagCompound tag;
                Object o = iterator.next();
                if (!(o instanceof NBTTagCompound) || !(tag = (NBTTagCompound)o).e().startsWith("Cargo") || (is = ItemStack.loadItemStackFromNBT((NBTTagCompound)tag)) != null && !HcsServer.isBannedItem(is)) continue;
                iterator.remove();
            }
        }
    }

    private void rebuildStoredData() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.storedVehicles.tagCount(); ++i) {
            NBTBase tag = this.storedVehicles.tagAt(i);
            if (!(tag instanceof NBTTagCompound)) continue;
            NBTTagCompound kek = (NBTTagCompound)tag;
            sb.append(kek.getString("kek"));
            sb.append('\n');
        }
        if (sb.length() < 64) {
            this.v().updateObject(6, (Object)sb.toString());
        } else {
            this.storedVehicles = new NBTTagList();
            this.v().updateObject(6, (Object)"");
        }
    }

    public void storeVehicle(IVehicle veh, EntityPlayer p) {
        String locked;
        if (veh.entity().isDead) {
            return;
        }
        if (veh.entity() instanceof EntityDriveable && (locked = ((EntityDriveable)veh).locked()) != null && !locked.equals(p.username)) {
            p.a(ChatMessageComponent.createFromText((String)"\u00a7c\u041d\u0435 \u0442\u0432\u043e\u044f \u043c\u0430\u0448\u0438\u043d\u0430!"));
            return;
        }
        if (this.vehTicks == 0) {
            this.vehTicks = 5;
            if (this.storedVehicles.tagCount() < 3) {
                NBTTagCompound tag = this.veh2nbt(veh);
                veh.entity().isDead = true;
                if (tag != null) {
                    this.storedVehicles.appendTag((NBTBase)tag);
                } else {
                    p.a(VEH_DESTROYED);
                }
                this.rebuildStoredData();
            }
        }
    }

    public void spawnVehicle(int slot, EntityPlayer p) {
        if (this.vehTicks == 0 && this.storedVehicles.tagCount() > slot) {
            this.vehTicks = 5;
            IVehicle v = this.nbt2veh(this.storedVehicles.removeTag(slot));
            if (v != null) {
                boolean flag = v.entity().forceSpawn;
                v.entity().forceSpawn = true;
                if (v.entity() instanceof EntityDriveable) {
                    EntityDriveable ed = (EntityDriveable)v.entity();
                    ed.placed = 0L;
                    ed.lock(p.username);
                }
                Entity e = v.entity();
                e.setPosition(e.posX, e.posY + (double)1.1f, e.posZ);
                this.q.spawnEntityInWorld(e);
                v.entity().forceSpawn = flag;
            } else {
                p.a(VEH_DESTROYED);
            }
            this.rebuildStoredData();
        }
    }

    private NBTTagCompound veh2nbt(IVehicle veh) {
        NBTTagCompound tag = new NBTTagCompound(veh.vehName());
        tag.setString("kek", veh.vehName());
        if (veh.entity().writeToNBTOptional(tag)) {
            return tag;
        }
        return null;
    }

    private IVehicle nbt2veh(NBTBase b) {
        if (b instanceof NBTTagCompound) {
            NBTTagCompound tag = (NBTTagCompound)b;
            tag.removeTag("AircraftUniqueId");
            tag.removeTag("HeliUniqueId");
            try {
                Entity e = EntityList.createEntityFromNBT((NBTTagCompound)tag, (World)this.q);
                if (e instanceof IVehicle) {
                    return (IVehicle)e;
                }
            }
            catch (Error e) {
                throw new RuntimeException("Failed to read vehicle: " + tag.getString("id"), e);
            }
        }
        return null;
    }

    public boolean d(NBTTagCompound par1NBTTagCompound) {
        String s = this.Q();
        if (!this.M && s != null && !EntityHouseServer.wrongPlace(this.u, this.w)) {
            par1NBTTagCompound.setString("id", s);
            this.e(par1NBTTagCompound);
            return true;
        }
        return false;
    }

    public boolean c(NBTTagCompound par1NBTTagCompound) {
        return false;
    }

    static {
        try {
            File secondary = new File("./housezones.txt");
            Scanner in = secondary.isFile() ? new Scanner(secondary) : new Scanner(EntityHouseServer.class.getResourceAsStream("/housezones.txt"));
            while (in.hasNextLine()) {
                String[] s = in.nextLine().split(" ");
                wrongPlaces.add(new double[]{Double.parseDouble(s[0]), Double.parseDouble(s[1]), Double.parseDouble(s[2])});
            }
            in.close();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        VEH_DESTROYED = ChatMessageComponent.createFromText((String)"\u042d\u0442\u043e \u0442\u0440\u0430\u043d\u0441\u043f\u043e\u0440\u0442\u043d\u043e\u0435 \u0441\u0440\u0435\u0434\u0441\u0442\u0432\u043e \u0431\u044b\u043b\u043e \u043f\u043e\u0432\u0440\u0435\u0436\u0434\u0435\u043d\u043e.");
    }
}

