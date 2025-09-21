package com.melut.punishsystem.models;

import com.melut.punishsystem.DiscordPunishBot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Report {

    public enum ReportStatus {
        PENDING("pending"),
        UNDER_REVIEW("under_review"),
        INVESTIGATING("investigating"),
        RESOLVED("resolved"),
        DISMISSED("dismissed"),
        DUPLICATE("duplicate");
        
        private final String status;
        
        ReportStatus(String status) {
            this.status = status;
        }
        
        public String getStatus() {
            return status;
        }
    }

    public enum ReportPriority {
        LOW("low"),
        MEDIUM("medium"),
        HIGH("high"),
        URGENT("urgent"),
        CRITICAL("critical");
        
        private final String priority;
        
        ReportPriority(String priority) {
            this.priority = priority;
        }
        
        public String getPriority() {
            return priority;
        }
    }

    private int id;
    private String reporterName;
    private String reporterUuid;
    private String reportedPlayerName;
    private String reportedPlayerUuid;
    private String reason;
    private String description;
    private String evidence;
    private ReportStatus status;
    private ReportPriority priority;
    private String reviewerName;
    private String reviewerUuid;
    private String adminResponse;
    private LocalDateTime reportDate;
    private LocalDateTime reviewDate;
    private LocalDateTime resolvedDate;
    private String discordMessageId;
    private boolean anonymous;
    private final DiscordPunishBot plugin;

    public Report(String reporterName, String reporterUuid, String reportedPlayerName, 
                  String reportedPlayerUuid, String reason, String description, boolean anonymous) {
        this.plugin = DiscordPunishBot.getInstance();
        this.reporterName = reporterName;
        this.reporterUuid = reporterUuid;
        this.reportedPlayerName = reportedPlayerName;
        this.reportedPlayerUuid = reportedPlayerUuid;
        this.reason = reason;
        this.description = description;
        this.anonymous = anonymous;
        this.status = ReportStatus.PENDING;
        this.priority = ReportPriority.MEDIUM;
        this.reportDate = LocalDateTime.now();
    }

    public Report(int id, String reporterName, String reporterUuid, String reportedPlayerName,
                  String reportedPlayerUuid, String reason, String description, String evidence,
                  ReportStatus status, ReportPriority priority, String reviewerName, String reviewerUuid,
                  String adminResponse, LocalDateTime reportDate, LocalDateTime reviewDate,
                  LocalDateTime resolvedDate, String discordMessageId, boolean anonymous) {
        this.plugin = DiscordPunishBot.getInstance();
        this.id = id;
        this.reporterName = reporterName;
        this.reporterUuid = reporterUuid;
        this.reportedPlayerName = reportedPlayerName;
        this.reportedPlayerUuid = reportedPlayerUuid;
        this.reason = reason;
        this.description = description;
        this.evidence = evidence;
        this.status = status;
        this.priority = priority;
        this.reviewerName = reviewerName;
        this.reviewerUuid = reviewerUuid;
        this.adminResponse = adminResponse;
        this.reportDate = reportDate;
        this.reviewDate = reviewDate;
        this.resolvedDate = resolvedDate;
        this.discordMessageId = discordMessageId;
        this.anonymous = anonymous;
    }

    public int getId() { return id; }
    public String getReporterName() { return reporterName; }
    public String getReporterUuid() { return reporterUuid; }
    public String getReportedPlayerName() { return reportedPlayerName; }
    public String getReportedPlayerUuid() { return reportedPlayerUuid; }
    public String getReason() { return reason; }
    public String getDescription() { return description; }
    public String getEvidence() { return evidence; }
    public ReportStatus getStatus() { return status; }
    public ReportPriority getPriority() { return priority; }
    public String getReviewerName() { return reviewerName; }
    public String getReviewerUuid() { return reviewerUuid; }
    public String getAdminResponse() { return adminResponse; }
    public LocalDateTime getReportDate() { return reportDate; }
    public LocalDateTime getReviewDate() { return reviewDate; }
    public LocalDateTime getResolvedDate() { return resolvedDate; }
    public String getDiscordMessageId() { return discordMessageId; }
    public boolean isAnonymous() { return anonymous; }

    public void setId(int id) { this.id = id; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public void setReporterUuid(String reporterUuid) { this.reporterUuid = reporterUuid; }
    public void setReportedPlayerName(String reportedPlayerName) { this.reportedPlayerName = reportedPlayerName; }
    public void setReportedPlayerUuid(String reportedPlayerUuid) { this.reportedPlayerUuid = reportedPlayerUuid; }
    public void setReason(String reason) { this.reason = reason; }
    public void setDescription(String description) { this.description = description; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public void setPriority(ReportPriority priority) { this.priority = priority; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    public void setReviewerUuid(String reviewerUuid) { this.reviewerUuid = reviewerUuid; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }
    public void setReviewDate(LocalDateTime reviewDate) { this.reviewDate = reviewDate; }
    public void setResolvedDate(LocalDateTime resolvedDate) { this.resolvedDate = resolvedDate; }
    public void setDiscordMessageId(String discordMessageId) { this.discordMessageId = discordMessageId; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public String getFormattedReportDate() {
        if (plugin != null) {
            String dateFormat = plugin.getConfigManager().getString("settings.date-format", "dd.MM.yyyy HH:mm");
            return reportDate.format(DateTimeFormatter.ofPattern(dateFormat));
        }
        return reportDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public String getFormattedReviewDate() {
        if (reviewDate == null) return "N/A";
        if (plugin != null) {
            String dateFormat = plugin.getConfigManager().getString("settings.date-format", "dd.MM.yyyy HH:mm");
            return reviewDate.format(DateTimeFormatter.ofPattern(dateFormat));
        }
        return reviewDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public String getFormattedResolvedDate() {
        if (resolvedDate == null) return "N/A";
        if (plugin != null) {
            String dateFormat = plugin.getConfigManager().getString("settings.date-format", "dd.MM.yyyy HH:mm");
            return resolvedDate.format(DateTimeFormatter.ofPattern(dateFormat));
        }
        return resolvedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public String getStatusDisplay() {
        if (plugin != null && plugin.getLanguageManager() != null) {
            return plugin.getLanguageManager().getMessage("report.status." + status.getStatus());
        }
        return status.getStatus().toUpperCase();
    }

    public String getPriorityDisplay() {
        if (plugin != null && plugin.getLanguageManager() != null) {
            return plugin.getLanguageManager().getMessage("report.priority." + priority.getPriority());
        }
        return priority.getPriority().toUpperCase();
    }

    public String getDisplayReporterName() {
        return anonymous ? "Anonymous" : reporterName;
    }

    public boolean isPending() {
        return status == ReportStatus.PENDING;
    }

    public boolean isUnderReview() {
        return status == ReportStatus.UNDER_REVIEW;
    }

    public boolean isResolved() {
        return status == ReportStatus.RESOLVED;
    }

    public boolean isDismissed() {
        return status == ReportStatus.DISMISSED;
    }

    public boolean isDuplicate() {
        return status == ReportStatus.DUPLICATE;
    }

    public void setUnderReview(String reviewerName, String reviewerUuid) {
        this.status = ReportStatus.UNDER_REVIEW;
        this.reviewerName = reviewerName;
        this.reviewerUuid = reviewerUuid;
        this.reviewDate = LocalDateTime.now();
    }

    public void setInvestigating(String reviewerName, String reviewerUuid) {
        this.status = ReportStatus.INVESTIGATING;
        this.reviewerName = reviewerName;
        this.reviewerUuid = reviewerUuid;
        this.reviewDate = LocalDateTime.now();
    }

    public boolean isInvestigating() {
        return status == ReportStatus.INVESTIGATING;
    }

    public void resolve(String reviewerName, String reviewerUuid, String response) {
        this.status = ReportStatus.RESOLVED;
        this.reviewerName = reviewerName;
        this.reviewerUuid = reviewerUuid;
        this.adminResponse = response;
        this.resolvedDate = LocalDateTime.now();
    }

    public void dismiss(String reviewerName, String reviewerUuid, String response) {
        this.status = ReportStatus.DISMISSED;
        this.reviewerName = reviewerName;
        this.reviewerUuid = reviewerUuid;
        this.adminResponse = response;
        this.resolvedDate = LocalDateTime.now();
    }

    public void markDuplicate(String reviewerName, String reviewerUuid, String response) {
        this.status = ReportStatus.DUPLICATE;
        this.reviewerName = reviewerName;
        this.reviewerUuid = reviewerUuid;
        this.adminResponse = response;
        this.resolvedDate = LocalDateTime.now();
    }

    @Override
    public String toString() {
        if (plugin != null && plugin.getLanguageManager() != null) {
            return plugin.getLanguageManager().getFormattedMessage("report.to-string",
                    "id", String.valueOf(id),
                    "reporter", getDisplayReporterName(),
                    "reported", reportedPlayerName,
                    "reason", reason,
                    "status", getStatusDisplay(),
                    "date", getFormattedReportDate());
        }
        return "Report{id=" + id + ", reporter=" + getDisplayReporterName() + 
               ", reported=" + reportedPlayerName + ", status=" + status + "}";
    }
}