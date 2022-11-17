/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.common.IPlayerTracker
 *  cpw.mods.fml.common.network.IPacketHandler
 *  cpw.mods.fml.common.network.NetworkRegistry
 *  cpw.mods.fml.common.registry.GameRegistry
 *  cpw.mods.fml.relauncher.Side
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.server.MinecraftServer
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.ForgeSubscribe
 *  net.minecraftforge.event.entity.EntityEvent$EntityConstructing
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingUpdateEvent
 *  vintarz.core.VSP
 */
package vintarz.ntr.server;

import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import hcsmod.server.HcsServer;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import vintarz.core.VSP;
import vintarz.ntr.server.NtrPlayerData;
import vintarz.ntr.server.SPacketHandler;

public class ServerNtr
implements IPlayerTracker {
    public static final boolean disable = Boolean.parseBoolean(System.getProperty("vtz.friendsys.disable", "false"));
    private static final Charset utf8 = Charset.forName("UTF-8");
    static final Map<String, String> targetingPlayers = new HashMap<String, String>();
    private static Map<String, List<TrackedPlayer>> trackedByPlayers = new HashMap<String, List<TrackedPlayer>>();

    public static void init() {
        NetworkRegistry.instance().registerChannel((IPacketHandler)new SPacketHandler(), "vzNtr", Side.SERVER);
        ServerNtr inst = new ServerNtr();
        MinecraftForge.EVENT_BUS.register((Object)inst);
        GameRegistry.registerPlayerTracker((IPlayerTracker)inst);
    }

    @ForgeSubscribe
    public void onEntityConstructing(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer && NtrPlayerData.get((EntityPlayer)event.entity) == null) {
            NtrPlayerData.register((EntityPlayer)event.entity);
        }
    }

    public static NtrPlayerData get(String name) {
        EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(name);
        return p == null ? null : NtrPlayerData.get((EntityPlayer)p);
    }

    public void onPlayerLogin(EntityPlayer player) {
        if (disable) {
            SPacketHandler.disable(player);
        }
        NtrPlayerData.get(player).sendAllies();
        targetingPlayers.put(player.username, "");
        for (List<TrackedPlayer> trackedBy : trackedByPlayers.values()) {
            trackedBy.add(new TrackedPlayer(player.getCommandSenderName()));
        }
        ArrayList<TrackedPlayer> trackedBy = new ArrayList<TrackedPlayer>();
        trackedByPlayers.put(player.getCommandSenderName(), trackedBy);
        for (EntityPlayer p : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (p == player) continue;
            trackedBy.add(new TrackedPlayer(p.getCommandSenderName()));
        }
    }

    public void onPlayerLogout(EntityPlayer player) {
        targetingPlayers.remove(player.username);
        List<TrackedPlayer> trackedBy = trackedByPlayers.remove(player.getCommandSenderName());
        for (TrackedPlayer track : trackedBy) {
            if (!track.forceDrawUsername) continue;
            VSP os = new VSP(5, "vzNtr");
            try {
                os.writeInt(track.entityID);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            os.send((EntityPlayer)MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(track.trackedBy));
        }
        block3: for (List<TrackedPlayer> trackedBy2 : trackedByPlayers.values()) {
            Iterator<TrackedPlayer> i = trackedBy2.iterator();
            while (i.hasNext()) {
                TrackedPlayer t = i.next();
                if (!t.trackedBy.equals(player.getCommandSenderName())) continue;
                i.remove();
                continue block3;
            }
        }
    }

    public void onPlayerChangedDimension(EntityPlayer player) {
    }

    public void onPlayerRespawn(EntityPlayer player) {
    }

    @ForgeSubscribe
    public void $(LivingDeathEvent ev) {
        if (!ev.entity.worldObj.isRemote && ev.entityLiving instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)ev.entityLiving;
            List<TrackedPlayer> trackedByPlayers = ServerNtr.trackedByPlayers.get(p.getCommandSenderName());
            for (TrackedPlayer track : trackedByPlayers) {
                if (!track.forceDrawUsername) continue;
                VSP os = new VSP(5, "vzNtr");
                try {
                    os.writeInt(track.entityID);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                os.send((EntityPlayer)MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(track.trackedBy));
                track.forceDrawUsername = false;
            }
        }
    }

    @ForgeSubscribe
    public void $(LivingEvent.LivingUpdateEvent ev) {
        if (!ev.entity.worldObj.isRemote && ev.entityLiving instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)ev.entityLiving;
            List<TrackedPlayer> trackedByPlayers = ServerNtr.trackedByPlayers.get(p.getCommandSenderName());
            for (TrackedPlayer track : trackedByPlayers) {
                boolean forcedraw;
                EntityPlayerMP spectator = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(track.trackedBy);
                if (track.forceDrawUsername && track.entityID != p.k) {
                    VSP os = new VSP(5, "vzNtr");
                    try {
                        os.writeInt(track.entityID);
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                    os.send((EntityPlayer)spectator);
                    track.forceDrawUsername = false;
                }
                NtrPlayerData d = NtrPlayerData.get((EntityPlayer)spectator);
                double x = p.u - spectator.u;
                double z = p.w - spectator.w;
                boolean friend = !HcsServer.isHarxcoreServer && d.hasAlly(p.getCommandSenderName());
                boolean cheater = HcsServer.playerHasData(p.username, "cheater") && !HcsServer.playerHasData(spectator.username, "cheater");
                boolean bl = forcedraw = p.T() && x * x + z * z < 16384.0 && (spectator.capabilities.isCreativeMode || friend || cheater);
                if (forcedraw) {
                    VSP os;
                    int color = -10240;
                    if (friend) {
                        color = -16711936;
                    }
                    String name = p.getDisplayName();
                    if (cheater) {
                        color = -32640;
                        name = name.concat("\nCHEATER\n\u0431\u0435\u0437\u043e\u0431\u0438\u0434\u043d\u044b\u0439");
                    }
                    if (!track.forceDrawUsername || color != track.color || !name.equals(track.displayName)) {
                        if (!track.forceDrawUsername) {
                            track.entityID = p.k;
                        }
                        track.forceDrawUsername = true;
                        track.color = color;
                        track.displayName = name;
                        os = new VSP(3, "vzNtr");
                        try {
                            os.writeInt(p.k);
                            os.writeInt((int)(p.u * 32.0));
                            os.writeShort((int)((p.v - 127.5 + (double)p.P) * 32.0));
                            os.writeInt((int)(p.w * 32.0));
                            os.writeInt(color);
                            byte[] data = name.getBytes(utf8);
                            os.writeByte(data.length);
                            os.write(data);
                        }
                        catch (IOException iOException) {
                            // empty catch block
                        }
                        os.send((EntityPlayer)spectator);
                        continue;
                    }
                    os = new VSP(4, "vzNtr");
                    try {
                        os.writeInt(p.k);
                        os.writeInt((int)(p.u * 32.0));
                        os.writeShort((int)((p.v - 127.5 + (double)p.P) * 32.0));
                        os.writeInt((int)(p.w * 32.0));
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                    os.send((EntityPlayer)spectator);
                    continue;
                }
                if (!track.forceDrawUsername) continue;
                track.forceDrawUsername = false;
                VSP os = new VSP(5, "vzNtr");
                try {
                    os.writeInt(p.k);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                os.send((EntityPlayer)spectator);
            }
        }
    }

    private static class TrackedPlayer {
        private final String trackedBy;
        private int entityID = -1;
        private boolean forceDrawUsername;
        private int color;
        private String displayName;

        private TrackedPlayer(String trackedBy) {
            this.trackedBy = trackedBy;
        }
    }
}

