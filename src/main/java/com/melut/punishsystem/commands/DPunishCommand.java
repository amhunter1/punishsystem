package com.melut.punishsystem.commands;

import com.melut.punishsystem.DiscordPunishBot;
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
            sender.sendMessage(plugin.getLanguageManager().getColoredMessage("no-permission"));
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
                    sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test-usage"));
                }
                break;
            case "stats":
                handleStats(sender);
                break;
            case "language":
            case "lang":
                if (args.length >= 2) {
                    handleLanguage(sender, args[1]);
                } else {
                    sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test-usage").replace("test <database|discord>", "language <tr|en>"));
                }
                break;
            default:
                sender.sendMessage(plugin.getLanguageManager().getColoredMessage("unknown-command"));
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        try {
            plugin.getConfigManager().reloadConfig();
            plugin.getLanguageManager().reloadLanguages();
            sender.sendMessage(plugin.getLanguageManager().getColoredMessage("config-reloaded"));
            plugin.getLogger().info(plugin.getLanguageManager().getMessage("log.config-reloaded").replace("%sender%", sender.getName()));
        } catch (Exception e) {
            sender.sendMessage(plugin.getLanguageManager().getColoredMessage("config-reload-error", "&cConfiguration reload error: %error%").replace("%error%", e.getMessage()));
            plugin.getLogger().severe(plugin.getLanguageManager().getMessage("log.config-reload-error", "Failed to reload config: %error%").replace("%error%", e.getMessage()));
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        for (String line : plugin.getLanguageManager().getColoredMessageList("help")) {
            sender.sendMessage(line);
        }
    }

    private void handleLanguage(CommandSender sender, String languageCode) {
        if (plugin.getLanguageManager().isLanguageAvailable(languageCode)) {
            plugin.getLanguageManager().setLanguage(languageCode);
            sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("config-reloaded", "language", languageCode));
        } else {
            sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("unknown-command", "available", String.join(", ", plugin.getLanguageManager().getAvailableLanguages().keySet())));
        }
    }

    private void handleInfo(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getColoredMessage("info.header"));
        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("info.version", "version", plugin.getDescription().getVersion()));
        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("info.author", "author", plugin.getDescription().getAuthors().get(0)));
        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("info.java-version", "java_version", System.getProperty("java.version")));
        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("info.server-version", "server_version", plugin.getServer().getBukkitVersion()));

        String dbType = plugin.getConfigManager().getString("database.type");
        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("info.database", "db_type", dbType.toUpperCase()));

        // Better Discord connection check
        boolean discordConnected = plugin.getDiscordBot() != null;
        sender.sendMessage(plugin.getLanguageManager().getColoredMessage("info.discord-status." + (discordConnected ? "online" : "offline")));

        // Language info
        sender.sendMessage(plugin.getLanguageManager().getColoredMessage("info.divider"));
        sender.sendMessage("§7Current Language: §e" + plugin.getLanguageManager().getCurrentLanguage() + " (" + plugin.getLanguageManager().getAvailableLanguages().get(plugin.getLanguageManager().getCurrentLanguage()) + ")");
        
        sender.sendMessage(plugin.getLanguageManager().getColoredMessage("info.punishment-types"));
        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("info.mute-count", "count", String.valueOf(plugin.getConfigManager().getMuteReasons().size())));
        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("info.ban-count", "count", String.valueOf(plugin.getConfigManager().getBanReasons().size())));
        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("info.kick-count", "count", String.valueOf(plugin.getConfigManager().getKickReasons().size())));
        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("info.tempban-count", "count", String.valueOf(plugin.getConfigManager().getTempBanReasons().size())));
        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("info.jail-count", "count", String.valueOf(plugin.getConfigManager().getJailReasons().size())));
        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("info.warn-count", "count", String.valueOf(plugin.getConfigManager().getWarnReasons().size())));
        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("info.other-count", "count", String.valueOf(plugin.getConfigManager().getOtherReasons().size())));
        
        // bStats info
        if (plugin.getMetricsManager() != null) {
            sender.sendMessage("§7bStats Metrics: §aEnabled");
        } else {
            sender.sendMessage("§7bStats Metrics: §cDisabled");
        }
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
            case "metrics":
            case "bstats":
                testMetrics(sender);
                break;
            case "language":
            case "lang":
                testLanguageSystem(sender);
                break;
            case "all":
                testAll(sender);
                break;
            default:
                sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test.invalid-type"));
                break;
        }
    }

    private void testDatabaseConnection(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test.database.start"));

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (plugin.getDatabaseManager().getConnection() != null &&
                        !plugin.getDatabaseManager().getConnection().isClosed()) {

                    String testQuery = "SELECT COUNT(*) FROM punishments LIMIT 1";
                    var stmt = plugin.getDatabaseManager().getConnection().prepareStatement(testQuery);
                    stmt.executeQuery();
                    stmt.close();

                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test.database.success"));
                    });
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test.database.failure"));
                    });
                }
            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("test.database.error", "error", e.getMessage()));
                });
            }
        });
    }

    private void testDiscordConnection(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test.discord.start"));

        try {
            boolean isConnected = plugin.getDiscordBot() != null;

            if (isConnected) {
                sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test.discord.success"));

                String guildId = plugin.getConfigManager().getString("discord.guild-id");
                if (!guildId.equals("YOUR_GUILD_ID_HERE")) {
                    sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("test.discord.guild-configured", "guild_id", guildId));
                } else {
                    sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test.discord.guild-not-configured"));
                }

                sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test.discord.roles.header"));
                testDiscordRole(sender, "mute");
                testDiscordRole(sender, "ban");
                testDiscordRole(sender, "kick");
                testDiscordRole(sender, "tempban");
                testDiscordRole(sender, "jail");
                testDiscordRole(sender, "warn");
                testDiscordRole(sender, "other");

                testLogChannel(sender);

            } else {
                sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test.discord.failure"));
                sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test.discord.check-token"));
            }
        } catch (Exception e) {
            sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("test.discord.error", "error", e.getMessage()));
        }
    }

    private void testDiscordRole(CommandSender sender, String roleType) {
        String roleId = plugin.getConfigManager().getString("discord.roles." + roleType);
        String defaultValue = roleType.toUpperCase() + "_ROLE_ID";
        boolean configured = !roleId.equals(defaultValue);
        String status = configured ? "§a✓" : "§c✗";
        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("test.discord.roles." + roleType, "status", status));
    }

    private void testLogChannel(CommandSender sender) {
        String logChannelId = plugin.getConfigManager().getString("discord.log-channel-id");
        if (!logChannelId.equals("LOG_CHANNEL_ID_HERE")) {
            sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("test.discord.log-channel-configured", "channel_id", logChannelId));
            
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    boolean accessible = testLogChannelAccess();
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        if (accessible) {
                            sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test.discord.log-channel-accessible"));
                        } else {
                            sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test.discord.log-channel-access-failure"));
                        }
                    });
                } catch (Exception e) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test.discord.log-channel-access-failure"));
                    });
                }
            });
        } else {
            sender.sendMessage(plugin.getLanguageManager().getColoredMessage("test.discord.log-channel-not-configured"));
        }
    }

    private boolean testLogChannelAccess() {
        try {
            if (plugin.getDiscordBot() != null) {
                plugin.getDiscordBot().sendErrorMessage("🔧 Test mesajı - Log kanalı çalışıyor!");
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private void testMetrics(CommandSender sender) {
        sender.sendMessage("&7bStats metrikleri test ediliyor...".replace("&", "§"));
        
        if (plugin.getMetricsManager() != null) {
            sender.sendMessage("&a✓ bStats başarıyla yüklendi!".replace("&", "§"));
            plugin.getMetricsManager().collectData();
            sender.sendMessage("&7Metrik verileri toplandı ve gönderildi.".replace("&", "§"));
        } else {
            sender.sendMessage("&c✗ bStats yüklenmedi!".replace("&", "§"));
        }
    }

    private void testLanguageSystem(CommandSender sender) {
        sender.sendMessage("&7Dil sistemi test ediliyor...".replace("&", "§"));
        
        if (plugin.getLanguageManager() != null) {
            sender.sendMessage("&a✓ Dil sistemi aktif!".replace("&", "§"));
            sender.sendMessage("&7Mevcut dil: &e" + plugin.getLanguageManager().getCurrentLanguage());
            sender.sendMessage("&7Kullanılabilir diller:");
            
            for (var entry : plugin.getLanguageManager().getAvailableLanguages().entrySet()) {
                sender.sendMessage("  &7• " + entry.getKey() + ": " + entry.getValue());
            }
        } else {
            sender.sendMessage("&c✗ Dil sistemi yüklenmedi!".replace("&", "§"));
        }
    }

    private void testAll(CommandSender sender) {
        sender.sendMessage("&6&l=== Tüm Sistemler Test Ediliyor ===".replace("&", "§"));
        sender.sendMessage("");
        
        sender.sendMessage("&e1. Veritabanı Testi:".replace("&", "§"));
        testDatabaseConnection(sender);
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            sender.sendMessage("&e2. Discord Testi:".replace("&", "§"));
            testDiscordConnection(sender);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                sender.sendMessage("&e3. Metrik Testi:".replace("&", "§"));
                testMetrics(sender);
                
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    sender.sendMessage("&e4. Dil Sistemi Testi:".replace("&", "§"));
                    testLanguageSystem(sender);
                    
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        sender.sendMessage("");
                        sender.sendMessage("&a&lTüm testler tamamlandı!".replace("&", "§"));
                    }, 20L);
                }, 40L);
            }, 40L);
        }, 60L);
    }

    private void handleStats(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getColoredMessage("stats.start"));

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String query = "SELECT punishment_type, COUNT(*) as count FROM punishments GROUP BY punishment_type";
                var connection = plugin.getDatabaseManager().getConnection();
                var statement = connection.prepareStatement(query);
                var resultSet = statement.executeQuery();

                int totalPunishments = 0;
                int muteCount = 0;
                int banCount = 0;
                int kickCount = 0;
                int tempbanCount = 0;
                int jailCount = 0;
                int warnCount = 0;
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
                        case "kick":
                            kickCount = count;
                            break;
                        case "tempban":
                            tempbanCount = count;
                            break;
                        case "jail":
                            jailCount = count;
                            break;
                        case "warn":
                            warnCount = count;
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
                final int finalKickCount = kickCount;
                final int finalTempbanCount = tempbanCount;
                final int finalJailCount = jailCount;
                final int finalWarnCount = warnCount;
                final int finalOtherCount = otherCount;
                final int finalTodayCount = todayCount;
                final int finalUniquePlayers = uniquePlayers;

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(plugin.getLanguageManager().getColoredMessage("stats.header"));
                    sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("stats.total", "count", String.valueOf(finalTotalPunishments)));
                    sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("stats.today", "count", String.valueOf(finalTodayCount)));
                    sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("stats.unique-players", "count", String.valueOf(finalUniquePlayers)));
                    sender.sendMessage(plugin.getLanguageManager().getColoredMessage("stats.divider"));
                    sender.sendMessage(plugin.getLanguageManager().getColoredMessage("stats.types.type-header"));
                    
                    if (finalTotalPunishments > 0) {
                        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("stats.types.mute",
                                "count", String.valueOf(finalMuteCount),
                                "percentage", String.format("%.1f", (double) finalMuteCount / finalTotalPunishments * 100)));
                        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("stats.types.ban",
                                "count", String.valueOf(finalBanCount),
                                "percentage", String.format("%.1f", (double) finalBanCount / finalTotalPunishments * 100)));
                        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("stats.types.kick",
                                "count", String.valueOf(finalKickCount),
                                "percentage", String.format("%.1f", (double) finalKickCount / finalTotalPunishments * 100)));
                        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("stats.types.tempban",
                                "count", String.valueOf(finalTempbanCount),
                                "percentage", String.format("%.1f", (double) finalTempbanCount / finalTotalPunishments * 100)));
                        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("stats.types.jail",
                                "count", String.valueOf(finalJailCount),
                                "percentage", String.format("%.1f", (double) finalJailCount / finalTotalPunishments * 100)));
                        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("stats.types.warn",
                                "count", String.valueOf(finalWarnCount),
                                "percentage", String.format("%.1f", (double) finalWarnCount / finalTotalPunishments * 100)));
                        sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("stats.types.other",
                                "count", String.valueOf(finalOtherCount),
                                "percentage", String.format("%.1f", (double) finalOtherCount / finalTotalPunishments * 100)));
                    } else {
                        sender.sendMessage("§7" + plugin.getLanguageManager().getMessage("stats.no-data", "No punishments issued yet."));
                    }
                });

            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(plugin.getLanguageManager().getFormattedMessage("stats.error", "error", e.getMessage()));
                });
            }
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("reload", "help", "info", "test", "stats", "language", "lang");
            String partialCommand = args[0].toLowerCase();

            for (String subcommand : subcommands) {
                if (subcommand.startsWith(partialCommand)) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("test")) {
                List<String> testTypes = Arrays.asList("database", "discord", "metrics", "bstats", "language", "lang", "all");
                String partialType = args[1].toLowerCase();

                for (String testType : testTypes) {
                    if (testType.startsWith(partialType)) {
                        completions.add(testType);
                    }
                }
            } else if (args[0].equalsIgnoreCase("language") || args[0].equalsIgnoreCase("lang")) {
                if (plugin.getLanguageManager() != null) {
                    List<String> languages = new ArrayList<>(plugin.getLanguageManager().getAvailableLanguages().keySet());
                    String partialLang = args[1].toLowerCase();

                    for (String lang : languages) {
                        if (lang.startsWith(partialLang)) {
                            completions.add(lang);
                        }
                    }
                }
            }
        }

        return completions;
    }
}