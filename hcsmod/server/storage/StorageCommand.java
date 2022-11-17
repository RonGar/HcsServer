/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.command.CommandBase
 *  net.minecraft.command.ICommand
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.ChatMessageComponent
 */
package hcsmod.server.storage;

import hcsmod.server.HcsServer;
import hcsmod.server.storage.InvLoadCallback;
import hcsmod.server.storage.StorageGroup;
import hcsmod.server.storage.StorageInventory;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;

public class StorageCommand
implements ICommand {
    private static MinecraftServer mcs = MinecraftServer.getServer();
    private List aliases = new ArrayList();
    private List actions;

    public StorageCommand() {
        this.aliases.add("openstorage");
        this.aliases.add("opens");
        this.aliases.add("os");
    }

    public int compareTo(Object arg0) {
        return 0;
    }

    public String c() {
        return "openstorage";
    }

    public String c(ICommandSender icommandsender) {
        return "/openstorage <\u043d\u0438\u043a> \n";
    }

    public List b() {
        return this.aliases;
    }

    public void b(final ICommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)this.c(sender)));
            return;
        }
        if (!HcsServer.storageEnabled && !(sender instanceof EntityPlayerMP)) {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u0425\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435 \u043e\u0442\u043a\u044e\u0447\u0435\u043d\u043e \u0438\u043b\u0438 \u0437\u0430\u043f\u0440\u043e\u0441 \u0438\u0437 \u043a\u043e\u043d\u0441\u043e\u043b\u0438"));
            return;
        }
        String playerName = args[0];
        final EntityPlayerMP ep = (EntityPlayerMP)sender;
        HcsServer.customStorage.loadInventory(playerName, StorageGroup.MAX, false, new InvLoadCallback(){

            @Override
            public void loadingDone(StorageInventory inv) {
                inv.openBy((EntityPlayer)ep);
            }

            @Override
            public void loadingFailed(String description) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText((String)description));
            }
        });
    }

    public boolean a(ICommandSender sender) {
        return sender instanceof EntityPlayer && sender.canCommandSenderUseCommand(4, sender.getCommandSenderName());
    }

    public List a(ICommandSender sender, String[] args) {
        return CommandBase.getListOfStringsMatchingLastWord((String[])args, (String[])MinecraftServer.getServer().getAllUsernames());
    }

    public boolean a(String[] astring, int i) {
        return false;
    }
}

