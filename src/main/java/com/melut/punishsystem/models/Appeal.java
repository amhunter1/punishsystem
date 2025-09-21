package com.melut.punishsystem.models;

import com.melut.punishsystem.DiscordPunishBot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Appeal {

    public enum AppealStatus {
        PENDING("pending"),
        APPROVED("approved"), 
        REJECTED("rejected"),
        UNDER_REVIEW("under_review");
        
        private final String status;
        
        AppealStatus(String status) {
            this.status = status;
        }
        
        public String getStatus() {
            return status;
        }
    }

    private int id;
    private String playerName;
    private String playerUuid;
    private int punishmentId;
    private String appealReason;
    private AppealStatus status;
    private String reviewerName;
    private String reviewerUuid;
    private String adminResponse;
    private LocalDateTime appealDate;
    private LocalDateTime reviewDate;
    private String discordMessageId;
    private final DiscordPunishBot plugin;

    public Appeal(String playerName, String playerUuid, int punishmentId, String appealReason) {
        this.plugin = DiscordPunishBot.getInstance();
        this.playerName = playerName;
        this.playerUuid = playerUuid;
        this.punishmentId = punishmentId;
        this.appealReason = appealReason;
        this.status = AppealStatus.PENDING;
        this.appealDate = LocalDateTime.now();
    }

    public Appeal(int id, String playerName, String playerUuid, int punishmentId, 
                  String appealReason, AppealStatus status, String reviewerName, 
                  String reviewerUuid, String adminResponse, LocalDateTime appealDate, 
                  LocalDateTime reviewDate, String discordMessageId) {
        this.plugin = DiscordPunishBot.getInstance();
        this.id = id;
        this.playerName = playerName;
        this.playerUuid = playerUuid;
        this.punishmentId = punishmentId;
        this.appealReason = appealReason;
        this.status = status;
        this.reviewerName = reviewerName;
        this.reviewerUuid = reviewerUuid;
        this.adminResponse = adminResponse;
        this.appealDate = appealDate;
        this.reviewDate = reviewDate;
        this.discordMessageId = discordMessageId;
    }

    public int getId() { return id; }
    public String getPlayerName() { return playerName; }
    public String getPlayerUuid() { return playerUuid; }
    public int getPunishmentId() { return punishmentId; }
    public String getAppealReason() { return appealReason; }
    public AppealStatus getStatus() { return status; }
    public String getReviewerName() { return reviewerName; }
    public String getReviewerUuid() { return reviewerUuid; }
    public String getAdminResponse() { return adminResponse; }
    public LocalDateTime getAppealDate() { return appealDate; }
    public LocalDateTime getReviewDate() { return reviewDate; }
    public String getDiscordMessageId() { return discordMessageId; }

    public void setId(int id) { this.id = id; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public void setPlayerUuid(String playerUuid) { this.playerUuid = playerUuid; }
    public void setPunishmentId(int punishmentId) { this.punishmentId = punishmentId; }
    public void setAppealReason(String appealReason) { this.appealReason = appealReason; }
    public void setStatus(AppealStatus status) { this.status = status; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    public void setReviewerUuid(String reviewerUuid) { this.reviewerUuid = reviewerUuid; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }
    public void setAppealDate(LocalDateTime appealDate) { this.appealDate = appealDate; }
    public void setReviewDate(LocalDateTime reviewDate) { this.reviewDate = reviewDate; }
    public void setDiscordMessageId(String discordMessageId) { this.discordMessageId = discordMessageId; }

    public String getFormattedAppealDate() {
        if (plugin != null) {
            String dateFormat = plugin.getConfigManager().getString("settings.date-format", "dd.MM.yyyy HH:mm");
            return appealDate.format(DateTimeFormatter.ofPattern(dateFormat));
        }
        return appealDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public String getFormattedReviewDate() {
        if (reviewDate == null) return "N/A";
        if (plugin != null) {
            String dateFormat = plugin.getConfigManager().getString("settings.date-format", "dd.MM.yyyy HH:mm");
            return reviewDate.format(DateTimeFormatter.ofPattern(dateFormat));
        }
        return reviewDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public String getStatusDisplay() {
        if (plugin != null && plugin.getLanguageManager() != null) {
            return plugin.getLanguageManager().getMessage("appeal.status." + status.getStatus());
        }
        return status.getStatus().toUpperCase();
    }

    public boolean isPending() {
        return status == AppealStatus.PENDING;
    }

    public boolean isApproved() {
        return status == AppealStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == AppealStatus.REJECTED;
    }

    public boolean isUnderReview() {
        return status == AppealStatus.UNDER_REVIEW;
    }

    public void approve(String reviewerName, String reviewerUuid, String response) {
        this.status = AppealStatus.APPROVED;
        this.reviewerName = reviewerName;
        this.reviewerUuid = reviewerUuid;
        this.adminResponse = response;
        this.reviewDate = LocalDateTime.now();
    }

    public void reject(String reviewerName, String reviewerUuid, String response) {
        this.status = AppealStatus.REJECTED;
        this.reviewerName = reviewerName;
        this.reviewerUuid = reviewerUuid;
        this.adminResponse = response;
        this.reviewDate = LocalDateTime.now();
    }

    public void setUnderReview(String reviewerName, String reviewerUuid) {
        this.status = AppealStatus.UNDER_REVIEW;
        this.reviewerName = reviewerName;
        this.reviewerUuid = reviewerUuid;
        this.reviewDate = LocalDateTime.now();
    }

    @Override
    public String toString() {
        if (plugin != null && plugin.getLanguageManager() != null) {
            return plugin.getLanguageManager().getFormattedMessage("appeal.to-string",
                    "id", String.valueOf(id),
                    "player", playerName,
                    "punishment_id", String.valueOf(punishmentId),
                    "status", getStatusDisplay(),
                    "date", getFormattedAppealDate());
        }
        return "Appeal{id=" + id + ", player=" + playerName + ", status=" + status + "}";
    }
}