package com.melut.punishsystem.managers;

import com.melut.punishsystem.DiscordPunishBot;
import com.melut.punishsystem.models.Report;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReportManager {

    private final DiscordPunishBot plugin;
    private final Map<String, Long> reportCooldowns = new ConcurrentHashMap<>();
    private final long REPORT_COOLDOWN_MINUTES = 5;

    public ReportManager(DiscordPunishBot plugin) {
        this.plugin = plugin;
        // createTables() will be called after database initialization
    }
    
    public void initialize() {
        createTables();
    }

    private void createTables() {
        // Reports table is now created in DatabaseManager.createTables()
        // This method is kept for compatibility but doesn't create new connections
        plugin.getLogger().info("Reports tablosu başarıyla oluşturuldu");
    }

    public Report createReport(String reporterName, String reporterUuid, String reportedPlayerName, 
                              String reportedPlayerUuid, String reason, String description, boolean anonymous) {
        
        if (isOnCooldown(reporterUuid)) {
            return null;
        }

        if (reporterUuid.equals(reportedPlayerUuid)) {
            return null;
        }

        Report report = new Report(reporterName, reporterUuid, reportedPlayerName, reportedPlayerUuid, 
                                 reason, description, anonymous);

        String insertQuery = """
            INSERT INTO reports (reporter_name, reporter_uuid, reported_player_name, reported_player_uuid, 
                               reason, description, status, priority, report_date, anonymous)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        Connection conn = plugin.getDatabaseManager().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, report.getReporterName());
            stmt.setString(2, report.getReporterUuid());
            stmt.setString(3, report.getReportedPlayerName());
            stmt.setString(4, report.getReportedPlayerUuid());
            stmt.setString(5, report.getReason());
            stmt.setString(6, report.getDescription());
            stmt.setString(7, report.getStatus().getStatus());
            stmt.setString(8, report.getPriority().getPriority());
            stmt.setTimestamp(9, Timestamp.valueOf(report.getReportDate()));
            stmt.setBoolean(10, report.isAnonymous());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    report.setId(keys.getInt(1));
                }
            }
            
            // Commit the report creation
            conn.commit();

            updateCooldown(reporterUuid);
            sendDiscordNotification(report);
            notifyModerators(report);

            plugin.getLogger().info("Şikayet başarıyla oluşturuldu ve kaydedildi: " + report.getDisplayReporterName() + " -> " + reportedPlayerName + " - Şikayet ID: " + report.getId());

            return report;

        } catch (SQLException e) {
            try {
                conn.rollback();
                plugin.getLogger().severe("Şikayet oluşturma hatası, rollback yapıldı: " + e.getMessage());
            } catch (SQLException rollbackError) {
                plugin.getLogger().severe("Report rollback hatası: " + rollbackError.getMessage());
            }
            plugin.getLogger().severe("Failed to create report: " + e.getMessage());
            return null;
        }
    }

    public Report getReport(int reportId) {
        String selectQuery = """
            SELECT * FROM reports WHERE id = ?
        """;

        Connection conn = plugin.getDatabaseManager().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {

            stmt.setInt(1, reportId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return createReportFromResultSet(rs);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Report getirme hatası: " + e.getMessage());
        }

        return null;
    }

    public List<Report> getPlayerReports(String reporterUuid, int limit) {
        List<Report> reports = new ArrayList<>();
        String selectQuery = """
            SELECT * FROM reports
            WHERE reporter_uuid = ?
            ORDER BY report_date DESC
            LIMIT ?
        """;

        Connection conn = plugin.getDatabaseManager().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {

            stmt.setString(1, reporterUuid);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reports.add(createReportFromResultSet(rs));
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Player reports getirme hatası: " + e.getMessage());
            e.printStackTrace();
        }

        return reports;
    }

    public List<Report> getReportsByStatus(Report.ReportStatus status) {
        List<Report> reports = new ArrayList<>();
        String selectQuery = """
            SELECT * FROM reports 
            WHERE status = ? 
            ORDER BY report_date ASC
        """;

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectQuery)) {

            stmt.setString(1, status.getStatus());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reports.add(createReportFromResultSet(rs));
            }

        } catch (SQLException e) {
            plugin.getLogger().severe(plugin.getLanguageManager().getFormattedMessage("log.status-reports-error",
                "error", e.getMessage()));
        }

        return reports;
    }

    public List<Report> getPendingReports() {
        return getReportsByStatus(Report.ReportStatus.PENDING);
    }

    public boolean resolveReport(int reportId, String reviewerName, String reviewerUuid, String response) {
        Report report = getReport(reportId);
        if (report == null || report.isResolved()) {
            return false;
        }

        report.resolve(reviewerName, reviewerUuid, response);

        return updateReportStatus(report);
    }

    public boolean dismissReport(int reportId, String reviewerName, String reviewerUuid, String response) {
        Report report = getReport(reportId);
        if (report == null || report.isResolved() || report.isDismissed()) {
            return false;
        }

        report.dismiss(reviewerName, reviewerUuid, response);

        return updateReportStatus(report);
    }

    public boolean markDuplicate(int reportId, String reviewerName, String reviewerUuid, String response) {
        Report report = getReport(reportId);
        if (report == null || report.isDuplicate()) {
            return false;
        }

        report.markDuplicate(reviewerName, reviewerUuid, response);

        return updateReportStatus(report);
    }

    public boolean setUnderReview(int reportId, String reviewerName, String reviewerUuid) {
        Report report = getReport(reportId);
        if (report == null || !report.isPending()) {
            return false;
        }

        report.setUnderReview(reviewerName, reviewerUuid);

        return updateReportStatus(report);
    }

    public boolean setInvestigating(int reportId, String reviewerName, String reviewerUuid) {
        Report report = getReport(reportId);
        if (report == null || !report.isPending()) {
            return false;
        }

        report.setInvestigating(reviewerName, reviewerUuid);

        boolean result = updateReportStatus(report);
        
        if (result && plugin.getConfigManager().getBoolean("reward-system.enabled", true)) {
            // Report onaylandığında ödül ver
            executeRewardCommands(report.getReporterName(), "report-correct");
            
            // Oyuncuya özel mesaj gönder
            String specialMessage = plugin.getConfigManager().getString("reward-system.report-approved-message",
                "§a§l[ŞİKAYET] §7Şikayetin doğru çıktı!");
            sendSpecialMessage(report.getReporterUuid(), specialMessage);
        }
        
        return result;
    }

    public boolean updatePriority(int reportId, Report.ReportPriority priority) {
        String updateQuery = """
            UPDATE reports SET priority = ? WHERE id = ?
        """;

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setString(1, priority.getPriority());
            stmt.setInt(2, reportId);

            int updated = stmt.executeUpdate();
            
            if (updated > 0) {
                plugin.getLogger().info(plugin.getLanguageManager().getFormattedMessage("log.report-priority-updated",
                    "report_id", String.valueOf(reportId),
                    "priority", priority.getPriority()));
                    
                return true;
            }

        } catch (SQLException e) {
            plugin.getLogger().severe(plugin.getLanguageManager().getFormattedMessage("log.report-priority-error",
                "error", e.getMessage()));
        }

        return false;
    }

    public boolean addEvidence(int reportId, String evidence) {
        String updateQuery = """
            UPDATE reports SET evidence = ? WHERE id = ?
        """;

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setString(1, evidence);
            stmt.setInt(2, reportId);

            int updated = stmt.executeUpdate();
            
            if (updated > 0) {
                plugin.getLogger().info(plugin.getLanguageManager().getFormattedMessage("log.report-evidence-added",
                    "report_id", String.valueOf(reportId)));
                    
                return true;
            }

        } catch (SQLException e) {
            plugin.getLogger().severe(plugin.getLanguageManager().getFormattedMessage("log.report-evidence-error",
                "error", e.getMessage()));
        }

        return false;
    }

    public boolean isOnCooldown(String reporterUuid) {
        Long lastReport = reportCooldowns.get(reporterUuid);
        if (lastReport == null) {
            return false;
        }

        long minutesPassedMs = System.currentTimeMillis() - lastReport;
        long cooldownMs = REPORT_COOLDOWN_MINUTES * 60 * 1000;
        
        return minutesPassedMs < cooldownMs;
    }

    public long getRemainingCooldown(String reporterUuid) {
        Long lastReport = reportCooldowns.get(reporterUuid);
        if (lastReport == null) {
            return 0;
        }

        long minutesPassedMs = System.currentTimeMillis() - lastReport;
        long cooldownMs = REPORT_COOLDOWN_MINUTES * 60 * 1000;
        
        return Math.max(0, cooldownMs - minutesPassedMs) / (60 * 1000);
    }

    public int getPlayerReportCount(String reportedPlayerUuid, int daysPeriod) {
        String selectQuery = """
            SELECT COUNT(*) FROM reports 
            WHERE reported_player_uuid = ? AND report_date >= ?
        """;

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysPeriod);

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectQuery)) {

            stmt.setString(1, reportedPlayerUuid);
            stmt.setTimestamp(2, Timestamp.valueOf(cutoffDate));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe(plugin.getLanguageManager().getFormattedMessage("log.report-count-error",
                "error", e.getMessage()));
        }

        return 0;
    }

    private boolean updateReportStatus(Report report) {
        String updateQuery = """
            UPDATE reports 
            SET status = ?, reviewer_name = ?, reviewer_uuid = ?, admin_response = ?, 
                review_date = ?, resolved_date = ?
            WHERE id = ?
        """;

        Connection conn = plugin.getDatabaseManager().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setString(1, report.getStatus().getStatus());
            stmt.setString(2, report.getReviewerName());
            stmt.setString(3, report.getReviewerUuid());
            stmt.setString(4, report.getAdminResponse());
            stmt.setTimestamp(5, report.getReviewDate() != null ? Timestamp.valueOf(report.getReviewDate()) : null);
            stmt.setTimestamp(6, report.getResolvedDate() != null ? Timestamp.valueOf(report.getResolvedDate()) : null);
            stmt.setInt(7, report.getId());

            int updated = stmt.executeUpdate();
            
            if (updated > 0) {
                conn.commit(); // Commit the update
                
                notifyReporter(report);
                sendDiscordUpdate(report);
                
                plugin.getLogger().info("Report durumu güncellendi: ID " + report.getId() + " -> " + report.getStatus().getStatus() + " (İnceleyici: " + report.getReviewerName() + ")");
                    
                return true;
            }

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackError) {
                plugin.getLogger().severe("Report update rollback hatası: " + rollbackError.getMessage());
            }
            plugin.getLogger().severe("Report güncelleme hatası: " + e.getMessage());
        }

        return false;
    }

    private void updateCooldown(String reporterUuid) {
        reportCooldowns.put(reporterUuid, System.currentTimeMillis());
    }

    private void notifyModerators(Report report) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            String message = plugin.getLanguageManager().getFormattedMessage("report.moderator-notification",
                "reporter", report.getDisplayReporterName(),
                "reported", report.getReportedPlayerName(),
                "reason", report.getReason(),
                "report_id", String.valueOf(report.getId()));

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("discordpunish.report.notify")) {
                    player.sendMessage(message);
                }
            }
        });
    }

    private void notifyReporter(Report report) {
        if (report.isAnonymous()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player reporter = Bukkit.getPlayer(report.getReporterUuid());
            if (reporter != null && reporter.isOnline()) {
                String messageKey = "report.status-update." + report.getStatus().getStatus();
                reporter.sendMessage(plugin.getLanguageManager().getFormattedMessage(messageKey,
                    "report_id", String.valueOf(report.getId()),
                    "reported", report.getReportedPlayerName(),
                    "response", report.getAdminResponse() != null ? report.getAdminResponse() : ""));
            }
        });
    }

    private void sendDiscordNotification(Report report) {
        if (plugin.getDiscordBot() != null) {
            plugin.getDiscordBot().sendReportNotification(report);
        }
    }

    private void sendDiscordUpdate(Report report) {
        if (plugin.getDiscordBot() != null) {
            plugin.getDiscordBot().updateReportMessage(report);
        }
    }

    private Report createReportFromResultSet(ResultSet rs) throws SQLException {
        return new Report(
            rs.getInt("id"),
            rs.getString("reporter_name"),
            rs.getString("reporter_uuid"),
            rs.getString("reported_player_name"),
            rs.getString("reported_player_uuid"),
            rs.getString("reason"),
            rs.getString("description"),
            rs.getString("evidence"),
            Report.ReportStatus.valueOf(rs.getString("status").toUpperCase()),
            Report.ReportPriority.valueOf(rs.getString("priority").toUpperCase()),
            rs.getString("reviewer_name"),
            rs.getString("reviewer_uuid"),
            rs.getString("admin_response"),
            rs.getTimestamp("report_date").toLocalDateTime(),
            rs.getTimestamp("review_date") != null ? rs.getTimestamp("review_date").toLocalDateTime() : null,
            rs.getTimestamp("resolved_date") != null ? rs.getTimestamp("resolved_date").toLocalDateTime() : null,
            rs.getString("discord_message_id"),
            rs.getBoolean("anonymous")
        );
    }

    public void cleanupOldReports() {
        String deleteQuery = """
            DELETE FROM reports 
            WHERE report_date < ? AND status IN ('resolved', 'dismissed', 'duplicate')
        """;

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(60);

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {

            stmt.setTimestamp(1, Timestamp.valueOf(cutoffDate));
            int deleted = stmt.executeUpdate();

            if (deleted > 0) {
                plugin.getLogger().info(plugin.getLanguageManager().getFormattedMessage("log.old-reports-cleaned",
                    "count", String.valueOf(deleted)));
            }

        } catch (SQLException e) {
            plugin.getLogger().severe(plugin.getLanguageManager().getFormattedMessage("log.reports-cleanup-error",
                "error", e.getMessage()));
        }
    }
    
    /**
     * Config'ten belirtilen komutları çalıştırır
     */
    private void executeRewardCommands(String playerName, String configKey) {
        try {
            List<String> commands = plugin.getConfigManager().getStringList("reward-system." + configKey + "-commands");
            
            if (commands != null && !commands.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    for (String command : commands) {
                        String processedCommand = command.replace("%player%", playerName);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                        plugin.getLogger().info("Ödül komutu çalıştırıldı: " + processedCommand);
                    }
                });
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Ödül komutu çalıştırma hatası: " + e.getMessage());
        }
    }
    
    /**
     * Oyuncuya özel mesaj gönderir (online ise)
     */
    private void sendSpecialMessage(String playerUuid, String message) {
        try {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Player player = Bukkit.getPlayer(playerUuid);
                if (player != null && player.isOnline()) {
                    player.sendMessage(message);
                    plugin.getLogger().info("Özel mesaj gönderildi: " + player.getName());
                }
            });
        } catch (Exception e) {
            plugin.getLogger().severe("Özel mesaj gönderme hatası: " + e.getMessage());
        }
    }
}