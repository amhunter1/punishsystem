package com.melut.punishsystem.managers;

import com.melut.punishsystem.DiscordPunishBot;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bstats.charts.SimplePie;
import org.bstats.charts.AdvancedPie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MetricsManager {

    private final DiscordPunishBot plugin;
    private final Metrics metrics;
    private static final int PLUGIN_ID = 27312; // bStats plugin ID

    public MetricsManager(DiscordPunishBot plugin) {
        this.plugin = plugin;
        this.metrics = new Metrics(plugin, PLUGIN_ID);
        
        setupCustomCharts();
        plugin.getLogger().info("bStats metrics initialized with ID: " + PLUGIN_ID);
    }

    private void setupCustomCharts() {
        // Database type chart
        metrics.addCustomChart(new SimplePie("database_type", () -> {
            String dbType = plugin.getConfigManager().getString("database.type", "sqlite");
            return dbType.toLowerCase();
        }));

        // Language usage chart
        metrics.addCustomChart(new SimplePie("default_language", () -> {
            if (plugin.getLanguageManager() != null) {
                return plugin.getLanguageManager().getCurrentLanguage();
            }
            return "tr";
        }));

        // Discord bot status chart
        metrics.addCustomChart(new SimplePie("discord_bot_enabled", () -> {
            return plugin.getDiscordBot() != null ? "enabled" : "disabled";
        }));

        // Total punishments chart
        metrics.addCustomChart(new SingleLineChart("total_punishments", this::getTotalPunishmentsCount));

        // Punishment types distribution
        metrics.addCustomChart(new AdvancedPie("punishment_types_distribution", this::getPunishmentTypesDistribution));

        // Active punishments chart
        metrics.addCustomChart(new SingleLineChart("active_punishments", this::getActivePunishmentsCount));

        // Daily punishments chart
        metrics.addCustomChart(new SingleLineChart("daily_punishments", this::getDailyPunishmentsCount));

        // Plugin features usage
        metrics.addCustomChart(new AdvancedPie("enabled_features", this::getEnabledFeatures));

        plugin.getLogger().info("bStats custom charts registered successfully!");
    }

    private int getTotalPunishmentsCount() {
        try {
            Connection connection = plugin.getDatabaseManager().getConnection();
            if (connection == null || connection.isClosed()) {
                return 0;
            }

            String query = "SELECT COUNT(*) as total FROM punishments";
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get total punishments count for metrics: " + e.getMessage());
        }
        return 0;
    }

    private Map<String, Integer> getPunishmentTypesDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        
        try {
            Connection connection = plugin.getDatabaseManager().getConnection();
            if (connection == null || connection.isClosed()) {
                return distribution;
            }

            String query = "SELECT punishment_type, COUNT(*) as count FROM punishments GROUP BY punishment_type";
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    String type = rs.getString("punishment_type");
                    int count = rs.getInt("count");
                    distribution.put(capitalize(type), count);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get punishment types distribution for metrics: " + e.getMessage());
        }
        
        // Ensure we always return something
        if (distribution.isEmpty()) {
            distribution.put("None", 0);
        }
        
        return distribution;
    }

    private int getActivePunishmentsCount() {
        try {
            Connection connection = plugin.getDatabaseManager().getConnection();
            if (connection == null || connection.isClosed()) {
                return 0;
            }

            String query = "SELECT COUNT(*) as active FROM punishments WHERE active = 1";
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getInt("active");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get active punishments count for metrics: " + e.getMessage());
        }
        return 0;
    }

    private int getDailyPunishmentsCount() {
        try {
            Connection connection = plugin.getDatabaseManager().getConnection();
            if (connection == null || connection.isClosed()) {
                return 0;
            }

            String query;
            if (plugin.getConfigManager().getString("database.type", "sqlite").equalsIgnoreCase("sqlite")) {
                query = "SELECT COUNT(*) as daily FROM punishments WHERE DATE(date_issued) = DATE('now')";
            } else {
                query = "SELECT COUNT(*) as daily FROM punishments WHERE DATE(date_issued) = CURDATE()";
            }

            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getInt("daily");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get daily punishments count for metrics: " + e.getMessage());
        }
        return 0;
    }

    private Map<String, Integer> getEnabledFeatures() {
        Map<String, Integer> features = new HashMap<>();
        
        // Check cooldowns
        boolean cooldownsEnabled = plugin.getConfigManager().getBoolean("settings.cooldowns.enabled", true);
        features.put("Cooldowns", cooldownsEnabled ? 1 : 0);
        
        // Check daily limits
        boolean dailyLimitsEnabled = plugin.getConfigManager().getBoolean("settings.daily-limits.enabled", true);
        features.put("Daily Limits", dailyLimitsEnabled ? 1 : 0);
        
        // Check Discord integration
        boolean discordEnabled = plugin.getDiscordBot() != null;
        features.put("Discord Integration", discordEnabled ? 1 : 0);
        
        // Check database type
        String dbType = plugin.getConfigManager().getString("database.type", "sqlite");
        features.put("MySQL Database", dbType.equalsIgnoreCase("mysql") ? 1 : 0);
        features.put("SQLite Database", dbType.equalsIgnoreCase("sqlite") ? 1 : 0);
        
        // Check multi-language support
        boolean multiLang = plugin.getLanguageManager() != null && 
                           plugin.getLanguageManager().getAvailableLanguages().size() > 1;
        features.put("Multi Language", multiLang ? 1 : 0);
        
        return features;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public void shutdown() {
        if (metrics != null) {
            plugin.getLogger().info("bStats metrics shutting down...");
        }
    }

    public Metrics getMetrics() {
        return metrics;
    }

    // Method to manually trigger metrics data collection for testing
    public void collectData() {
        try {
            plugin.getLogger().info("Manually collecting bStats data...");
            plugin.getLogger().info("Total punishments: " + getTotalPunishmentsCount());
            plugin.getLogger().info("Active punishments: " + getActivePunishmentsCount());
            plugin.getLogger().info("Daily punishments: " + getDailyPunishmentsCount());
            plugin.getLogger().info("Language: " + plugin.getLanguageManager().getCurrentLanguage());
            plugin.getLogger().info("Database: " + plugin.getConfigManager().getString("database.type"));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to collect metrics data: " + e.getMessage());
        }
    }
}