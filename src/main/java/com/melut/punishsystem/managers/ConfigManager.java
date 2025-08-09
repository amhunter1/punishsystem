package com.melut.punishsystem.managers;

import com.melut.punishsystem.DiscordPunishBot;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {

    private final DiscordPunishBot plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(DiscordPunishBot plugin) {
        this.plugin = plugin;
        createConfig();
    }

    private void createConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
            setupDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void setupDefaultConfig() {
        config = plugin.getConfig();

        config.set("discord.token", "YOUR_BOT_TOKEN_HERE");
        config.set("discord.guild-id", "YOUR_GUILD_ID_HERE");
        config.set("discord.log-channel-id", "LOG_CHANNEL_ID_HERE");
        config.set("discord.roles.mute", "MUTE_ROLE_ID");
        config.set("discord.roles.ban", "BAN_ROLE_ID");
        config.set("discord.roles.kick", "KICK_ROLE_ID");
        config.set("discord.roles.tempban", "TEMPBAN_ROLE_ID");
        config.set("discord.roles.jail", "JAIL_ROLE_ID");
        config.set("discord.roles.warn", "WARN_ROLE_ID");
        config.set("discord.roles.other", "OTHER_ROLE_ID");

        config.set("database.type", "sqlite");
        config.set("database.host", "localhost");
        config.set("database.port", 3306);
        config.set("database.database", "punishments");
        config.set("database.username", "root");
        config.set("database.password", "password");

        // Mute punishments
        config.set("punishments.mute.agir-kufur.command", "essentials:mute %player% 7h %reason%");
        config.set("punishments.mute.agir-kufur.display", "Ağır Küfür");
        config.set("punishments.mute.agir-kufur.duration", "7 saat");
        config.set("punishments.mute.spam.command", "essentials:mute %player% 3h %reason%");
        config.set("punishments.mute.spam.display", "Spam");
        config.set("punishments.mute.spam.duration", "3 saat");
        config.set("punishments.mute.reklam.command", "essentials:mute %player% 12h %reason%");
        config.set("punishments.mute.reklam.display", "Reklam");
        config.set("punishments.mute.reklam.duration", "12 saat");
        config.set("punishments.mute.kufur.command", "essentials:mute %player% 2h %reason%");
        config.set("punishments.mute.kufur.display", "Küfür");
        config.set("punishments.mute.kufur.duration", "2 saat");
        config.set("punishments.mute.hakaret.command", "essentials:mute %player% 5h %reason%");
        config.set("punishments.mute.hakaret.display", "Hakaret");
        config.set("punishments.mute.hakaret.duration", "5 saat");
        config.set("punishments.mute.flood.command", "essentials:mute %player% 1h %reason%");
        config.set("punishments.mute.flood.display", "Flood");
        config.set("punishments.mute.flood.duration", "1 saat");

        // Ban punishments
        config.set("punishments.ban.hile.command", "essentials:ban %player% %reason%");
        config.set("punishments.ban.hile.display", "Hile/Cheat");
        config.set("punishments.ban.hile.duration", "Kalıcı");
        config.set("punishments.ban.griefing.command", "essentials:ban %player% %reason%");
        config.set("punishments.ban.griefing.display", "Griefing");
        config.set("punishments.ban.griefing.duration", "Kalıcı");
        config.set("punishments.ban.duping.command", "essentials:ban %player% %reason%");
        config.set("punishments.ban.duping.display", "Duping");
        config.set("punishments.ban.duping.duration", "Kalıcı");
        config.set("punishments.ban.exploit.command", "essentials:ban %player% %reason%");
        config.set("punishments.ban.exploit.display", "Exploit Kullanımı");
        config.set("punishments.ban.exploit.duration", "Kalıcı");
        config.set("punishments.ban.ddos.command", "essentials:ban %player% %reason%");
        config.set("punishments.ban.ddos.display", "DDoS Teşebbüsü");
        config.set("punishments.ban.ddos.duration", "Kalıcı");

        // Kick punishments
        config.set("punishments.kick.uyari-kick.command", "essentials:kick %player% %reason%");
        config.set("punishments.kick.uyari-kick.display", "Uyarı Kick");
        config.set("punishments.kick.uyari-kick.duration", "Anlık");
        config.set("punishments.kick.spam-kick.command", "essentials:kick %player% %reason%");
        config.set("punishments.kick.spam-kick.display", "Spam Kick");
        config.set("punishments.kick.spam-kick.duration", "Anlık");
        config.set("punishments.kick.flood-kick.command", "essentials:kick %player% %reason%");
        config.set("punishments.kick.flood-kick.display", "Flood Kick");
        config.set("punishments.kick.flood-kick.duration", "Anlık");

        // TempBan punishments
        config.set("punishments.tempban.troll.command", "essentials:tempban %player% 3d %reason%");
        config.set("punishments.tempban.troll.display", "Troll");
        config.set("punishments.tempban.troll.duration", "3 gün");
        config.set("punishments.tempban.spam-tempban.command", "essentials:tempban %player% 1d %reason%");
        config.set("punishments.tempban.spam-tempban.display", "Spam TempBan");
        config.set("punishments.tempban.spam-tempban.duration", "1 gün");
        config.set("punishments.tempban.kufur-tempban.command", "essentials:tempban %player% 7d %reason%");
        config.set("punishments.tempban.kufur-tempban.display", "Küfür TempBan");
        config.set("punishments.tempban.kufur-tempban.duration", "7 gün");
        config.set("punishments.tempban.griefing-tempban.command", "essentials:tempban %player% 14d %reason%");
        config.set("punishments.tempban.griefing-tempban.display", "Griefing TempBan");
        config.set("punishments.tempban.griefing-tempban.duration", "14 gün");
        config.set("punishments.tempban.hile-tempban.command", "essentials:tempban %player% 30d %reason%");
        config.set("punishments.tempban.hile-tempban.display", "Hile TempBan");
        config.set("punishments.tempban.hile-tempban.duration", "30 gün");

        // Jail punishments
        config.set("punishments.jail.kufur-jail.command", "essentials:jail %player% 2h %reason%");
        config.set("punishments.jail.kufur-jail.display", "Küfür Hapis");
        config.set("punishments.jail.kufur-jail.duration", "2 saat");
        config.set("punishments.jail.spam-jail.command", "essentials:jail %player% 1h %reason%");
        config.set("punishments.jail.spam-jail.display", "Spam Hapis");
        config.set("punishments.jail.spam-jail.duration", "1 saat");
        config.set("punishments.jail.griefing-jail.command", "essentials:jail %player% 6h %reason%");
        config.set("punishments.jail.griefing-jail.display", "Griefing Hapis");
        config.set("punishments.jail.griefing-jail.duration", "6 saat");
        config.set("punishments.jail.troll-jail.command", "essentials:jail %player% 4h %reason%");
        config.set("punishments.jail.troll-jail.display", "Troll Hapis");
        config.set("punishments.jail.troll-jail.duration", "4 saat");

        // Warn punishments
        config.set("punishments.warn.uyari.command", "essentials:warn %player% %reason%");
        config.set("punishments.warn.uyari.display", "Uyarı");
        config.set("punishments.warn.uyari.duration", "Kalıcı");
        config.set("punishments.warn.ilk-uyari.command", "essentials:warn %player% %reason%");
        config.set("punishments.warn.ilk-uyari.display", "İlk Uyarı");
        config.set("punishments.warn.ilk-uyari.duration", "Kalıcı");
        config.set("punishments.warn.son-uyari.command", "essentials:warn %player% %reason%");
        config.set("punishments.warn.son-uyari.display", "Son Uyarı");
        config.set("punishments.warn.son-uyari.duration", "Kalıcı");

        // Other punishments
        config.set("punishments.other.warn.command", "essentials:warn %player% %reason%");
        config.set("punishments.other.warn.display", "Uyarı");
        config.set("punishments.other.warn.duration", "Kalıcı");
        config.set("punishments.other.note.command", "essentials:note %player% %reason%");
        config.set("punishments.other.note.display", "Not");
        config.set("punishments.other.note.duration", "Kalıcı");

        // Settings
        config.set("settings.debug", false);
        config.set("settings.max-punishment-display", 10);
        config.set("settings.auto-save-interval", 30);
        config.set("settings.date-format", "dd.MM.yyyy HH:mm");
        
        // Cooldowns
        config.set("settings.cooldowns.enabled", true);
        config.set("settings.cooldowns.mute", 30);
        config.set("settings.cooldowns.ban", 60);
        config.set("settings.cooldowns.kick", 15);
        config.set("settings.cooldowns.tempban", 45);
        config.set("settings.cooldowns.jail", 30);
        config.set("settings.cooldowns.warn", 10);

        // Daily limits
        config.set("settings.daily-limits.enabled", true);
        config.set("settings.daily-limits.mute", 50);
        config.set("settings.daily-limits.ban", 20);
        config.set("settings.daily-limits.kick", 100);
        config.set("settings.daily-limits.tempban", 30);
        config.set("settings.daily-limits.jail", 40);
        config.set("settings.daily-limits.warn", 200);

        // Messages
        config.set("messages.no-permission", "&cBu komutu kullanma yetkiniz yok!");
        config.set("messages.player-not-found", "&cOyuncu bulunamadı!");
        config.set("messages.punishment-executed", "&aCeza başarıyla uygulandı!");
        config.set("messages.no-punishments", "&eOyuncunun hiç cezası bulunmuyor.");
        config.set("messages.punishment-history", "&6=== %player% Ceza Geçmişi ===");
        config.set("messages.punishment-entry", "&7%date% &8- &c%type% &8- &f%reason% &8- &e%admin%");
        config.set("messages.config-reloaded", "&aKonfigürasyon başarıyla yeniden yüklendi!");
        config.set("messages.usage", "&cKullanım: /ceza <oyuncu>");

        // Cooldown messages
        config.set("messages.cooldown.mute", "&cSusturma komutu için %time% saniye beklemelisiniz!");
        config.set("messages.cooldown.ban", "&cYasaklama komutu için %time% saniye beklemelisiniz!");
        config.set("messages.cooldown.kick", "&cAtma komutu için %time% saniye beklemelisiniz!");
        config.set("messages.cooldown.tempban", "&cGeçici yasaklama komutu için %time% saniye beklemelisiniz!");
        config.set("messages.cooldown.jail", "&cHapis komutu için %time% saniye beklemelisiniz!");
        config.set("messages.cooldown.warn", "&cUyarı komutu için %time% saniye beklemelisiniz!");

        // Daily limit messages
        config.set("messages.daily-limit.mute", "&cGünlük susturma limitine ulaştınız!");
        config.set("messages.daily-limit.ban", "&cGünlük yasaklama limitine ulaştınız!");
        config.set("messages.daily-limit.kick", "&cGünlük atma limitine ulaştınız!");
        config.set("messages.daily-limit.tempban", "&cGünlük geçici yasaklama limitine ulaştınız!");
        config.set("messages.daily-limit.jail", "&cGünlük hapis limitine ulaştınız!");
        config.set("messages.daily-limit.warn", "&cGünlük uyarı limitine ulaştınız!");

        config.set("discord.embeds.punishment.title", "🔨 Yeni Ceza");
        config.set("discord.embeds.punishment.color", 15158332);
        config.set("discord.embeds.punishment.footer", "Sunucu Ceza Sistemi");

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe(getString("messages.log.config-save-error").replace("%error%", e.getMessage()));
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public String getString(String path) {
        return config.getString(path, "");
    }

    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    public int getInt(String path) {
        return config.getInt(path, 0);
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path, false);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public String getColoredString(String path) {
        return getString(path).replace("&", "§");
    }

    public String getColoredString(String path, String defaultValue) {
        return getString(path, defaultValue).replace("&", "§");
    }

    public List<String> getMuteReasons() {
        return Arrays.asList("agir-kufur", "spam", "reklam", "kufur", "hakaret", "flood");
    }

    public List<String> getBanReasons() {
        return Arrays.asList("hile", "griefing", "duping", "exploit", "ddos");
    }

    public List<String> getKickReasons() {
        return Arrays.asList("uyari-kick", "spam-kick", "flood-kick");
    }

    public List<String> getTempBanReasons() {
        return Arrays.asList("troll", "spam-tempban", "kufur-tempban", "griefing-tempban", "hile-tempban");
    }

    public List<String> getJailReasons() {
        return Arrays.asList("kufur-jail", "spam-jail", "griefing-jail", "troll-jail");
    }

    public List<String> getWarnReasons() {
        return Arrays.asList("uyari", "ilk-uyari", "son-uyari");
    }

    public List<String> getOtherReasons() {
        return Arrays.asList("warn", "note");
    }

    public String getPunishmentCommand(String type, String reason) {
        return getString("punishments." + type + "." + reason + ".command");
    }

    public String getPunishmentDisplay(String type, String reason) {
        return getString("punishments." + type + "." + reason + ".display");
    }

    public String getPunishmentDuration(String type, String reason) {
        return getString("punishments." + type + "." + reason + ".duration", "Kalıcı");
    }
}