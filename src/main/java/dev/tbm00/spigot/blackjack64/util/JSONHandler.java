package dev.tbm00.spigot.blackjack64.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import dev.tbm00.spigot.blackjack64.Blackjack64;
import dev.tbm00.spigot.blackjack64.object.BlackjackPlayer;
import dev.tbm00.spigot.blackjack64.object.PlayerStats;

import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.io.Reader;
import java.io.FileReader;
import java.io.Writer;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class JSONHandler {
    private static JavaPlugin javaPlugin;
    private static File jsonFile;
    private static Gson gson;
    private static Map<UUID, PlayerStats> statsMap;

    public static void init(Blackjack64 plugin) {
        javaPlugin = plugin;
        jsonFile = new File(javaPlugin.getDataFolder(), "player_stats.json");
        gson = new Gson();

        if (!javaPlugin.getDataFolder().exists()) javaPlugin.getDataFolder().mkdirs();
        try {
            if (!jsonFile.exists()) {
                jsonFile.createNewFile();
                try (Writer writer = new FileWriter(jsonFile)) {
                    gson.toJson(new HashMap<UUID, PlayerStats>(), writer);
                }
            }
        } catch (IOException e) {
            javaPlugin.getLogger().severe("Could not create player_stats.json: " + e.getMessage());
        }

        loadStats();
    }

    // load stats from JSON
    private static void loadStats() {
        try (Reader reader = new FileReader(jsonFile)) {
            Type type = new TypeToken<Map<UUID, PlayerStats>>() {}.getType();
            Map<UUID, PlayerStats> playerStats = gson.fromJson(reader, type);

            if (playerStats == null)
                statsMap = new HashMap<>();
            else statsMap = playerStats;
        } catch (IOException e) {
            javaPlugin.getLogger().severe("Could not read player_stats.json: " + e.getMessage());
            statsMap = new HashMap<>();
        }
    }

    // get saved stats from loaded stats map
    public static BlackjackPlayer getSavedPlayerStats(Player player) {
        UUID uuid = player.getUniqueId();
        if (statsMap != null && statsMap.containsKey(uuid)) {
            PlayerStats stats = statsMap.get(uuid);

            int wins = stats.getWins();
            int losses = stats.getLosses();
            int ties = stats.getTies();
            double winnings = stats.getWinnings();
            return new BlackjackPlayer(uuid, wins, losses, ties, winnings);
        } else {
            return new BlackjackPlayer(uuid, 0, 0, 0, 0);
        }
    }

    // save stats to JSON
    public static void saveStats(Set<BlackjackPlayer> blackjackPlayers) {
        Map<UUID, PlayerStats> newStatsMap = new HashMap<>();
        for (BlackjackPlayer player : blackjackPlayers) {
            newStatsMap.put(player.getUuid(), player.getStats());
        }

        // Update our local map
        statsMap = newStatsMap;

        CompletableFuture.runAsync(() -> {
            try (Writer writer = new FileWriter(jsonFile)) {
                gson.toJson(newStatsMap, writer);
            } catch (IOException e) {
                javaPlugin.getLogger().severe("Could not write to player_stats.json: " + e.getMessage());
            }
        });
    }
}