/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.common.zombie.ZombieGroup
 *  hcsmod.entity.EntityZombieDayZ
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.command.CommandBase
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.command.WrongUsageException
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.ChatMessageComponent
 */
package hcsmod.server;

import hcsmod.common.zombie.ZombieGroup;
import hcsmod.entity.EntityZombieDayZ;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.HcsServer;
import hcsmod.server.SPacketHandler;
import hcsmod.server.airdrop.AirdropSystem;
import hcsmod.server.event.EventSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;

public class ZombiesCommand
extends CommandBase {
    private List actions = new ArrayList();

    public ZombiesCommand() {
        this.actions.add("reload");
        this.actions.add("limitCheck");
        this.actions.add("checkCount");
        this.actions.add("list");
        this.actions.add("get");
        this.actions.add("clear");
    }

    public String c() {
        return "z";
    }

    public String c(ICommandSender sender) {
        return "\n\u00a77z reload - \u043f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u043a\u043e\u043d\u0444\u0438\u0433\nz list - \u0441\u043f\u0438\u0441\u043e\u043a \u0433\u0440\u0443\u043f\u043f\nz get \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 - \u0432\u044b\u0431\u043e\u0440 \u0433\u0440\u0443\u043f\u043f\u044b\nz limitCheck \u0437\u043e\u043c\u0431\u0438 \u0440\u0430\u0434\u0438\u0443\u0441 - \u0440\u0430\u0441\u0441\u0447\u0438\u0442\u0430\u0442\u044c\nz checkCount \u0440\u0430\u0441\u0441\u0442\u043e\u044f\u043d\u0438\u0435 - \u0432\u044b\u0432\u043e\u0434 \u043a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u0430 \u0437\u043e\u043c\u0431\u0438 \u043d\u0430 \u0440\u0430\u0441\u0441\u0442\u043e\u044f\u043d\u0438\u0438 (-1 - off)\nz clear - \u043e\u0447\u0438\u0441\u0442\u0438\u0442\u044c \u0432\u044b\u0434\u0435\u043b\u0435\u043d\u0438\u0435\n";
    }

    public void b(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            throw new WrongUsageException(this.c(sender), new Object[0]);
        }
        String action = args[0];
        EntityPlayer player = (EntityPlayer)sender;
        if (args.length == 1) {
            if (action.equals("reload")) {
                for (Object o : sender.getEntityWorld().loadedEntityList) {
                    if (!(o instanceof EntityZombieDayZ)) continue;
                    EntityZombieDayZ entityZombieDayZ = (EntityZombieDayZ)o;
                    entityZombieDayZ.x();
                }
                HcsServer.readZombieConfig();
                this.sendAllZombieGroupsZones();
                if (EventSystem.config.enabled) {
                    EventSystem.reloadConfig();
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0418\u0432\u0435\u043d\u0442 \u043f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d."));
                }
                if (AirdropSystem.config.enabled) {
                    AirdropSystem.reloadConfig();
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u042d\u0438\u0440\u0434\u0440\u043e\u043f\u044b \u043f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u044b."));
                }
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0413\u0440\u0443\u043f\u043f\u044b \u0437\u043e\u043c\u0431\u0438 \u043f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u044b."));
            } else if (action.equals("clear")) {
                ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
                ep.selectedZombieGroup = "";
                SPacketHandler.clearZombieSpawnInfo(player);
            } else if (action.equals("list")) {
                for (String groupName : HcsServer.zombieGroups.keySet()) {
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)groupName));
                }
            }
        } else if (args.length == 2) {
            if (action.equals("get")) {
                ZombieGroup zombieGroup = HcsServer.zombieGroups.get(args[1]);
                if (zombieGroup == null) {
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0413\u0440\u0443\u043f\u043f\u044b \u0441 id " + args[1] + " \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442.")));
                    return;
                }
                ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
                ep.selectedSpawnZone = "";
                ep.selectedZombieGroup = args[1];
                if (zombieGroup.spawnZones == null || zombieGroup.spawnZones.size() == 0) {
                    if (zombieGroup.outdoorChunks.size() > 0) {
                        SPacketHandler.sendAirdropZombieSpawnInfo(player, zombieGroup);
                    } else {
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0423 \u0437\u043e\u043c\u0431\u0438 \u043d\u0435\u0442 \u0437\u043e\u043d \u0441\u043f\u0430\u0443\u043d\u0430, \u043f\u0440\u043e\u0432\u0435\u0440\u044c\u0442\u0435 \u043a\u043e\u043d\u0444\u0438\u0433."));
                    }
                } else {
                    SPacketHandler.clearZombieSpawnInfo(player);
                    for (String zoneName : zombieGroup.spawnZones) {
                        SPacketHandler.sendZombieSpawnInfo(player, HcsServer.spawnZones.get(zoneName));
                    }
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0417\u043e\u043d\u0430 " + args[1] + " \u0432\u044b\u0431\u0440\u0430\u043d\u0430.")));
                }
            } else if (action.equals("checkCount")) {
                int distance = Integer.parseInt(args[1]);
                ExtendedPlayer extendedPlayer = ExtendedPlayer.server((EntityPlayer)player);
                extendedPlayer.zombieCheckCountDist = distance;
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u043f\u0440\u043e\u0432\u0440\u0435\u043a\u0438 \u043a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u0430 \u0437\u043e\u043c\u0431\u0438 \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0430 \u043d\u0430 " + distance)));
            }
        } else if (args.length == 3 && action.equals("limitCheck")) {
            int countZombies = Integer.parseInt(args[1]);
            int radius = Integer.parseInt(args[2]);
            int checkRadius = radius * 2;
            int chunksInRadius = (int)(Math.floor(Math.PI * (double)(radius * radius)) + 1.0);
            int chunksInCheckRadius = (int)(Math.floor(Math.PI * (double)(checkRadius * checkRadius)) + 1.0);
            float test = (float)chunksInCheckRadius / (float)chunksInRadius;
            int result = (int)((float)countZombies * test);
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0420\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442 " + result)));
        }
    }

    private void sendAllZombieGroupsZones() {
        for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (!(o instanceof EntityPlayer)) continue;
            EntityPlayer p = (EntityPlayer)o;
            if (ExtendedPlayer.server((EntityPlayer)p).selectedZombieGroup.equals("")) continue;
            SPacketHandler.clearZombieSpawnInfo(p);
            for (String zoneName : HcsServer.zombieGroups.get((Object)ExtendedPlayer.server((EntityPlayer)p).selectedZombieGroup).spawnZones) {
                SPacketHandler.sendZombieSpawnInfo(p, HcsServer.spawnZones.get(zoneName));
            }
        }
    }

    public List a(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return CommandBase.getListOfStringsFromIterableMatchingLastWord((String[])args, (Iterable)this.actions);
        }
        return null;
    }
}

