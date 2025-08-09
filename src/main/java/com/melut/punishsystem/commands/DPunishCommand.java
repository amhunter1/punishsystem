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
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test-usage"));
                }
                break;
            case "stats":
                handleStats(sender);
                break;
            default:
                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.unknown-command"));
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        try {
            plugin.getConfigManager().reloadConfig();
            sender.sendMessage(plugin.getConfigManager().getColoredString("messages.config-reloaded"));
            plugin.getLogger().info(plugin.getConfigManager().getString("messages.log.config-reloaded").replace("%sender%", sender.getName()));
        } catch (Exception e) {
            sender.sendMessage(plugin.getConfigManager().getColoredString("messages.config-reload-error").replace("%error%", e.getMessage()));
            plugin.getLogger().severe(plugin.getConfigManager().getString("messages.log.config-reload-error").replace("%error%", e.getMessage()));
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        for (String line : plugin.getConfigManager().getStringList("messages.help")) {
            sender.sendMessage(line.replace("&", "§"));
        }
    }

    private void handleInfo(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.info.header"));
        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.info.version").replace("%version%", plugin.getDescription().getVersion()));
        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.info.author").replace("%author%", plugin.getDescription().getAuthors().get(0)));
        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.info.java-version").replace("%java_version%", System.getProperty("java.version")));
        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.info.server-version").replace("%server_version%", plugin.getServer().getBukkitVersion()));

        String dbType = plugin.getConfigManager().getString("database.type");
        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.info.database").replace("%db_type%", dbType.toUpperCase()));

        boolean discordConnected = plugin.getDiscordBot() != null &&
                plugin.getDiscordBot().getClass().getDeclaredFields().length > 0;
        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.info.discord-status." + (discordConnected ? "online" : "offline")));

        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.info.divider"));
        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.info.punishment-types"));
        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.info.mute-count").replace("%count%", String.valueOf(plugin.getConfigManager().getMuteReasons().size())));
        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.info.ban-count").replace("%count%", String.valueOf(plugin.getConfigManager().getBanReasons().size())));
        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.info.other-count").replace("%count%", String.valueOf(plugin.getConfigManager().getOtherReasons().size())));
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
                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.invalid-type"));
                break;
        }
    }

    private void testDatabaseConnection(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.database.start"));

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (plugin.getDatabaseManager().getConnection() != null &&
                        !plugin.getDatabaseManager().getConnection().isClosed()) {

                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.database.success"));
                    });
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.database.failure"));
                    });
                }
            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.database.error").replace("%error%", e.getMessage()));
                });
            }
        });
    }

    private void testDiscordConnection(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.discord.start"));

        try {
            boolean isConnected = plugin.getDiscordBot() != null;

            if (isConnected) {
                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.discord.success"));

                String guildId = plugin.getConfigManager().getString("discord.guild-id");
                if (!guildId.equals("YOUR_GUILD_ID_HERE")) {
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.discord.guild-configured").replace("%guild_id%", guildId));
                } else {
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.discord.guild-not-configured"));
                }

                String muteRole = plugin.getConfigManager().getString("discord.roles.mute");
                String banRole = plugin.getConfigManager().getString("discord.roles.ban");
                String otherRole = plugin.getConfigManager().getString("discord.roles.other");

                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.discord.roles.header"));
                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.discord.roles.mute").replace("%status%", (!muteRole.equals("MUTE_ROLE_ID") ? "§a✓" : "§c✗")));
                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.discord.roles.ban").replace("%status%", (!banRole.equals("BAN_ROLE_ID") ? "§a✓" : "§c✗")));
                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.discord.roles.other").replace("%status%", (!otherRole.equals("OTHER_ROLE_ID") ? "§a✓" : "§c✗")));

                String logChannelId = plugin.getConfigManager().getString("discord.log-channel-id");
                if (!logChannelId.equals("LOG_CHANNEL_ID_HERE")) {
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.discord.log-channel-configured").replace("%channel_id%", logChannelId));

                    try {
                        var logChannel = plugin.getDiscordBot().getClass()
                                .getDeclaredField("guild").get(plugin.getDiscordBot());
                        if (logChannel != null) {
                            sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.discord.log-channel-accessible"));
                        }
                    } catch (Exception ignored) {
                        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.discord.log-channel-access-failure"));
                    }
                } else {
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.discord.log-channel-not-configured"));
                }

            } else {
                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.discord.failure"));
                sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.discord.check-token"));
            }
        } catch (Exception e) {
            sender.sendMessage(plugin.getConfigManager().getColoredString("messages.test.discord.error").replace("%error%", e.getMessage()));
        }
    }

    private void handleStats(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.stats.start"));

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
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.stats.header"));
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.stats.total").replace("%count%", String.valueOf(finalTotalPunishments)));
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.stats.today").replace("%count%", String.valueOf(finalTodayCount)));
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.stats.unique-players").replace("%count%", String.valueOf(finalUniquePlayers)));
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.stats.divider"));
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.stats.types.type-header"));
                    
                    if (finalTotalPunishments > 0) {
                        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.stats.types.mute")
                                .replace("%count%", String.valueOf(finalMuteCount))
                                .replace("%percentage%", String.format("%.1f", (double) finalMuteCount / finalTotalPunishments * 100)));
                        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.stats.types.ban")
                                .replace("%count%", String.valueOf(finalBanCount))
                                .replace("%percentage%", String.format("%.1f", (double) finalBanCount / finalTotalPunishments * 100)));
                        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.stats.types.kick")
                                .replace("%count%", String.valueOf(finalKickCount))
                                .replace("%percentage%", String.format("%.1f", (double) finalKickCount / finalTotalPunishments * 100)));
                        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.stats.types.tempban")
                                .replace("%count%", String.valueOf(finalTempbanCount))
                                .replace("%percentage%", String.format("%.1f", (double) finalTempbanCount / finalTotalPunishments * 100)));
                        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.stats.types.jail")
                                .replace("%count%", String.valueOf(finalJailCount))
                                .replace("%percentage%", String.format("%.1f", (double) finalJailCount / finalTotalPunishments * 100)));
                        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.stats.types.warn")
                                .replace("%count%", String.valueOf(finalWarnCount))
                                .replace("%percentage%", String.format("%.1f", (double) finalWarnCount / finalTotalPunishments * 100)));
                        sender.sendMessage(plugin.getConfigManager().getColoredString("messages.stats.types.other")
                                .replace("%count%", String.valueOf(finalOtherCount))
                                .replace("%percentage%", String.format("%.1f", (double) finalOtherCount / finalTotalPunishments * 100)));
                    } else {
                        sender.sendMessage("§7Henüz hiç ceza verilmemiş.");
                    }
                });

            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(plugin.getConfigManager().getColoredString("messages.stats.error").replace("%error%", e.getMessage()));
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