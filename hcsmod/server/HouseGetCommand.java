/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.command.ICommand
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.util.ChatMessageComponent
 *  vintarz.core.VSP
 */
package hcsmod.server;

import hcsmod.player.ExtendedPlayer;
import java.io.IOException;
import java.util.List;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatMessageComponent;
import vintarz.core.VSP;

public class HouseGetCommand
implements ICommand {
    private static final ChatMessageComponent item_hand = ChatMessageComponent.createFromText((String)"\u00a7c\u0423 \u0442\u0435\u0431\u044f \u043d\u0435 \u0434\u043e\u043b\u0436\u043d\u043e \u0431\u044b\u0442\u044c \u043d\u0438\u0447\u0435\u0433\u043e \u0432 \u0440\u0443\u043a\u0435!");
    private static final ChatMessageComponent no_houses = ChatMessageComponent.createFromText((String)"\u00a7c\u042d\u0442\u0430 \u043a\u043e\u043c\u043d\u0430\u0434\u0430 \u0434\u043b\u044f \u043f\u0435\u0440\u0435\u043d\u043e\u0441\u0430 \u0434\u043e\u043c\u043e\u0432, \u0430 \u043d\u0435 \u0434\u043b\u044f \u0442\u0435\u0431\u044f.");
    private static final ChatMessageComponent cancelled = ChatMessageComponent.createFromText((String)"\u00a7d\u0422\u044b \u0432\u0437\u044f\u043b \u0447\u0442\u043e-\u0442\u043e \u0432 \u0440\u0443\u043a\u0443, \u0440\u0435\u0436\u0438\u043c \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0438 \u0434\u043e\u043c\u0430 \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d.");

    public String c() {
        return "gethouse";
    }

    public String c(ICommandSender icommandsender) {
        return null;
    }

    public List b() {
        return null;
    }

    public void b(ICommandSender icommandsender, String[] astring) {
        if (icommandsender instanceof EntityPlayerMP && astring.length == 0) {
            EntityPlayerMP p = (EntityPlayerMP)icommandsender;
            if (p.by() != null) {
                p.sendChatToPlayer(item_hand);
                return;
            }
            ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)p);
            if (!ep.hasHousesForTransfer) {
                p.sendChatToPlayer(no_houses);
                return;
            }
            VSP os = new VSP(15, "HCSMOD");
            try {
                os.writeBoolean(true);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            os.send((EntityPlayer)p);
            ep.placeHouse = true;
        }
    }

    public boolean a(ICommandSender icommandsender) {
        return true;
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

    public static void cancelled(EntityPlayer p) {
        VSP os = new VSP(15, "HCSMOD");
        try {
            os.writeBoolean(false);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        os.send(p);
        p.a(cancelled);
    }
}

