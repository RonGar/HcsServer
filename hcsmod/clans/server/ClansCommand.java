/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.clans.common.ClanBase
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.command.CommandBase
 *  net.minecraft.command.CommandException
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.command.WrongUsageException
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.ChatMessageComponent
 *  net.minecraft.util.MathHelper
 *  net.minecraft.util.Vec3
 */
package hcsmod.clans.server;

import hcsmod.clans.common.ClanBase;
import hcsmod.clans.server.ClansNetwork;
import hcsmod.clans.server.ClansServer;
import hcsmod.clans.server.ServerBase;
import hcsmod.clans.server.ServerClan;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.ExtendedStorage;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class ClansCommand
extends CommandBase {
    public String c() {
        return "clans";
    }

    public String c(ICommandSender icommandsender) {
        return "press tab";
    }

    public List a(ICommandSender sender, String[] args) {
        if (args.length <= 1) {
            return ClansCommand.a((String[])args, (String[])new String[]{"base", "disband"});
        }
        if ("base".equals(args[0])) {
            if (args.length == 2) {
                return ClansCommand.a((String[])args, (String[])new String[]{"list", "create", "delete", "move", "setCaptureRegion", "setProtectionRegion", "getCaptureRegion", "getProtectionRegion", "teleportTo"});
            }
            if (args.length == 3 && ("delete".equals(args[1]) || "move".equals(args[1]) || "setCaptureRegion".equals(args[1]) || "setProtectionRegion".equals(args[1]) || "getCaptureRegion".equals(args[1]) || "getProtectionRegion".equals(args[1]) || "teleportTo".equals(args[1]))) {
                String[] baseIdList = new String[ClansServer.bases.size()];
                int i = 0;
                for (UUID baseID : ClansServer.bases.keySet()) {
                    baseIdList[i++] = baseID.toString();
                }
                return ClansCommand.a((String[])args, (String[])baseIdList);
            }
        }
        return null;
    }

    public void b(ICommandSender sender, String[] args) {
        if (args.length >= 1) {
            if ("base".equals(args[0]) && args.length >= 2) {
                if (this.processBase(sender, args)) {
                    return;
                }
            } else if ("disband".equals(args[0])) {
                this.processDisband(sender, args);
                return;
            }
        }
        throw new WrongUsageException(this.c(sender), new Object[0]);
    }

    private void processDisband(ICommandSender sender, String[] args) {
        if (args.length != 2) {
            throw new WrongUsageException("/clans disband <ClanTag>", new Object[0]);
        }
        ServerClan clan = null;
        for (ServerClan c : ClansServer.clans.values()) {
            if (!c.tag.equals(args[1])) continue;
            clan = c;
            break;
        }
        if (clan == null) {
            throw new CommandException("No clan with tag [" + args[1] + "]", new Object[0]);
        }
        ClansServer.disbandClan(clan);
        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"Done"));
    }

    private boolean processBase(ICommandSender sender, String[] args) {
        if ("list".equals(args[1])) {
            this.processBaseList(sender);
            return true;
        }
        if ("create".equals(args[1])) {
            this.processBaseCreate(sender, args);
            return true;
        }
        if ("delete".equals(args[1])) {
            this.processBaseDelete(sender, args);
            return true;
        }
        if ("move".equals(args[1])) {
            this.processBaseMove(sender, args);
            return true;
        }
        if ("setCaptureRegion".equals(args[1])) {
            this.processSetCaptureRegion((EntityPlayer)sender, args);
            return true;
        }
        if ("setProtectionRegion".equals(args[1])) {
            this.processSetProtectionRegion((EntityPlayer)sender, args);
            return true;
        }
        if ("getCaptureRegion".equals(args[1])) {
            this.processSelectCaptureRegion((EntityPlayer)sender, args);
            return true;
        }
        if ("getProtectionRegion".equals(args[1])) {
            this.processSelectProtectionRegion((EntityPlayer)sender, args);
            return true;
        }
        if ("teleportTo".equals(args[1])) {
            if (args.length != 3) {
                throw new CommandException("/clans base teleportTo <id>", new Object[0]);
            }
            ServerBase base = null;
            try {
                base = ClansServer.bases.get(UUID.fromString(args[2]));
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (base == null) {
                throw new CommandException("Unknown base id", new Object[0]);
            }
            ((EntityPlayer)sender).a((double)base.posX, (double)base.posY, (double)base.posZ);
            return true;
        }
        return false;
    }

    private void processSetCaptureRegion(EntityPlayer sender, String[] args) {
        if (args.length != 3) {
            throw new CommandException("/clans base setCaptureRegion <id>", new Object[0]);
        }
        ServerBase base = null;
        try {
            base = ClansServer.bases.get(UUID.fromString(args[2]));
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (base == null) {
            throw new CommandException("Unknown base id", new Object[0]);
        }
        ExtendedStorage es = ExtendedStorage.get(ExtendedPlayer.server((EntityPlayer)sender));
        AxisAlignedBB region = es.getSelectionBox();
        if (region != null) {
            base.captureRegion.minX = region.minX;
            base.captureRegion.minY = region.minY;
            base.captureRegion.minZ = region.minZ;
            base.captureRegion.maxX = region.maxX;
            base.captureRegion.maxY = region.maxY;
            base.captureRegion.maxZ = region.maxZ;
        }
        sender.a(ChatMessageComponent.createFromText((String)("Capture region set for " + base.name)));
    }

    private void processSetProtectionRegion(EntityPlayer sender, String[] args) {
        if (args.length != 3) {
            throw new CommandException("/clans base setProtectionRegion <id>", new Object[0]);
        }
        ServerBase base = null;
        try {
            base = ClansServer.bases.get(UUID.fromString(args[2]));
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (base == null) {
            throw new CommandException("Unknown base id", new Object[0]);
        }
        ExtendedStorage es = ExtendedStorage.get(ExtendedPlayer.server((EntityPlayer)sender));
        AxisAlignedBB region = es.getSelectionBox();
        if (region != null) {
            base.protectionRegion.minX = region.minX;
            base.protectionRegion.minY = region.minY;
            base.protectionRegion.minZ = region.minZ;
            base.protectionRegion.maxX = region.maxX;
            base.protectionRegion.maxY = region.maxY;
            base.protectionRegion.maxZ = region.maxZ;
        }
        sender.a(ChatMessageComponent.createFromText((String)("Protection region set for " + base.name)));
    }

    private void processSelectCaptureRegion(EntityPlayer sender, String[] args) {
        if (args.length != 3) {
            throw new CommandException("/clans base selectCaptureRegion <id>", new Object[0]);
        }
        ServerBase base = null;
        try {
            base = ClansServer.bases.get(UUID.fromString(args[2]));
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (base == null) {
            throw new CommandException("Unknown base id", new Object[0]);
        }
        ExtendedStorage es = ExtendedStorage.get(ExtendedPlayer.server((EntityPlayer)sender));
        Vec3 one = Vec3.createVectorHelper((double)base.captureRegion.minX, (double)base.captureRegion.minY, (double)base.captureRegion.minZ);
        Vec3 two = Vec3.createVectorHelper((double)base.captureRegion.maxX, (double)base.captureRegion.maxY, (double)base.captureRegion.maxZ);
        es.setOrUpdateSelection(sender, one, two);
        sender.a(ChatMessageComponent.createFromText((String)("Capture region selected for " + base.name)));
    }

    private void processSelectProtectionRegion(EntityPlayer sender, String[] args) {
        if (args.length != 3) {
            throw new CommandException("/clans base selectProtectionRegion <id>", new Object[0]);
        }
        ServerBase base = null;
        try {
            base = ClansServer.bases.get(UUID.fromString(args[2]));
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (base == null) {
            throw new CommandException("Unknown base id", new Object[0]);
        }
        ExtendedStorage es = ExtendedStorage.get(ExtendedPlayer.server((EntityPlayer)sender));
        Vec3 one = Vec3.createVectorHelper((double)base.protectionRegion.minX, (double)base.protectionRegion.minY, (double)base.protectionRegion.minZ);
        Vec3 two = Vec3.createVectorHelper((double)base.protectionRegion.maxX, (double)base.protectionRegion.maxY, (double)base.protectionRegion.maxZ);
        es.setOrUpdateSelection(sender, one, two);
        sender.a(ChatMessageComponent.createFromText((String)("Protection region selected for " + base.name)));
    }

    private void processBaseDelete(ICommandSender sender, String[] args) {
        if (args.length != 3) {
            throw new WrongUsageException("/clans base delete <id>", new Object[0]);
        }
        ClanBase base = null;
        try {
            base = ClansServer.bases.get(UUID.fromString(args[2]));
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (base == null) {
            throw new CommandException("Unknown base id", new Object[0]);
        }
        boolean removed = ClansServer.writeableBases.remove(base.id, (Object)base);
        if (!removed) {
            throw new CommandException("Try again", new Object[0]);
        }
        ClansNetwork.sendBaseList(null);
        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("Deleted base " + base.name)));
    }

    private void processBaseMove(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayerMP)) {
            throw new CommandException("Only usable by player!", new Object[0]);
        }
        if (args.length != 3) {
            throw new WrongUsageException("/clans base move <id>", new Object[0]);
        }
        ClanBase base = null;
        try {
            base = ClansServer.bases.get(UUID.fromString(args[2]));
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (base == null) {
            throw new CommandException("Unknown base id", new Object[0]);
        }
        EntityPlayer p = (EntityPlayer)sender;
        base.posX = MathHelper.floor_double((double)p.u);
        base.posY = MathHelper.floor_double((double)p.v) + 1;
        base.posZ = MathHelper.floor_double((double)p.w);
        ClansNetwork.sendBaseList(null);
        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("Moved base " + base.name)));
    }

    private void processBaseCreate(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayerMP)) {
            throw new CommandException("Only usable by player!", new Object[0]);
        }
        if (args.length < 3) {
            throw new WrongUsageException("/clans base create <name>", new Object[0]);
        }
        StringJoiner sj = new StringJoiner(" ");
        for (int i = 2; i < args.length; ++i) {
            sj.add(args[i]);
        }
        EntityPlayer p = (EntityPlayer)sender;
        ServerBase base = new ServerBase(sj.toString(), MathHelper.floor_double((double)p.u), MathHelper.floor_double((double)p.v) + 1, MathHelper.floor_double((double)p.w));
        ClansServer.writeableBases.put(base.id, base);
        ClansNetwork.sendBaseList(null);
        sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)("Base created with id " + base.id)));
    }

    private void processBaseList(ICommandSender sender) {
        for (ServerBase base : ClansServer.bases.values()) {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)(base.id + " " + base.name)));
        }
    }
}

