package com.melut.punishsystem.commands;

import com.melut.punishsystem.DiscordPunishBot;
import com.melut.punishsystem.discord.DiscordBot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DPunishCommand implements CommandExecutor, TabCompleter {

    private final DiscordPunishBot plugin;

    public DPunishCommand(DiscordPunishBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("discordpunish.admin")) {
            sender.sendMessage(plugin.getConfigManager().getColoredString("messages.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "help":
                sendHelpMessage(sender);
                break;
            case "info":
                handleInfo(sender);
                break;
            case "test":
                if (args.length >= 2) {
                    handleTest(sender, args[1]);
                } else {
                    sender.sendMessage("§cKullanım: /dpunish test <database|discord>");
                }
                break;
            case "stats":
                handleStats(sender);
                break;
            default:
                sender.sendMessage("§cBilinmeyen komut! Kullanım: /dpunish help");
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        try {
            plugin.getConfigManager().reloadConfig();
            sender.sendMessage(plugin.getConfigManager().getColoredString("messages.config-reloaded"));
            plugin.getLogger().info("Configuration reloaded by " + sender.getName());
        } catch (Exception e) {
            sender.sendMessage("§cKonfigürasyon yüklenirken hata oluştu: " + e.getMessage());
            plugin.getLogger().severe("Failed to reload config: " + e.getMessage());
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§6§l=== DiscordPunishBot Yardım ===");
        sender.sendMessage("§e/dpunish reload §7- Konfigürasyonu yeniden yükle");
        sender.sendMessage("§e/dpunish info §7- Plugin bilgilerini göster");
        sender.sendMessage("§e/dpunish test <type> §7- Bağlantıları test et");
        sender.sendMessage("§e/dpunish stats §7- Genel istatistikleri göster");
        sender.sendMessage("§e/ceza <oyuncu> §7- Oyuncu ceza geçmişini görüntüle");
        sender.sendMessage("§8" + "─".repeat(40));
        sender.sendMessage("§7Discord botunu kullanarak ceza vermek için Discord sunucunuzdaki");
        sender.sendMessage("§7slash komutlarını kullanın: §e/mute, /ban, /diger, /ceza");
    }

    private void handleInfo(CommandSender sender) {
        sender.sendMessage("§6§l=== DiscordPunishBot Bilgileri ===");
        sender.sendMessage("§7Versiyon: §e" + plugin.getDescription().getVersion());
        sender.sendMessage("§7Yapımcı: §e" + plugin.getDescription().getAuthors().get(0));
        sender.sendMessage("§7Java Versiyonu: §e" + System.getProperty("java.version"));
        sender.sendMessage("§7Sunucu Versiyonu: §e" + plugin.getServer().getBukkitVersion());

        String dbType = plugin.getConfigManager().getString("database.type");
        sender.sendMessage("§7Veritabanı: §e" + dbType.toUpperCase());

        boolean discordConnected = plugin.getDiscordBot() != null &&
                plugin.getDiscordBot().getClass().getDeclaredFields().length > 0;
        sender.sendMessage("§7Discord Bot: " + (discordConnected ? "§aÇevrimiçi" : "§cÇevrimdışı"));

        sender.sendMessage("§8" + "─".repeat(30));
        sender.sendMessage("§7Yapılandırılmış Ceza Türleri:");
        sender.sendMessage("§7• Susturma: §e" + plugin.getConfigManager().getMuteReasons().size() + " sebep");
        sender.sendMessage("§7• Yasaklama: §e" + plugin.getConfigManager().getBanReasons().size() + " sebep");
        sender.sendMessage("§7• Diğer: §e" + plugin.getConfigManager().getOtherReasons().size() + " sebep");
    }

    private void handleTest(CommandSender sender, String testType) {
        switch (testType.toLowerCase()) {
            case "database":
            case "db":
                testDatabaseConnection(sender);
                break;
            case "discord":
                testDiscordConnection(sender);
                break;
            default:
                sender.sendMessage("§cGeçerli test türleri: database, discord");
                break;
        }
    }

    private void testDatabaseConnection(CommandSender sender) {
        sender.sendMessage("§7Veritabanı bağlantısı test ediliyor...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (plugin.getDatabaseManager().getConnection() != null &&
                        !plugin.getDatabaseManager().getConnection().isClosed()) {

                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        sender.sendMessage("§a✓ Veritabanı bağlantısı başarılı!");
                    });
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        sender.sendMessage("§c✗ Veritabanı bağlantısı başarısız!");
                    });
                }
            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage("§c✗ Veritabanı hatası: " + e.getMessage());
                });
            }
        });
    }

    private void testDiscordConnection(CommandSender sender) {
        sender.sendMessage("§7Discord bot bağlantısı test ediliyor...");

        try {
            boolean isConnected = plugin.getDiscordBot() != null;

            if (isConnected) {
                sender.sendMessage("§a✓ Discord bot bağlantısı aktif!");

                String guildId = plugin.getConfigManager().getString("discord.guild-id");
                if (!guildId.equals("YOUR_GUILD_ID_HERE")) {
                    sender.sendMessage("§a✓ Guild ID yapılandırılmış: " + guildId);
                } else {
                    sender.sendMessage("§c⚠ Guild ID yapılandırılmamış!");
                }

                String muteRole = plugin.getConfigManager().getString("discord.roles.mute");
                String banRole = plugin.getConfigManager().getString("discord.roles.ban");
                String otherRole = plugin.getConfigManager().getString("discord.roles.other");

                sender.sendMessage("§7Roller:");
                sender.sendMessage("  §7• Mute: " + (!muteRole.equals("MUTE_ROLE_ID") ? "§a✓" : "§c✗"));
                sender.sendMessage("  §7• Ban: " + (!banRole.equals("BAN_ROLE_ID") ? "§a✓" : "§c✗"));
                sender.sendMessage("  §7• Other: " + (!otherRole.equals("OTHER_ROLE_ID") ? "§a✓" : "§c✗"));

                String logChannelId = plugin.getConfigManager().getString("discord.log-channel-id");
                if (!logChannelId.equals("LOG_CHANNEL_ID_HERE")) {
                    sender.sendMessage("§a✓ Log kanalı ID yapılandırılmış: " + logChannelId);

                    try {
                        var logChannel = plugin.getDiscordBot().getClass()
                                .getDeclaredField("guild").get(plugin.getDiscordBot());
                        if (logChannel != null) {
                            sender.sendMessage("§a✓ Log kanalı erişilebilir");
                        }
                    } catch (Exception ignored) {
                        sender.sendMessage("§c⚠ Log kanalı erişim testi başarısız");
                    }
                } else {
                    sender.sendMessage("§c⚠ Log kanalı ID yapılandırılmamış!");
                }

            } else {
                sender.sendMessage("§c✗ Discord bot bağlantısı yok!");
                sender.sendMessage("§7Token kontrol edin ve botu yeniden başlatın.");
            }
        } catch (Exception e) {
            sender.sendMessage("§c✗ Discord test hatası: " + e.getMessage());
        }
    }

    private void handleStats(CommandSender sender) {
        sender.sendMessage("§7İstatistikler hesaplanıyor...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String query = "SELECT punishment_type, COUNT(*) as count FROM punishments GROUP BY punishment_type";
                var connection = plugin.getDatabaseManager().getConnection();
                var statement = connection.prepareStatement(query);
                var resultSet = statement.executeQuery();

                int totalPunishments = 0;
                int muteCount = 0;
                int banCount = 0;
                int otherCount = 0;

                while (resultSet.next()) {
                    String type = resultSet.getString("punishment_type");
                    int count = resultSet.getInt("count");
                    totalPunishments += count;

                    switch (type.toLowerCase()) {
                        case "mute":
                            muteCount = count;
                            break;
                        case "ban":
                            banCount = count;
                            break;
                        default:
                            otherCount = count;
                            break;
                    }
                }

                String todayQuery = "SELECT COUNT(*) as today_count FROM punishments WHERE DATE(date_issued) = CURDATE()";
                if (plugin.getConfigManager().getString("database.type").equalsIgnoreCase("sqlite")) {
                    todayQuery = "SELECT COUNT(*) as today_count FROM punishments WHERE DATE(date_issued) = DATE('now')";
                }

                var todayStatement = connection.prepareStatement(todayQuery);
                var todayResult = todayStatement.executeQuery();
                int todayCount = todayResult.next() ? todayResult.getInt("today_count") : 0;

                String playersQuery = "SELECT COUNT(DISTINCT player_uuid) as unique_players FROM punishments";
                var playersStatement = connection.prepareStatement(playersQuery);
                var playersResult = playersStatement.executeQuery();
                int uniquePlayers = playersResult.next() ? playersResult.getInt("unique_players") : 0;

                final int finalTotalPunishments = totalPunishments;
                final int finalMuteCount = muteCount;
                final int finalBanCount = banCount;
                final int finalOtherCount = otherCount;
                final int finalTodayCount = todayCount;
                final int finalUniquePlayers = uniquePlayers;

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage("§6§l=== Sunucu Ceza İstatistikleri ===");
                    sender.sendMessage("§7Toplam Cezalar: §e" + finalTotalPunishments);
                    sender.sendMessage("§7Bugünkü Cezalar: §e" + finalTodayCount);
                    sender.sendMessage("§7Cezalı Oyuncular: §e" + finalUniquePlayers);
                    sender.sendMessage("§8" + "─".repeat(30));
                    sender.sendMessage("§7Ceza Türleri:");
                    sender.sendMessage("§7• Susturma: §e" + finalMuteCount + " §8(" + String.format("%.1f", (double) finalMuteCount / finalTotalPunishments * 100) + "%)");
                    sender.sendMessage("§7• Yasaklama: §c" + finalBanCount + " §8(" + String.format("%.1f", (double) finalBanCount / finalTotalPunishments * 100) + "%)");
                    sender.sendMessage("§7• Diğer: §b" + finalOtherCount + " §8(" + String.format("%.1f", (double) finalOtherCount / finalTotalPunishments * 100) + "%)");
                });

            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage("§cİstatistik hatası: " + e.getMessage());
                });
            }
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("reload", "help", "info", "test", "stats");
            String partialCommand = args[0].toLowerCase();

            for (String subcommand : subcommands) {
                if (subcommand.startsWith(partialCommand)) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("test")) {
            List<String> testTypes = Arrays.asList("database", "discord");
            String partialType = args[1].toLowerCase();

            for (String testType : testTypes) {
                if (testType.startsWith(partialType)) {
                    completions.add(testType);
                }
            }
        }

        return completions;
    }
}