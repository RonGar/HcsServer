/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.command.ICommand
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.command.WrongUsageException
 *  net.minecraft.entity.player.EntityPlayer
 */
package vintarz.tradesystem.server;

import java.util.List;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import vintarz.tradesystem.server.TradeSystemServer;

public class TradeCommand
implements ICommand {
    public String c() {
        return "vtrade";
    }

    public String c(ICommandSender icommandsender) {
        return "/vtrade reload /vtrade spawn <\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0442\u043e\u0440\u0433\u043e\u0432\u0446\u0430>";
    }

    public List b() {
        return null;
    }

    public void b(ICommandSender icommandsender, String[] astring) {
        if (astring.length == 1 && "reload".equals(astring[0])) {
            TradeSystemServer.reload();
        } else if (astring.length >= 2 && "spawn".equals(astring[0]) && icommandsender instanceof EntityPlayer) {
            StringBuilder sb = new StringBuilder(astring[1]);
            for (int i = 2; i < astring.length; ++i) {
                sb.append(' ');
                sb.append(astring[i]);
            }
            TradeSystemServer.spawn((EntityPlayer)icommandsender, sb.toString());
        } else {
            throw new WrongUsageException(this.c(icommandsender), new Object[0]);
        }
    }

    public boolean a(ICommandSender sender) {
        return sender.canCommandSenderUseCommand(4, sender.getCommandSenderName());
    }

    public List a(ICommandSender icommandsender, String[] astring) {
        return null;
    }

    public boolean a(String[] astring, int i) {
        return false;
    }

    public int compareTo(Object o) {
        return 0;
    }
}

