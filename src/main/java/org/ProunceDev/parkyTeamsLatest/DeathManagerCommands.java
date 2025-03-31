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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeathManagerCommands implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /team_death_tracker <resetall | reset <player>>", NamedTextColor.YELLOW));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "resetall" -> {
                DeathManager.resetAll();
                sender.sendMessage(Component.text("All player death flags have been reset.", NamedTextColor.GREEN));
            }
            case "reset" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /team_death_tracker reset <player>", NamedTextColor.RED));
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                DeathManager.setPlayerDied(target, false);

                Component message = Component.text("Reset death flag for ", NamedTextColor.GREEN)
                        .append(Component.text(Objects.requireNonNull(target.getName()), NamedTextColor.YELLOW))
                        .append(Component.text(".", NamedTextColor.GREEN));

                sender.sendMessage(message);
            }
            default -> sender.sendMessage(Component.text("Unknown subcommand. Use resetall or reset <player>.", NamedTextColor.RED));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (!sender.isOp()) return suggestions;

        if (args.length == 1) {
            suggestions.add("resetall");
            suggestions.add("reset");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (DeathManager.hasPlayerDied(offlinePlayer)) {
                    suggestions.add(offlinePlayer.getName());
                }
            }
        }

        return suggestions;
    }
}
