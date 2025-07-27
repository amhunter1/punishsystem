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
        config.set("discord.roles.mute", "MUTE_ROLE_ID");
        config.set("discord.roles.ban", "BAN_ROLE_ID");
        config.set("discord.roles.other", "OTHER_ROLE_ID");

        config.set("database.type", "sqlite");
        config.set("database.host", "localhost");
        config.set("database.port", 3306);
        config.set("database.database", "punishments");
        config.set("database.username", "root");
        config.set("database.password", "password");

        config.set("punishments.mute.agir-kufur.command", "essentials:mute %player% 7h %reason%");
        config.set("punishments.mute.agir-kufur.display", "Ağır Küfür");
        config.set("punishments.mute.spam.command", "essentials:mute %player% 3h %reason%");
        config.set("punishments.mute.spam.display", "Spam");
        config.set("punishments.mute.reklam.command", "essentials:mute %player% 12h %reason%");
        config.set("punishments.mute.reklam.display", "Reklam");
        config.set("punishments.mute.kufur.command", "essentials:mute %player% 2h %reason%");
        config.set("punishments.mute.kufur.display", "Küfür");

        config.set("punishments.ban.hile.command", "essentials:ban %player% %reason%");
        config.set("punishments.ban.hile.display", "Hile/Cheat");
        config.set("punishments.ban.griefing.command", "essentials:tempban %player% 7d %reason%");
        config.set("punishments.ban.griefing.display", "Griefing");
        config.set("punishments.ban.troll.command", "essentials:tempban %player% 3d %reason%");
        config.set("punishments.ban.troll.display", "Troll");
        config.set("punishments.ban.duping.command", "essentials:ban %player% %reason%");
        config.set("punishments.ban.duping.display", "Duping");

        config.set("punishments.other.warn.command", "essentials:warn %player% %reason%");
        config.set("punishments.other.warn.display", "Uyarı");

        config.set("messages.no-permission", "&cBu komutu kullanma yetkiniz yok!");
        config.set("messages.player-not-found", "&cOyuncu bulunamadı!");
        config.set("messages.punishment-executed", "&aCeza başarıyla uygulandı!");
        config.set("messages.no-punishments", "&eOyuncunun hiç cezası bulunmuyor.");
        config.set("messages.punishment-history", "&6=== %player% Ceza Geçmişi ===");
        config.set("messages.punishment-entry", "&7%date% &8- &c%type% &8- &f%reason% &8- &e%admin%");
        config.set("messages.config-reloaded", "&aKonfigürasyon başarıyla yeniden yüklendi!");

        config.set("discord.embeds.punishment.title", "🔨 Yeni Ceza");
        config.set("discord.embeds.punishment.color", 15158332);
        config.set("discord.embeds.punishment.footer", "Sunucu Ceza Sistemi");

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Config dosyası kaydedilemedi: " + e.getMessage());
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
        return Arrays.asList("agir-kufur", "spam", "reklam", "kufur");
    }

    public List<String> getBanReasons() {
        return Arrays.asList("hile", "griefing", "troll", "duping");
    }

    public List<String> getOtherReasons() {
        return Arrays.asList("warn");
    }

    public String getPunishmentCommand(String type, String reason) {
        return getString("punishments." + type + "." + reason + ".command");
    }

    public String getPunishmentDisplay(String type, String reason) {
        return getString("punishments." + type + "." + reason + ".display");
    }
}