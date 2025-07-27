package com.melut.punishsystem.managers;

import com.melut.punishsystem.DiscordPunishBot;
import com.melut.punishsystem.models.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PunishmentManager {

    private final DiscordPunishBot plugin;

    public PunishmentManager(DiscordPunishBot plugin) {
        this.plugin = plugin;
    }

    public void executePunishment(String playerName, String adminName, String type, String reason) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        Player adminPlayer = Bukkit.getPlayer(adminName);

        if (offlinePlayer == null) {
            plugin.getLogger().warning("Player not found: " + playerName);
            return;
        }

        String command = plugin.getConfigManager().getPunishmentCommand(type, reason);
        String displayReason = plugin.getConfigManager().getPunishmentDisplay(type, reason);

        if (command == null || command.isEmpty()) {
            plugin.getLogger().warning("No command found for punishment type: " + type + ", reason: " + reason);
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

                plugin.getLogger().info(String.format("Punishment executed: %s punished %s for %s (Type: %s)",
                        adminName, playerName, displayReason, type));

            } else {
                plugin.getLogger().warning("Failed to execute punishment command: " + command);
            }
        });
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
            return plugin.getConfigManager().getColoredString("messages.no-punishments");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(plugin.getConfigManager().getColoredString("messages.punishment-history")
                .replace("%player%", playerName)).append("\n");

        for (int i = 0; i < punishments.size(); i++) {
            Punishment punishment = punishments.get(i);
            String entry = plugin.getConfigManager().getColoredString("messages.punishment-entry")
                    .replace("%date%", punishment.getFormattedDate())
                    .replace("%type%", punishment.getTypeDisplay())
                    .replace("%reason%", punishment.getReason())
                    .replace("%admin%", punishment.getAdminName());

            sb.append("§7").append(i + 1).append(". ").append(entry);
            if (i < punishments.size() - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public boolean isValidPunishmentType(String type) {
        return type.equals("mute") || type.equals("ban") || type.equals("other");
    }

    public boolean isValidReason(String type, String reason) {
        switch (type) {
            case "mute":
                return plugin.getConfigManager().getMuteReasons().contains(reason);
            case "ban":
                return plugin.getConfigManager().getBanReasons().contains(reason);
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
            case "other":
                return plugin.getConfigManager().getOtherReasons();
            default:
                return List.of();
        }
    }
}