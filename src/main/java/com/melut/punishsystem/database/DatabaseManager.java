package com.melut.punishsystem.database;

import com.melut.punishsystem.DiscordPunishBot;
import com.melut.punishsystem.models.Punishment;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private final DiscordPunishBot plugin;
    private Connection connection;
    private final String databaseType;
    private volatile boolean isInitialized = false;

    public DatabaseManager(DiscordPunishBot plugin) {
        this.plugin = plugin;
        this.databaseType = "sqlite"; // Force SQLite
    }

    public void initializeDatabase() {
        try {
            plugin.getLogger().info("Initializing SQLite database...");
            setupSQLite();
            createTables();
            isInitialized = true;
            plugin.getLogger().info("Database initialized successfully with SQLite");

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            isInitialized = false;
        } catch (Exception e) {
            plugin.getLogger().severe("Unexpected error during database initialization: " + e.getMessage());
            e.printStackTrace();
            isInitialized = false;
        }
    }

    private void setupSQLite() throws SQLException {
        File databaseFile = new File(plugin.getDataFolder(), "punishments.db");
        String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath() + "?journal_mode=WAL&busy_timeout=30000&cache_size=10000&synchronous=NORMAL";
        
        plugin.getLogger().info("Creating SQLite database at: " + databaseFile.getAbsolutePath());

        try {
            // Force load SQLite JDBC
            Class.forName("org.sqlite.JDBC");
            plugin.getLogger().info("SQLite JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("SQLite JDBC driver not found!");
            throw new SQLException("SQLite driver not found", e);
        }

        try {
            // Close existing connection if any
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            
            connection = DriverManager.getConnection(url);
            connection.setAutoCommit(false); // Change to false for better performance
            
            // Enable foreign keys
            try (PreparedStatement stmt = connection.prepareStatement("PRAGMA foreign_keys = ON")) {
                stmt.execute();
            }
            
            // Set busy timeout
            try (PreparedStatement stmt = connection.prepareStatement("PRAGMA busy_timeout = 30000")) {
                stmt.execute();
            }
            
            // Commit the pragma settings
            connection.commit();
            
            plugin.getLogger().info("SQLite connection established successfully with optimizations");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to SQLite: " + e.getMessage());
            throw e;
        }
    }

    private void setupMySQL() throws SQLException {
        String host = plugin.getConfigManager().getString("database.host");
        int port = plugin.getConfigManager().getInt("database.port");
        String database = plugin.getConfigManager().getString("database.database");
        String username = plugin.getConfigManager().getString("database.username");
        String password = plugin.getConfigManager().getString("database.password");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("MySQL JDBC driver not found!");
            return;
        }

        connection = DriverManager.getConnection(url, username, password);
    }

    private void createTables() throws SQLException {
        plugin.getLogger().info("Creating database tables...");
        
        // PUNISHMENTS TABLE
        String createPunishmentsTable = """
            CREATE TABLE IF NOT EXISTS punishments (
                id INTEGER PRIMARY KEY %s,
                player_name VARCHAR(16) NOT NULL,
                player_uuid VARCHAR(36) NOT NULL,
                punishment_type VARCHAR(20) NOT NULL,
                reason VARCHAR(100) NOT NULL,
                admin_name VARCHAR(16) NOT NULL,
                admin_uuid VARCHAR(36) NOT NULL,
                date_issued DATETIME NOT NULL,
                active BOOLEAN DEFAULT 1
            )
        """.formatted("AUTOINCREMENT");

        try (PreparedStatement stmt = connection.prepareStatement(createPunishmentsTable)) {
            stmt.executeUpdate();
            plugin.getLogger().info("Punishments tablosu başarıyla oluşturuldu");
        } catch (SQLException e) {
            plugin.getLogger().severe("Punishments tablosu oluşturulamadı: " + e.getMessage());
            throw e;
        }

        // PUNISHMENT_STATS TABLE
        String createStatsTable = """
            CREATE TABLE IF NOT EXISTS punishment_stats (
                id INTEGER PRIMARY KEY %s,
                player_uuid VARCHAR(36) NOT NULL,
                total_punishments INTEGER DEFAULT 0,
                total_mutes INTEGER DEFAULT 0,
                total_bans INTEGER DEFAULT 0,
                total_kicks INTEGER DEFAULT 0,
                total_tempbans INTEGER DEFAULT 0,
                total_jails INTEGER DEFAULT 0,
                total_warns INTEGER DEFAULT 0,
                total_other INTEGER DEFAULT 0,
                last_punishment DATETIME,
                UNIQUE(player_uuid)
            )
        """.formatted("AUTOINCREMENT");

        try (PreparedStatement stmt = connection.prepareStatement(createStatsTable)) {
            stmt.executeUpdate();
            plugin.getLogger().info("Punishment_stats tablosu başarıyla oluşturuldu");
        } catch (SQLException e) {
            plugin.getLogger().severe("Punishment_stats tablosu oluşturulamadı: " + e.getMessage());
            throw e;
        }

        // REPORTS TABLE (moved to ReportManager but keeping for compatibility)
        String createReportsTable = """
            CREATE TABLE IF NOT EXISTS reports (
                id INTEGER PRIMARY KEY %s,
                reporter_name VARCHAR(16) NOT NULL,
                reporter_uuid VARCHAR(36) NOT NULL,
                reported_player_name VARCHAR(16) NOT NULL,
                reported_player_uuid VARCHAR(36) NOT NULL,
                reason VARCHAR(100) NOT NULL,
                description TEXT,
                evidence TEXT,
                status VARCHAR(20) DEFAULT 'PENDING',
                priority VARCHAR(20) DEFAULT 'MEDIUM',
                reviewer_name VARCHAR(16),
                reviewer_uuid VARCHAR(36),
                admin_response TEXT,
                report_date DATETIME NOT NULL,
                review_date DATETIME,
                resolved_date DATETIME,
                discord_message_id VARCHAR(20),
                anonymous BOOLEAN DEFAULT 0
            )
        """.formatted("AUTOINCREMENT");

        try (PreparedStatement stmt = connection.prepareStatement(createReportsTable)) {
            stmt.executeUpdate();
            plugin.getLogger().info("Reports tablosu DatabaseManager'dan oluşturuldu");
        } catch (SQLException e) {
            plugin.getLogger().warning("Reports tablosu DatabaseManager'dan oluşturulamadı (normal): " + e.getMessage());
        }

        // APPEALS TABLE (moved to AppealManager but keeping for compatibility)
        String createAppealsTable = """
            CREATE TABLE IF NOT EXISTS appeals (
                id INTEGER PRIMARY KEY %s,
                punishment_id INTEGER NOT NULL,
                player_name VARCHAR(16) NOT NULL,
                player_uuid VARCHAR(36) NOT NULL,
                appeal_reason TEXT NOT NULL,
                appeal_date DATETIME NOT NULL,
                status VARCHAR(20) DEFAULT 'PENDING',
                reviewer_name VARCHAR(16),
                reviewer_uuid VARCHAR(36),
                admin_response TEXT,
                review_date DATETIME,
                discord_message_id VARCHAR(20),
                FOREIGN KEY(punishment_id) REFERENCES punishments(id)
            )
        """.formatted("AUTOINCREMENT");

        try (PreparedStatement stmt = connection.prepareStatement(createAppealsTable)) {
            stmt.executeUpdate();
            plugin.getLogger().info("Appeals tablosu DatabaseManager'dan oluşturuldu");
        } catch (SQLException e) {
            plugin.getLogger().warning("Appeals tablosu DatabaseManager'dan oluşturulamadı (normal): " + e.getMessage());
        }

        // REWARD_POINTS TABLE
        String createRewardPointsTable = """
            CREATE TABLE IF NOT EXISTS reward_points (
                id INTEGER PRIMARY KEY %s,
                player_name VARCHAR(16) NOT NULL,
                player_uuid VARCHAR(36) NOT NULL,
                points INTEGER DEFAULT 0,
                total_earned INTEGER DEFAULT 0,
                last_updated DATETIME NOT NULL,
                UNIQUE(player_uuid)
            )
        """.formatted("AUTOINCREMENT");

        try (PreparedStatement stmt = connection.prepareStatement(createRewardPointsTable)) {
            stmt.executeUpdate();
            plugin.getLogger().info("Reward_points tablosu başarıyla oluşturuldu");
        } catch (SQLException e) {
            plugin.getLogger().severe("Reward_points tablosu oluşturulamadı: " + e.getMessage());
            throw e;
        }

        // Commit all table creations
        connection.commit();
        plugin.getLogger().info("Tüm veritabanı tabloları commit edildi");

        addNewColumnsIfNotExist();
    }

    private void addNewColumnsIfNotExist() throws SQLException {
        if (databaseType.equalsIgnoreCase("sqlite")) {
            try {
                String[] newColumns = {
                    "ALTER TABLE punishment_stats ADD COLUMN total_kicks INTEGER DEFAULT 0",
                    "ALTER TABLE punishment_stats ADD COLUMN total_tempbans INTEGER DEFAULT 0",
                    "ALTER TABLE punishment_stats ADD COLUMN total_jails INTEGER DEFAULT 0",
                    "ALTER TABLE punishment_stats ADD COLUMN total_warns INTEGER DEFAULT 0"
                };

                for (String columnQuery : newColumns) {
                    try (PreparedStatement stmt = connection.prepareStatement(columnQuery)) {
                        stmt.executeUpdate();
                    } catch (SQLException ignored) {
                        // Column already exists, ignore
                    }
                }
                
                // Commit column additions
                connection.commit();
                plugin.getLogger().info("Veritabanı kolon güncellemeleri commit edildi");
                
            } catch (Exception e) {
                plugin.getLogger().warning("Kolon güncellemesi hatası: " + e.getMessage());
            }
        }
    }

    public void addPunishment(Punishment punishment) {
        String insertPunishment = """
            INSERT INTO punishments (player_name, player_uuid, punishment_type, reason, admin_name, admin_uuid, date_issued)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = getConnection().prepareStatement(insertPunishment, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, punishment.getPlayerName());
            stmt.setString(2, punishment.getPlayerUuid());
            stmt.setString(3, punishment.getType());
            stmt.setString(4, punishment.getReason());
            stmt.setString(5, punishment.getAdminName());
            stmt.setString(6, punishment.getAdminUuid());
            stmt.setTimestamp(7, Timestamp.valueOf(punishment.getDateIssued()));

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys != null && keys.next()) {
                    punishment.setId(keys.getInt(1));
                } else {
                    try (PreparedStatement lastIdStmt = connection.prepareStatement(databaseType.equalsIgnoreCase("sqlite") ?
                            "SELECT last_insert_rowid()" : "SELECT LAST_INSERT_ID()")) {
                        try (ResultSet rs = lastIdStmt.executeQuery()) {
                            if (rs.next()) {
                                punishment.setId(rs.getInt(1));
                            }
                        }
                    } catch (SQLException ignored) { }
                }
            }

            updatePlayerStats(punishment.getPlayerUuid(), punishment.getType());
            
            // Commit the punishment insertion
            connection.commit();
            plugin.getLogger().info("Ceza veritabanına başarıyla eklendi ve commit edildi: " + punishment.getPlayerName() + " - " + punishment.getType());

        } catch (SQLException e) {
            try {
                connection.rollback();
                plugin.getLogger().severe("Ceza ekleme hatası, rollback yapıldı: " + e.getMessage());
            } catch (SQLException rollbackError) {
                plugin.getLogger().severe("Rollback hatası: " + rollbackError.getMessage());
            }
            plugin.getLogger().severe("Failed to add punishment: " + e.getMessage());
        }
    }

    private void updatePlayerStats(String playerUuid, String punishmentType) {
        String upsertStats = databaseType.equalsIgnoreCase("sqlite") ?
                """
                INSERT OR REPLACE INTO punishment_stats (player_uuid, total_punishments, total_mutes, total_bans, total_kicks, total_tempbans, total_jails, total_warns, total_other, last_punishment)
                VALUES (?, 
                    COALESCE((SELECT total_punishments FROM punishment_stats WHERE player_uuid = ?), 0) + 1,
                    COALESCE((SELECT total_mutes FROM punishment_stats WHERE player_uuid = ?), 0) + ?,
                    COALESCE((SELECT total_bans FROM punishment_stats WHERE player_uuid = ?), 0) + ?,
                    COALESCE((SELECT total_kicks FROM punishment_stats WHERE player_uuid = ?), 0) + ?,
                    COALESCE((SELECT total_tempbans FROM punishment_stats WHERE player_uuid = ?), 0) + ?,
                    COALESCE((SELECT total_jails FROM punishment_stats WHERE player_uuid = ?), 0) + ?,
                    COALESCE((SELECT total_warns FROM punishment_stats WHERE player_uuid = ?), 0) + ?,
                    COALESCE((SELECT total_other FROM punishment_stats WHERE player_uuid = ?), 0) + ?,
                    ?
                )
                """ :
                """
                INSERT INTO punishment_stats (player_uuid, total_punishments, total_mutes, total_bans, total_kicks, total_tempbans, total_jails, total_warns, total_other, last_punishment)
                VALUES (?, 1, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                total_punishments = total_punishments + 1,
                total_mutes = total_mutes + ?,
                total_bans = total_bans + ?,
                total_kicks = total_kicks + ?,
                total_tempbans = total_tempbans + ?,
                total_jails = total_jails + ?,
                total_warns = total_warns + ?,
                total_other = total_other + ?,
                last_punishment = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(upsertStats)) {
            int muteCount = punishmentType.equals("mute") ? 1 : 0;
            int banCount = punishmentType.equals("ban") ? 1 : 0;
            int kickCount = punishmentType.equals("kick") ? 1 : 0;
            int tempbanCount = punishmentType.equals("tempban") ? 1 : 0;
            int jailCount = punishmentType.equals("jail") ? 1 : 0;
            int warnCount = punishmentType.equals("warn") ? 1 : 0;
            int otherCount = (!punishmentType.equals("mute") && !punishmentType.equals("ban") && 
                             !punishmentType.equals("kick") && !punishmentType.equals("tempban") && 
                             !punishmentType.equals("jail") && !punishmentType.equals("warn")) ? 1 : 0;

            if (databaseType.equalsIgnoreCase("sqlite")) {
                stmt.setString(1, playerUuid);
                stmt.setString(2, playerUuid);
                stmt.setString(3, playerUuid);
                stmt.setInt(4, muteCount);
                stmt.setString(5, playerUuid);
                stmt.setInt(6, banCount);
                stmt.setString(7, playerUuid);
                stmt.setInt(8, kickCount);
                stmt.setString(9, playerUuid);
                stmt.setInt(10, tempbanCount);
                stmt.setString(11, playerUuid);
                stmt.setInt(12, jailCount);
                stmt.setString(13, playerUuid);
                stmt.setInt(14, warnCount);
                stmt.setString(15, playerUuid);
                stmt.setInt(16, otherCount);
                stmt.setTimestamp(17, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                stmt.setString(1, playerUuid);
                stmt.setInt(2, muteCount);
                stmt.setInt(3, banCount);
                stmt.setInt(4, kickCount);
                stmt.setInt(5, tempbanCount);
                stmt.setInt(6, jailCount);
                stmt.setInt(7, warnCount);
                stmt.setInt(8, otherCount);
                stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setInt(10, muteCount);
                stmt.setInt(11, banCount);
                stmt.setInt(12, kickCount);
                stmt.setInt(13, tempbanCount);
                stmt.setInt(14, jailCount);
                stmt.setInt(15, warnCount);
                stmt.setInt(16, otherCount);
                stmt.setTimestamp(17, Timestamp.valueOf(LocalDateTime.now()));
            }

            stmt.executeUpdate();
            // Note: commit handled by addPunishment method

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update player stats: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackError) {
                plugin.getLogger().severe("Stats rollback hatası: " + rollbackError.getMessage());
            }
        }
    }

    // Ödül Puanları Sistemi Metodları
    public void addRewardPoints(String playerUuid, String playerName, int points, String reason) {
        String upsertQuery = databaseType.equalsIgnoreCase("sqlite") ?
                """
                INSERT OR REPLACE INTO reward_points (player_uuid, player_name, points, total_earned, last_updated)
                VALUES (?, ?,
                    COALESCE((SELECT points FROM reward_points WHERE player_uuid = ?), 0) + ?,
                    COALESCE((SELECT total_earned FROM reward_points WHERE player_uuid = ?), 0) + ?,
                    ?
                )
                """ :
                """
                INSERT INTO reward_points (player_uuid, player_name, points, total_earned, last_updated)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                points = points + ?,
                total_earned = total_earned + ?,
                last_updated = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(upsertQuery)) {
            if (databaseType.equalsIgnoreCase("sqlite")) {
                stmt.setString(1, playerUuid);
                stmt.setString(2, playerName);
                stmt.setString(3, playerUuid);
                stmt.setInt(4, points);
                stmt.setString(5, playerUuid);
                stmt.setInt(6, points);
                stmt.setTimestamp(7, Timestamp.valueOf(java.time.LocalDateTime.now()));
            } else {
                stmt.setString(1, playerUuid);
                stmt.setString(2, playerName);
                stmt.setInt(3, points);
                stmt.setInt(4, points);
                stmt.setTimestamp(5, Timestamp.valueOf(java.time.LocalDateTime.now()));
                stmt.setInt(6, points);
                stmt.setInt(7, points);
                stmt.setTimestamp(8, Timestamp.valueOf(java.time.LocalDateTime.now()));
            }

            stmt.executeUpdate();
            connection.commit();
            
            plugin.getLogger().info("Oyuncu " + playerName + " adlı kişiye " + points + " ödül puanı eklendi. Sebep: " + reason);

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackError) {
                plugin.getLogger().severe("Reward points rollback hatası: " + rollbackError.getMessage());
            }
            plugin.getLogger().severe("Ödül puanı ekleme hatası: " + e.getMessage());
        }
    }

    public int getRewardPoints(String playerUuid) {
        String query = "SELECT points FROM reward_points WHERE player_uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, playerUuid);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("points");
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Ödül puanı alma hatası: " + e.getMessage());
        }

        return 0;
    }

    public boolean spendRewardPoints(String playerUuid, int points) {
        if (getRewardPoints(playerUuid) < points) {
            return false;
        }

        String updateQuery = "UPDATE reward_points SET points = points - ?, last_updated = ? WHERE player_uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setInt(1, points);
            stmt.setTimestamp(2, Timestamp.valueOf(java.time.LocalDateTime.now()));
            stmt.setString(3, playerUuid);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                connection.commit();
                return true;
            }
            return false;

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackError) {
                plugin.getLogger().severe("Spend points rollback hatası: " + rollbackError.getMessage());
            }
            plugin.getLogger().severe("Ödül puanı harcama hatası: " + e.getMessage());
            return false;
        }
    }
    public List<Punishment> getPlayerPunishments(String playerUuid, int limit) {
        List<Punishment> punishments = new ArrayList<>();

        String query = """
            SELECT * FROM punishments
            WHERE player_uuid = ?
            ORDER BY date_issued DESC
            LIMIT ?
        """;

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUuid);
            stmt.setInt(2, limit);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Punishment punishment = new Punishment(
                        rs.getString("player_name"),
                        rs.getString("player_uuid"),
                        rs.getString("punishment_type"),
                        rs.getString("reason"),
                        rs.getString("admin_name"),
                        rs.getString("admin_uuid"),
                        rs.getTimestamp("date_issued").toLocalDateTime()
                );
                punishment.setId(rs.getInt("id"));
                punishment.setActive(rs.getBoolean("active"));

                punishments.add(punishment);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get player punishments: " + e.getMessage());
        }

        return punishments;
    }

    public int getTotalPunishments(String playerUuid) {
        String query = "SELECT total_punishments FROM punishment_stats WHERE player_uuid = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUuid);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total_punishments");
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get total punishments: " + e.getMessage());
        }

        return 0;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
        }
    }

    public Punishment getPunishment(int punishmentId) {
        String query = "SELECT * FROM punishments WHERE id = ?";
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, punishmentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Punishment punishment = new Punishment(
                    rs.getString("player_name"),
                    rs.getString("player_uuid"),
                    rs.getString("punishment_type"),
                    rs.getString("reason"),
                    rs.getString("admin_name"),
                    rs.getString("admin_uuid"),
                    rs.getTimestamp("date_issued").toLocalDateTime()
                );
                punishment.setId(rs.getInt("id"));
                punishment.setActive(rs.getBoolean("active"));
                return punishment;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get punishment by ID: " + e.getMessage());
        }
        
        return null;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                synchronized (this) {
                    if (connection == null || connection.isClosed()) {
                        setupSQLite();
                        isInitialized = true;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database connection test failed: " + e.getMessage());
            synchronized (this) {
                try {
                    setupSQLite();
                    isInitialized = true;
                } catch (SQLException setupError) {
                    plugin.getLogger().severe("Failed to setup database: " + setupError.getMessage());
                }
            }
        }
        
        return connection;
    }

}
