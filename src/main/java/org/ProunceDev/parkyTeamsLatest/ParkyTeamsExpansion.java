package org.ProunceDev.parkyTeamsLatest;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParkyTeamsExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "parkyteams";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ProunceDev";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (identifier.equals("teams_remaining")) {
            return String.valueOf(TeamManager.getTeams().size());
        }
        if (identifier.equals("formatted_name")) {
            String teamName = TeamManager.getPlayerTeam(player);

            if (teamName != null) {
                NamedTextColor color = TeamManager.getTeamColor(teamName);

                String teamColorTag = color.toString().toLowerCase();

                return "<" + teamColorTag + ">[" + teamName + "]</" + teamColorTag + "> <white>" + player.getName() + "</white>";

            } else {
                String playerName = player.getName();
                return "<white>" + playerName + "</white>";
            }
        }
        return null;
    }
}
