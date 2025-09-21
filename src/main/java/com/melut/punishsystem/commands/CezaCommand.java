package com.melut.punishsystem.commands;

import com.melut.punishsystem.DiscordPunishBot;
import com.melut.punishsystem.models.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CezaCommand implements CommandExecutor, TabCompleter {

    private final DiscordPunishBot plugin;

    public CezaCommand(DiscordPunishBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("discordpunish.ceza")) {
            sender.sendMessage(plugin.getLanguageManager().getColoredMessage("no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(plugin.getLanguageManager().getColoredMessage("usage"));
            return true;
        }

        String targetPlayerName = args[0];

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);

            if (targetPlayer == null || (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline())) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(plugin.getLanguageManager().getColoredMessage("player-not-found"));
                });
                return;
            }

            List<Punishment> punishments = plugin.getPunishmentManager().getPlayerPunishments(targetPlayer.getName(), 10);
            int totalPunishments = plugin.getPunishmentManager().getTotalPunishments(targetPlayer.getName());

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (punishments.isEmpty()) {
                    sender.sendMessage(plugin.getLanguageManager().getColoredMessage("no-punishments"));
                    return;
                }

                sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("punishment-history", "player", targetPlayer.getName()));
                sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("punishment-summary",
                        "total", String.valueOf(totalPunishments),
                        "shown", String.valueOf(Math.min(10, punishments.size()))));
                sender.sendMessage(plugin.getLanguageManager().getColoredMessage("divider-long"));

                for (int i = 0; i < punishments.size(); i++) {
                    Punishment punishment = punishments.get(i);

                    String statusText = punishment.isActive()
                            ? plugin.getLanguageManager().getColoredMessage("status.active")
                            : plugin.getLanguageManager().getColoredMessage("status.inactive");

                    String entryMessage = plugin.getLanguageManager().getFormattedMessage("punishment-entry",
                            "index", String.valueOf(i + 1),
                            "date", punishment.getFormattedDate(),
                            "type", punishment.getTypeDisplay());

                    sender.sendMessage(entryMessage);
                    
                    sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("punishment-reason", "reason", punishment.getReason()));
                    
                    sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("punishment-details",
                            "admin", punishment.getAdminName(),
                            "status", statusText));

                    if (i < punishments.size() - 1) {
                        sender.sendMessage(plugin.getLanguageManager().getColoredMessage("divider-short"));
                    }
                }

                sender.sendMessage(plugin.getLanguageManager().getColoredMessage("divider-long"));

                sendPunishmentStats(sender, targetPlayer.getName());
            });
        });

        return true;
    }

    private void sendPunishmentStats(CommandSender sender, String playerName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);
            if (targetPlayer == null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(plugin.getLanguageManager().getColoredMessage("player-not-found"));
                });
                return;
            }

            List<Punishment> allPunishments = plugin.getPunishmentManager().getPlayerPunishments(playerName, 1000);

            int muteCount = 0;
            int banCount = 0;
            int kickCount = 0;
            int tempbanCount = 0;
            int jailCount = 0;
            int warnCount = 0;
            int otherCount = 0;
            int activeCount = 0;

            for (Punishment punishment : allPunishments) {
                switch (punishment.getType().toLowerCase()) {
                    case "mute":
                        muteCount++;
                        break;
                    case "ban":
                        banCount++;
                        break;
                    case "kick":
                        kickCount++;
                        break;
                    case "tempban":
                        tempbanCount++;
                        break;
                    case "jail":
                        jailCount++;
                        break;
                    case "warn":
                        warnCount++;
                        break;
                    default:
                        otherCount++;
                        break;
                }
                if (punishment.isActive()) {
                    activeCount++;
                }
            }

            final int finalMuteCount = muteCount;
            final int finalBanCount = banCount;
            final int finalKickCount = kickCount;
            final int finalTempbanCount = tempbanCount;
            final int finalJailCount = jailCount;
            final int finalWarnCount = warnCount;
            final int finalOtherCount = otherCount;
            final int finalActiveCount = activeCount;
            final List<Punishment> finalPunishments = allPunishments;

            Bukkit.getScheduler().runTask(plugin, () -> {
                sender.sendMessage(plugin.getLanguageManager().getColoredMessage("ceza-stats.header"));
                sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("ceza-stats.types",
                        "mute_count", String.valueOf(finalMuteCount),
                        "ban_count", String.valueOf(finalBanCount),
                        "kick_count", String.valueOf(finalKickCount),
                        "tempban_count", String.valueOf(finalTempbanCount),
                        "jail_count", String.valueOf(finalJailCount),
                        "warn_count", String.valueOf(finalWarnCount)));
                        
                sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("ceza-stats.active",
                        "count", String.valueOf(finalActiveCount),
                        "color", finalActiveCount > 0 ? "c" : "a"));

                if (!finalPunishments.isEmpty()) {
                    Punishment latest = finalPunishments.get(0);
                    sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("ceza-stats.latest",
                            "date", latest.getFormattedDate(),
                            "reason", latest.getReason()));
                }
            });
        });
    }

    private String sanitizePlaceholderArtifacts(String message) {
        if (message == null) return "";
        String sanitized = message.replace("/reason/", "").replace("/admin/", "");
        return sanitized.replaceAll("%[a-zA-Z0-9_]+%", "");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partialName = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partialName)) {
                    completions.add(player.getName());
                }
            }

            return completions.stream()
                    .limit(10)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

}
