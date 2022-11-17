/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayerMP
 */
package hcsmod.server;

import extendedDmgSrc.IExtendedDamageCalculator;
import hcsmod.server.HarxCoreArmor;
import hcsmod.server.HcsServer;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

public class ExtendedDamageCalculator
implements IExtendedDamageCalculator {
    public static float ARMOR_DESTRUCTION_MULTIPLIER = 1.0f;
    public static int[] ids;
    public static float[] values;

    public static float getValue(int input) {
        if (ids == null || input == 0) {
            return 0.0f;
        }
        for (int i = 0; i < ids.length; ++i) {
            if (ids[i] != input) continue;
            return values[i];
        }
        return 0.0f;
    }

    public static float damage(float damage, EntityPlayerMP player, int armorSlot) {
        int armor = HarxCoreArmor.getValue(player.bn.armorInventory[armorSlot]);
        if (armor > 0) {
            int armorDamage = (int)(damage * ARMOR_DESTRUCTION_MULTIPLIER);
            if (armorDamage < 1) {
                armorDamage = 1;
            }
            player.bn.armorInventory[armorSlot].damageItem(armorDamage, (EntityLivingBase)player);
            if (player.bn.armorInventory[armorSlot].stackSize == 0) {
                player.bn.armorInventory[armorSlot] = null;
            }
            damage *= 1.0f - ExtendedDamageCalculator.getValue(armor);
        }
        return damage;
    }

    @Override
    public float damageHead(EntityPlayerMP player, float damage) {
        return ExtendedDamageCalculator.damage(damage, player, 3) * 1.5f;
    }

    @Override
    public float damageBody(EntityPlayerMP player, float damage) {
        return ExtendedDamageCalculator.damage(damage, player, 2);
    }

    @Override
    public float damageLegs(EntityPlayerMP player, float damage) {
        return ExtendedDamageCalculator.damage(damage, player, 1) * 0.75f;
    }

    @Override
    public float damageUnspecified(EntityPlayerMP player, float damage) {
        float head = this.damageHead(player, damage / 3.0f);
        float body = this.damageBody(player, damage / 3.0f);
        float legs = this.damageLegs(player, damage / 3.0f);
        return head + body + legs;
    }

    public static void reload() {
        Properties properties = new Properties();
        try (FileInputStream in2 = new FileInputStream("hardcore/damage_reduction_percent.properties");){
            properties.load(in2);
        }
        catch (IOException in2) {
            // empty catch block
        }
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ArrayList<Float> values = new ArrayList<Float>();
        for (Map.Entry<Object, Object> e : properties.entrySet()) {
            ids.add(Integer.parseUnsignedInt((String)e.getKey()));
            values.add(Float.valueOf((float)Integer.parseUnsignedInt((String)e.getValue()) / 100.0f));
        }
        ExtendedDamageCalculator.ids = HcsServer.intListToArray(ids);
        ExtendedDamageCalculator.values = ExtendedDamageCalculator.floatListToArray(values);
    }

    public static float[] floatListToArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < array.length; ++i) {
            array[i] = list.get(i).floatValue();
        }
        return array;
    }

    static {
        ExtendedDamageCalculator.reload();
    }
}

