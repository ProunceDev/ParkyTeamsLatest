package org.ProunceDev.parkyTeamsLatest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class TabListSorter {
    public static void updateTabList() {
        List<Player> players = Bukkit.getOnlinePlayers().stream().sorted((player1, player2) -> {
            String teamName1 = TeamManager.getPlayerTeam(player1);
            String teamName2 = TeamManager.getPlayerTeam(player2);

            if (teamName1 == null && teamName2 == null) {
                return player1.getName().compareTo(player2.getName());
            } else if (teamName1 == null) {
                return 1; // Null teams come after players with teams
            } else if (teamName2 == null) {
                return -1; // Null teams come after players with teams
            }

            return teamName1.compareTo(teamName2);
        }).collect(Collectors.toList());
    }
}
