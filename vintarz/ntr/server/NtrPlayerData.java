/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.nbt.NBTBase
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.nbt.NBTTagList
 *  net.minecraft.nbt.NBTTagString
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.world.World
 *  net.minecraftforge.common.IExtendedEntityProperties
 */
package vintarz.ntr.server;

import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import vintarz.ntr.server.SPacketHandler;
import vintarz.ntr.server.ServerNtr;

public class NtrPlayerData
implements IExtendedEntityProperties {
    public static final String EXT_PROP_NAME = "VZNTRPLR";
    public final ArrayList<String> allies = new ArrayList();
    private EntityPlayer me;

    public static final NtrPlayerData get(EntityPlayer player) {
        if ((NtrPlayerData)player.getExtendedProperties(EXT_PROP_NAME) == null) {
            NtrPlayerData.register(player);
        }
        return ((NtrPlayerData)player.getExtendedProperties(EXT_PROP_NAME)).setOwner(player);
    }

    static final void register(EntityPlayer player) {
        player.registerExtendedProperties(EXT_PROP_NAME, (IExtendedEntityProperties)new NtrPlayerData());
    }

    private NtrPlayerData setOwner(EntityPlayer player) {
        this.me = player;
        return this;
    }

    public void addAlly(String ally) {
        this.allies.add(ally);
        SPacketHandler.addAlly(this.me, ally);
    }

    public void remAlly(String ally) {
        this.allies.remove(ally);
        SPacketHandler.remAlly(this.me, ally);
    }

    public boolean hasAlly(String ally) {
        return this.allies.contains(ally);
    }

    public void init(Entity entity, World world) {
    }

    public void sendAllies() {
        if (ServerNtr.disable) {
            return;
        }
        for (String username : MinecraftServer.getServer().getConfigurationManager().getAllUsernames()) {
            EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(username);
            NtrPlayerData ep = NtrPlayerData.get((EntityPlayer)p);
            if (this.hasAlly(username)) {
                if (ep.hasAlly(this.me.username)) continue;
                this.allies.remove(username);
                continue;
            }
            if (!ep.hasAlly(this.me.username)) continue;
            ep.remAlly(this.me.username);
        }
        SPacketHandler.sendAllies(this.me, this.allies);
    }

    public void saveNBTData(NBTTagCompound compound) {
        if (!ServerNtr.disable && !this.allies.isEmpty()) {
            NBTTagList tl = new NBTTagList("allies");
            for (String str : this.allies) {
                NBTTagString tag = new NBTTagString(str, str);
                tl.appendTag((NBTBase)tag);
            }
            compound.setTag("allies", (NBTBase)tl);
        }
    }

    public void loadNBTData(NBTTagCompound compound) {
        this.allies.clear();
        if (!ServerNtr.disable && compound.hasKey("allies")) {
            NBTTagList tl = compound.getTagList("allies");
            for (int i = 0; i < tl.tagCount(); ++i) {
                NBTTagString str = (NBTTagString)tl.tagAt(i);
                if (this.allies.contains(str.data)) continue;
                this.allies.add(str.data);
            }
        }
    }
}

