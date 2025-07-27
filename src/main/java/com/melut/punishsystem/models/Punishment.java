package com.melut.punishsystem.models;

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

    public Punishment(String playerName, String playerUuid, String type, String reason,
                      String adminName, String adminUuid, LocalDateTime dateIssued) {
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
        return dateIssued.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public String getTypeDisplay() {
        switch (type.toLowerCase()) {
            case "mute":
                return "Susturma";
            case "ban":
                return "Yasaklama";
            case "warn":
                return "Uyarı";
            default:
                return type;
        }
    }

    @Override
    public String toString() {
        return String.format("Punishment{id=%d, player='%s', type='%s', reason='%s', admin='%s', date='%s'}",
                id, playerName, type, reason, adminName, getFormattedDate());
    }
}