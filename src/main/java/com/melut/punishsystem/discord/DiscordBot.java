package com.melut.punishsystem.discord;

import com.melut.punishsystem.DiscordPunishBot;
import com.melut.punishsystem.models.Punishment;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DiscordBot extends ListenerAdapter {

    private final DiscordPunishBot plugin;
    private JDA jda;
    private Guild guild;
    private Role muteRole;
    private Role banRole;
    private Role kickRole;
    private Role tempbanRole;
    private Role jailRole;
    private Role warnRole;
    private Role otherRole;

    public DiscordBot(DiscordPunishBot plugin) {
        this.plugin = plugin;
    }

    public void start() {
        String token = plugin.getConfigManager().getString("discord.token");

        if (token == null || token.equals("YOUR_BOT_TOKEN_HERE")) {
            plugin.getLogger().severe(plugin.getConfigManager().getString("messages.log.token-not-configured"));
            return;
        }

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(this)
                    .build();

        } catch (Exception e) {
            plugin.getLogger().severe(plugin.getConfigManager().getString("messages.log.bot-start-failure").replace("%error%", e.getMessage()));
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        plugin.getLogger().info(plugin.getConfigManager().getString("messages.log.bot-ready").replace("%tag%", event.getJDA().getSelfUser().getAsTag()));

        String guildId = plugin.getConfigManager().getString("discord.guild-id");
        if (guildId != null && !guildId.equals("YOUR_GUILD_ID_HERE")) {
            guild = jda.getGuildById(guildId);

            if (guild != null) {
                setupRoles();
                registerCommands();
            } else {
                plugin.getLogger().warning(plugin.getConfigManager().getString("messages.log.guild-not-found").replace("%guild_id%", guildId));
            }
        }
    }

    private void setupRoles() {
        String muteRoleId = plugin.getConfigManager().getString("discord.roles.mute");
        String banRoleId = plugin.getConfigManager().getString("discord.roles.ban");
        String kickRoleId = plugin.getConfigManager().getString("discord.roles.kick");
        String tempbanRoleId = plugin.getConfigManager().getString("discord.roles.tempban");
        String jailRoleId = plugin.getConfigManager().getString("discord.roles.jail");
        String warnRoleId = plugin.getConfigManager().getString("discord.roles.warn");
        String otherRoleId = plugin.getConfigManager().getString("discord.roles.other");

        if (!muteRoleId.equals("MUTE_ROLE_ID")) {
            muteRole = guild.getRoleById(muteRoleId);
        }
        if (!banRoleId.equals("BAN_ROLE_ID")) {
            banRole = guild.getRoleById(banRoleId);
        }
        if (!kickRoleId.equals("KICK_ROLE_ID")) {
            kickRole = guild.getRoleById(kickRoleId);
        }
        if (!tempbanRoleId.equals("TEMPBAN_ROLE_ID")) {
            tempbanRole = guild.getRoleById(tempbanRoleId);
        }
        if (!jailRoleId.equals("JAIL_ROLE_ID")) {
            jailRole = guild.getRoleById(jailRoleId);
        }
        if (!warnRoleId.equals("WARN_ROLE_ID")) {
            warnRole = guild.getRoleById(warnRoleId);
        }
        if (!otherRoleId.equals("OTHER_ROLE_ID")) {
            otherRole = guild.getRoleById(otherRoleId);
        }
    }

    private void registerCommands() {
        List<CommandData> commands = List.of(
                Commands.slash("mute", plugin.getConfigManager().getString("messages.discord.commands.mute.description"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getConfigManager().getString("messages.discord.commands.mute.option-player"), true)
                        .addOption(OptionType.STRING, "sebep", plugin.getConfigManager().getString("messages.discord.commands.mute.option-reason"), false),

                Commands.slash("ban", plugin.getConfigManager().getString("messages.discord.commands.ban.description"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getConfigManager().getString("messages.discord.commands.ban.option-player"), true)
                        .addOption(OptionType.STRING, "sebep", plugin.getConfigManager().getString("messages.discord.commands.ban.option-reason"), false),

                Commands.slash("kick", plugin.getConfigManager().getString("messages.discord.commands.kick.description"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getConfigManager().getString("messages.discord.commands.kick.option-player"), true)
                        .addOption(OptionType.STRING, "sebep", plugin.getConfigManager().getString("messages.discord.commands.kick.option-reason"), false),

                Commands.slash("tempban", plugin.getConfigManager().getString("messages.discord.commands.tempban.description"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getConfigManager().getString("messages.discord.commands.tempban.option-player"), true)
                        .addOption(OptionType.STRING, "sebep", plugin.getConfigManager().getString("messages.discord.commands.tempban.option-reason"), false),

                Commands.slash("jail", plugin.getConfigManager().getString("messages.discord.commands.jail.description"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getConfigManager().getString("messages.discord.commands.jail.option-player"), true)
                        .addOption(OptionType.STRING, "sebep", plugin.getConfigManager().getString("messages.discord.commands.jail.option-reason"), false),

                Commands.slash("warn", plugin.getConfigManager().getString("messages.discord.commands.warn.description"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getConfigManager().getString("messages.discord.commands.warn.option-player"), true)
                        .addOption(OptionType.STRING, "sebep", plugin.getConfigManager().getString("messages.discord.commands.warn.option-reason"), false),

                Commands.slash("diger", plugin.getConfigManager().getString("messages.discord.commands.other.description"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getConfigManager().getString("messages.discord.commands.other.option-player"), true)
                        .addOption(OptionType.STRING, "sebep", plugin.getConfigManager().getString("messages.discord.commands.other.option-reason"), false),

                Commands.slash("ceza", plugin.getConfigManager().getString("messages.discord.commands.ceza.description"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getConfigManager().getString("messages.discord.commands.ceza.option-player"), true)
        );

        guild.updateCommands().addCommands(commands).queue(
                success -> plugin.getLogger().info(plugin.getConfigManager().getString("messages.log.commands-registered")),
                error -> plugin.getLogger().severe(plugin.getConfigManager().getString("messages.log.commands-register-failure").replace("%error%", error.getMessage()))
        );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!hasPermission(event, event.getName())) {
            event.reply(plugin.getConfigManager().getString("messages.discord.no-permission")).setEphemeral(true).queue();
            return;
        }

        String playerName = event.getOption("oyuncu").getAsString();

        switch (event.getName()) {
            case "mute":
                handlePunishmentCommand(event, playerName, "mute");
                break;
            case "ban":
                handlePunishmentCommand(event, playerName, "ban");
                break;
            case "kick":
                handlePunishmentCommand(event, playerName, "kick");
                break;
            case "tempban":
                handlePunishmentCommand(event, playerName, "tempban");
                break;
            case "jail":
                handlePunishmentCommand(event, playerName, "jail");
                break;
            case "warn":
                handlePunishmentCommand(event, playerName, "warn");
                break;
            case "diger":
                handlePunishmentCommand(event, playerName, "other");
                break;
            case "ceza":
                handleCezaCommand(event, playerName);
                break;
        }
    }

    private void handlePunishmentCommand(SlashCommandInteractionEvent event, String playerName, String type) {
        String customReason = event.getOption("sebep") != null ? event.getOption("sebep").getAsString() : null;

        if (customReason != null) {
            if (plugin.getPunishmentManager().isValidReason(type, customReason)) {
                executePunishment(event, playerName, type, customReason);
            } else {
                event.reply(plugin.getConfigManager().getString("messages.discord.invalid-reason"))
                        .setEphemeral(true)
                        .queue(success -> showReasonMenuAsFollowup(event, playerName, type));
            }
        } else {
            showReasonMenu(event, playerName, type);
        }
    }

    private void showReasonMenu(SlashCommandInteractionEvent event, String playerName, String type) {
        List<String> reasons = plugin.getPunishmentManager().getAvailableReasons(type);

        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("punishment:" + type + ":" + playerName)
                .setPlaceholder(plugin.getConfigManager().getString("messages.discord.reason-menu-placeholder"));

        for (String reason : reasons) {
            String display = plugin.getConfigManager().getPunishmentDisplay(type, reason);
            menuBuilder.addOption(display, reason, plugin.getConfigManager().getString("messages.discord.reason-menu-option").replace("%display%", display));
        }

        event.reply(plugin.getConfigManager().getString("messages.discord.reason-menu")
                        .replace("%player%", playerName)
                        .replace("%type%", getTypeDisplayName(type)))
                .addActionRow(menuBuilder.build())
                .setEphemeral(true)
                .queue();
    }

    private void showReasonMenuAsFollowup(SlashCommandInteractionEvent event, String playerName, String type) {
        List<String> reasons = plugin.getPunishmentManager().getAvailableReasons(type);

        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("punishment:" + type + ":" + playerName)
                .setPlaceholder(plugin.getConfigManager().getString("messages.discord.reason-menu-placeholder"));

        for (String reason : reasons) {
            String display = plugin.getConfigManager().getPunishmentDisplay(type, reason);
            menuBuilder.addOption(display, reason, plugin.getConfigManager().getString("messages.discord.reason-menu-option").replace("%display%", display));
        }

        event.getHook().editOriginal(plugin.getConfigManager().getString("messages.discord.reason-menu")
                        .replace("%player%", playerName)
                        .replace("%type%", getTypeDisplayName(type)))
                .setActionRow(menuBuilder.build())
                .queue();
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String[] parts = event.getComponentId().split(":");
        if (parts.length != 3 || !parts[0].equals("punishment")) {
            return;
        }

        String type = parts[1];
        String playerName = parts[2];
        String reason = event.getSelectedOptions().get(0).getValue();

        executePunishment(event, playerName, type, reason);
    }

    private void executePunishment(Object event, String playerName, String type, String reason) {
        String adminName = null;

        if (event instanceof SlashCommandInteractionEvent) {
            adminName = ((SlashCommandInteractionEvent) event).getUser().getName();
            ((SlashCommandInteractionEvent) event).reply(plugin.getConfigManager().getString("messages.discord.punishment-executing")).setEphemeral(true).queue();
        } else if (event instanceof StringSelectInteractionEvent) {
            adminName = ((StringSelectInteractionEvent) event).getUser().getName();
            ((StringSelectInteractionEvent) event).reply(plugin.getConfigManager().getString("messages.discord.punishment-executing")).setEphemeral(true).queue();
        }

        final String finalAdminName = adminName;

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getPunishmentManager().executePunishment(playerName, finalAdminName, type, reason);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (event instanceof SlashCommandInteractionEvent) {
                    ((SlashCommandInteractionEvent) event).getHook()
                            .editOriginal(plugin.getConfigManager().getString("messages.discord.punishment-success")
                                    .replace("%player%", playerName)
                                    .replace("%reason%", plugin.getConfigManager().getPunishmentDisplay(type, reason)))
                            .queue();
                } else if (event instanceof StringSelectInteractionEvent) {
                    ((StringSelectInteractionEvent) event).getHook()
                            .editOriginal(plugin.getConfigManager().getString("messages.discord.punishment-success")
                                    .replace("%player%", playerName)
                                    .replace("%reason%", plugin.getConfigManager().getPunishmentDisplay(type, reason)))
                            .queue();
                }
            }, 20L);
        });
    }

    private void handleCezaCommand(SlashCommandInteractionEvent event, String playerName) {
        event.deferReply(true).queue();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Punishment> punishments = plugin.getPunishmentManager().getPlayerPunishments(playerName, 10);
            int total = plugin.getPunishmentManager().getTotalPunishments(playerName);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(plugin.getConfigManager().getString("messages.discord.ceza-embed-title").replace("%player%", playerName))
                    .setColor(Color.ORANGE)
                    .setFooter(plugin.getConfigManager().getString("messages.discord.ceza-embed-footer").replace("%total%", String.valueOf(total)))
                    .setTimestamp(java.time.Instant.now());

            if (punishments.isEmpty()) {
                embed.setDescription(plugin.getConfigManager().getString("messages.discord.ceza-no-punishments"));
            } else {
                StringBuilder description = new StringBuilder();
                for (int i = 0; i < Math.min(10, punishments.size()); i++) {
                    Punishment p = punishments.get(i);
                    description.append(plugin.getConfigManager().getString("messages.discord.ceza-entry")
                            .replace("%index%", String.valueOf(i + 1))
                            .replace("%date%", p.getFormattedDate())
                            .replace("%type%", p.getTypeDisplay())
                            .replace("%reason%", p.getReason())
                            .replace("%admin%", p.getAdminName())
                            .replace("%status%", p.isActive()
                                    ? plugin.getConfigManager().getString("messages.discord.status.active")
                                    : plugin.getConfigManager().getString("messages.discord.status.inactive")));
                }
                embed.setDescription(description.toString());
            }

            event.getHook().editOriginalEmbeds(embed.build()).queue();
        });
    }

    public void sendPunishmentNotification(Punishment punishment) {
        if (guild == null) return;

        String logChannelId = plugin.getConfigManager().getString("discord.log-channel-id");
        TextChannel logChannel = null;

        if (!logChannelId.isEmpty() && !logChannelId.equals("LOG_CHANNEL_ID_HERE")) {
            logChannel = guild.getTextChannelById(logChannelId);
        }

        if (logChannel == null) {
            logChannel = guild.getTextChannels().stream()
                    .filter(c -> c.getName().contains("ceza") || c.getName().contains("punishment") || c.getName().contains("log"))
                    .findFirst()
                    .orElse(null);
        }

        if (logChannel == null) {
            logChannel = guild.getSystemChannel();
            if (logChannel == null && !guild.getTextChannels().isEmpty()) {
                logChannel = guild.getTextChannels().get(0);
            }
        }

        if (logChannel == null) {
            plugin.getLogger().warning(plugin.getConfigManager().getString("messages.log.no-log-channel"));
            return;
        }

        String command = plugin.getConfigManager().getPunishmentCommand(punishment.getType(),
                getPunishmentReasonKey(punishment.getType(), punishment.getReason()));
        String duration = extractDurationFromCommand(command);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(plugin.getConfigManager().getString("discord.embeds.punishment.title"))
                    .setColor(getPunishmentColor(punishment.getType()))
                    .setAuthor(punishment.getAdminName(), null, null)
                    .addField(plugin.getConfigManager().getString("messages.discord.embed-field-player"), punishment.getPlayerName(), true)
                    .addField(plugin.getConfigManager().getString("messages.discord.embed-field-type"), punishment.getTypeDisplay(), true)
                    .addField(plugin.getConfigManager().getString("messages.discord.embed-field-reason"), punishment.getReason(), false)
                    .addField(plugin.getConfigManager().getString("messages.discord.embed-field-date"), punishment.getFormattedDate(), true)
                    .addField(plugin.getConfigManager().getString("messages.discord.embed-field-duration"), duration.isEmpty() ? plugin.getConfigManager().getString("messages.discord.duration-permanent") : duration, true)
                    .addField(plugin.getConfigManager().getString("messages.discord.embed-field-id"), "#" + punishment.getId(), true)
                    .setFooter(plugin.getConfigManager().getString("discord.embeds.punishment.footer"))
                    .setTimestamp(java.time.Instant.now());

        embed.setThumbnail(getPunishmentThumbnail(punishment.getType()));

        logChannel.sendMessageEmbeds(embed.build()).queue(
                success -> plugin.getLogger().info(plugin.getConfigManager().getString("messages.log.notification-sent")),
                error -> plugin.getLogger().warning(plugin.getConfigManager().getString("messages.log.notification-failure").replace("%error%", error.getMessage()))
        );
    }

    public void sendErrorMessage(String message) {
        if (guild == null) return;

        String logChannelId = plugin.getConfigManager().getString("discord.log-channel-id");
        TextChannel logChannel = null;

        if (!logChannelId.isEmpty() && !logChannelId.equals("LOG_CHANNEL_ID_HERE")) {
            logChannel = guild.getTextChannelById(logChannelId);
        }

        if (logChannel != null) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("⚠️ Hata")
                    .setDescription(message)
                    .setColor(Color.RED)
                    .setTimestamp(java.time.Instant.now());

            logChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    private int getPunishmentColor(String type) {
        switch (type.toLowerCase()) {
            case "mute":
                return 0xF39C12; // Orange
            case "ban":
                return 0xE74C3C; // Red
            case "kick":
                return 0xF1C40F; // Yellow
            case "tempban":
                return 0x9B59B6; // Purple
            case "jail":
                return 0x3498DB; // Blue
            case "warn":
                return 0x2ECC71; // Green
            default:
                return 0x3498DB; // Blue
        }
    }

    private String getPunishmentThumbnail(String type) {
        switch (type.toLowerCase()) {
            case "mute":
                return plugin.getConfigManager().getString("messages.discord.embed-thumbnail-mute");
            case "ban":
                return plugin.getConfigManager().getString("messages.discord.embed-thumbnail-ban");
            case "kick":
                return plugin.getConfigManager().getString("messages.discord.embed-thumbnail-kick");
            case "tempban":
                return plugin.getConfigManager().getString("messages.discord.embed-thumbnail-tempban");
            case "jail":
                return plugin.getConfigManager().getString("messages.discord.embed-thumbnail-jail");
            case "warn":
                return plugin.getConfigManager().getString("messages.discord.embed-thumbnail-warn");
            default:
                return plugin.getConfigManager().getString("messages.discord.embed-thumbnail-other");
        }
    }

    private String extractDurationFromCommand(String command) {
        if (command == null) return "";

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+[smhdw])");
        java.util.regex.Matcher matcher = pattern.matcher(command);

        if (matcher.find()) {
            String duration = matcher.group(1);
            return formatDuration(duration);
        }

        return "";
    }

    private String formatDuration(String duration) {
        if (duration == null || duration.isEmpty()) return "";

        char unit = duration.charAt(duration.length() - 1);
        String number = duration.substring(0, duration.length() - 1);

        switch (unit) {
            case 's': return number + plugin.getConfigManager().getString("messages.discord.duration-unit-seconds");
            case 'm': return number + plugin.getConfigManager().getString("messages.discord.duration-unit-minutes");
            case 'h': return number + plugin.getConfigManager().getString("messages.discord.duration-unit-hours");
            case 'd': return number + plugin.getConfigManager().getString("messages.discord.duration-unit-days");
            case 'w': return number + plugin.getConfigManager().getString("messages.discord.duration-unit-weeks");
            default: return duration;
        }
    }

    private String getPunishmentReasonKey(String type, String displayReason) {
        List<String> reasons = plugin.getPunishmentManager().getAvailableReasons(type);
        for (String reason : reasons) {
            String display = plugin.getConfigManager().getPunishmentDisplay(type, reason);
            if (display.equals(displayReason)) {
                return reason;
            }
        }
        return "";
    }

    private boolean hasPermission(SlashCommandInteractionEvent event, String commandType) {
        switch (commandType) {
            case "mute":
                return muteRole != null && event.getMember().getRoles().contains(muteRole);
            case "ban":
                return banRole != null && event.getMember().getRoles().contains(banRole);
            case "kick":
                return kickRole != null && event.getMember().getRoles().contains(kickRole);
            case "tempban":
                return tempbanRole != null && event.getMember().getRoles().contains(tempbanRole);
            case "jail":
                return jailRole != null && event.getMember().getRoles().contains(jailRole);
            case "warn":
                return warnRole != null && event.getMember().getRoles().contains(warnRole);
            case "diger":
                return otherRole != null && event.getMember().getRoles().contains(otherRole);
            case "ceza":
                return true;
            default:
                return false;
        }
    }

    private String getTypeDisplayName(String type) {
        switch (type) {
            case "mute": return plugin.getConfigManager().getString("messages.discord.type-display.mute");
            case "ban": return plugin.getConfigManager().getString("messages.discord.type-display.ban");
            case "kick": return plugin.getConfigManager().getString("messages.discord.type-display.kick");
            case "tempban": return plugin.getConfigManager().getString("messages.discord.type-display.tempban");
            case "jail": return plugin.getConfigManager().getString("messages.discord.type-display.jail");
            case "warn": return plugin.getConfigManager().getString("messages.discord.type-display.warn");
            case "other": return plugin.getConfigManager().getString("messages.discord.type-display.other");
            default: return type;
        }
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            try {
                if (!jda.awaitShutdown(10, TimeUnit.SECONDS)) {
                    jda.shutdownNow();
                }
            } catch (InterruptedException e) {
                jda.shutdownNow();
            }
        }
    }

}
