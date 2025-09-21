package com.melut.punishsystem.discord;

import com.melut.punishsystem.DiscordPunishBot;
import com.melut.punishsystem.models.Appeal;
import com.melut.punishsystem.models.Punishment;
import com.melut.punishsystem.models.Report;
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
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
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
            plugin.getLogger().severe(plugin.getLanguageManager().getMessage("log.token-not-configured"));
            return;
        }

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(this)
                    .build();

        } catch (Exception e) {
            plugin.getLogger().severe(plugin.getLanguageManager().getFormattedMessage("log.bot-start-failure", "error", e.getMessage()));
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        plugin.getLogger().info(plugin.getLanguageManager().getFormattedMessage("log.bot-ready", "tag", event.getJDA().getSelfUser().getAsTag()));

        String guildId = plugin.getConfigManager().getString("discord.guild-id");
        if (guildId != null && !guildId.equals("YOUR_GUILD_ID_HERE")) {
            guild = jda.getGuildById(guildId);

            if (guild != null) {
                setupRoles();
                registerCommands();
            } else {
                plugin.getLogger().warning(plugin.getLanguageManager().getFormattedMessage("log.guild-not-found", "guild_id", guildId));
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
                Commands.slash("mute", plugin.getLanguageManager().getDiscordCommandDescription("mute"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getLanguageManager().getDiscordCommandOption("mute", "option-player"), true)
                        .addOption(OptionType.STRING, "sebep", plugin.getLanguageManager().getDiscordCommandOption("mute", "option-reason"), false),

                Commands.slash("ban", plugin.getLanguageManager().getDiscordCommandDescription("ban"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getLanguageManager().getDiscordCommandOption("ban", "option-player"), true)
                        .addOption(OptionType.STRING, "sebep", plugin.getLanguageManager().getDiscordCommandOption("ban", "option-reason"), false),

                Commands.slash("kick", plugin.getLanguageManager().getDiscordCommandDescription("kick"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getLanguageManager().getDiscordCommandOption("kick", "option-player"), true)
                        .addOption(OptionType.STRING, "sebep", plugin.getLanguageManager().getDiscordCommandOption("kick", "option-reason"), false),

                Commands.slash("tempban", plugin.getLanguageManager().getDiscordCommandDescription("tempban"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getLanguageManager().getDiscordCommandOption("tempban", "option-player"), true)
                        .addOption(OptionType.STRING, "sebep", plugin.getLanguageManager().getDiscordCommandOption("tempban", "option-reason"), false),

                Commands.slash("jail", plugin.getLanguageManager().getDiscordCommandDescription("jail"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getLanguageManager().getDiscordCommandOption("jail", "option-player"), true)
                        .addOption(OptionType.STRING, "sebep", plugin.getLanguageManager().getDiscordCommandOption("jail", "option-reason"), false),

                Commands.slash("warn", plugin.getLanguageManager().getDiscordCommandDescription("warn"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getLanguageManager().getDiscordCommandOption("warn", "option-player"), true)
                        .addOption(OptionType.STRING, "sebep", plugin.getLanguageManager().getDiscordCommandOption("warn", "option-reason"), false),

                Commands.slash("diger", plugin.getLanguageManager().getDiscordCommandDescription("other"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getLanguageManager().getDiscordCommandOption("other", "option-player"), true)
                        .addOption(OptionType.STRING, "sebep", plugin.getLanguageManager().getDiscordCommandOption("other", "option-reason"), false),

                Commands.slash("ceza", plugin.getLanguageManager().getDiscordCommandDescription("ceza"))
                        .addOption(OptionType.STRING, "oyuncu", plugin.getLanguageManager().getDiscordCommandOption("ceza", "option-player"), true)
        );

        guild.updateCommands().addCommands(commands).queue(
                success -> plugin.getLogger().info(plugin.getLanguageManager().getMessage("log.commands-registered")),
                error -> plugin.getLogger().severe(plugin.getLanguageManager().getFormattedMessage("log.commands-register-failure", "error", error.getMessage()))
        );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!hasPermission(event, event.getName())) {
            event.reply(plugin.getLanguageManager().getMessage("discord.no-permission")).setEphemeral(true).queue();
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
                event.reply(plugin.getLanguageManager().getMessage("discord.invalid-reason"))
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
                .setPlaceholder(plugin.getLanguageManager().getMessage("discord.reason-menu-placeholder"));

        for (String reason : reasons) {
            String display = plugin.getConfigManager().getPunishmentDisplay(type, reason);
            menuBuilder.addOption(display, reason, plugin.getLanguageManager().getFormattedMessage("discord.reason-menu-option", "display", display));
        }

        event.reply(plugin.getLanguageManager().getFormattedMessage("discord.reason-menu",
                        "player", playerName,
                        "type", getTypeDisplayName(type)))
                .addActionRow(menuBuilder.build())
                .setEphemeral(true)
                .queue();
    }

    private void showReasonMenuAsFollowup(SlashCommandInteractionEvent event, String playerName, String type) {
        List<String> reasons = plugin.getPunishmentManager().getAvailableReasons(type);

        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("punishment:" + type + ":" + playerName)
                .setPlaceholder(plugin.getLanguageManager().getMessage("discord.reason-menu-placeholder"));

        for (String reason : reasons) {
            String display = plugin.getConfigManager().getPunishmentDisplay(type, reason);
            menuBuilder.addOption(display, reason, plugin.getLanguageManager().getFormattedMessage("discord.reason-menu-option", "display", display));
        }

        event.getHook().editOriginal(plugin.getLanguageManager().getFormattedMessage("discord.reason-menu",
                        "player", playerName,
                        "type", getTypeDisplayName(type)))
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

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        
        if (buttonId.startsWith("report_")) {
            handleReportButtonClick(event, buttonId);
        } else if (buttonId.startsWith("appeal_")) {
            handleAppealButtonClick(event, buttonId);
        }
    }

    private void handleReportButtonClick(ButtonInteractionEvent event, String buttonId) {
        String reportAdminRoleId = plugin.getConfigManager().getString("discord.report-admin-role-id");
        if (!reportAdminRoleId.equals("REPORT_ADMIN_ROLE_ID") &&
            !event.getMember().getRoles().stream().anyMatch(role -> role.getId().equals(reportAdminRoleId))) {
            event.reply("❌ Bu işlem için yetkiniz bulunmuyor!").setEphemeral(true).queue();
            return;
        }

        String[] parts = buttonId.split("_");
        if (parts.length != 3) return;
        
        String action = parts[1]; // approve, reject, investigate
        int reportId = Integer.parseInt(parts[2]);
        
        String adminName = event.getUser().getName();
        String adminId = event.getUser().getId();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Report report = plugin.getReportManager().getReport(reportId);
            if (report == null) {
                event.reply("❌ Report bulunamadı!").setEphemeral(true).queue();
                return;
            }

            switch (action) {
                case "approve":
                    handleReportApproval(event, report, adminName, adminId);
                    break;
                case "reject":
                    handleReportRejection(event, report, adminName, adminId);
                    break;
                case "investigate":
                    handleReportInvestigation(event, report, adminName, adminId);
                    break;
            }
        });
    }

    private void handleReportApproval(ButtonInteractionEvent event, Report report, String adminName, String adminId) {
        plugin.getReportManager().resolveReport(report.getId(), adminName, adminId, "Report onaylandı - Manuel ceza işlemi gerekli");
        
        if (plugin.getConfigManager().getBoolean("reward-system.enabled", true)) {
            // Eco komutu çalıştır
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                java.util.List<String> commands = plugin.getConfigManager().getStringList("reward-system.report-correct-commands");
                if (commands != null && !commands.isEmpty()) {
                    for (String command : commands) {
                        String processedCommand = command.replace("%player%", report.getReporterName());
                        org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), processedCommand);
                        plugin.getLogger().info("Report ödül komutu çalıştırıldı: " + processedCommand);
                    }
                }
            });
        }

        updateReportLogMessage(event, report, "✅ ONAYLANDI", 0x2ECC71, adminName, "Report onaylandı! Manuel ceza işlemi gerekli.");
        
        event.reply("✅ Report başarıyla onaylandı! Artık manuel olarak oyuncuya ceza verebilirsiniz. Report eden kişiye ödül verildi.").setEphemeral(true).queue();
    }

    private void handleReportRejection(ButtonInteractionEvent event, Report report, String adminName, String adminId) {
        plugin.getReportManager().dismissReport(report.getId(), adminName, adminId, "Report reddedildi - Geçersiz şikayet");
        
        updateReportLogMessage(event, report, "❌ REDDEDİLDİ", 0xE74C3C, adminName, "Report geçersiz bulundu ve reddedildi.");
        
        event.reply("❌ Report başarıyla reddedildi!").setEphemeral(true).queue();
    }

    private void handleReportInvestigation(ButtonInteractionEvent event, Report report, String adminName, String adminId) {
        plugin.getReportManager().setUnderReview(report.getId(), adminName, adminId);
        
        updateReportInvestigationMessage(event, report, adminName, adminId);
        
        event.reply("🔍 Report inceleme altına alındı! Butonlar aktif kaldı, inceleme sonrası Onayla veya Reddet butonlarını kullanabilirsiniz.").setEphemeral(true).queue();
    }

    private void updateReportLogMessage(ButtonInteractionEvent event, Report report, String status, int color, String adminName, String response) {
        EmbedBuilder embed = createDetailedReportEmbed(report, status, color, adminName, response);

        event.getMessage().editMessageEmbeds(embed.build()).setComponents().queue();
    }
    
    private void updateReportInvestigationMessage(ButtonInteractionEvent event, Report report, String adminName, String adminId) {
        EmbedBuilder embed = createDetailedReportEmbed(report, "🔍 İNCELENİYOR", 0x3498DB, adminName, "Report inceleme altına alındı");
        
        Button approveButton = Button.success("report_approve_" + report.getId(), "✅ Onayla & Manuel Ceza");
        Button rejectButton = Button.danger("report_reject_" + report.getId(), "❌ Reddet");
        Button investigateButton = Button.secondary("report_investigate_" + report.getId(), "🔍 İncelemeye Al").asDisabled();
        
        event.getMessage().editMessageEmbeds(embed.build())
                .setActionRow(approveButton, rejectButton, investigateButton)
                .queue();
    }
    
    private EmbedBuilder createDetailedReportEmbed(Report report, String status, int color, String adminName, String response) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🚨 REPORT SİSTEMİ - " + status)
                .setColor(color)
                .setThumbnail(report.isAnonymous() ? "https://mc-heads.net/avatar/steve" : getPlayerAvatar(report.getReporterName()));

        embed.addField("🆔 Report ID", "`#" + report.getId() + "`", true)
             .addField("🎯 Şikayet Edilen", "**" + report.getReportedPlayerName() + "**", true)
             .addField("👤 Şikayet Eden", "**" + report.getDisplayReporterName() + "**", true);
        
        embed.addField("\u200B", "═══════════════════════════════", false);
        
        embed.addField("⚠️ Şikayet Sebebi", "```" + report.getReason() + "```", false);
        
        if (report.getDescription() != null && !report.getDescription().isEmpty()) {
            embed.addField("📝 Açıklama", "```" + report.getDescription() + "```", false);
        }
        
        embed.addField("🏷️ Öncelik Seviyesi", getPriorityDisplayWithIcon(report.getPriority()), true)
             .addField("📊 Report Durumu", status, true)
             .addField("📅 Report Tarihi", "**" + report.getFormattedReportDate() + "**", true);
        
        if (adminName != null) {
            embed.addField("👨‍💼 İşlem Yapan Admin", "**" + adminName + "**", true);
        }
        
        if (response != null && !response.isEmpty()) {
            embed.addField("💬 Admin Yanıtı", "```" + response + "```", false);
        }
        
        embed.setFooter("DiscordPunishBot Report Sistemi | Developed by Melut", "https://mc-heads.net/avatar/notch")
             .setTimestamp(java.time.Instant.now());

        return embed;
    }
    
    private String getPriorityDisplayWithIcon(Report.ReportPriority priority) {
        switch (priority) {
            case LOW:
                return "🟢 **DÜŞÜK**";
            case MEDIUM:
                return "🟡 **ORTA**";
            case HIGH:
                return "🟠 **YÜKSEK**";
            case URGENT:
                return "🔴 **ACİL**";
            case CRITICAL:
                return "⚫ **KRİTİK**";
            default:
                return "⚪ **BİLİNMEYEN**";
        }
    }

    private void executePunishment(Object event, String playerName, String type, String reason) {
        String adminName = null;

        if (event instanceof SlashCommandInteractionEvent) {
            adminName = ((SlashCommandInteractionEvent) event).getUser().getName();
            ((SlashCommandInteractionEvent) event).reply(plugin.getLanguageManager().getMessage("discord.punishment-executing")).setEphemeral(true).queue();
        } else if (event instanceof StringSelectInteractionEvent) {
            adminName = ((StringSelectInteractionEvent) event).getUser().getName();
            ((StringSelectInteractionEvent) event).reply(plugin.getLanguageManager().getMessage("discord.punishment-executing")).setEphemeral(true).queue();
        }

        final String finalAdminName = adminName;

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getPunishmentManager().executePunishment(playerName, finalAdminName, type, reason);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (event instanceof SlashCommandInteractionEvent) {
                    ((SlashCommandInteractionEvent) event).getHook()
                            .editOriginal(plugin.getLanguageManager().getFormattedMessage("discord.punishment-success",
                                    "player", playerName,
                                    "reason", plugin.getConfigManager().getPunishmentDisplay(type, reason)))
                            .queue();
                } else if (event instanceof StringSelectInteractionEvent) {
                    ((StringSelectInteractionEvent) event).getHook()
                            .editOriginal(plugin.getLanguageManager().getFormattedMessage("discord.punishment-success",
                                    "player", playerName,
                                    "reason", plugin.getConfigManager().getPunishmentDisplay(type, reason)))
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
                    .setTitle(plugin.getLanguageManager().getFormattedMessage("discord.ceza-embed-title", "player", playerName))
                    .setColor(Color.ORANGE)
                    .setFooter(plugin.getLanguageManager().getFormattedMessage("discord.ceza-embed-footer", "total", String.valueOf(total)))
                    .setTimestamp(java.time.Instant.now());

            if (punishments.isEmpty()) {
                embed.setDescription(plugin.getLanguageManager().getMessage("discord.ceza-no-punishments"));
            } else {
                StringBuilder description = new StringBuilder();
                for (int i = 0; i < Math.min(10, punishments.size()); i++) {
                    Punishment p = punishments.get(i);
                    description.append(plugin.getLanguageManager().getFormattedMessage("discord.ceza-entry",
                            "index", String.valueOf(i + 1),
                            "date", p.getFormattedDate(),
                            "type", p.getTypeDisplay(),
                            "reason", p.getReason(),
                            "admin", p.getAdminName(),
                            "status", p.isActive()
                                    ? plugin.getLanguageManager().getMessage("discord.status.active")
                                    : plugin.getLanguageManager().getMessage("discord.status.inactive")));
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
            plugin.getLogger().warning(plugin.getLanguageManager().getMessage("log.no-log-channel"));
            return;
        }

        String command = plugin.getConfigManager().getPunishmentCommand(punishment.getType(),
                getPunishmentReasonKey(punishment.getType(), punishment.getReason()));
        String duration = extractDurationFromCommand(command);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(plugin.getConfigManager().getString("discord.embeds.punishment.title"))
                    .setColor(getPunishmentColor(punishment.getType()))
                    .setAuthor(punishment.getAdminName(), null, null)
                    .addField(plugin.getLanguageManager().getMessage("discord.embed-field-player"), punishment.getPlayerName(), true)
                    .addField(plugin.getLanguageManager().getMessage("discord.embed-field-type"), punishment.getTypeDisplay(), true)
                    .addField(plugin.getLanguageManager().getMessage("discord.embed-field-reason"), punishment.getReason(), false)
                    .addField(plugin.getLanguageManager().getMessage("discord.embed-field-date"), punishment.getFormattedDate(), true)
                    .addField(plugin.getLanguageManager().getMessage("discord.embed-field-duration"), duration.isEmpty() ? plugin.getLanguageManager().getMessage("discord.duration-permanent") : duration, true)
                    .addField(plugin.getLanguageManager().getMessage("discord.embed-field-id"), "#" + punishment.getId(), true)
                    .setFooter(plugin.getConfigManager().getString("discord.embeds.punishment.footer"))
                    .setTimestamp(java.time.Instant.now());

        String thumbnailUrl = getPunishmentThumbnail(punishment.getType());
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty() &&
            (thumbnailUrl.startsWith("http://") || thumbnailUrl.startsWith("https://"))) {
            embed.setThumbnail(thumbnailUrl);
        }

        logChannel.sendMessageEmbeds(embed.build()).queue(
                success -> plugin.getLogger().info(plugin.getLanguageManager().getMessage("log.notification-sent")),
                error -> plugin.getLogger().warning(plugin.getLanguageManager().getFormattedMessage("log.notification-failure", "error", error.getMessage()))
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
                return 0xF39C12;
            case "ban":
                return 0xE74C3C;
            case "kick":
                return 0xF1C40F;
            case "tempban":
                return 0x9B59B6;
            case "jail":
                return 0x3498DB;
            case "warn":
                return 0x2ECC71;
            default:
                return 0x3498DB;
        }
    }

    private String getPunishmentThumbnail(String type) {
        // Return null since we don't have valid thumbnail URLs configured
        return null;
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
        return plugin.getLanguageManager().getMessage("discord.type-display." + type, type);
    }

    public void sendAppealNotification(Appeal appeal) {
        if (guild == null) return;

        TextChannel appealChannel = getAppealChannel();
        if (appealChannel == null) return;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📋 YENİ İTİRAZ GELDİ - İNCELEME GEREKLİ!")
                .setColor(0x3498DB)
                .setThumbnail(getPlayerAvatar(appeal.getPlayerName()))
                .setDescription("**Yeni bir itiraz geldi ve admin kontrolü bekliyor!**");
                
        // Temel bilgiler
        embed.addField("🆔 İtiraz ID", "`#" + appeal.getId() + "`", true)
             .addField("⚖️ Ceza ID", "`#" + appeal.getPunishmentId() + "`", true)
             .addField("👤 Oyuncu", "**" + appeal.getPlayerName() + "**", true);
        
        // Divider
        embed.addField("\u200B", "═══════════════════════════════", false);
        
        // İtiraz detayları
        embed.addField("📝 İtiraz Gerekçesi", "```" + appeal.getAppealReason() + "```", false);
        
        // Status ve tarih bilgileri
        embed.addField("📊 Durum", "🟡 **BEKLEMEDEİ**", true)
             .addField("📅 İtiraz Tarihi", "**" + appeal.getFormattedAppealDate() + "**", true)
             .addField("⏳ Bekleme Süresi", "**Yeni**", true);
        
        // Divider
        embed.addField("\u200B", "═══════════════════════════════", false);
        
        embed.setFooter("DiscordPunishBot İtiraz Sistemi | İtiraz ID: #" + appeal.getId(), "https://mc-heads.net/avatar/notch")
             .setTimestamp(java.time.Instant.now());

        // İtiraz butonları ekle
        Button approveButton = Button.success("appeal_approve_" + appeal.getId(), "✅ İtirazı Onayla");
        Button rejectButton = Button.danger("appeal_reject_" + appeal.getId(), "❌ İtirazı Reddet");
        Button investigateButton = Button.secondary("appeal_investigate_" + appeal.getId(), "🔍 İncelemeye Al");

        appealChannel.sendMessageEmbeds(embed.build())
                .setActionRow(approveButton, rejectButton, investigateButton)
                .queue(
                message -> {
                    String messageId = message.getId();
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
                            java.sql.PreparedStatement stmt = conn.prepareStatement(
                                "UPDATE appeals SET discord_message_id = ? WHERE id = ?");
                            stmt.setString(1, messageId);
                            stmt.setInt(2, appeal.getId());
                            stmt.executeUpdate();
                            conn.commit();
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to save Discord message ID: " + e.getMessage());
                        }
                    });
                },
                error -> plugin.getLogger().warning("Failed to send appeal notification: " + error.getMessage())
        );
    }

    public void updateAppealMessage(Appeal appeal, boolean approved) {
        if (guild == null || appeal.getDiscordMessageId() == null || appeal.getDiscordMessageId().isEmpty()) {
            plugin.getLogger().warning("Appeal Discord mesajı güncellenemiyor: Guild null veya mesaj ID boş (Appeal ID: " + appeal.getId() + ")");
            return;
        }

        TextChannel appealChannel = getAppealChannel();
        if (appealChannel == null) {
            plugin.getLogger().warning("Appeal kanalı bulunamadı, güncelleme atlanıyor");
            return;
        }

        appealChannel.retrieveMessageById(appeal.getDiscordMessageId()).queue(
                message -> {
                    try {
                        String statusText = approved ? "✅ **ONAYLANDI**" : "❌ **REDDEDİLDİ**";
                        int statusColor = approved ? 0x2ECC71 : 0xE74C3C;

                        EmbedBuilder embed = createDetailedAppealEmbed(appeal, statusText, statusColor,
                                               appeal.getReviewerName(), appeal.getAdminResponse());

                        message.editMessageEmbeds(embed.build()).setComponents().queue(
                            success -> plugin.getLogger().info("Appeal Discord mesajı başarıyla güncellendi: ID " + appeal.getId()),
                            updateError -> plugin.getLogger().warning("Appeal mesajı güncelleme hatası: " + updateError.getMessage())
                        );
                    } catch (Exception e) {
                        plugin.getLogger().severe("Appeal mesajı güncelleme sırasında hata: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                error -> {
                    plugin.getLogger().warning("Discord Appeal mesajı bulunamadı veya erişilemedi (ID: " + appeal.getDiscordMessageId() + "): " + error.getMessage());
                    // Mesaj bulunamadığında veritabanından message ID'yi temizleyebiliriz
                    clearInvalidDiscordMessageId(appeal.getId(), "appeals");
                }
        );
    }

    public void sendReportNotification(Report report) {
        if (guild == null) return;

        // Özel report log kanalını al
        TextChannel reportLogChannel = getReportLogChannel();
        if (reportLogChannel == null) return;

        int priorityColor = switch (report.getPriority()) {
            case LOW -> 0x95A5A6;
            case MEDIUM -> 0xF39C12;
            case HIGH -> 0xE67E22;
            case URGENT -> 0xE74C3C;
            case CRITICAL -> 0x8E44AD;
        };

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🚨 YENİ REPORT GELDİ - İNCELEME GEREKLİ!")
                .setColor(priorityColor)
                .setThumbnail(report.isAnonymous() ? "https://mc-heads.net/avatar/steve" : getPlayerAvatar(report.getReporterName()))
                .setDescription("**Yeni bir report geldi ve admin müdahalesi bekliyor!**");

        // Temel bilgiler
        embed.addField("🆔 Report ID", "`#" + report.getId() + "`", true)
             .addField("🎯 Şikayet Edilen", "**" + report.getReportedPlayerName() + "**", true)
             .addField("👤 Şikayet Eden", "**" + report.getDisplayReporterName() + "**", true);
        
        // Divider
        embed.addField("\u200B", "═══════════════════════════════", false);
        
        // Ana detaylar
        embed.addField("⚠️ Şikayet Sebebi", "```" + report.getReason() + "```", false);

        if (report.getDescription() != null && !report.getDescription().isEmpty()) {
            embed.addField("📝 Detaylı Açıklama", "```" + report.getDescription() + "```", false);
        }

        // Status ve tarih bilgileri
        embed.addField("🏷️ Öncelik", getPriorityDisplayWithIcon(report.getPriority()), true)
             .addField("📊 Durum", "🔴 **İNCELENMEYİ BEKLİYOR**", true)
             .addField("📅 Report Zamanı", "**" + report.getFormattedReportDate() + "**", true);
        
        // Divider
        embed.addField("\u200B", "═══════════════════════════════", false);
        
        // Admin ping
        embed.addField("👥 İşlem Yapacak Adminler", "<@&" + plugin.getConfigManager().getString("discord.report-admin-role-id") + ">", false);
        
        embed.setFooter("DiscordPunishBot Report Sistemi | Report ID: #" + report.getId(), "https://mc-heads.net/avatar/notch")
             .setTimestamp(java.time.Instant.now());

        // Onay/Reddetme/İncelemeye Alma butonları - Güncelleme
        Button approveButton = Button.success("report_approve_" + report.getId(), "✅ Onayla & Manuel Ceza");
        Button rejectButton = Button.danger("report_reject_" + report.getId(), "❌ Reddet");
        Button investigateButton = Button.secondary("report_investigate_" + report.getId(), "🔍 İncelemeye Al");

        reportLogChannel.sendMessageEmbeds(embed.build())
                .setActionRow(approveButton, rejectButton, investigateButton)
                .queue(
                        message -> {
                            String messageId = message.getId();
                            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                                try {
                                    java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
                                    java.sql.PreparedStatement stmt = conn.prepareStatement(
                                        "UPDATE reports SET discord_message_id = ? WHERE id = ?");
                                    stmt.setString(1, messageId);
                                    stmt.setInt(2, report.getId());
                                    stmt.executeUpdate();
                                    conn.commit();
                                } catch (Exception e) {
                                    plugin.getLogger().warning("Report Discord mesaj ID kaydetme hatası: " + e.getMessage());
                                }
                            });
                        },
                        error -> plugin.getLogger().warning("Report bildirimi gönderme hatası: " + error.getMessage())
                );

    }

    private void sendNormalReportNotification(Report report, TextChannel reportChannel) {
        int priorityColor = switch (report.getPriority()) {
            case LOW -> 0x95A5A6;
            case MEDIUM -> 0xF39C12;
            case HIGH -> 0xE67E22;
            case URGENT -> 0xE74C3C;
            case CRITICAL -> 0x8E44AD;
        };

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🚨 " + plugin.getLanguageManager().getMessage("discord.report.new-title"))
                .setColor(priorityColor)
                .setAuthor(report.getDisplayReporterName(), null,
                          report.isAnonymous() ? null : getPlayerAvatar(report.getReporterName()))
                .addField(plugin.getLanguageManager().getMessage("discord.report.field-id"),
                         String.valueOf(report.getId()), true)
                .addField(plugin.getLanguageManager().getMessage("discord.report.field-reported"),
                         report.getReportedPlayerName(), true)
                .addField(plugin.getLanguageManager().getMessage("discord.report.field-reporter"),
                         report.getDisplayReporterName(), true)
                .addField(plugin.getLanguageManager().getMessage("discord.report.field-reason"),
                         report.getReason(), true)
                .addField(plugin.getLanguageManager().getMessage("discord.report.field-priority"),
                         report.getPriorityDisplay(), true)
                .addField(plugin.getLanguageManager().getMessage("discord.report.field-status"),
                         report.getStatusDisplay(), true);

        if (report.getDescription() != null && !report.getDescription().isEmpty()) {
            embed.addField(plugin.getLanguageManager().getMessage("discord.report.field-description"),
                         report.getDescription(), false);
        }

        embed.addField(plugin.getLanguageManager().getMessage("discord.report.field-date"),
                     report.getFormattedReportDate(), true)
             .setFooter(plugin.getLanguageManager().getMessage("discord.report.footer"))
             .setTimestamp(java.time.Instant.now());

        reportChannel.sendMessageEmbeds(embed.build()).queue();
    }

    public void updateReportMessage(Report report) {
        if (guild == null || report.getDiscordMessageId() == null || report.getDiscordMessageId().isEmpty()) {
            plugin.getLogger().warning("Report Discord mesajı güncellenemiyor: Guild null veya mesaj ID boş (Report ID: " + report.getId() + ")");
            return;
        }

        TextChannel reportChannel = getReportLogChannel(); // Log kanalını kullan
        if (reportChannel == null) {
            plugin.getLogger().warning("Report log kanalı bulunamadı, güncelleme atlanıyor");
            return;
        }

        reportChannel.retrieveMessageById(report.getDiscordMessageId()).queue(
                message -> {
                    try {
                        int statusColor = switch (report.getStatus()) {
                            case PENDING -> 0xF39C12;
                            case UNDER_REVIEW -> 0x3498DB;
                            case INVESTIGATING -> 0x8E44AD;
                            case RESOLVED -> 0x2ECC71;
                            case DISMISSED -> 0x95A5A6;
                            case DUPLICATE -> 0x9B59B6;
                        };

                        String statusText = switch (report.getStatus()) {
                            case PENDING -> "🔴 **İNCELENMEYİ BEKLİYOR**";
                            case UNDER_REVIEW -> "🔍 **İNCELENİYOR**";
                            case INVESTIGATING -> "🔍 **ARAŞTIRILIYOR**";
                            case RESOLVED -> "✅ **ÇÖZÜLdÜ**";
                            case DISMISSED -> "❌ **REDDEDİLDİ**";
                            case DUPLICATE -> "⚪ **TEKRAR EDEN**";
                        };

                        EmbedBuilder embed = createDetailedReportEmbed(report, statusText, statusColor,
                                                report.getReviewerName(), report.getAdminResponse());

                        message.editMessageEmbeds(embed.build()).queue(
                            success -> plugin.getLogger().info("Report Discord mesajı başarıyla güncellendi: ID " + report.getId()),
                            updateError -> plugin.getLogger().warning("Report mesajı güncelleme hatası: " + updateError.getMessage())
                        );
                    } catch (Exception e) {
                        plugin.getLogger().severe("Report mesajı güncelleme sırasında hata: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                error -> {
                    plugin.getLogger().warning("Discord Report mesajı bulunamadı veya erişilemedi (ID: " + report.getDiscordMessageId() + "): " + error.getMessage());
                    // Mesaj bulunamadığında veritabanından message ID'yi temizleyebiliriz
                    clearInvalidDiscordMessageId(report.getId(), "reports");
                }
        );
    }

    private TextChannel getAppealChannel() {
        String appealChannelId = plugin.getConfigManager().getString("discord.appeal-channel-id", "");
        
        if (!appealChannelId.isEmpty() && !appealChannelId.equals("APPEAL_CHANNEL_ID_HERE")) {
            return guild.getTextChannelById(appealChannelId);
        }

        return guild.getTextChannels().stream()
                .filter(c -> c.getName().contains("appeal") || c.getName().contains("itiraz"))
                .findFirst()
                .orElse(getLogChannel());
    }

    private TextChannel getReportLogChannel() {
        String reportLogChannelId = plugin.getConfigManager().getString("discord.report-log-channel-id", "");
        
        if (!reportLogChannelId.isEmpty() && !reportLogChannelId.equals("REPORT_LOG_CHANNEL_ID_HERE")) {
            TextChannel channel = guild.getTextChannelById(reportLogChannelId);
            if (channel != null) return channel;
        }

        // Fallback: report-log isimli kanal ara
        return guild.getTextChannels().stream()
                .filter(c -> c.getName().contains("report-log") || c.getName().contains("report_log") ||
                           c.getName().contains("sikayet-log") || c.getName().contains("sikayet_log"))
                .findFirst()
                .orElse(getReportChannel());
    }

    private TextChannel getReportChannel() {
        String reportChannelId = plugin.getConfigManager().getString("discord.report-channel-id", "");
        
        if (!reportChannelId.isEmpty() && !reportChannelId.equals("REPORT_CHANNEL_ID_HERE")) {
            return guild.getTextChannelById(reportChannelId);
        }

        return guild.getTextChannels().stream()
                .filter(c -> c.getName().contains("report") || c.getName().contains("sikayet"))
                .findFirst()
                .orElse(getLogChannel());
    }

    private void handleAppealButtonClick(ButtonInteractionEvent event, String buttonId) {
        // Appeal admin yetkisi kontrolü
        String appealAdminRoleId = plugin.getConfigManager().getString("discord.appeal-admin-role-id", "APPEAL_ADMIN_ROLE_ID");
        if (!appealAdminRoleId.equals("APPEAL_ADMIN_ROLE_ID") &&
            !event.getMember().getRoles().stream().anyMatch(role -> role.getId().equals(appealAdminRoleId))) {
            event.reply("❌ Bu işlem için yetkiniz bulunmuyor!").setEphemeral(true).queue();
            return;
        }

        String[] parts = buttonId.split("_");
        if (parts.length != 3) return;
        
        String action = parts[1]; // approve, reject, investigate
        int appealId = Integer.parseInt(parts[2]);
        
        String adminName = event.getUser().getName();
        String adminId = event.getUser().getId();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Appeal appeal = plugin.getAppealManager().getAppeal(appealId);
            if (appeal == null) {
                event.reply("❌ İtiraz bulunamadı!").setEphemeral(true).queue();
                return;
            }

            switch (action) {
                case "approve":
                    handleAppealApproval(event, appeal, adminName, adminId);
                    break;
                case "reject":
                    handleAppealRejection(event, appeal, adminName, adminId);
                    break;
                case "investigate":
                    handleAppealInvestigation(event, appeal, adminName, adminId);
                    break;
            }
        });
    }
    
    private void handleAppealApproval(ButtonInteractionEvent event, Appeal appeal, String adminName, String adminId) {
        plugin.getAppealManager().approveAppeal(appeal.getId(), adminName, adminId, "İtiraz onaylandı - Ceza kaldırıldı");
        
        updateAppealLogMessage(event, appeal, "✅ ONAYLANDI", 0x2ECC71, adminName, "İtiraz onaylandı ve ceza kaldırıldı.");
        
        event.reply("✅ İtiraz başarıyla onaylandı! Oyuncunun cezası kaldırıldı ve ödül verildi.").setEphemeral(true).queue();
    }
    
    private void handleAppealRejection(ButtonInteractionEvent event, Appeal appeal, String adminName, String adminId) {
        plugin.getAppealManager().rejectAppeal(appeal.getId(), adminName, adminId, "İtiraz reddedildi - Ceza geçerli");
        
        updateAppealLogMessage(event, appeal, "❌ REDDEDİLDİ", 0xE74C3C, adminName, "İtiraz geçersiz bulundu ve reddedildi.");
        
        event.reply("❌ İtiraz başarıyla reddedildi!").setEphemeral(true).queue();
    }
    
    private void handleAppealInvestigation(ButtonInteractionEvent event, Appeal appeal, String adminName, String adminId) {
        // İtiraz incelemeye alma işlemi - butonları koruyalım
        updateAppealInvestigationMessage(event, appeal, adminName, adminId);
        
        event.reply("🔍 İtiraz inceleme altına alındı! Butonlar aktif kaldı, inceleme sonrası Onayla veya Reddet butonlarını kullanabilirsiniz.").setEphemeral(true).queue();
    }
    
    private void updateAppealLogMessage(ButtonInteractionEvent event, Appeal appeal, String status, int color, String adminName, String response) {
        EmbedBuilder embed = createDetailedAppealEmbed(appeal, status, color, adminName, response);

        // Butonları kaldır (sadece Onayla/Reddet'te)
        event.getMessage().editMessageEmbeds(embed.build()).setComponents().queue();
    }
    
    private void updateAppealInvestigationMessage(ButtonInteractionEvent event, Appeal appeal, String adminName, String adminId) {
        EmbedBuilder embed = createDetailedAppealEmbed(appeal, "🔍 İNCELENİYOR", 0x3498DB, adminName, "İtiraz inceleme altına alındı");
        
        // Butonları koru! (Onayla, Reddet, İncelemeye Al butonları kalacak)
        Button approveButton = Button.success("appeal_approve_" + appeal.getId(), "✅ İtirazı Onayla");
        Button rejectButton = Button.danger("appeal_reject_" + appeal.getId(), "❌ İtirazı Reddet");
        Button investigateButton = Button.secondary("appeal_investigate_" + appeal.getId(), "🔍 İncelemeye Al").asDisabled();
        
        event.getMessage().editMessageEmbeds(embed.build())
                .setActionRow(approveButton, rejectButton, investigateButton)
                .queue();
    }
    
    private EmbedBuilder createDetailedAppealEmbed(Appeal appeal, String status, int color, String adminName, String response) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📋 İTİRAZ SİSTEMİ - " + status)
                .setColor(color)
                .setThumbnail(getPlayerAvatar(appeal.getPlayerName()));

        // Header kısmı - temel bilgiler
        embed.addField("🆔 İtiraz ID", "`#" + appeal.getId() + "`", true)
             .addField("⚖️ Ceza ID", "`#" + appeal.getPunishmentId() + "`", true)
             .addField("👤 Oyuncu", "**" + appeal.getPlayerName() + "**", true);
        
        // Divider
        embed.addField("\u200B", "═══════════════════════════════", false);
        
        // Ana detaylar
        embed.addField("📝 İtiraz Gerekçesi", "```" + appeal.getAppealReason() + "```", false);
        
        // Status bilgileri
        embed.addField("📊 İtiraz Durumu", status, true)
             .addField("📅 İtiraz Tarihi", "**" + appeal.getFormattedAppealDate() + "**", true);
        
        // Admin bilgisi (eğer varsa)
        if (adminName != null) {
            embed.addField("👨‍💼 İşlem Yapan Admin", "**" + adminName + "**", true);
        }
        
        if (response != null && !response.isEmpty()) {
            embed.addField("💬 Admin Yanıtı", "```" + response + "```", false);
        }
        
        // Footer
        embed.setFooter("DiscordPunishBot İtiraz Sistemi | Developed by Melut", "https://mc-heads.net/avatar/notch")
             .setTimestamp(java.time.Instant.now());

        return embed;
    }

    private TextChannel getLogChannel() {
        String logChannelId = plugin.getConfigManager().getString("discord.log-channel-id");
        
        if (!logChannelId.isEmpty() && !logChannelId.equals("LOG_CHANNEL_ID_HERE")) {
            TextChannel logChannel = guild.getTextChannelById(logChannelId);
            if (logChannel != null) return logChannel;
        }

        return guild.getTextChannels().stream()
                .filter(c -> c.getName().contains("ceza") || c.getName().contains("punishment") || c.getName().contains("log"))
                .findFirst()
                .orElse(guild.getSystemChannel());
    }
    
    private void clearInvalidDiscordMessageId(int recordId, String tableName) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
                java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE " + tableName + " SET discord_message_id = NULL WHERE id = ?");
                stmt.setInt(1, recordId);
                stmt.executeUpdate();
                conn.commit();
                plugin.getLogger().info("Geçersiz Discord message ID temizlendi: " + tableName + " ID " + recordId);
            } catch (Exception e) {
                plugin.getLogger().warning("Discord message ID temizleme hatası: " + e.getMessage());
            }
        });
    }

    private String getPlayerAvatar(String playerName) {
        return "https://mc-heads.net/avatar/" + playerName;
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
