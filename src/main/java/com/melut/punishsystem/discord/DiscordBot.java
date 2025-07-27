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
    private Role otherRole;

    public DiscordBot(DiscordPunishBot plugin) {
        this.plugin = plugin;
    }

    public void start() {
        String token = plugin.getConfigManager().getString("discord.token");

        if (token == null || token.equals("YOUR_BOT_TOKEN_HERE")) {
            plugin.getLogger().severe("Discord bot token not configured! Please set it in config.yml");
            return;
        }

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(this)
                    .build();

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to start Discord bot: " + e.getMessage());
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        plugin.getLogger().info("Discord bot is ready! Logged in as: " + event.getJDA().getSelfUser().getAsTag());

        String guildId = plugin.getConfigManager().getString("discord.guild-id");
        if (guildId != null && !guildId.equals("YOUR_GUILD_ID_HERE")) {
            guild = jda.getGuildById(guildId);

            if (guild != null) {
                setupRoles();
                registerCommands();
            } else {
                plugin.getLogger().warning("Guild not found with ID: " + guildId);
            }
        }
    }

    private void setupRoles() {
        String muteRoleId = plugin.getConfigManager().getString("discord.roles.mute");
        String banRoleId = plugin.getConfigManager().getString("discord.roles.ban");
        String otherRoleId = plugin.getConfigManager().getString("discord.roles.other");

        if (!muteRoleId.equals("MUTE_ROLE_ID")) {
            muteRole = guild.getRoleById(muteRoleId);
        }
        if (!banRoleId.equals("BAN_ROLE_ID")) {
            banRole = guild.getRoleById(banRoleId);
        }
        if (!otherRoleId.equals("OTHER_ROLE_ID")) {
            otherRole = guild.getRoleById(otherRoleId);
        }
    }

    private void registerCommands() {
        List<CommandData> commands = List.of(
                Commands.slash("mute", "Oyuncuyu sustur")
                        .addOption(OptionType.STRING, "oyuncu", "Susturulacak oyuncu", true)
                        .addOption(OptionType.STRING, "sebep", "Susturma sebebi", false),

                Commands.slash("ban", "Oyuncuyu yasakla")
                        .addOption(OptionType.STRING, "oyuncu", "Yasaklanacak oyuncu", true)
                        .addOption(OptionType.STRING, "sebep", "Yasaklama sebebi", false),

                Commands.slash("diger", "Diğer ceza türleri")
                        .addOption(OptionType.STRING, "oyuncu", "Cezalandırılacak oyuncu", true)
                        .addOption(OptionType.STRING, "sebep", "Ceza sebebi", false),

                Commands.slash("ceza", "Oyuncunun ceza geçmişini görüntüle")
                        .addOption(OptionType.STRING, "oyuncu", "Sorgulanacak oyuncu", true)
        );

        guild.updateCommands().addCommands(commands).queue(
                success -> plugin.getLogger().info("Discord commands registered successfully!"),
                error -> plugin.getLogger().severe("Failed to register Discord commands: " + error.getMessage())
        );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!hasPermission(event, event.getName())) {
            event.reply("❌ Bu komutu kullanma yetkiniz yok!").setEphemeral(true).queue();
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
                event.reply("❌ Geçersiz sebep! Lütfen aşağıdaki menüden seçim yapın:")
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
                .setPlaceholder("Ceza sebebini seçin...");

        for (String reason : reasons) {
            String display = plugin.getConfigManager().getPunishmentDisplay(type, reason);
            menuBuilder.addOption(display, reason, "Sebep: " + display);
        }

        event.reply("**" + playerName + "** için " + getTypeDisplayName(type) + " sebebini seçin:")
                .addActionRow(menuBuilder.build())
                .setEphemeral(true)
                .queue();
    }

    private void showReasonMenuAsFollowup(SlashCommandInteractionEvent event, String playerName, String type) {
        List<String> reasons = plugin.getPunishmentManager().getAvailableReasons(type);

        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("punishment:" + type + ":" + playerName)
                .setPlaceholder("Ceza sebebini seçin...");

        for (String reason : reasons) {
            String display = plugin.getConfigManager().getPunishmentDisplay(type, reason);
            menuBuilder.addOption(display, reason, "Sebep: " + display);
        }

        event.getHook().editOriginal("**" + playerName + "** için " + getTypeDisplayName(type) + " sebebini seçin:")
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
            ((SlashCommandInteractionEvent) event).reply("⏳ Ceza uygulanıyor...").setEphemeral(true).queue();
        } else if (event instanceof StringSelectInteractionEvent) {
            adminName = ((StringSelectInteractionEvent) event).getUser().getName();
            ((StringSelectInteractionEvent) event).reply("⏳ Ceza uygulanıyor...").setEphemeral(true).queue();
        }

        final String finalAdminName = adminName;

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getPunishmentManager().executePunishment(playerName, finalAdminName, type, reason);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (event instanceof SlashCommandInteractionEvent) {
                    ((SlashCommandInteractionEvent) event).getHook()
                            .editOriginal("✅ **" + playerName + "** oyuncusuna **" +
                                    plugin.getConfigManager().getPunishmentDisplay(type, reason) +
                                    "** cezası başarıyla uygulandı!")
                            .queue();
                } else if (event instanceof StringSelectInteractionEvent) {
                    ((StringSelectInteractionEvent) event).getHook()
                            .editOriginal("✅ **" + playerName + "** oyuncusuna **" +
                                    plugin.getConfigManager().getPunishmentDisplay(type, reason) +
                                    "** cezası başarıyla uygulandı!")
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
                    .setTitle("📋 " + playerName + " - Ceza Geçmişi")
                    .setColor(Color.ORANGE)
                    .setFooter("Toplam " + total + " ceza", null)
                    .setTimestamp(java.time.Instant.now());

            if (punishments.isEmpty()) {
                embed.setDescription("Bu oyuncunun hiç cezası bulunmuyor.");
            } else {
                StringBuilder description = new StringBuilder();
                for (int i = 0; i < Math.min(10, punishments.size()); i++) {
                    Punishment p = punishments.get(i);
                    description.append(String.format("**%d.** %s - **%s** - %s\n*%s tarafından - %s*\n\n",
                            i + 1,
                            p.getFormattedDate(),
                            p.getTypeDisplay(),
                            p.getReason(),
                            p.getAdminName(),
                            p.isActive() ? "Aktif" : "Pasif"
                    ));
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
            plugin.getLogger().warning("No suitable text channel found for punishment notifications!");
            return;
        }

        String command = plugin.getConfigManager().getPunishmentCommand(punishment.getType(),
                getPunishmentReasonKey(punishment.getType(), punishment.getReason()));
        String duration = extractDurationFromCommand(command);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(plugin.getConfigManager().getString("discord.embeds.punishment.title"))
                .setColor(getPunishmentColor(punishment.getType()))
                .addField("👤 Oyuncu", punishment.getPlayerName(), true)
                .addField("⚖️ Ceza Türü", punishment.getTypeDisplay(), true)
                .addField("📝 Sebep", punishment.getReason(), true)
                .addField("👮 Yetkili", punishment.getAdminName(), true)
                .addField("🕐 Tarih", punishment.getFormattedDate(), true)
                .addField("⏰ Süre", duration.isEmpty() ? "Kalıcı" : duration, true)
                .addField("📊 ID", "#" + punishment.getId(), true)
                .setFooter(plugin.getConfigManager().getString("discord.embeds.punishment.footer"))
                .setTimestamp(java.time.Instant.now());

        switch (punishment.getType().toLowerCase()) {
            case "mute":
                embed.setThumbnail("https://cdn.discordapp.com/emojis/🔇.png");
                break;
            case "ban":
                embed.setThumbnail("https://cdn.discordapp.com/emojis/🔨.png");
                break;
            default:
                embed.setThumbnail("https://cdn.discordapp.com/emojis/⚠️.png");
                break;
        }

        logChannel.sendMessageEmbeds(embed.build()).queue(
                success -> plugin.getLogger().info("Punishment notification sent to Discord log channel"),
                error -> plugin.getLogger().warning("Failed to send punishment notification: " + error.getMessage())
        );
    }

    private int getPunishmentColor(String type) {
        switch (type.toLowerCase()) {
            case "mute":
                return 0xF39C12; // Orange
            case "ban":
                return 0xE74C3C; // Red
            default:
                return 0x3498DB; // Blue
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
            case 's': return number + " saniye";
            case 'm': return number + " dakika";
            case 'h': return number + " saat";
            case 'd': return number + " gün";
            case 'w': return number + " hafta";
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
            case "mute": return "susturma";
            case "ban": return "yasaklama";
            case "other": return "diğer ceza";
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