/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  co.uk.flansmods.common.guns.ItemGun
 *  hcsmod.HCS
 *  net.minecraft.inventory.InventoryCrafting
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.crafting.IRecipe
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.world.World
 */
package hcsmod.server;

import co.uk.flansmods.common.guns.ItemGun;
import hcsmod.HCS;
import hcsmod.server.HcsServer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class RepairRecipe
implements IRecipe {
    public static final RepairRecipe instance = new RepairRecipe();
    private static final int[] PROTA = new int[]{2558, 2559, 2560, 2561};
    private static final int[] ITEMS_FOR_REPAIR = new int[]{7735, 7736, 7737, 7738, 7739, 7740, 7741, 7742, 7743, 7744, 7745, 7746, 7721, 7498, 7499, 7500, 7501, 7759, 7760, 7761, 7762, 7754, 7755, 7756, 7757, 7763, 7764, 7765, 7766, 7767, 7768, 7769, 7770, 7779, 7780, 7781, 7782, 7783, 7784, 7785, 7786, 7787, 7788, 7789, 7790, 7791, 7792, 7793, 7794};
    private static final int[] ITEMS_FOR_REPAIR_HARDCORE = new int[]{306, 307, 308, 309};

    private RepairRecipe() {
    }

    public boolean a(InventoryCrafting inventorycrafting, World world) {
        return this.a(inventorycrafting) != null;
    }

    public ItemStack a(InventoryCrafting inventorycrafting) {
        ItemStack is0 = null;
        ItemStack is1 = null;
        for (int j = 0; j < inventorycrafting.getSizeInventory(); ++j) {
            ItemStack is = inventorycrafting.getStackInSlot(j);
            if (is == null) continue;
            if (is0 == null) {
                is0 = is;
                continue;
            }
            if (is1 == null) {
                is1 = is;
                continue;
            }
            return null;
        }
        if (is0 != null && is1 != null) {
            ItemStack result;
            if (is0.getItem() == is1.getItem() && is0.stackSize == 1 && is1.stackSize == 1 && this.containsStrict((Object)is0.getItem(), (Object[])HCS.JAG)) {
                NBTTagCompound tag0 = is0.stackTagCompound;
                NBTTagCompound tag1 = is1.stackTagCompound;
                if (!(tag0 != null && tag1 != null && tag0.hasKey("juggerOwner") && tag1.hasKey("juggerOwner") && tag0.getString("juggerOwner").equals(tag1.getString("juggerOwner")))) {
                    return null;
                }
                int bullets = is0.getMaxDamage() - is0.getItemDamage() * 3 / 4;
                bullets += (is1.getMaxDamage() - is1.getItemDamage()) * 3 / 4;
                bullets = Math.min(bullets, is0.getMaxDamage());
                ItemStack result2 = new ItemStack(is0.getItem(), 1, is0.getMaxDamage() - bullets);
                result2.stackTagCompound = (NBTTagCompound)tag0.copy();
                return result2;
            }
            if (is0.stackSize == 1 && this.containsStrict((Object)is0.getItem(), (Object[])HCS.JAG) && is1.getItem() == Item.ingotIron || is1.stackSize == 1 && this.containsStrict((Object)is1.getItem(), (Object[])HCS.JAG) && is0.getItem() == Item.ingotIron) {
                if (is0.getItem() == Item.ingotIron) {
                    is0 = is1;
                }
                ItemStack result3 = new ItemStack(is0.getItem(), 1, Math.max(0, is0.getItemDamage() - 60));
                result3.stackTagCompound = (NBTTagCompound)is0.stackTagCompound.copy();
                return result3;
            }
            if (is0.getItem() == is1.getItem() && is0.stackSize == 1 && is1.stackSize == 1 && is0.isItemStackDamageable() && this.containsInt(is0.itemID, PROTA)) {
                int bullets = is0.getMaxDamage() - is0.getItemDamage() * 3 / 4;
                bullets += (is1.getMaxDamage() - is1.getItemDamage()) * 3 / 4;
                bullets = Math.min(bullets, is0.getMaxDamage());
                return new ItemStack(is0.getItem(), 1, is0.getMaxDamage() - bullets);
            }
            if (is0.stackSize == 1 && is1.stackSize == 1 && is0.isItemStackDamageable() && is0.getItem() instanceof ItemGun && is1.getUnlocalizedName().equals(Item.ingotIron.getUnlocalizedName()) || is1.getUnlocalizedName().equals(HCS.gunrepair1.getUnlocalizedName())) {
                NBTTagCompound tag0 = is0.stackTagCompound;
                if (is1.getUnlocalizedName().equals(Item.ingotIron.getUnlocalizedName())) {
                    result = new ItemStack(is0.getItem(), 1, is0.getItemDamage() - 100);
                    result.stackTagCompound = (NBTTagCompound)tag0.copy();
                    return result;
                }
                if (is1.getUnlocalizedName().equals(HCS.gunrepair1.getUnlocalizedName()) && is0.getItem() instanceof ItemGun) {
                    result = new ItemStack(is0.getItem(), 1, 0);
                    result.stackTagCompound = (NBTTagCompound)tag0.copy();
                    return result;
                }
            } else if (is0.stackSize == 1 && is1.stackSize == 1 && is0.isItemStackDamageable() && this.containsInt(is0.itemID, HcsServer.isHarxcoreServer ? ITEMS_FOR_REPAIR_HARDCORE : ITEMS_FOR_REPAIR) && is1.getUnlocalizedName().equals(Item.ingotIron.getUnlocalizedName()) || is1.getUnlocalizedName().equals(HCS.gunrepair1.getUnlocalizedName())) {
                NBTTagCompound tag0 = is0.stackTagCompound;
                if (is1.getUnlocalizedName().equals(Item.ingotIron.getUnlocalizedName())) {
                    result = new ItemStack(is0.getItem(), 1, is0.getItemDamage() - 60);
                    if (tag0 != null) {
                        result.stackTagCompound = (NBTTagCompound)tag0.copy();
                    }
                    return result;
                }
            }
        }
        return null;
    }

    public int a() {
        return 0;
    }

    public ItemStack b() {
        return null;
    }

    private boolean containsStrict(Object o, Object[] array) {
        for (Object a : array) {
            if (a != o) continue;
            return true;
        }
        return false;
    }

    private boolean containsInt(int i, int[] array) {
        for (int v : array) {
            if (v != i) continue;
            return true;
        }
        return false;
    }
}

