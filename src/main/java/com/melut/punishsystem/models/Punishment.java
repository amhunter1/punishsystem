package com.melut.punishsystem.models;

import com.melut.punishsystem.DiscordPunishBot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Punishment {

    private int id;
    private String playerName;
    private String playerUuid;
    private String type;
    private String reason;
    private String adminName;
    private String adminUuid;
    private LocalDateTime dateIssued;
    private boolean active;
    private final DiscordPunishBot plugin;

    public Punishment(String playerName, String playerUuid, String type, String reason,
                      String adminName, String adminUuid, LocalDateTime dateIssued) {
        this.plugin = DiscordPunishBot.getInstance();
        this.playerName = playerName;
        this.playerUuid = playerUuid;
        this.type = type;
        this.reason = reason;
        this.adminName = adminName;
        this.adminUuid = adminUuid;
        this.dateIssued = dateIssued;
        this.active = true;
    }

    public int getId() { return id; }
    public String getPlayerName() { return playerName; }
    public String getPlayerUuid() { return playerUuid; }
    public String getType() { return type; }
    public String getReason() { return reason; }
    public String getAdminName() { return adminName; }
    public String getAdminUuid() { return adminUuid; }
    public LocalDateTime getDateIssued() { return dateIssued; }
    public boolean isActive() { return active; }

    public void setId(int id) { this.id = id; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public void setPlayerUuid(String playerUuid) { this.playerUuid = playerUuid; }
    public void setType(String type) { this.type = type; }
    public void setReason(String reason) { this.reason = reason; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public void setAdminUuid(String adminUuid) { this.adminUuid = adminUuid; }
    public void setDateIssued(LocalDateTime dateIssued) { this.dateIssued = dateIssued; }
    public void setActive(boolean active) { this.active = active; }

    public String getFormattedDate() {
        String dateFormat = plugin.getConfigManager().getString("settings.date-format");
        return dateIssued.format(DateTimeFormatter.ofPattern(dateFormat));
    }

    public String getTypeDisplay() {
        return plugin.getLanguageManager().getPunishmentTypeDisplay(type);
    }

    @Override
    public String toString() {
        return plugin.getLanguageManager().getFormattedMessage("punishment-to-string",
                "id", String.valueOf(id),
                "player", playerName,
                "type", type,
                "reason", reason,
                "admin", adminName,
                "date", getFormattedDate());
    }
}
