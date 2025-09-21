package com.melut.punishsystem.managers;

import com.melut.punishsystem.DiscordPunishBot;
import com.melut.punishsystem.models.Appeal;
import com.melut.punishsystem.models.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppealManager {

    private final DiscordPunishBot plugin;
    private final Map<String, Long> appealCooldowns = new ConcurrentHashMap<>();
    private final long APPEAL_COOLDOWN_HOURS = 24;

    public AppealManager(DiscordPunishBot plugin) {
        this.plugin = plugin;
        // createTables() will be called after database initialization
    }
    
    public void initialize() {
        createTables();
    }

    private void createTables() {
        // Appeals table is now created in DatabaseManager.createTables()
        // This method is kept for compatibility but doesn't create new connections
        plugin.getLogger().info("Appeals tablosu başarıyla oluşturuldu");
    }

    public Appeal createAppeal(String playerName, String playerUuid, int punishmentId, String reason) {
        if (hasActivePunishmentAppeal(playerUuid, punishmentId)) {
            return null;
        }

        if (isOnCooldown(playerUuid)) {
            return null;
        }

        Appeal appeal = new Appeal(playerName, playerUuid, punishmentId, reason);

        String insertQuery = """
            INSERT INTO appeals (player_name, player_uuid, punishment_id, appeal_reason, status, appeal_date)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        Connection conn = plugin.getDatabaseManager().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, appeal.getPlayerName());
            stmt.setString(2, appeal.getPlayerUuid());
            stmt.setInt(3, appeal.getPunishmentId());
            stmt.setString(4, appeal.getAppealReason());
            stmt.setString(5, appeal.getStatus().getStatus());
            stmt.setTimestamp(6, Timestamp.valueOf(appeal.getAppealDate()));

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    appeal.setId(keys.getInt(1));
                }
            }
            
            // Commit the appeal creation
            conn.commit();

            updateCooldown(playerUuid);
            sendDiscordNotification(appeal);

            plugin.getLogger().info("İtiraz başarıyla oluşturuldu ve kaydedildi: " + playerName + " - İtiraz ID: " + appeal.getId());

            return appeal;

        } catch (SQLException e) {
            try {
                conn.rollback();
                plugin.getLogger().severe("İtiraz oluşturma hatası, rollback yapıldı: " + e.getMessage());
            } catch (SQLException rollbackError) {
                plugin.getLogger().severe("İtiraz rollback hatası: " + rollbackError.getMessage());
            }
            plugin.getLogger().severe("Failed to create appeal: " + e.getMessage());
            return null;
        }
    }

    public Appeal getAppeal(int appealId) {
        String selectQuery = """
            SELECT * FROM appeals WHERE id = ?
        """;

        Connection conn = plugin.getDatabaseManager().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {

            stmt.setInt(1, appealId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return createAppealFromResultSet(rs);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Appeal getirme hatası: " + e.getMessage());
        }

        return null;
    }

    public List<Appeal> getPlayerAppeals(String playerUuid, int limit) {
        List<Appeal> appeals = new ArrayList<>();
        String selectQuery = """
            SELECT * FROM appeals 
            WHERE player_uuid = ? 
            ORDER BY appeal_date DESC 
            LIMIT ?
        """;

        Connection conn = plugin.getDatabaseManager().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {

            stmt.setString(1, playerUuid);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                appeals.add(createAppealFromResultSet(rs));
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Player appeals getirme hatası: " + e.getMessage());
        }

        return appeals;
    }

    public List<Appeal> getPendingAppeals() {
        List<Appeal> appeals = new ArrayList<>();
        String selectQuery = """
            SELECT * FROM appeals 
            WHERE status = 'pending' 
            ORDER BY appeal_date ASC
        """;

        Connection conn = plugin.getDatabaseManager().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                appeals.add(createAppealFromResultSet(rs));
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Pending appeals getirme hatası: " + e.getMessage());
        }

        return appeals;
    }

    public boolean approveAppeal(int appealId, String reviewerName, String reviewerUuid, String response) {
        Appeal appeal = getAppeal(appealId);
        if (appeal == null || !appeal.isPending()) {
            return false;
        }

        appeal.approve(reviewerName, reviewerUuid, response);

        String updateQuery = """
            UPDATE appeals 
            SET status = ?, reviewer_name = ?, reviewer_uuid = ?, admin_response = ?, review_date = ?
            WHERE id = ?
        """;

        Connection conn = plugin.getDatabaseManager().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setString(1, appeal.getStatus().getStatus());
            stmt.setString(2, appeal.getReviewerName());
            stmt.setString(3, appeal.getReviewerUuid());
            stmt.setString(4, appeal.getAdminResponse());
            stmt.setTimestamp(5, Timestamp.valueOf(appeal.getReviewDate()));
            stmt.setInt(6, appealId);

            int updated = stmt.executeUpdate();
            
            if (updated > 0) {
                conn.commit(); // Commit the approval
                
                removePunishment(appeal);
                
                // İtiraz kabul edildiğinde config'ten komutları çalıştır
                if (plugin.getConfigManager().getBoolean("reward-system.enabled", true)) {
                    executeRewardCommands(appeal.getPlayerName(), "appeal-approved");
                    
                    // Oyuncuya özel mesaj gönder
                    String specialMessage = plugin.getConfigManager().getString("reward-system.appeal-approved-message",
                        "§a§l[İTİRAZ] §7İtirazın onaylandı!");
                    sendSpecialMessage(appeal.getPlayerUuid(), specialMessage);
                }
                
                notifyPlayer(appeal, true);
                sendDiscordUpdate(appeal, true);
                
                plugin.getLogger().info("İtiraz onaylandı: ID " + appealId + " - İnceleyici: " + reviewerName);
                    
                return true;
            }

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackError) {
                plugin.getLogger().severe("Appeal approve rollback hatası: " + rollbackError.getMessage());
            }
            plugin.getLogger().severe("Appeal onaylama hatası: " + e.getMessage());
        }

        return false;
    }

    public boolean rejectAppeal(int appealId, String reviewerName, String reviewerUuid, String response) {
        Appeal appeal = getAppeal(appealId);
        if (appeal == null || !appeal.isPending()) {
            return false;
        }

        appeal.reject(reviewerName, reviewerUuid, response);

        String updateQuery = """
            UPDATE appeals 
            SET status = ?, reviewer_name = ?, reviewer_uuid = ?, admin_response = ?, review_date = ?
            WHERE id = ?
        """;

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setString(1, appeal.getStatus().getStatus());
            stmt.setString(2, appeal.getReviewerName());
            stmt.setString(3, appeal.getReviewerUuid());
            stmt.setString(4, appeal.getAdminResponse());
            stmt.setTimestamp(5, Timestamp.valueOf(appeal.getReviewDate()));
            stmt.setInt(6, appealId);

            int updated = stmt.executeUpdate();
            
            if (updated > 0) {
                notifyPlayer(appeal, false);
                sendDiscordUpdate(appeal, false);
                
                plugin.getLogger().info(plugin.getLanguageManager().getFormattedMessage("log.appeal-rejected",
                    "appeal_id", String.valueOf(appealId),
                    "reviewer", reviewerName));
                    
                return true;
            }

        } catch (SQLException e) {
            plugin.getLogger().severe(plugin.getLanguageManager().getFormattedMessage("log.appeal-reject-error",
                "error", e.getMessage()));
        }

        return false;
    }

    public boolean hasActivePunishmentAppeal(String playerUuid, int punishmentId) {
        String selectQuery = """
            SELECT COUNT(*) FROM appeals 
            WHERE player_uuid = ? AND punishment_id = ? AND status = 'pending'
        """;

        Connection conn = plugin.getDatabaseManager().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {

            stmt.setString(1, playerUuid);
            stmt.setInt(2, punishmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Active appeal check hatası: " + e.getMessage());
        }

        return false;
    }

    public boolean isOnCooldown(String playerUuid) {
        Long lastAppeal = appealCooldowns.get(playerUuid);
        if (lastAppeal == null) {
            return false;
        }

        long hoursPassedMs = System.currentTimeMillis() - lastAppeal;
        long cooldownMs = APPEAL_COOLDOWN_HOURS * 60 * 60 * 1000;
        
        return hoursPassedMs < cooldownMs;
    }

    public long getRemainingCooldown(String playerUuid) {
        Long lastAppeal = appealCooldowns.get(playerUuid);
        if (lastAppeal == null) {
            return 0;
        }

        long hoursPassedMs = System.currentTimeMillis() - lastAppeal;
        long cooldownMs = APPEAL_COOLDOWN_HOURS * 60 * 60 * 1000;
        
        return Math.max(0, cooldownMs - hoursPassedMs) / (60 * 60 * 1000);
    }

    private void updateCooldown(String playerUuid) {
        appealCooldowns.put(playerUuid, System.currentTimeMillis());
    }

    private void removePunishment(Appeal appeal) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(appeal.getPlayerUuid());
            if (player != null) {
                String unbanCommand = "pardon " + player.getName();
                String unmuteCommand = "essentials:mute " + player.getName() + " 0";
                
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), unbanCommand);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), unmuteCommand);
                
                plugin.getLogger().info(plugin.getLanguageManager().getFormattedMessage("log.punishment-removed",
                    "player", player.getName(),
                    "appeal_id", String.valueOf(appeal.getId())));
            }
        });
    }

    private void notifyPlayer(Appeal appeal, boolean approved) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player player = Bukkit.getPlayer(appeal.getPlayerUuid());
            if (player != null && player.isOnline()) {
                String messageKey = approved ? "appeal.approved" : "appeal.rejected";
                player.sendMessage(plugin.getLanguageManager().getFormattedMessage(messageKey,
                    "appeal_id", String.valueOf(appeal.getId()),
                    "response", appeal.getAdminResponse()));
            }
        });
    }

    private void sendDiscordNotification(Appeal appeal) {
        if (plugin.getDiscordBot() != null) {
            plugin.getDiscordBot().sendAppealNotification(appeal);
        }
    }

    private void sendDiscordUpdate(Appeal appeal, boolean approved) {
        if (plugin.getDiscordBot() != null) {
            plugin.getDiscordBot().updateAppealMessage(appeal, approved);
        }
    }

    private Appeal createAppealFromResultSet(ResultSet rs) throws SQLException {
        return new Appeal(
            rs.getInt("id"),
            rs.getString("player_name"),
            rs.getString("player_uuid"),
            rs.getInt("punishment_id"),
            rs.getString("appeal_reason"),
            Appeal.AppealStatus.valueOf(rs.getString("status").toUpperCase()),
            rs.getString("reviewer_name"),
            rs.getString("reviewer_uuid"),
            rs.getString("admin_response"),
            rs.getTimestamp("appeal_date").toLocalDateTime(),
            rs.getTimestamp("review_date") != null ? rs.getTimestamp("review_date").toLocalDateTime() : null,
            rs.getString("discord_message_id")
        );
    }

    public void cleanupOldAppeals() {
        String deleteQuery = """
            DELETE FROM appeals 
            WHERE appeal_date < ? AND status IN ('approved', 'rejected')
        """;

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {

            stmt.setTimestamp(1, Timestamp.valueOf(cutoffDate));
            int deleted = stmt.executeUpdate();

            if (deleted > 0) {
                plugin.getLogger().info(plugin.getLanguageManager().getFormattedMessage("log.old-appeals-cleaned",
                    "count", String.valueOf(deleted)));
            }

        } catch (SQLException e) {
            plugin.getLogger().severe(plugin.getLanguageManager().getFormattedMessage("log.appeals-cleanup-error",
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