package com.melut.punishsystem.managers;

import com.melut.punishsystem.DiscordPunishBot;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class LanguageManager {

    private final DiscordPunishBot plugin;
    private final Map<String, FileConfiguration> languageConfigs;
    private String defaultLanguage;
    private FileConfiguration currentLanguageConfig;

    public LanguageManager(DiscordPunishBot plugin) {
        this.plugin = plugin;
        this.languageConfigs = new HashMap<>();
        this.defaultLanguage = "tr"; // Default to Turkish
        
        loadLanguages();
    }

    private void loadLanguages() {
        // Create lang directory if it doesn't exist
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        // Load available language files
        loadLanguageFile("turkish");
        loadLanguageFile("english");

        // Get default language from config
        this.defaultLanguage = plugin.getConfigManager().getString("settings.default-language", "tr");
        
        // Set current language config
        setLanguage(defaultLanguage);
        
        plugin.getLogger().info("Language system loaded. Current language: " + defaultLanguage);
    }

    private void loadLanguageFile(String languageName) {
        String fileName = languageName.toLowerCase() + ".yml";
        File langFile = new File(plugin.getDataFolder(), "lang" + File.separator + fileName);

        // Save default language file from resources if it doesn't exist
        if (!langFile.exists()) {
            try (InputStream inputStream = plugin.getResource("lang/" + fileName)) {
                if (inputStream != null) {
                    plugin.saveResource("lang/" + fileName, false);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Could not save default language file: " + fileName);
            }
        }

        // Load the language file
        if (langFile.exists()) {
            try {
                FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
                
                // Also load from JAR to ensure we have all keys
                try (InputStream inputStream = plugin.getResource("lang/" + fileName)) {
                    if (inputStream != null) {
                        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(inputStream, StandardCharsets.UTF_8)
                        );
                        langConfig.setDefaults(defaultConfig);
                    }
                }
                
                String langCode = langConfig.getString("language", languageName.substring(0, 2));
                languageConfigs.put(langCode, langConfig);
                
                plugin.getLogger().info("Loaded language file: " + fileName + " (" + langCode + ")");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load language file: " + fileName, e);
            }
        }
    }

    public void setLanguage(String languageCode) {
        if (languageConfigs.containsKey(languageCode)) {
            this.currentLanguageConfig = languageConfigs.get(languageCode);
            this.defaultLanguage = languageCode;
            plugin.getLogger().info("Language changed to: " + languageCode);
        } else {
            plugin.getLogger().warning("Language not found: " + languageCode + ". Using default.");
            this.currentLanguageConfig = languageConfigs.get("tr");
            if (this.currentLanguageConfig == null && !languageConfigs.isEmpty()) {
                this.currentLanguageConfig = languageConfigs.values().iterator().next();
            }
        }
    }

    public String getMessage(String path) {
        return getMessage(path, "");
    }

    public String getMessage(String path, String defaultValue) {
        if (currentLanguageConfig == null) {
            return defaultValue;
        }
        
        String message = currentLanguageConfig.getString("messages." + path, defaultValue);
        if (message.isEmpty() && !defaultValue.isEmpty()) {
            return defaultValue;
        }
        
        return message;
    }

    public String getColoredMessage(String path) {
        return getColoredMessage(path, "");
    }

    public String getColoredMessage(String path, String defaultValue) {
        String message = getMessage(path, defaultValue);
        return message.replace("&", "§");
    }

    public List<String> getMessageList(String path) {
        if (currentLanguageConfig == null) {
            return List.of();
        }
        
        return currentLanguageConfig.getStringList("messages." + path);
    }

    public List<String> getColoredMessageList(String path) {
        List<String> messages = getMessageList(path);
        return messages.stream()
                .map(msg -> msg.replace("&", "§"))
                .toList();
    }

    public String getCurrentLanguage() {
        return defaultLanguage;
    }

    public boolean isLanguageAvailable(String languageCode) {
        return languageConfigs.containsKey(languageCode);
    }

    public Map<String, String> getAvailableLanguages() {
        Map<String, String> languages = new HashMap<>();
        for (Map.Entry<String, FileConfiguration> entry : languageConfigs.entrySet()) {
            String code = entry.getKey();
            String name = entry.getValue().getString("name", code);
            languages.put(code, name);
        }
        return languages;
    }

    public void reloadLanguages() {
        languageConfigs.clear();
        loadLanguages();
    }

    // Utility method for quick message formatting with placeholders
    public String getFormattedMessage(String path, Object... placeholders) {
        String message = getColoredMessage(path);
        
        // Replace placeholders in the format %key%
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                String placeholder = "%" + placeholders[i] + "%";
                String replacement = String.valueOf(placeholders[i + 1]);
                message = message.replace(placeholder, replacement);
            }
        }
        
        return message;
    }

    // Method to get punishment type display name
    public String getPunishmentTypeDisplay(String type) {
        String typeDisplay = getMessage("punishment-types." + type.toLowerCase());
        if (typeDisplay.isEmpty()) {
            return getMessage("punishment-types.default", type).replace("%type%", type);
        }
        return typeDisplay;
    }

    // Method to get Discord command descriptions and options
    public String getDiscordCommandDescription(String command) {
        return getMessage("discord.commands." + command + ".description", "");
    }

    public String getDiscordCommandOption(String command, String option) {
        return getMessage("discord.commands." + command + "." + option, "");
    }
}