package org.ProunceDev.parkyTeamsLatest;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

public class ParkyTeamsLatest extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ParkyTeamsExpansion().register();
            getLogger().info("PlaceholderAPI hooked: Registered parkyteams hooks");
        } else {
            getLogger().warning("Could not find PlaceholderAPI! Placeholders will not work.");
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(Bukkit.getPluginCommand("team")).setExecutor(new Commands());
        Objects.requireNonNull(Bukkit.getPluginCommand("team_death_tracker")).setExecutor(new DeathManagerCommands());
        getLogger().info("Plugin Enabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        TeamManager.updatePlayer(player);
        String teamName = TeamManager.getPlayerTeam(player);
        if (teamName != null) {
            NamedTextColor color = TeamManager.getTeamColor(teamName);

            Component formattedName = Component.text("[" + teamName + "] ", color)
                    .append(Component.text(player.getName(), NamedTextColor.WHITE));

            event.joinMessage(formattedName.append(Component.text(" joined the server.")));
        } else {
            event.joinMessage(Component.text(player.getName(), NamedTextColor.WHITE).append(Component.text(" joined the server.")));
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (DeathManager.hasPlayerDied(player)) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("You're life in this event is over.", NamedTextColor.RED));
        }
    }


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String playerName = event.getPlayer().getDisplayName();
        String message = event.getMessage();

        String newFormat = playerName + ": " + message;

        event.setFormat(newFormat);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        DeathManager.setPlayerDied(player, true);
        TeamManager.getPlayerTeam(player);
        String teamName = TeamManager.getPlayerTeam(player);

        if (teamName != null) {
            TeamManager.removeMember(teamName, player);
        }
        player.kick(Component.text("You're life in this event is over.", NamedTextColor.RED));
    }
}
