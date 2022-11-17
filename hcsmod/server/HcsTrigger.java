/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.dedicated.DedicatedServer
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.world.WorldServer
 */
package hcsmod.server;

import java.util.List;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldServer;

public class HcsTrigger {
    public final AxisAlignedBB box;
    public final String[] commands;

    public HcsTrigger(AxisAlignedBB box, String[] commands) {
        this.box = box;
        this.commands = commands;
    }

    public HcsTrigger(List<String> input) {
        String[] pos1 = input.remove(0).split(" ");
        String[] pos2 = input.remove(0).split(" ");
        this.box = AxisAlignedBB.getBoundingBox((double)Math.min(Long.parseLong(pos1[0]), Long.parseLong(pos2[0])), (double)Math.min(Long.parseLong(pos1[1]), Long.parseLong(pos2[1])), (double)Math.min(Long.parseLong(pos1[2]), Long.parseLong(pos2[2])), (double)Math.max(Long.parseLong(pos1[0]), Long.parseLong(pos2[0])), (double)Math.max(Long.parseLong(pos1[1]), Long.parseLong(pos2[1])), (double)Math.max(Long.parseLong(pos1[2]), Long.parseLong(pos2[2])));
        this.commands = input.toArray(new String[0]);
    }

    public void tick() {
        WorldServer world = MinecraftServer.getServer().worldServers[0];
        List players = world.getEntitiesWithinAABB(EntityPlayer.class, this.box);
        for (EntityPlayer player : players) {
            for (String cmd : this.commands) {
                HcsTrigger.executeCommandAsServer(cmd.replace("{player}", player.username));
            }
        }
    }

    private static void executeCommandAsServer(String cmd) {
        MinecraftServer server = MinecraftServer.getServer();
        try {
            DedicatedServer ds = (DedicatedServer)server;
            ds.addPendingCommand(cmd, (ICommandSender)server);
        }
        catch (Throwable t) {
            server.getCommandManager().executeCommand((ICommandSender)MinecraftServer.getServer(), cmd);
        }
    }
}

