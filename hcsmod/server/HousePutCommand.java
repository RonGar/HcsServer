/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.items.ItemPalatka
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.command.ICommand
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.nbt.CompressedStreamTools
 *  net.minecraft.nbt.NBTBase
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.nbt.NBTTagDouble
 *  net.minecraft.nbt.NBTTagList
 *  net.minecraft.util.ChatMessageComponent
 *  net.minecraft.util.MovingObjectPosition
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.World
 *  vintarz.core.VSP
 */
package hcsmod.server;

import hcsmod.items.ItemPalatka;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.EntityHouseServer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import vintarz.core.VSP;

public class HousePutCommand
implements ICommand {
    private static final ChatMessageComponent no_houses = ChatMessageComponent.createFromText((String)"\u00a7c\u042d\u0442\u0430 \u043a\u043e\u043c\u043d\u0430\u0434\u0430 \u0434\u043b\u044f \u043f\u0435\u0440\u0435\u043d\u043e\u0441\u0430 \u0434\u043e\u043c\u043e\u0432, \u0430 \u043d\u0435 \u0434\u043b\u044f \u0442\u0435\u0431\u044f.");
    private static final ChatMessageComponent gethouse = ChatMessageComponent.createFromText((String)"\u00a7c\u041d\u0435-\u043d\u0435-\u043d\u0435, \u044d\u0442\u043e \u0442\u0430\u043a \u043d\u0435 \u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442. \u0421\u043d\u0430\u0447\u0430\u043b\u0430 \u00a7\u0430/gethouse");
    private static final ChatMessageComponent success = ChatMessageComponent.createFromText((String)"\u00a7a\u0414\u043e\u043c \u00a7f\u043f\u0435\u0440\u0435\u043d\u0435\u0441\u0451\u043d\u00a7a \u0438 \u00a7f\u043f\u0440\u043e\u0434\u043b\u0451\u043d\u00a7a \u043d\u0430 1 \u0434\u0435\u043d\u044c. \u041f\u0440\u0438\u044f\u0442\u043d\u043e\u0439 \u0438\u0433\u0440\u044b.");
    private static final ChatMessageComponent more = ChatMessageComponent.createFromText((String)"\u00a7d\u0423 \u0442\u0435\u0431\u044f \u0435\u0441\u0442\u044c \u0435\u0449\u0451 \u0434\u043e\u043c\u0430 \u0434\u043b\u044f \u043f\u0435\u0440\u0435\u043d\u043e\u0441\u0430, \u043f\u0440\u043e\u0434\u043e\u043b\u0436\u0430\u0439 \u0438\u0445 \u0441\u0442\u0430\u0432\u0438\u0442\u044c.");

    public String c() {
        return "puthouse";
    }

    public String c(ICommandSender icommandsender) {
        return null;
    }

    public List b() {
        return null;
    }

    public void b(ICommandSender icommandsender, String[] astring) {
        if (icommandsender instanceof EntityPlayerMP && astring.length == 0) {
            EntityPlayerMP p = (EntityPlayerMP)icommandsender;
            ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
            if (!ep.hasHousesForTransfer) {
                p.sendChatToPlayer(no_houses);
                return;
            }
            if (!ep.placeHouse) {
                p.sendChatToPlayer(gethouse);
                return;
            }
            World w = p.q;
            Vec3 vec3 = w.getWorldVec3Pool().getVecFromPool(p.u, p.v + (double)p.getEyeHeight(), p.w);
            Vec3 lookVec = p.aa();
            Vec3 vec31 = w.getWorldVec3Pool().getVecFromPool(p.u + lookVec.xCoord * 6.0, p.v + (double)p.getEyeHeight() + lookVec.yCoord * 6.0, p.w + lookVec.zCoord * 6.0);
            MovingObjectPosition b = w.rayTraceBlocks_do_do(vec3, vec31, false, true);
            if (b == null || b.sideHit != 1) {
                p.sendChatToPlayer(gethouse);
                return;
            }
            double x = b.hitVec.xCoord;
            double y = b.hitVec.yCoord;
            double z = b.hitVec.zCoord;
            float yaw = (float)((int)((p.A - 45.0f) / 90.0f)) * 90.0f + 45.0f;
            try {
                File file = new File("HouseTransfer/" + p.bu);
                NBTTagCompound tag = CompressedStreamTools.read((File)file);
                if (tag != null) {
                    NBTTagList list = tag.getTagList("houses");
                    NBTTagCompound house = (NBTTagCompound)list.removeTag(0);
                    EntityHouseServer entity = (EntityHouseServer)EntityList.createEntityFromNBT((NBTTagCompound)house, (World)w);
                    entity.a(x, y, z, yaw, 0.0f);
                    if (!ItemPalatka.checkPlacement((Entity)entity, (EntityPlayer)p)) {
                        return;
                    }
                    entity.removeTime += 86400000L;
                    entity.checkupTime += 43200000L;
                    w.spawnEntityInWorld((Entity)entity);
                    int vehs = entity.storedVehicles.tagCount();
                    for (int i = 0; i < vehs; ++i) {
                        NBTTagCompound veh = (NBTTagCompound)entity.storedVehicles.tagAt(i);
                        NBTTagList pos = new NBTTagList();
                        pos.appendTag((NBTBase)new NBTTagDouble(null, p.u));
                        pos.appendTag((NBTBase)new NBTTagDouble(null, p.E.maxY));
                        pos.appendTag((NBTBase)new NBTTagDouble(null, p.w));
                        veh.setTag("Pos", (NBTBase)pos);
                    }
                    if (list.tagCount() > 0) {
                        CompressedStreamTools.write((NBTTagCompound)tag, (File)file);
                    } else {
                        file.delete();
                    }
                    file = new File("HouseTransfer/" + p.bu + ".log");
                    FileOutputStream fos = new FileOutputStream(file, true);
                    fos.write(("Time:" + System.currentTimeMillis() + " PlaceX:" + x + " PlaceY:" + y + " PlaceZ:" + z + "\n").getBytes("UTF8"));
                    fos.close();
                    p.sendChatToPlayer(success);
                } else {
                    p.sendChatToPlayer(no_houses);
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
            ep.checkHousesForTransfer((EntityPlayer)p);
            if (ep.hasHousesForTransfer) {
                p.sendChatToPlayer(more);
            } else {
                ep.placeHouse = false;
                VSP os = new VSP(15, "HCSMOD");
                try {
                    os.writeBoolean(false);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                os.send((EntityPlayer)p);
            }
        }
    }

    public boolean a(ICommandSender icommandsender) {
        return true;
    }

    public List a(ICommandSender icommandsender, String[] astring) {
        return null;
    }

    public boolean a(String[] astring, int i) {
        return false;
    }

    public int compareTo(Object o) {
        return 0;
    }
}

