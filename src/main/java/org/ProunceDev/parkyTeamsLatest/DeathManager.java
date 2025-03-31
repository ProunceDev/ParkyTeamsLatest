package org.ProunceDev.parkyTeamsLatest;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathManager {
    private static final File DEATH_FILE = new File("plugins/ParkyTeams/deaths.yml");
    private static FileConfiguration deathConfig;

    private static Map<UUID, Boolean> deathMap = new HashMap<>();

    static {
        loadDeathData();
    }

    public static boolean hasPlayerDied(OfflinePlayer player) {
        return deathMap.getOrDefault(player.getUniqueId(), false);
    }

    public static boolean hasPlayerDied(Player player) {
        return deathMap.getOrDefault(player.getUniqueId(), false);
    }

    public static void setPlayerDied(OfflinePlayer player, boolean died) {
        deathMap.put(player.getUniqueId(), died);
        saveDeathData();
    }

    public static void resetAll() {
        deathMap.clear();
        saveDeathData();
    }

    private static void saveDeathData() {
        if (deathConfig == null) return;

        for (String key : deathConfig.getKeys(false)) {
            deathConfig.set(key, null);
        }

        for (Map.Entry<UUID, Boolean> entry : deathMap.entrySet()) {
            deathConfig.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            deathConfig.save(DEATH_FILE);
        } catch (IOException e) {
            Bukkit.getLogger().warning("[ParkyTeams] Failed to save death data to " + DEATH_FILE);
        }
    }

    private static void loadDeathData() {
        if (!DEATH_FILE.exists()) {
            try {
                DEATH_FILE.getParentFile().mkdirs();
                DEATH_FILE.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().warning("[ParkyTeams] Could not create deaths.yml!");
                return;
            }
        }

        deathConfig = YamlConfiguration.loadConfiguration(DEATH_FILE);
        for (String key : deathConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                boolean died = deathConfig.getBoolean(key);
                deathMap.put(uuid, died);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("[ParkyTeams] Invalid UUID in deaths.yml: " + key);
            }
        }
    }
}
