package com.melut.punishsystem;

import org.bukkit.plugin.java.JavaPlugin;
import com.melut.punishsystem.commands.AppealCommand;
import com.melut.punishsystem.commands.CezaCommand;
import com.melut.punishsystem.commands.DPunishCommand;
import com.melut.punishsystem.commands.ReportCommand;
import com.melut.punishsystem.database.DatabaseManager;
import com.melut.punishsystem.discord.DiscordBot;
import com.melut.punishsystem.managers.AppealManager;
import com.melut.punishsystem.managers.ConfigManager;
import com.melut.punishsystem.managers.LanguageManager;
import com.melut.punishsystem.managers.MetricsManager;
import com.melut.punishsystem.managers.PunishmentManager;
import com.melut.punishsystem.managers.ReportManager;
import java.io.File;

public class DiscordPunishBot extends JavaPlugin {

    private static DiscordPunishBot instance;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private MetricsManager metricsManager;
    private DatabaseManager databaseManager;
    private PunishmentManager punishmentManager;
    private AppealManager appealManager;
    private ReportManager reportManager;
    private DiscordBot discordBot;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.punishmentManager = new PunishmentManager(this);

        // Initialize database first
        databaseManager.initializeDatabase();
        
        // Then create appeal and report managers (after database is ready)
        this.appealManager = new AppealManager(this);
        this.reportManager = new ReportManager(this);
        
        // Initialize appeal and report tables
        this.appealManager.initialize();
        this.reportManager.initialize();

        this.discordBot = new DiscordBot(this);

        this.metricsManager = new MetricsManager(this);

        getCommand("ceza").setExecutor(new CezaCommand(this));
        getCommand("dpunish").setExecutor(new DPunishCommand(this));
        getCommand("appeal").setExecutor(new AppealCommand(this));
        getCommand("report").setExecutor(new ReportCommand(this));

        discordBot.start();

        getLogger().info(languageManager.getMessage("log.plugin-enabled"));
        getLogger().info(languageManager.getMessage("log.bot-connecting"));
    }

    @Override
    public void onDisable() {
        if (discordBot != null) {
            discordBot.shutdown();
        }

        if (metricsManager != null) {
            metricsManager.shutdown();
        }

        if (databaseManager != null) {
            databaseManager.closeConnection();
        }

        getLogger().info(languageManager != null ?
            languageManager.getMessage("log.plugin-disabled") :
            "DiscordPunishBot has been disabled!");
    }

    public static DiscordPunishBot getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public MetricsManager getMetricsManager() {
        return metricsManager;
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

    public AppealManager getAppealManager() {
        return appealManager;
    }

    public ReportManager getReportManager() {
        return reportManager;
    }
}