/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.command.CommandBase
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.command.WrongUsageException
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.ChatMessageComponent
 *  net.minecraft.util.Vec3
 */
package hcsmod.server.zones;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.ExtendedStorage;
import hcsmod.server.zones.PvpSystem;
import hcsmod.server.zones.PvpZoneData;
import hcsmod.server.zones.regions.Region;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Vec3;

public class PvpZonesCommand
extends CommandBase {
    private List actions = new ArrayList();

    public PvpZonesCommand() {
        this.actions.add("reload");
        this.actions.add("list");
        this.actions.add("get");
        this.actions.add("clear");
        this.actions.add("add");
        this.actions.add("remove");
        this.actions.add("write");
    }

    public String c() {
        return "pvp";
    }

    public String c(ICommandSender sender) {
        return "\n\u00a77pvp reload - \u043f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u043a\u043e\u043d\u0444\u0438\u0433\npvp list - \u0441\u043f\u0438\u0441\u043e\u043a \u0437\u043e\u043d\npvp get <name> - \u0432\u044b\u0431\u043e\u0440 \u0437\u043e\u043d\u044b\npvp clear - \u043e\u0447\u0438\u0441\u0442\u0438\u0442\u044c \u0432\u044b\u0431\u043e\u0440\npvp add <name> <displayName> - \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0437\u043e\u043d\u0443\npvp remove - \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u0443\u044e \u0437\u043e\u043d\u0443\npvp write - \u0437\u0430\u043f\u0438\u0441\u0430\u0442\u044c \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u0438\u044f\n";
    }

    public void b(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            throw new WrongUsageException(this.c(sender), new Object[0]);
        }
        String action = args[0];
        EntityPlayer player = (EntityPlayer)sender;
        if (args.length == 1) {
            if (action.equals("reload")) {
                PvpSystem.reloadConfig();
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u041a\u043e\u043d\u0444\u0438\u0433 \u043f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d"));
            } else if (action.equals("write")) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try (FileWriter writer = new FileWriter("hcsConfig/pvpZones.json");){
                    gson.toJson(PvpSystem.config, (Appendable)writer);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0418\u0437\u043c\u0435\u043d\u0435\u043d\u0438\u044f \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0437\u0430\u043f\u0438\u0441\u0430\u043d\u044b \u0432 \u0444\u0430\u0439\u043b"));
            } else if (action.equals("clear")) {
                ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
                ExtendedStorage es = ExtendedStorage.get(ep);
                ep.selectedPvpZone = "";
                es.removeSelectionBox(player);
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0412\u044b\u0431\u043e\u0440 \u043e\u0447\u0438\u0449\u0435\u043d"));
            } else if (action.equals("list")) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0421\u043f\u0438\u0441\u043e\u043a \u0437\u043e\u043d:"));
                for (String regionName : PvpSystem.config.keySet()) {
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)regionName));
                }
            } else if (action.equals("remove")) {
                ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
                ExtendedStorage es = ExtendedStorage.get(ep);
                if (ep.selectedPvpZone.equals("")) {
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0437\u043e\u043d\u0443 \u0447\u0435\u0440\u0435\u0437 /pvp get <name>."));
                    return;
                }
                if (!PvpSystem.config.containsKey(ep.selectedPvpZone)) {
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0417\u043e\u043d\u044b \u0441 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435\u043c " + ep.selectedPvpZone + " \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442.")));
                    return;
                }
                for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
                    if (!(o instanceof EntityPlayer)) continue;
                    EntityPlayer p = (EntityPlayer)o;
                    ExtendedPlayer extendedPlayer = ExtendedPlayer.server((EntityPlayer)p);
                    if (p == player || !extendedPlayer.selectedPvpZone.equals(ep.selectedPvpZone)) continue;
                    extendedPlayer.selectedPvpZone = "";
                    ExtendedStorage.get(extendedPlayer).removeSelectionBox(p);
                }
                es.removeSelectionBox(player);
                PvpSystem.config.remove(ep.selectedPvpZone);
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0417\u043e\u043d\u044b \u0441 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435\u043c " + ep.selectedPvpZone + " \u0443\u0434\u0430\u043b\u0435\u043d\u0430.")));
                ep.selectedPvpZone = "";
            }
        } else if (args.length == 2) {
            if (action.equals("get")) {
                PvpZoneData pvpZoneData = PvpSystem.config.get(args[1]);
                if (pvpZoneData.region == null) {
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0417\u043e\u043d\u044b \u0441 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435\u043c " + args[1] + " \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442.")));
                    return;
                }
                ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
                ExtendedStorage es = ExtendedStorage.get(ep);
                ep.selectedPvpZone = args[1];
                Vec3 one = Vec3.createVectorHelper((double)pvpZoneData.region.minX, (double)pvpZoneData.region.minY, (double)pvpZoneData.region.minZ);
                Vec3 two = Vec3.createVectorHelper((double)pvpZoneData.region.maxX, (double)pvpZoneData.region.maxY, (double)pvpZoneData.region.maxZ);
                es.setOrUpdateSelection(player, one, two);
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0417\u043e\u043d\u0430 \u0441 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435\u043c " + ep.selectedPvpZone)));
            }
        } else if (args.length == 3 && action.equals("add")) {
            ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
            ExtendedStorage es = ExtendedStorage.get(ep);
            PvpZoneData pvpZoneData = PvpSystem.config.get(args[1]);
            if (pvpZoneData != null) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0417\u043e\u043d\u0430 \u0441 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435\u043c " + args[1] + " \u0443\u0436\u0435 \u0435\u0441\u0442\u044c.")));
                return;
            }
            String regionName = args[1];
            AxisAlignedBB selectionBox = es.getSelectionBox();
            if (selectionBox == null) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u041e\u0448\u0438\u0431\u043a\u0430! \u0412\u044b\u0434\u0435\u043b\u0438\u0442\u0435 \u0437\u043e\u043d\u0443 \u0447\u0435\u0440\u0435\u0437 /h pos1  \u0438 /h pos2\n(\u0441\u043d\u044f\u0442\u044c \u0432\u044b\u0434\u0435\u043b\u0435\u043d\u0438\u0435 /h pos0)"));
                return;
            }
            PvpSystem.config.put(regionName, new PvpZoneData(new Region(selectionBox.minX, selectionBox.minY, selectionBox.minZ, selectionBox.maxX, selectionBox.maxY, selectionBox.maxZ), args[2]));
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0417\u043e\u043d\u0430 \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0430"));
        }
    }

    public List a(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return CommandBase.getListOfStringsFromIterableMatchingLastWord((String[])args, (Iterable)this.actions);
        }
        return null;
    }
}

