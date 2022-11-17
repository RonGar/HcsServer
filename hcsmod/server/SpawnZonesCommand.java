/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  hcsmod.common.zombie.IndoorLocation
 *  hcsmod.common.zombie.OutdoorLocation
 *  hcsmod.common.zombie.SpawnZone
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.command.CommandBase
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.command.WrongUsageException
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.ChatMessageComponent
 */
package hcsmod.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hcsmod.common.zombie.IndoorLocation;
import hcsmod.common.zombie.OutdoorLocation;
import hcsmod.common.zombie.SpawnZone;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.ExtendedStorage;
import hcsmod.server.HcsServer;
import hcsmod.server.SPacketHandler;
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

public class SpawnZonesCommand
extends CommandBase {
    private List actions = new ArrayList();

    public SpawnZonesCommand() {
        this.actions.add("list");
        this.actions.add("add");
        this.actions.add("remove");
        this.actions.add("get");
        this.actions.add("clear");
        this.actions.add("addIndoor");
        this.actions.add("addOutdoor");
        this.actions.add("removeIndoor");
        this.actions.add("removeOutdoor");
        this.actions.add("read");
        this.actions.add("write");
    }

    public String c() {
        return "sz";
    }

    public String c(ICommandSender sender) {
        return "\n\u00a77sz list - \u0441\u043f\u0438\u0441\u043e\u043a \u0437\u043e\u043d\nsz add \u0438\u043c\u044f - \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0437\u043e\u043d\u0443\nsz remove \u0438\u043c\u044f - \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0437\u043e\u043d\u0443\nsz get \u0438\u043c\u044f - \u0432\u044b\u0431\u043e\u0440 \u0437\u043e\u043d\u044b\nsz clear - \u043e\u0447\u0438\u0441\u0442\u0438\u0442\u044c \u0432\u044b\u0434\u0435\u043b\u0435\u043d\u0438\u0435\nsz addIndoor - \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0437\u043e\u043d\u0443\nsz addOutdoor \u0440\u0430\u0434\u0438\u0443\u0441 - \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0437\u043e\u043d\u0443\nsz removeIndoor - \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0437\u043e\u043d\u0443\nsz removeOutdoor - \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0437\u043e\u043d\u0443\nsz read - \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u043a\u043e\u043d\u0444\u0438\u0433\nsz write - \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u0438\u044f\n";
    }

    public void b(ICommandSender sender, String[] args) {
        block65: {
            EntityPlayer player;
            String action;
            block64: {
                if (args.length == 0) {
                    throw new WrongUsageException(this.c(sender), new Object[0]);
                }
                action = args[0];
                player = (EntityPlayer)sender;
                if (args.length != 1) break block64;
                switch (action) {
                    case "read": {
                        HcsServer.readSpawnZones();
                        SpawnZonesCommand.updateAllGroups();
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0424\u0430\u0439\u043b \u0441 \u0437\u043e\u043d\u0430\u043c\u0438 \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d"));
                        break;
                    }
                    case "write": {
                        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
                        try (FileWriter writer = new FileWriter("hcsConfig/zombies/spawnZones.json");){
                            gson.toJson(HcsServer.spawnZones, (Appendable)writer);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0418\u0437\u043c\u0435\u043d\u0435\u043d\u0438\u044f \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0437\u0430\u043f\u0438\u0441\u0430\u043d\u044b \u0432 \u0444\u0430\u0439\u043b"));
                        break;
                    }
                    case "clear": {
                        ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
                        ep.selectedSpawnZone = "";
                        SPacketHandler.clearZombieSpawnInfo(player);
                        break;
                    }
                    case "list": {
                        if (HcsServer.spawnZones.isEmpty()) {
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0417\u043e\u043d\u044b \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044e\u0442, \u0434\u043e\u0431\u0430\u0432\u044c\u0442\u0435, \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u044f /sz add \u0438\u043c\u044f"));
                            break;
                        }
                        for (String groupName : HcsServer.spawnZones.keySet()) {
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)groupName));
                        }
                        break block65;
                    }
                    case "addIndoor": {
                        int maxZ;
                        int minZ;
                        int maxX;
                        int minX;
                        ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
                        ExtendedStorage es = ExtendedStorage.get(ep);
                        if (ep.selectedSpawnZone.equals("")) {
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u041e\u0448\u0438\u0431\u043a\u0430! \u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0437\u043e\u043d\u0443 \u0447\u0435\u0440\u0435\u0437 /sz get \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435"));
                            return;
                        }
                        SpawnZone spawnZone = HcsServer.spawnZones.get(ep.selectedSpawnZone);
                        if (spawnZone == null) {
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u041e\u0448\u0438\u0431\u043a\u0430! \u0412\u044b\u0431\u0440\u0430\u043d\u043d\u043e\u0439 \u0437\u043e\u043d\u044b \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442"));
                            return;
                        }
                        AxisAlignedBB selectionBox = es.getSelectionBox();
                        if (selectionBox == null) {
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u041e\u0448\u0438\u0431\u043a\u0430! \u0412\u044b\u0434\u0435\u043b\u0438\u0442\u0435 \u0437\u043e\u043d\u0443 \u0447\u0435\u0440\u0435\u0437 /h pos1  \u0438 /h pos2\n(\u0441\u043d\u044f\u0442\u044c \u0432\u044b\u0434\u0435\u043b\u0435\u043d\u0438\u0435 /h pos0)"));
                            return;
                        }
                        if (Math.abs(selectionBox.maxY - selectionBox.minY) >= (double)0.1f) {
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u041e\u0448\u0438\u0431\u043a\u0430! \u0412\u044b\u0441\u043e\u0442\u0430 \u0432\u044b\u0434\u0435\u043b\u0435\u043d\u0438\u044f \u0434\u043e\u043b\u0436\u043d\u0430 \u0431\u044b\u0442\u044c \u043e\u0434\u0438\u043d\u0430\u043a\u043e\u0432\u043e\u0439"));
                            return;
                        }
                        int Y = (int)Math.floor(selectionBox.minY);
                        if (selectionBox.minX <= selectionBox.maxX) {
                            minX = (int)Math.floor(selectionBox.minX);
                            maxX = (int)Math.floor(selectionBox.maxX);
                        } else {
                            minX = (int)Math.floor(selectionBox.maxX);
                            maxX = (int)Math.floor(selectionBox.minX);
                        }
                        if (selectionBox.minZ <= selectionBox.maxZ) {
                            minZ = (int)Math.floor(selectionBox.minZ);
                            maxZ = (int)Math.floor(selectionBox.maxZ);
                        } else {
                            minZ = (int)Math.floor(selectionBox.maxZ);
                            maxZ = (int)Math.floor(selectionBox.minZ);
                        }
                        IndoorLocation indoorLocation = new IndoorLocation(minX, minZ, maxX, maxZ, Y);
                        es.removeSelectionBox(player);
                        HcsServer.spawnZones.get((Object)ep.selectedSpawnZone).indoorLocations.add(indoorLocation);
                        HcsServer.computeSpawnZones();
                        this.sendZoneToPlayers(ep.selectedSpawnZone);
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0417\u043e\u043d\u0430 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0430"));
                        break;
                    }
                    case "removeIndoor": {
                        ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
                        if (ep.selectedSpawnZone.equals("")) {
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u041e\u0448\u0438\u0431\u043a\u0430! \u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0437\u043e\u043d\u0443 \u0447\u0435\u0440\u0435\u0437 /sz get \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435"));
                            return;
                        }
                        SpawnZone spawnZone = HcsServer.spawnZones.get(ep.selectedSpawnZone);
                        if (spawnZone == null) {
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u041e\u0448\u0438\u0431\u043a\u0430! \u0412\u044b\u0431\u0440\u0430\u043d\u043d\u043e\u0439 \u0437\u043e\u043d\u044b \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442"));
                            return;
                        }
                        IndoorLocation currentIndoorLocation = null;
                        int i = -1;
                        for (IndoorLocation indoorLocation : spawnZone.indoorLocations) {
                            ++i;
                            if (!(player.u >= (double)indoorLocation.x1) || !(player.w >= (double)indoorLocation.z1) || !(player.u <= (double)indoorLocation.x2) || !(player.w <= (double)indoorLocation.z2) || !(Math.abs(player.v - (double)indoorLocation.y) <= 0.5)) continue;
                            currentIndoorLocation = indoorLocation;
                            break;
                        }
                        if (currentIndoorLocation == null) {
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u041e\u0448\u0438\u0431\u043a\u0430! \u0412\u044b \u043d\u0435 \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0432 \u0437\u0435\u043b\u0435\u043d\u043e\u0439 \u0437\u043e\u043d\u0435"));
                            return;
                        }
                        spawnZone.indoorLocations.remove(i);
                        HcsServer.computeSpawnZones();
                        this.sendZoneToPlayers(ep.selectedSpawnZone);
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0417\u043e\u043d\u0430 \u0443\u0434\u0430\u043b\u0435\u043d\u0430"));
                        break;
                    }
                    case "removeOutdoor": {
                        ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
                        if (ep.selectedSpawnZone.equals("")) {
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u041e\u0448\u0438\u0431\u043a\u0430! \u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0437\u043e\u043d\u0443 \u0447\u0435\u0440\u0435\u0437 /sz get \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435"));
                            return;
                        }
                        SpawnZone spawnZone = HcsServer.spawnZones.get(ep.selectedSpawnZone);
                        if (spawnZone == null) {
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u041e\u0448\u0438\u0431\u043a\u0430! \u0412\u044b\u0431\u0440\u0430\u043d\u043d\u043e\u0439 \u0437\u043e\u043d\u044b \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442"));
                            return;
                        }
                        OutdoorLocation currentOutdoorLocation = null;
                        int i = -1;
                        for (OutdoorLocation outdoorLocation : spawnZone.outdoorLocations) {
                            ++i;
                            if (!(Math.sqrt(Math.pow((double)outdoorLocation.x - player.u, 2.0) + Math.pow((double)outdoorLocation.z - player.w, 2.0)) < 3.0)) continue;
                            currentOutdoorLocation = outdoorLocation;
                            break;
                        }
                        if (currentOutdoorLocation == null) {
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u041e\u0448\u0438\u0431\u043a\u0430! \u0412\u044b \u043d\u0435 \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0440\u044f\u0434\u043e\u043c \u0441 \u0446\u0435\u043d\u0442\u0440\u043e\u043c \u0437\u043e\u043d\u044b"));
                            return;
                        }
                        spawnZone.outdoorLocations.remove(i);
                        HcsServer.computeSpawnZones();
                        this.sendZoneToPlayers(ep.selectedSpawnZone);
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0417\u043e\u043d\u0430 \u0443\u0434\u0430\u043b\u0435\u043d\u0430"));
                        break;
                    }
                }
                break block65;
            }
            if (args.length == 2) {
                if (action.equals("get")) {
                    SpawnZone spawnZone = HcsServer.spawnZones.get(args[1]);
                    if (spawnZone == null) {
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0413\u0440\u0443\u043f\u043f\u044b \u0441 id " + args[1] + " \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442.")));
                        return;
                    }
                    ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
                    ep.selectedZombieGroup = "";
                    ep.selectedSpawnZone = args[1];
                    SPacketHandler.clearZombieSpawnInfo(player);
                    SPacketHandler.sendZombieSpawnInfo(player, spawnZone);
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0417\u043e\u043d\u0430 " + args[1] + " \u0432\u044b\u0431\u0440\u0430\u043d\u0430.")));
                } else if (action.equals("add")) {
                    HcsServer.spawnZones.put(args[1], new SpawnZone());
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0417\u043e\u043d\u0430 " + args[1] + " \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0430.")));
                } else if (action.equals("remove")) {
                    if (HcsServer.spawnZones.containsKey(args[1])) {
                        HcsServer.spawnZones.remove(args[1]);
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0417\u043e\u043d\u0430 " + args[1] + " \u0443\u0434\u0430\u043b\u0435\u043d\u0430.")));
                    } else {
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u0417\u043e\u043d\u0430 " + args[1] + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430.")));
                    }
                } else if (action.equals("addOutdoor")) {
                    ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
                    ExtendedStorage es = ExtendedStorage.get(ep);
                    int radius = Integer.parseInt(args[1]);
                    if (ep.selectedSpawnZone.equals("")) {
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u041e\u0448\u0438\u0431\u043a\u0430! \u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0437\u043e\u043d\u0443 \u0447\u0435\u0440\u0435\u0437 /sz get \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435"));
                        return;
                    }
                    SpawnZone spawnZone = HcsServer.spawnZones.get(ep.selectedSpawnZone);
                    if (spawnZone == null) {
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u041e\u0448\u0438\u0431\u043a\u0430! \u0412\u044b\u0431\u0440\u0430\u043d\u043d\u043e\u0439 \u0437\u043e\u043d\u044b \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442"));
                        return;
                    }
                    OutdoorLocation outdoorLocation = new OutdoorLocation((int)player.u, (int)player.w, radius);
                    es.removeSelectionBox(player);
                    HcsServer.spawnZones.get((Object)ep.selectedSpawnZone).outdoorLocations.add(outdoorLocation);
                    HcsServer.computeSpawnZones();
                    this.sendZoneToPlayers(ep.selectedSpawnZone);
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0417\u043e\u043d\u0430 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0430"));
                }
            }
        }
    }

    private void sendZoneToPlayers(String selectedZone) {
        for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (!(o instanceof EntityPlayer)) continue;
            EntityPlayer p = (EntityPlayer)o;
            if (!ExtendedPlayer.server((EntityPlayer)p).selectedSpawnZone.equals(selectedZone)) continue;
            SPacketHandler.clearZombieSpawnInfo(p);
            SPacketHandler.sendZombieSpawnInfo(p, HcsServer.spawnZones.get(selectedZone));
        }
    }

    public static void updateAllGroups() {
        for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (!(o instanceof EntityPlayer)) continue;
            EntityPlayer p = (EntityPlayer)o;
            if (ExtendedPlayer.server((EntityPlayer)p).selectedSpawnZone.equals("")) continue;
            SPacketHandler.clearZombieSpawnInfo(p);
            SPacketHandler.sendZombieSpawnInfo(p, HcsServer.spawnZones.get(ExtendedPlayer.server((EntityPlayer)p).selectedSpawnZone));
        }
    }

    public List a(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return CommandBase.getListOfStringsFromIterableMatchingLastWord((String[])args, (Iterable)this.actions);
        }
        return null;
    }
}

