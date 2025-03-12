package org.ProunceDev.parkyTeamsLatest;

import org.bukkit.entity.Player;

import java.util.*;

public class InviteManager {

    private static final Map<Player, Set<String>> playerInvites = new HashMap<>();

    public static void sendInvite(Player player, String teamName) {
        playerInvites.putIfAbsent(player, new HashSet<>());

        playerInvites.get(player).add(teamName);
    }

    public static boolean isInvited(Player player, String teamName) {
        Set<String> invites = playerInvites.get(player);
        return invites != null && invites.contains(teamName);
    }

    public static List<String> getInvitedTeams(Player player) {
        Set<String> invites = playerInvites.get(player);
        if (invites != null) {
            return new ArrayList<>(invites);
        }
        return new ArrayList<>();
    }

    public static void removeAllInvites(Player player) {
        playerInvites.remove(player);
    }
}
