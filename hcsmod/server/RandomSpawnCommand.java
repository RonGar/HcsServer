/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.command.CommandBase
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.command.WrongUsageException
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.ChatMessageComponent
 *  net.minecraft.util.MathHelper
 */
package hcsmod.server;

import hcsmod.player.ExtendedPlayer;
import hcsmod.server.Location;
import hcsmod.server.RandomSpawn;
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
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.MathHelper;

public class RandomSpawnCommand
extends CommandBase {
    private List actions = new ArrayList();

    public RandomSpawnCommand() {
        this.actions.add("list");
        this.actions.add("get");
        this.actions.add("add");
        this.actions.add("remove");
        this.actions.add("write");
        this.actions.add("clear");
    }

    public String c() {
        return "rs";
    }

    public String c(ICommandSender sender) {
        return "\n\u00a77rs list - \u0441\u043f\u0438\u0441\u043e\u043a \u0437\u043e\u043d\nrs get - \u0432\u044b\u0431\u043e\u0440 \u0437\u043e\u043d\u044b\nrs add - \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0442\u043e\u0447\u043a\u0443\nrs remove - \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0442\u043e\u0447\u043a\u0443\nrs write - \u0437\u0430\u043f\u0438\u0441\u0430\u0442\u044c \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u0438\u044f \u0437\u043e\u043d\u044b \u0432 \u0444\u0430\u0439\u043b\nrs clear - \u0443\u0431\u0440\u0430\u0442\u044c \u043e\u0442\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435 \u0442\u043e\u0447\u0435\u043a";
    }

    public void b(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            throw new WrongUsageException(this.c(sender), new Object[0]);
        }
        String action = args[0];
        EntityPlayer player = (EntityPlayer)sender;
        if ("list".equals(action)) {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0421\u043f\u0438\u0441\u043e\u043a \u0437\u043e\u043d \u0441\u043f\u0430\u0443\u043d\u0430:"));
            for (String s : RandomSpawn.spawns.keySet()) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)s));
            }
        } else if ("get".equals(action) && args.length > 1) {
            StringBuilder loc = new StringBuilder();
            if (this.isBadLocation(sender, args, loc)) {
                return;
            }
            ExtendedPlayer.server((EntityPlayer)player).selectedRandomSpawnZone = loc.toString();
            SPacketHandler.sendRandomSpawnLocations(player, RandomSpawn.spawns.get((Object)loc.toString()).locations);
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u00a7a\u0417\u043e\u043d\u0430: " + loc)));
        } else if ("add".equals(action)) {
            ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
            if (ep.selectedRandomSpawnZone == null || ep.selectedRandomSpawnZone.equals("")) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u00a7c\u0417\u043e\u043d\u0430 \u043d\u0435 \u0432\u044b\u0431\u0440\u0430\u043d\u0430, \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439\u0442\u0435 /rs get <\u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435> "));
                return;
            }
            RandomSpawn.spawns.get((Object)ep.selectedRandomSpawnZone).locations.add(new Location(MathHelper.floor_double((double)player.u), MathHelper.floor_double((double)player.v), MathHelper.floor_double((double)player.w)));
            this.sendToPlayers(ep.selectedRandomSpawnZone);
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u00a7a\u0417\u043e\u043d\u0430: " + ep.selectedRandomSpawnZone)));
        } else if ("remove".equals(action)) {
            ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
            if (ep.selectedRandomSpawnZone == null || ep.selectedRandomSpawnZone.equals("")) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u00a7c\u0417\u043e\u043d\u0430 \u043d\u0435 \u0432\u044b\u0431\u0440\u0430\u043d\u0430, \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439\u0442\u0435 /rs get <\u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435> "));
                return;
            }
            int remEl = -1;
            Location ploc = new Location(MathHelper.floor_double((double)player.u), MathHelper.floor_double((double)player.v), MathHelper.floor_double((double)player.w));
            for (int i = 0; i < RandomSpawn.spawns.get((Object)ep.selectedRandomSpawnZone).locations.size(); ++i) {
                Location tmp = RandomSpawn.spawns.get((Object)ep.selectedRandomSpawnZone).locations.get(i);
                if (!(Math.sqrt(Math.pow(tmp.X - ploc.X, 2.0) + Math.pow(tmp.Z - ploc.Z, 2.0)) < 2.0)) continue;
                remEl = i;
            }
            if (remEl == -1) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u00a7c\u0422\u043e\u0447\u043a\u0430 \u0432 \u0437\u043e\u043d\u0435 " + ep.selectedRandomSpawnZone + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430")));
                return;
            }
            RandomSpawn.spawns.get((Object)ep.selectedRandomSpawnZone).locations.remove(remEl);
            this.sendToPlayers(ep.selectedRandomSpawnZone);
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u00a7a\u0422\u043e\u0447\u043a\u0430 \u0432 \u0437\u043e\u043d\u0435 " + ep.selectedRandomSpawnZone + " \u0443\u0434\u0430\u043b\u0435\u043d\u0430")));
        } else if ("clear".equals(action)) {
            ExtendedPlayer.server((EntityPlayer)player).selectedRandomSpawnZone = "";
            SPacketHandler.sendRandomSpawnLocations(player, null);
        } else if ("write".equals(action)) {
            ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)player);
            if (ep.selectedRandomSpawnZone == null || ep.selectedRandomSpawnZone.equals("")) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u00a7c\u0417\u043e\u043d\u0430 \u043d\u0435 \u0432\u044b\u0431\u0440\u0430\u043d\u0430, \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439\u0442\u0435 /rs get <\u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435> "));
                return;
            }
            RandomSpawn rs = RandomSpawn.spawns.get(ep.selectedRandomSpawnZone);
            FileWriter writer = null;
            try {
                writer = new FileWriter("SpawnLocations/" + ep.selectedRandomSpawnZone + ".txt");
                writer.write((int)rs.posX + " " + (int)rs.posZ + " " + (int)rs.radius + " " + rs.cooldown + "\n");
                for (Location l : rs.locations) {
                    writer.write(l.X + " " + l.Y + " " + l.Z + "\n");
                }
                writer.close();
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u00a7a\u0417\u043e\u043d\u0430: " + ep.selectedRandomSpawnZone + " \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0437\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u0432 \u0444\u0430\u0439\u043b")));
            }
            catch (IOException e) {
                e.printStackTrace();
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u00a7c\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u043f\u043e\u043f\u044b\u0442\u043a\u0435 \u0437\u0430\u043f\u0438\u0441\u0438 \u0432 \u0444\u0430\u0439\u043b \u0437\u043e\u043d\u044b: " + ep.selectedRandomSpawnZone)));
            }
        } else {
            throw new WrongUsageException(this.c(sender), new Object[0]);
        }
    }

    private void sendToPlayers(String selectedRandomSpawnZone) {
        for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (!(o instanceof EntityPlayer)) continue;
            EntityPlayer p = (EntityPlayer)o;
            if (!ExtendedPlayer.server((EntityPlayer)p).selectedRandomSpawnZone.equals(selectedRandomSpawnZone)) continue;
            SPacketHandler.sendRandomSpawnLocations(p, RandomSpawn.spawns.get((Object)selectedRandomSpawnZone).locations);
        }
    }

    private boolean isBadLocation(ICommandSender sender, String[] args, StringBuilder loc) {
        for (int i = 1; i < args.length; ++i) {
            loc.append(args[i]);
            if (i >= args.length - 1) continue;
            loc.append(" ");
        }
        if (!RandomSpawn.spawns.containsKey(loc.toString())) {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("\u00a7c\u0417\u043e\u043d\u0430 " + loc + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430")));
            return true;
        }
        return false;
    }

    public List a(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return CommandBase.getListOfStringsFromIterableMatchingLastWord((String[])args, (Iterable)this.actions);
        }
        return null;
    }
}

