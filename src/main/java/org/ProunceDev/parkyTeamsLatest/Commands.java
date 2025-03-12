package org.ProunceDev.parkyTeamsLatest;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Commands implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("team")) {
            String action = args[0];

            if (commandSender instanceof Player sender) {
                String teamName = TeamManager.getPlayerTeam(sender);
                if (action.equalsIgnoreCase("create")) {

                    if (teamName != null) {
                        commandSender.sendMessage("You can't create a team if you are already in one.");
                        return true;
                    }

                    if (args.length != 2) {
                        commandSender.sendMessage("Invalid command usage, make sure not to include spaces in your team name.");
                        return true;
                    }

                    if (args[1].length() > 10) {
                        commandSender.sendMessage("Team name can't be over 10 characters long.");
                        return true;
                    }

                    NamedTextColor randomColor = ColorUtil.getRandomColor();

                    TeamManager.createTeam(args[1], randomColor, sender);

                    Component message = Component.text("Team ").append(Component.text(args[1], randomColor)).append(Component.text(" was created."));
                    commandSender.sendMessage(message);
                    return true;
                }

                if (action.equalsIgnoreCase("disband")) {

                    if (teamName == null) {
                        commandSender.sendMessage("You can't disband a team if you aren't in one.");
                        return true;
                    }

                    if (args.length != 2) {
                        commandSender.sendMessage("Invalid command usage, make sure to type your team name ( case sensitive ).");
                        return true;
                    }

                    if (args[1].length() > 10) {
                        commandSender.sendMessage("Team name can't be over 10 characters long.");
                        return true;
                    }
                    if (!TeamManager.isTeamOwner(sender, args[1])) {
                        commandSender.sendMessage("You aren't the owner of this team.");
                        return true;
                    }

                    NamedTextColor teamColor = TeamManager.getTeamColor(args[1]);
                    TeamManager.disbandTeam(args[1]);

                    Component message = Component.text("Team ").append(Component.text(args[1], teamColor)).append(Component.text(" was disbanded."));
                    commandSender.sendMessage(message);
                    return true;
                }

                if (action.equalsIgnoreCase("leave")) {
                    if (teamName == null) {
                        commandSender.sendMessage("You can't leave a team if you aren't in one.");
                        return true;
                    }

                    TeamManager.removeMember(teamName, sender);

                    commandSender.sendMessage(Component.text("You left the team ", NamedTextColor.WHITE).append(Component.text(teamName, TeamManager.getTeamColor(teamName))));

                    Component message = Component.text(sender.getName(), NamedTextColor.YELLOW)
                            .append(Component.text(" left the team.", NamedTextColor.WHITE));

                    TeamManager.TeamData team = TeamManager.getTeamData(teamName);
                    if (team != null) {
                        for (UUID uuid : team.members) {
                            Player player = Bukkit.getPlayer(uuid);
                            if (player != null) {
                                player.sendMessage(message);
                            }
                        }
                    }
                    return true;
                }

                if (action.equalsIgnoreCase("invite")) {
                    if (teamName == null) {
                        commandSender.sendMessage("You can't invite to a team if you aren't in one.");
                        return true;
                    }

                    if (args.length != 2) {
                        commandSender.sendMessage("Invalid command usage, make sure to type the name of the player you want to invite.");
                        return true;
                    }

                    if (!TeamManager.isTeamOwner(sender, teamName)) {
                        commandSender.sendMessage("You aren't the owner of this team, so you can't invite people.");
                        return true;
                    }

                    if (TeamManager.getNumberOfTeamMembers(teamName) >= 3) {
                        commandSender.sendMessage("You can't invite anymore people since you are already at the max of 3.");
                        return true;
                    }

                    Player playerToInvite = Bukkit.getPlayer(args[1]);
                    if (playerToInvite == null) {
                        commandSender.sendMessage("Invalid player.");
                        return true;
                    }

                    if (playerToInvite.getUniqueId() == sender.getUniqueId()) {
                        commandSender.sendMessage("You can't invite yourself.");
                        return true;

                    }

                    if (TeamManager.getPlayerTeam(playerToInvite) != null) {
                        commandSender.sendMessage("That player is already in a team.");
                        return true;
                    }
                    Component inviteMessage = Component.text("Team ", NamedTextColor.WHITE)
                                .append(Component.text(teamName, TeamManager.getTeamColor(teamName)))
                                .append(Component.text(" invited you to join, run ", NamedTextColor.WHITE))
                                .append(Component.text("/team accept " + teamName, NamedTextColor.YELLOW))
                                .append(Component.text(" to accept.", NamedTextColor.WHITE));
                    playerToInvite.sendMessage(inviteMessage);

                    InviteManager.sendInvite(playerToInvite, teamName);

                    Component message = Component.text("Invited ", NamedTextColor.WHITE)
                            .append(Component.text(playerToInvite.getName(), NamedTextColor.YELLOW))
                            .append(Component.text(" to the team.", NamedTextColor.WHITE));
                    commandSender.sendMessage(message);

                    TeamManager.TeamData team = TeamManager.getTeamData(teamName);
                    if (team != null) {
                        for (UUID uuid : team.members) {
                            Player player = Bukkit.getPlayer(uuid);
                            if (player != null && player.getUniqueId() != sender.getUniqueId()) {
                                player.sendMessage(message);
                            }
                        }
                    }
                    return true;
                }

                if (action.equalsIgnoreCase("join") || action.equalsIgnoreCase("accept")) {

                    if (args.length != 2) {
                        commandSender.sendMessage("Invalid command usage, make sure to type the name of the team you want to accept the invite from.");
                        return true;
                    }

                    if (!InviteManager.isInvited(sender, args[1])) {
                        commandSender.sendMessage("You aren't invited to that team, or it doesn't exist.");
                        return true;
                    }

                    if (TeamManager.getNumberOfTeamMembers(args[1]) >= 3) {
                        commandSender.sendMessage("You can't join since this team is already at the max of 3.");
                        return true;
                    }

                    TeamManager.addMember(args[1], sender);
                    InviteManager.removeAllInvites(sender);

                    Component message = Component.text("Welcome ", NamedTextColor.WHITE)
                            .append(Component.text(sender.getName(), NamedTextColor.YELLOW))
                            .append(Component.text(" to the team.", NamedTextColor.WHITE));

                    teamName = TeamManager.getPlayerTeam(sender);
                    if (teamName != null) {
                        TeamManager.TeamData team = TeamManager.getTeamData(teamName);
                        if (team != null) {
                            for (UUID uuid : team.members) {
                                Player player = Bukkit.getPlayer(uuid);
                                if (player != null) {
                                    player.sendMessage(message);
                                }
                            }
                        }
                    }

                    return true;
                }

                if (action.equalsIgnoreCase("kick")) {

                    if (teamName == null) {
                        commandSender.sendMessage("You can't kick from a team if you aren't in one.");
                        return true;
                    }

                    if (args.length != 2) {
                        commandSender.sendMessage("Invalid command usage, make sure to type the name of the player you want to kick.");
                        return true;
                    }

                    if (!TeamManager.isTeamOwner(sender, teamName)) {
                        commandSender.sendMessage("You aren't the owner of this team, so you can't kick people.");
                        return true;
                    }
                    Player playerToKick = Bukkit.getPlayer(args[1]);
                    if (playerToKick == null) {
                        commandSender.sendMessage("Invalid player.");
                        return true;
                    }

                    if (playerToKick.getUniqueId() == sender.getUniqueId()) {
                        commandSender.sendMessage("You can't kick yourself.");
                        return true;

                    }

                    if (!Objects.equals(TeamManager.getPlayerTeam(playerToKick), teamName)) {
                        commandSender.sendMessage("That player isn't in your team.");
                        return true;
                    }

                    TeamManager.removeMember(teamName, playerToKick);

                    Component message = Component.text(playerToKick.getName() + " was kicked from the team.");
                    commandSender.sendMessage(message);

                    TeamManager.TeamData team = TeamManager.getTeamData(teamName);
                    if (team != null) {
                        for (UUID uuid : team.members) {
                            Player player = Bukkit.getPlayer(uuid);
                            if (player != null) {
                                player.sendMessage(message);
                            }
                        }
                    }
                    return true;
                }

                if (action.equalsIgnoreCase("list")) {
                    if (args.length != 2 && teamName == null) {
                        commandSender.sendMessage("You aren't in a team, and you didn't specify a team to list.");
                        return true;
                    }
                    TeamManager.TeamData teamData;
                    if (args.length == 2) {
                        teamData = TeamManager.getTeamData(args[1]);
                        if (teamData == null) {
                            commandSender.sendMessage("Invalid team.");
                            return true;
                        }
                    } else {
                        teamData = TeamManager.getTeamData(teamName);
                    }

                    Component header = Component.text("----- Players in team ", NamedTextColor.YELLOW)
                                    .append(Component.text(teamData.name, TeamManager.getTeamColor(teamData.name)))
                                    .append(Component.text(" -----", NamedTextColor.YELLOW));

                    commandSender.sendMessage(header);
                    for (UUID uuid : teamData.members) {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                        if (TeamManager.isTeamOwner((Player) player, teamData.name)) {
                            Component message = Component.text("> ", NamedTextColor.AQUA)
                                    .append(Component.text("[Owner] ", NamedTextColor.RED))
                                    .append(Component.text(player.getName(), NamedTextColor.YELLOW));
                            commandSender.sendMessage(message);
                        } else {
                            Component message = Component.text("> ", NamedTextColor.AQUA)
                                .append(Component.text(player.getName(), NamedTextColor.YELLOW));
                            commandSender.sendMessage(message);
                        }
                    }
                }
            } else {
                commandSender.sendMessage("You can't use this command in the console.");
                return true;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("invite");
            suggestions.add("accept");
            suggestions.add("join");
            suggestions.add("leave");
            suggestions.add("kick");
            suggestions.add("create");
            suggestions.add("disband");
            suggestions.add("list");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("invite")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (TeamManager.getPlayerTeam(player) == null) {
                        suggestions.add(player.getName());
                    }
                }
            } else if (args[0].equalsIgnoreCase("disband") || args[0].equalsIgnoreCase("list")) {
                for (TeamManager.TeamData team : TeamManager.getTeams()) {
                    suggestions.add(team.name);
                }
            } else if (args[0].equalsIgnoreCase("kick")) {
                if (sender instanceof Player senderPlayer) {
                    String teamName = TeamManager.getPlayerTeam(senderPlayer);
                    if (teamName != null) {
                        TeamManager.TeamData team = TeamManager.getTeamData(teamName);
                        if (team != null) {
                            for (UUID uuid : team.members) {
                                Player player = Bukkit.getPlayer(uuid);
                                if (player != null) {
                                    suggestions.add(player.getName());
                                }
                            }
                        }
                    }
                }
            } else if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("join")) {
                if (sender instanceof Player senderPlayer) {
                    List<String> invites = InviteManager.getInvitedTeams(senderPlayer);
                    suggestions.addAll(invites);
                }
            }
        }

        return suggestions;
    }
}