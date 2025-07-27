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
            sender.sendMessage("§cKullanım: /ceza <oyuncu>");
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

                sender.sendMessage("§6§l=== " + targetPlayer.getName() + " Ceza Geçmişi ===");
                sender.sendMessage("§7Toplam Ceza: §e" + totalPunishments + " §7| §7Gösterilen: §e" + Math.min(10, punishments.size()));
                sender.sendMessage("§8" + "─".repeat(50));

                for (int i = 0; i < punishments.size(); i++) {
                    Punishment punishment = punishments.get(i);

                    sender.sendMessage(String.format("§7%d. §f%s §8- §c%s",
                            i + 1,
                            punishment.getFormattedDate(),
                            punishment.getTypeDisplay()
                    ));

                    sender.sendMessage(String.format("   §7Sebep: §f%s", punishment.getReason()));
                    sender.sendMessage(String.format("   §7Yetkili: §e%s §8| §7Durum: %s",
                            punishment.getAdminName(),
                            punishment.isActive() ? "§aAktif" : "§cPasif"
                    ));

                    if (i < punishments.size() - 1) {
                        sender.sendMessage("§8" + "─".repeat(30));
                    }
                }

                sender.sendMessage("§8" + "─".repeat(50));


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
                    sender.sendMessage("§cOyuncu bulunamadı!");
                });
                return;
            }

            List<Punishment> allPunishments = plugin.getPunishmentManager().getPlayerPunishments(playerName, 1000);

            int[] stats = new int[4];

            for (Punishment punishment : allPunishments) {
                switch (punishment.getType().toLowerCase()) {
                    case "mute":
                        stats[0]++;
                        break;
                    case "ban":
                        stats[1]++;
                        break;
                    default:
                        stats[2]++;
                        break;
                }
                if (punishment.isActive()) {
                    stats[3]++;
                }
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                sender.sendMessage("§6İstatistikler:");
                sender.sendMessage(String.format("§7Susturma: §e%d §8| §7Yasaklama: §c%d §8| §7Diğer: §b%d",
                        stats[0], stats[1], stats[2]));
                sender.sendMessage("§7Aktif Cezalar: §" + (stats[3] > 0 ? "c" : "a") + stats[3]);

                if (!allPunishments.isEmpty()) {
                    Punishment latest = allPunishments.get(0);
                    sender.sendMessage("§7Son Ceza: §f" + latest.getFormattedDate() + " §8(§e" + latest.getReason() + "§8)");
                }
            });
        });
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