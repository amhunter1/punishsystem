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
            sender.sendMessage(plugin.getConfigManager().getColoredString("messages.no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(plugin.getConfigManager().getColoredString("messages.usage"));
            return true;
        }

        String targetPlayerName = args[0];

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);

            if (targetPlayer == null || (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline())) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.player-not-found"));
                });
                return;
            }

            List<Punishment> punishments = plugin.getPunishmentManager().getPlayerPunishments(targetPlayer.getName(), 10);
            int totalPunishments = plugin.getPunishmentManager().getTotalPunishments(targetPlayer.getName());

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (punishments.isEmpty()) {
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.no-punishments"));
                    return;
                }

                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.punishment-history").replace("%player%", targetPlayer.getName()));
                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.punishment-summary")
                        .replace("%total%", String.valueOf(totalPunishments))
                        .replace("%shown%", String.valueOf(Math.min(10, punishments.size()))));
                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.divider-long"));

                for (int i = 0; i < punishments.size(); i++) {
                    Punishment punishment = punishments.get(i);

                    String entryTemplate = plugin.getConfigManager().getColoredString("messages.punishment-entry");
                    String reasonLine = plugin.getConfigManager().getColoredString("messages.punishment-reason");
                    String detailsLine = plugin.getConfigManager().getColoredString("messages.punishment-details");

                    String statusText = punishment.isActive()
                            ? plugin.getConfigManager().getColoredString("messages.status.active")
                            : plugin.getConfigManager().getColoredString("messages.status.inactive");

                    // Ortak placeholder değişimleri (entry şablonunda %reason%/%admin% yer alıyorsa da destekler)
                    String entryMessage = entryTemplate
                            .replace("%index%", String.valueOf(i + 1))
                            .replace("%date%", punishment.getFormattedDate())
                            .replace("%type%", punishment.getTypeDisplay())
                            .replace("%reason%", punishment.getReason())
                            .replace("%admin%", punishment.getAdminName())
                            .replace("%status%", statusText);

                    // Eski/yanlış placeholderları temizle (örn. /reason/, /admin/ veya kalmış %xyz%)
                    sender.sendMessage(sanitizePlaceholderArtifacts(entryMessage));

                    // Eğer entry satırı zaten bu placeholder'ları içeriyorsa tekrar etmeyelim
                    if (!entryTemplate.contains("%reason%")) {
                        sender.sendMessage(reasonLine.replace("%reason%", punishment.getReason()));
                    }

                    if (!entryTemplate.contains("%admin%") && !entryTemplate.contains("%status%")) {
                        sender.sendMessage(detailsLine
                                .replace("%admin%", punishment.getAdminName())
                                .replace("%status%", statusText));
                    }

                    if (i < punishments.size() - 1) {
                        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.divider-short"));
                    }
                }

                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.divider-long"));

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
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.player-not-found"));
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
                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.ceza-stats.header"));
                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.ceza-stats.types")
                        .replace("%mute_count%", String.valueOf(finalMuteCount))
                        .replace("%ban_count%", String.valueOf(finalBanCount))
                        .replace("%kick_count%", String.valueOf(finalKickCount))
                        .replace("%tempban_count%", String.valueOf(finalTempbanCount))
                        .replace("%jail_count%", String.valueOf(finalJailCount))
                        .replace("%warn_count%", String.valueOf(finalWarnCount)));
                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.ceza-stats.active")
                        .replace("%count%", String.valueOf(finalActiveCount))
                        .replace("%color%", finalActiveCount > 0 ? "c" : "a"));

                if (!finalPunishments.isEmpty()) {
                    Punishment latest = finalPunishments.get(0);
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.ceza-stats.latest")
                            .replace("%date%", latest.getFormattedDate())
                            .replace("%reason%", latest.getReason()));
                }
            });
        });
    }

    private String sanitizePlaceholderArtifacts(String message) {
        if (message == null) return "";
        // /reason/ ve /admin/ gibi artıkları kaldır
        String sanitized = message.replace("/reason/", "").replace("/admin/", "");
        // Kalan %...% placeholderlarını sil
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