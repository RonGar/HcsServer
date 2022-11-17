/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsmod.clans.common.Clan
 *  hcsmod.clans.common.ClanPlayer
 *  hcsmod.clans.common.ClanPlayer$Role
 */
package hcsmod.clans.server;

import hcsmod.clans.common.Clan;
import hcsmod.clans.common.ClanPlayer;
import hcsmod.clans.server.ServerBase;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ServerClan
extends Clan {
    private final List<ClanPlayer> members = new ArrayList<ClanPlayer>();
    private final List<ClanPlayer> guests = new ArrayList<ClanPlayer>();
    public ServerBase capturedBase;

    public void clearMembers() {
        this.members.clear();
        this.guests.clear();
    }

    public void addMember(ClanPlayer clanPlayer) {
        if (clanPlayer.role.higherOrEquals(ClanPlayer.Role.PRIVATE)) {
            this.members.add(clanPlayer);
        } else {
            this.guests.add(clanPlayer);
        }
    }

    public void removeMember(ClanPlayer clanPlayer) {
        this.members.remove((Object)clanPlayer);
        this.guests.remove((Object)clanPlayer);
    }

    public void forEachMemberAndGuest(Consumer<ClanPlayer> action) {
        this.members.forEach(action);
        this.guests.forEach(action);
    }

    public void validateMembers() {
        this.members.removeIf(clanPlayer -> !this.id.equals(clanPlayer.clan));
        this.guests.removeIf(clanPlayer -> !this.id.equals(clanPlayer.clan));
    }

    public int memberCount() {
        return this.members.size();
    }

    public int memberAndGuestCount() {
        return this.members.size() + this.guests.size();
    }
}

