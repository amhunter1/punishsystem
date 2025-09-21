package com.melut.punishsystem.managers;

import com.melut.punishsystem.DiscordPunishBot;
import com.melut.punishsystem.models.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PunishmentManager {

    private final DiscordPunishBot plugin;
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<String, Integer> dailyLimits = new ConcurrentHashMap<>();

    public PunishmentManager(DiscordPunishBot plugin) {
        this.plugin = plugin;
    }

    public void executePunishment(String playerName, String adminName, String type, String reason) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        Player adminPlayer = Bukkit.getPlayer(adminName);

        if (offlinePlayer == null) {
            plugin.getLogger().warning(plugin.getLanguageManager().getMessage("log.player-not-found").replace("%player%", playerName));
            return;
        }

        if (!checkCooldown(adminName, type)) {
            return;
        }

        if (!checkDailyLimit(adminName, type)) {
            return;
        }

        String command = plugin.getConfigManager().getPunishmentCommand(type, reason);
        String displayReason = plugin.getConfigManager().getPunishmentDisplay(type, reason);

        if (command == null || command.isEmpty()) {
            plugin.getLogger().warning(plugin.getLanguageManager().getFormattedMessage("log.no-punishment-command",
                    "type", type,
                    "reason", reason));
            return;
        }

        final String finalCommand = command.replace("%player%", playerName)
                .replace("%reason%", displayReason)
                .replace("%admin%", adminName);

        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);

            if (success) {
                Punishment punishment = new Punishment(
                        offlinePlayer.getName(),
                        offlinePlayer.getUniqueId().toString(),
                        type,
                        displayReason,
                        adminName,
                        adminPlayer != null ? adminPlayer.getUniqueId().toString() : "CONSOLE",
                        LocalDateTime.now()
                );

                plugin.getDatabaseManager().addPunishment(punishment);

                plugin.getDiscordBot().sendPunishmentNotification(punishment);

                updateCooldown(adminName, type);
                updateDailyLimit(adminName, type);

                plugin.getLogger().info(plugin.getLanguageManager().getFormattedMessage("log.punishment-executed",
                        "admin", adminName,
                        "player", playerName,
                        "reason", displayReason,
                        "type", type));

            } else {
                plugin.getLogger().warning(plugin.getLanguageManager().getFormattedMessage("log.punishment-failure",
                        "command", finalCommand));
            }
        });
    }

    private boolean checkCooldown(String adminName, String type) {
        if (!plugin.getConfigManager().getBoolean("settings.cooldowns.enabled", true)) {
            return true;
        }

        String key = adminName + ":" + type;
        long lastExecution = cooldowns.getOrDefault(key, 0L);
        long cooldownTime = plugin.getConfigManager().getInt("settings.cooldowns." + type, 30) * 1000L;
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastExecution < cooldownTime) {
            long remainingTime = (cooldownTime - (currentTime - lastExecution)) / 1000;
            String message = plugin.getLanguageManager().getFormattedMessage("cooldown." + type, "time", String.valueOf(remainingTime));
            
            if (plugin.getDiscordBot() != null) {
                plugin.getDiscordBot().sendErrorMessage(message);
            }
            
            plugin.getLogger().info(plugin.getLanguageManager().getFormattedMessage("log.cooldown-triggered",
                    "admin", adminName,
                    "type", type));
            return false;
        }

        return true;
    }

    private boolean checkDailyLimit(String adminName, String type) {
        if (!plugin.getConfigManager().getBoolean("settings.daily-limits.enabled", true)) {
            return true;
        }

        String key = adminName + ":" + type + ":" + LocalDateTime.now().toLocalDate();
        int currentCount = dailyLimits.getOrDefault(key, 0);
        int maxLimit = plugin.getConfigManager().getInt("settings.daily-limits." + type, 50);

        if (currentCount >= maxLimit) {
            String message = plugin.getLanguageManager().getMessage("daily-limit." + type);
            
            if (plugin.getDiscordBot() != null) {
                plugin.getDiscordBot().sendErrorMessage(message);
            }
            
            plugin.getLogger().info(plugin.getLanguageManager().getFormattedMessage("log.daily-limit-reached",
                    "admin", adminName,
                    "type", type));
            return false;
        }

        return true;
    }

    private void updateCooldown(String adminName, String type) {
        String key = adminName + ":" + type;
        cooldowns.put(key, System.currentTimeMillis());
    }

    private void updateDailyLimit(String adminName, String type) {
        String key = adminName + ":" + type + ":" + LocalDateTime.now().toLocalDate();
        dailyLimits.put(key, dailyLimits.getOrDefault(key, 0) + 1);
    }

    public List<Punishment> getPlayerPunishments(String playerName, int limit) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null) {
            return List.of();
        }

        return plugin.getDatabaseManager().getPlayerPunishments(player.getUniqueId().toString(), limit);
    }

    public int getTotalPunishments(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null) {
            return 0;
        }

        return plugin.getDatabaseManager().getTotalPunishments(player.getUniqueId().toString());
    }

    public String formatPunishmentHistory(String playerName, List<Punishment> punishments) {
        if (punishments.isEmpty()) {
            return plugin.getLanguageManager().getColoredMessage("no-punishments");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(plugin.getLanguageManager().getFormattedMessage("punishment-history", "player", playerName)).append("\n");

        for (int i = 0; i < punishments.size(); i++) {
            Punishment punishment = punishments.get(i);
            String entry = plugin.getLanguageManager().getFormattedMessage("punishment-entry",
                    "date", punishment.getFormattedDate(),
                    "type", punishment.getTypeDisplay(),
                    "reason", punishment.getReason(),
                    "admin", punishment.getAdminName());

            sb.append("§7").append(i + 1).append(". ").append(entry);
            if (i < punishments.size() - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public boolean isValidPunishmentType(String type) {
        return type.equals("mute") || type.equals("ban") || type.equals("kick") || 
               type.equals("tempban") || type.equals("jail") || type.equals("warn") || 
               type.equals("other");
    }

    public boolean isValidReason(String type, String reason) {
        switch (type) {
            case "mute":
                return plugin.getConfigManager().getMuteReasons().contains(reason);
            case "ban":
                return plugin.getConfigManager().getBanReasons().contains(reason);
            case "kick":
                return plugin.getConfigManager().getKickReasons().contains(reason);
            case "tempban":
                return plugin.getConfigManager().getTempBanReasons().contains(reason);
            case "jail":
                return plugin.getConfigManager().getJailReasons().contains(reason);
            case "warn":
                return plugin.getConfigManager().getWarnReasons().contains(reason);
            case "other":
                return plugin.getConfigManager().getOtherReasons().contains(reason);
            default:
                return false;
        }
    }

    public List<String> getAvailableReasons(String type) {
        switch (type) {
            case "mute":
                return plugin.getConfigManager().getMuteReasons();
            case "ban":
                return plugin.getConfigManager().getBanReasons();
            case "kick":
                return plugin.getConfigManager().getKickReasons();
            case "tempban":
                return plugin.getConfigManager().getTempBanReasons();
            case "jail":
                return plugin.getConfigManager().getJailReasons();
            case "warn":
                return plugin.getConfigManager().getWarnReasons();
            case "other":
                return plugin.getConfigManager().getOtherReasons();
            default:
                return List.of();
        }
    }

    public void clearCooldowns() {
        cooldowns.clear();
    }

    public void clearDailyLimits() {
        dailyLimits.clear();
    }

    public Map<String, Long> getCooldowns() {
        return new HashMap<>(cooldowns);
    }

    public Map<String, Integer> getDailyLimits() {
        return new HashMap<>(dailyLimits);
    }

}
