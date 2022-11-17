/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.command.CommandBase
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.command.WrongUsageException
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.util.ChatMessageComponent
 *  vintarz.core.VSP
 */
package hcsmod.server;

import hcsmod.server.airdrop.AirdropSystem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import vintarz.core.VSP;

public class MapCommand
extends CommandBase {
    private List actions = new ArrayList();
    public static Map<String, HashSet<String>> airdropSpawnList = new HashMap<String, HashSet<String>>();

    public MapCommand() {
        this.actions.add("airdropSpawnAdd");
        this.actions.add("airdropSpawnRemove");
    }

    public String c() {
        return "map";
    }

    public String c(ICommandSender sender) {
        return "TODO";
    }

    private static void addToAirdropList(String playerName, String groupId) {
        HashSet<Object> regions = airdropSpawnList.containsKey(playerName) ? airdropSpawnList.get(playerName) : new HashSet();
        regions.add(groupId);
        airdropSpawnList.put(playerName, regions);
    }

    private static void removeFromAirdropList(String playerName, String groupId) {
        if (airdropSpawnList.containsKey(playerName)) {
            HashSet<String> regions = airdropSpawnList.get(playerName);
            regions.remove(groupId);
            if (regions.isEmpty()) {
                airdropSpawnList.remove(playerName);
            }
        }
    }

    public void b(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            throw new WrongUsageException(this.c(sender), new Object[0]);
        }
        String action = args[0];
        EntityPlayer player = (EntityPlayer)sender;
        if (args.length == 2) {
            String groupId = args[1];
            if (action.equals("airdropSpawnAdd")) {
                List<AirdropSystem.AirdropLocation> airdropLocations = AirdropSystem.config.regions.get(groupId);
                if (airdropLocations == null || airdropLocations.size() == 0) {
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u041e\u0448\u0438\u0431\u043a\u0430"));
                    return;
                }
                MapCommand.sendMapDataToClient(true, "airdropSpawn_" + groupId, airdropLocations, player);
                MapCommand.addToAirdropList(player.getCommandSenderName(), groupId);
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("Airdrop spawn " + groupId + "\u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d")));
            } else if (action.equals("airdropSpawnRemove")) {
                MapCommand.sendMapDataToClient(false, "airdropSpawn_" + groupId, null, player);
                MapCommand.removeFromAirdropList(player.getCommandSenderName(), groupId);
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("Airdrop spawn " + groupId + "\u0443\u0434\u0430\u043b\u0435\u043d")));
            }
        }
    }

    public static void sendMapDataToClient(boolean add, String groupId, List<AirdropSystem.AirdropLocation> data, EntityPlayer p) {
        try {
            VSP os = new VSP(51, "HCSMOD");
            os.writeBoolean(add);
            os.writeUTF(groupId);
            if (add) {
                os.writeInt(data.size());
                for (AirdropSystem.AirdropLocation airdropLocation : data) {
                    os.writeInt(airdropLocation.x);
                    os.writeInt(airdropLocation.z);
                }
            }
            os.send(p);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List a(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return CommandBase.getListOfStringsFromIterableMatchingLastWord((String[])args, (Iterable)this.actions);
        }
        return null;
    }
}

