/*
 * Decompiled with CFR 0.150.
 */
package extendedDmgSrc;

import extendedDmgSrc.IExtendedDamageCalculator;

public class ExtendedDamageSource {
    public static IExtendedDamageCalculator calculator;
    public static BodyPart hitBodyPart;
    public static float armorPenetration;

    public static void reset() {
        hitBodyPart = null;
        armorPenetration = 0.0f;
    }

    public static enum BodyPart {
        HEAD,
        BODY,
        LEGS;

    }
}

