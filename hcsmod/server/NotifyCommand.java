/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.command.CommandBase
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.command.WrongUsageException
 *  net.minecraft.util.ChatMessageComponent
 */
package hcsmod.server;

import hcsmod.server.HcsNotification;
import java.util.StringJoiner;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatMessageComponent;

public class NotifyCommand
extends CommandBase {
    private static int counter;

    public String c() {
        return "notify";
    }

    public String c(ICommandSender sender) {
        return "/notify <list/temp/add/remove>";
    }

    public void b(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            throw new WrongUsageException(this.c(sender), new Object[0]);
        }
        String action = args[0];
        if ("add".equals(action)) {
            if (args.length < 3) {
                throw new WrongUsageException("/notify add <id> <text>", new Object[0]);
            }
            String id = args[1];
            String text = NotifyCommand.joinLast(args, 2);
            HcsNotification.add(new HcsNotification(id, text));
        } else if ("remove".equals(action)) {
            if (args.length != 2) {
                throw new WrongUsageException("/notify remove <id>", new Object[0]);
            }
            String id = args[1];
            HcsNotification.remove(id);
        } else if ("temp".equals(action)) {
            if (args.length < 3) {
                throw new WrongUsageException("/notify temp <time> <text>", new Object[0]);
            }
            int time = CommandBase.parseIntWithMin((ICommandSender)sender, (String)args[1], (int)0);
            String text = NotifyCommand.joinLast(args, 2);
            HcsNotification.add(new HcsNotification("temp" + Integer.toString(counter++, 36), text, time));
        } else if ("list".equals(action)) {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)(HcsNotification.notifications.size() + " notifications")));
            for (HcsNotification notification : HcsNotification.notifications) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)(notification.id + ": " + notification.text)));
            }
        } else {
            throw new WrongUsageException(this.c(sender), new Object[0]);
        }
    }

    public static String joinLast(String[] args, int from) {
        StringJoiner sj = new StringJoiner(" ");
        for (int i = from; i < args.length; ++i) {
            sj.add(args[i]);
        }
        return sj.toString();
    }
}

