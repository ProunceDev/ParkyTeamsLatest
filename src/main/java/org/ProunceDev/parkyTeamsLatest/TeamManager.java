package org.ProunceDev.parkyTeamsLatest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class TeamManager {
    private static final File TEAM_FILE = new File("plugins/ParkyTeams/teams.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
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

    public static void createTeam(String name, NamedTextColor color, Player owner) {
        if (teams.containsKey(name)) return;
        teams.put(name, new TeamData(name, color.toString(), owner.getUniqueId()));

        addMember(name, owner);
        // saveTeams(); technically addMember should save it as well.
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
            TeamManager.updatePlayer(player);
        }
    }

    public static void removeMember(String teamName, Player player) {
        TeamData team = teams.get(teamName);
        if (team != null) {
            team.members.remove(player.getUniqueId());
            saveTeams();
            TeamManager.updatePlayer(player);
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

        TabListSorter.updateTabList();

        String teamName = TeamManager.getPlayerTeam(player);

        if (teamName != null) {
            NamedTextColor color = TeamManager.getTeamColor(teamName);
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team team = scoreboard.getTeam(player.getName());

            if (team == null) {
                team = scoreboard.registerNewTeam(player.getName());
            }
            team.color(color);
            team.addEntry(player.getName());

            team.prefix(Component.text("[" + teamName + "] ", color));

            Component formattedName = Component.text("[" + teamName + "] ", color).append(Component.text(player.getName(), NamedTextColor.WHITE));
            player.playerListName(formattedName);
            player.displayName(formattedName);
        } else {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team team = scoreboard.getTeam(player.getName());

            if (team != null) {
                team.removeEntry(player.getName());
            }

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
        try (FileWriter writer = new FileWriter(TEAM_FILE)) {
            GSON.toJson(teams, writer);
        } catch (IOException e) {
            Bukkit.getLogger().info("[ParkyTeams] Failed to write to file " + TEAM_FILE);
        }
    }

    private static void loadTeams() {
        if (!TEAM_FILE.exists()) return;
        try (FileReader reader = new FileReader(TEAM_FILE)) {
            Type type = new TypeToken<Map<String, TeamData>>() {}.getType();
            teams = GSON.fromJson(reader, type);
            if (teams == null) teams = new HashMap<>();
        } catch (IOException e) {
            Bukkit.getLogger().info("[ParkyTeams] Failed to read from file " + TEAM_FILE);
        }
    }
}
