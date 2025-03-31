package org.ProunceDev.parkyTeamsLatest;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TeamManager {
    private static final File TEAM_FILE = new File("plugins/ParkyTeams/teams.yml");
    private static FileConfiguration teamConfig;

    private static Map<String, TeamData> teams = new HashMap<>();

    static {
        loadTeams();
    }

    public static class TeamData {
        String name;
        String color;
        UUID owner;
        List<UUID> members;

        public TeamData(String name, String color, UUID owner) {
            this.name = name;
            this.color = color;
            this.owner = owner;
            this.members = new ArrayList<>();
        }
    }

    public static List<TeamData> getTeams() {
        return new ArrayList<>(teams.values());
    }

    public static boolean isTeamOwner(Player player, String teamName) {
        TeamData team = teams.get(teamName);
        return team != null && team.owner.equals(player.getUniqueId());
    }

    public static boolean isTeamOwner(UUID uuid, String teamName) {
        TeamData team = teams.get(teamName);
        return team != null && team.owner.equals(uuid);
    }

    public static void createTeam(String name, NamedTextColor color, Player owner) {
        if (teams.containsKey(name)) return;
        teams.put(name, new TeamData(name, color.toString(), owner.getUniqueId()));
        addMember(name, owner);
        saveTeams();
    }

    public static void disbandTeam(String name) {
        if (teams.containsKey(name)) {
            List<UUID> oldMembers = teams.get(name).members;
            teams.remove(name);
            saveTeams();

            for (UUID uuid : oldMembers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    updatePlayer(player);
                }
            }
        }
    }

    public static void addMember(String teamName, Player player) {
        TeamData team = teams.get(teamName);
        if (team != null && !team.members.contains(player.getUniqueId())) {
            team.members.add(player.getUniqueId());
            saveTeams();
            updatePlayer(player);
        }
    }

    public static void removeMember(String teamName, Player player) {
        TeamData team = teams.get(teamName);
        if (team != null) {
            UUID playerId = player.getUniqueId();
            team.members.remove(playerId);

            if (team.owner.equals(playerId)) {
                if (!team.members.isEmpty()) {
                    UUID newOwner = team.members.getFirst();
                    team.owner = newOwner;
                    String newLeaderName = Bukkit.getOfflinePlayer(newOwner).getName();
                    Component message = Component.text("Since the old leader left, ", NamedTextColor.YELLOW)
                            .append(Component.text(newLeaderName, NamedTextColor.AQUA))
                            .append(Component.text(" is now the team leader.", NamedTextColor.YELLOW));
                    for (UUID memberId : team.members) {
                        Player member = Bukkit.getPlayer(memberId);
                        if (member != null && member.isOnline()) {
                            member.sendMessage(message);
                        }
                    }
                } else {
                    disbandTeam(teamName);
                }
            }

            saveTeams();
            updatePlayer(player);
        }
    }

    public static String getPlayerTeam(Player player) {
        UUID playerUUID = player.getUniqueId();
        for (Map.Entry<String, TeamData> entry : teams.entrySet()) {
            if (entry.getValue().members.contains(playerUUID)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static TeamData getTeamData(String teamName) {
        return teams.get(teamName);
    }

    public static NamedTextColor getTeamColor(String teamName) {
        TeamData team = teams.get(teamName);
        if (team != null) {
            return NamedTextColor.NAMES.value(team.color.toLowerCase());
        }
        return NamedTextColor.WHITE;
    }

    public static void updatePlayer(Player player) {
        //TabListSorter.updateTabList();

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = getPlayerTeam(player);
        Team team = scoreboard.getTeam(player.getName());

        if (team != null) {
            team.removeEntry(player.getName());
        }

        if (teamName != null) {
            NamedTextColor color = getTeamColor(teamName);
            if (team == null) {
                team = scoreboard.registerNewTeam(player.getName());
            }

            team.color(color);
            team.addEntry(player.getName());
            team.prefix(Component.text("[" + teamName + "] ", color));

            Component formattedName = Component.text("[" + teamName + "] ", color)
                    .append(Component.text(player.getName(), NamedTextColor.WHITE));

            player.playerListName(formattedName);
            player.displayName(formattedName);
        } else {
            player.playerListName(Component.text(player.getName(), NamedTextColor.WHITE));
            player.displayName(Component.text(player.getName(), NamedTextColor.WHITE));
        }
    }

    public static int getNumberOfTeamMembers(String teamName) {
        TeamData data = getTeamData(teamName);
        if (data != null && data.members != null) {
            return data.members.size();
        }
        return 0;
    }

    private static void saveTeams() {
        if (teamConfig == null) return;

        teamConfig.getKeys(false).forEach(key -> teamConfig.set(key, null));
        for (Map.Entry<String, TeamData> entry : teams.entrySet()) {
            String key = entry.getKey();
            TeamData data = entry.getValue();
            teamConfig.set(key + ".name", data.name);
            teamConfig.set(key + ".color", data.color);
            teamConfig.set(key + ".owner", data.owner.toString());

            List<String> memberUUIDs = data.members.stream().map(UUID::toString).toList();
            teamConfig.set(key + ".members", memberUUIDs);
        }

        try {
            teamConfig.save(TEAM_FILE);
        } catch (IOException e) {
            Bukkit.getLogger().warning("[ParkyTeams] Failed to save teams.yml!");
        }
    }

    private static void loadTeams() {
        if (!TEAM_FILE.exists()) {
            try {
                TEAM_FILE.getParentFile().mkdirs();
                TEAM_FILE.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().warning("[ParkyTeams] Could not create teams.yml!");
                return;
            }
        }

        teamConfig = YamlConfiguration.loadConfiguration(TEAM_FILE);
        for (String key : teamConfig.getKeys(false)) {
            String name = teamConfig.getString(key + ".name");
            String color = teamConfig.getString(key + ".color");
            String ownerStr = teamConfig.getString(key + ".owner");
            List<String> membersStr = teamConfig.getStringList(key + ".members");

            if (name != null && color != null && ownerStr != null) {
                try {
                    UUID owner = UUID.fromString(ownerStr);
                    TeamData data = new TeamData(name, color, owner);

                    for (String memberStr : membersStr) {
                        try {
                            data.members.add(UUID.fromString(memberStr));
                        } catch (IllegalArgumentException ignored) {}
                    }

                    teams.put(key, data);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("[ParkyTeams] Invalid UUID in teams.yml for team: " + key);
                }
            }
        }
    }
}
