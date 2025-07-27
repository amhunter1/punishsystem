package com.melut.punishsystem;

import org.bukkit.plugin.java.JavaPlugin;
import com.melut.punishsystem.commands.CezaCommand;
import com.melut.punishsystem.commands.DPunishCommand;
import com.melut.punishsystem.database.DatabaseManager;
import com.melut.punishsystem.discord.DiscordBot;
import com.melut.punishsystem.managers.ConfigManager;
import com.melut.punishsystem.managers.PunishmentManager;
import java.io.File;

public class DiscordPunishBot extends JavaPlugin {

    private static DiscordPunishBot instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PunishmentManager punishmentManager;
    private DiscordBot discordBot;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        this.configManager = new ConfigManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.punishmentManager = new PunishmentManager(this);

        this.discordBot = new DiscordBot(this);

        getCommand("ceza").setExecutor(new CezaCommand(this));
        getCommand("dpunish").setExecutor(new DPunishCommand(this));

        databaseManager.initializeDatabase();

        discordBot.start();

        getLogger().info("DiscordPunishBot has been enabled!");
        getLogger().info("Discord bot connecting...");
    }

    @Override
    public void onDisable() {
        if (discordBot != null) {
            discordBot.shutdown();
        }

        if (databaseManager != null) {
            databaseManager.closeConnection();
        }

        getLogger().info("DiscordPunishBot has been disabled!");
    }

    public static DiscordPunishBot getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public DiscordBot getDiscordBot() {
        return discordBot;
    }
}