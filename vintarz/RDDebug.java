/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.common.ITickHandler
 *  cpw.mods.fml.common.event.FMLPreInitializationEvent
 *  cpw.mods.fml.common.registry.TickRegistry
 *  cpw.mods.fml.relauncher.Side
 *  vintarz.RD
 *  vintarz.RDDebug$CipherInput
 *  vintarz.RDDebug$CipherOutput
 *  vintarz.RDDebug$CipherUtil
 */
package vintarz;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import vintarz.RD;
import vintarz.RDDebug;

public class RDDebug {
    public static void setupLib() {
        String bit = System.getProperty("sun.arch.data.model");
        bit = System.getProperty("rdLib" + bit, bit);
        RD.library = System.getProperty("rdLibrary", RD.library).replace("%BIT%", bit);
    }

    public static void init1(FMLPreInitializationEvent init) {
        RDDebug.setupLib();
        TickRegistry.registerTickHandler((ITickHandler)new /* Unavailable Anonymous Inner Class!! */, (Side)Side.CLIENT);
    }

    public static void init2(FMLPreInitializationEvent init) {
        RDDebug.debug();
    }

    public static void main(String[] args) {
        RDDebug.setupLib();
        System.load(new File(RD.library).getAbsolutePath());
        RDDebug.debug();
    }

    public static void debug() {
        byte[] key = new byte[]{5, -40, 34, 25, 84, -127, -56, -39, -122, -84, 6, -85, 35, -76, -4, 54, 6, -55, -72, -51, -29, 75, -94, -33, 95, -59, 85, 62, -53, -57, -19, 2, 20, 20, 117, 49, -105, 20, 27, 102, -4, 93, -20, -40, -45, -60, -36, -94, 91, -17, 18, -22, -85, -68, -103, 67, -47, 111, -46, 75, -35, 107, -58, 43, -67, -72, -36, 45, 25, -41, 36, -62, 8, 42, 12, -35, 121, 93, 78, 18, -125, -87, 69, 69, -127, 34, 1, 19, -95, -120, 73, -38, 21, -65, -48, 29};
        int ivSize = 24;
        try {
            SecureRandom rng = (SecureRandom)CipherUtil.random.get();
            byte[] key2 = new byte[32 + rng.nextInt(64)];
            rng.nextBytes(key2);
            CipherOutput output = new CipherOutput(key, ivSize);
            output.write(key2.length);
            output.write(key2);
            output.write(255);
            output.write(255);
            byte[] result = RD.rd((byte[])output.toByteArray());
            DataInputStream input = new DataInputStream((InputStream)new CipherInput(result, key2));
            byte[] verify = new byte[key2.length];
            input.readFully(verify);
            for (int i = 0; i < verify.length; ++i) {
                if (verify[i] == key[i]) continue;
                System.out.println("invalid response");
                return;
            }
            int version = input.read();
            if (version == 0) {
                RDDebug.parseV1(input);
            } else {
                System.out.println("invalid version: " + version);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseV1(DataInputStream input) throws IOException {
        int r;
        while ((r = input.read()) != 0) {
            System.out.println("jni: " + r);
        }
        while ((r = input.read()) != 0) {
            System.out.println("obj: " + r);
        }
        while (input.available() > 0) {
            String modified = input.readUTF();
            System.out.println(modified);
        }
    }
}

