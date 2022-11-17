/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.common.registry.GameRegistry
 *  cpw.mods.fml.relauncher.Side
 *  hcsmod.HCS
 *  net.minecraft.block.Block
 *  net.minecraft.crash.CrashReport
 *  net.minecraft.crash.CrashReportCategory
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.stats.AchievementList
 *  net.minecraft.stats.StatBase
 *  net.minecraft.util.ReportedException
 *  net.minecraftforge.event.ForgeSubscribe
 *  net.minecraftforge.event.entity.player.EntityItemPickupEvent
 */
package hcsmod.server;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import hcsmod.HCS;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.util.ReportedException;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public class ItemPickupUtil {
    static Random rand = new Random();

    @ForgeSubscribe
    public void onVanillaPickup(EntityItemPickupEvent ev) {
        ev.setCanceled(true);
    }

    public static void pickupItem(EntityItem ei, EntityPlayer p) {
        ItemStack itemstack = ei.getEntityItem();
        int i = itemstack.stackSize;
        if (i <= 0 || ItemPickupUtil.addToInv(itemstack, p)) {
            if (itemstack.itemID == Block.wood.blockID) {
                p.triggerAchievement((StatBase)AchievementList.mineWood);
            }
            if (itemstack.itemID == Item.leather.itemID) {
                p.triggerAchievement((StatBase)AchievementList.killCow);
            }
            if (itemstack.itemID == Item.diamond.itemID) {
                p.triggerAchievement((StatBase)AchievementList.diamonds);
            }
            if (itemstack.itemID == Item.blazeRod.itemID) {
                p.triggerAchievement((StatBase)AchievementList.blazeRod);
            }
            GameRegistry.onPickupNotification((EntityPlayer)p, (EntityItem)ei);
            p.playSound("random.pop", 0.2f, ((rand.nextFloat() - rand.nextFloat()) * 0.7f + 1.0f) * 2.0f);
            p.a((Entity)ei, i);
            if (itemstack.stackSize <= 0) {
                ei.x();
            }
        }
    }

    public static boolean addToInv(ItemStack par1ItemStack, EntityPlayer thePlayer) {
        if (par1ItemStack == null) {
            return false;
        }
        if (par1ItemStack.stackSize == 0) {
            return false;
        }
        try {
            int i;
            if (par1ItemStack.isItemDamaged()) {
                int i2 = thePlayer.inventory.getFirstEmptyStack();
                if (i2 >= 0 && i2 < HCS.getBackpackLVL((EntityPlayer)thePlayer, (Side)Side.SERVER) * 9 + 9) {
                    thePlayer.inventory.mainInventory[i2] = ItemStack.copyItemStack((ItemStack)par1ItemStack);
                    thePlayer.inventory.mainInventory[i2].animationsToGo = 5;
                    par1ItemStack.stackSize = 0;
                    return true;
                }
                if (thePlayer.inventory.player.capabilities.isCreativeMode) {
                    par1ItemStack.stackSize = 0;
                    return true;
                }
                return false;
            }
            do {
                i = par1ItemStack.stackSize;
                par1ItemStack.stackSize = ItemPickupUtil.storePartialItemStack(par1ItemStack, thePlayer);
            } while (par1ItemStack.stackSize > 0 && par1ItemStack.stackSize < i);
            if (par1ItemStack.stackSize == i && thePlayer.inventory.player.capabilities.isCreativeMode) {
                par1ItemStack.stackSize = 0;
                return true;
            }
            return par1ItemStack.stackSize < i;
        }
        catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport((Throwable)throwable, (String)"Adding item to inventory");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being added");
            crashreportcategory.addCrashSection("Item ID", (Object)par1ItemStack.itemID);
            crashreportcategory.addCrashSection("Item data", (Object)par1ItemStack.getItemDamage());
            throw new ReportedException(crashreport);
        }
    }

    private static int storePartialItemStack(ItemStack par1ItemStack, EntityPlayer player) {
        int i = par1ItemStack.itemID;
        int j = par1ItemStack.stackSize;
        if (par1ItemStack.getMaxStackSize() == 1) {
            int k = player.inventory.getFirstEmptyStack();
            if (k < 0 && k < HCS.getBackpackLVL((EntityPlayer)player, (Side)Side.SERVER) * 9 + 9) {
                return j;
            }
            if (k < HCS.getBackpackLVL((EntityPlayer)player, (Side)Side.SERVER) * 9 + 9) {
                if (player.inventory.mainInventory[k] == null) {
                    player.inventory.mainInventory[k] = ItemStack.copyItemStack((ItemStack)par1ItemStack);
                }
                return 0;
            }
            return j;
        }
        int k = ItemPickupUtil.storeItemStack(par1ItemStack, player);
        if (k < 0 && k < HCS.getBackpackLVL((EntityPlayer)player, (Side)Side.SERVER) * 9 + 9) {
            k = player.inventory.getFirstEmptyStack();
        }
        if (k < 0 && k < HCS.getBackpackLVL((EntityPlayer)player, (Side)Side.SERVER) * 9 + 9) {
            return j;
        }
        if (k < HCS.getBackpackLVL((EntityPlayer)player, (Side)Side.SERVER) * 9 + 9) {
            if (player.inventory.mainInventory[k] == null) {
                player.inventory.mainInventory[k] = new ItemStack(i, 0, par1ItemStack.getItemDamage());
                if (par1ItemStack.hasTagCompound()) {
                    player.inventory.mainInventory[k].setTagCompound((NBTTagCompound)par1ItemStack.getTagCompound().copy());
                }
            }
            int l = j;
            if (j > player.inventory.mainInventory[k].getMaxStackSize() - player.inventory.mainInventory[k].stackSize) {
                l = player.inventory.mainInventory[k].getMaxStackSize() - player.inventory.mainInventory[k].stackSize;
            }
            if (l > player.inventory.getInventoryStackLimit() - player.inventory.mainInventory[k].stackSize) {
                l = player.inventory.getInventoryStackLimit() - player.inventory.mainInventory[k].stackSize;
            }
            if (l == 0) {
                return j;
            }
            player.inventory.mainInventory[k].stackSize += l;
            player.inventory.mainInventory[k].animationsToGo = 5;
            return j -= l;
        }
        return j;
    }

    private static int storeItemStack(ItemStack par1ItemStack, EntityPlayer p) {
        for (int i = 0; i < HCS.getBackpackLVL((EntityPlayer)p, (Side)Side.SERVER) * 9 + 9; ++i) {
            if (p.inventory.mainInventory[i] == null || p.inventory.mainInventory[i].itemID != par1ItemStack.itemID || !p.inventory.mainInventory[i].isStackable() || p.inventory.mainInventory[i].stackSize >= p.inventory.mainInventory[i].getMaxStackSize() || p.inventory.mainInventory[i].stackSize >= p.inventory.getInventoryStackLimit() || p.inventory.mainInventory[i].getHasSubtypes() && p.inventory.mainInventory[i].getItemDamage() != par1ItemStack.getItemDamage() || !ItemStack.areItemStackTagsEqual((ItemStack)p.inventory.mainInventory[i], (ItemStack)par1ItemStack)) continue;
            return i;
        }
        return -1;
    }
}

