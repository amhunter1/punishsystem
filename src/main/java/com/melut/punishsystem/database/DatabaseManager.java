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

    public DatabaseManager(DiscordPunishBot plugin) {
        this.plugin = plugin;
        this.databaseType = plugin.getConfigManager().getString("database.type", "sqlite");
    }

    public void initializeDatabase() {
        try {
            if (databaseType.equalsIgnoreCase("sqlite")) {
                setupSQLite();
            } else if (databaseType.equalsIgnoreCase("mysql")) {
                setupMySQL();
            }

            createTables();
            plugin.getLogger().info("Database connection established successfully!");

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
        }
    }

    private void setupSQLite() throws SQLException {
        File databaseFile = new File(plugin.getDataFolder(), "punishments.db");
        String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("SQLite JDBC driver not found!");
            return;
        }

        connection = DriverManager.getConnection(url);
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
        """.formatted(databaseType.equalsIgnoreCase("sqlite") ? "AUTOINCREMENT" : "AUTO_INCREMENT");

        try (PreparedStatement stmt = connection.prepareStatement(createPunishmentsTable)) {
            stmt.executeUpdate();
        }

        // Yeni ceza türleri için genişletilmiş istatistik tablosu
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
        """.formatted(databaseType.equalsIgnoreCase("sqlite") ? "AUTOINCREMENT" : "AUTO_INCREMENT");

        try (PreparedStatement stmt = connection.prepareStatement(createStatsTable)) {
            stmt.executeUpdate();
        }

        // Yeni sütunları eklemek için ALTER TABLE komutları
        addNewColumnsIfNotExist();
    }

    private void addNewColumnsIfNotExist() throws SQLException {
        // SQLite için sütun kontrolü
        if (databaseType.equalsIgnoreCase("sqlite")) {
            try {
                // Yeni sütunları ekle (eğer yoksa)
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
                        // Sütun zaten varsa hata vermez
                    }
                }
            } catch (Exception ignored) {
                // Tablo yoksa veya başka bir hata varsa görmezden gel
            }
        }
    }

    public void addPunishment(Punishment punishment) {
        String insertPunishment = """
            INSERT INTO punishments (player_name, player_uuid, punishment_type, reason, admin_name, admin_uuid, date_issued)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(insertPunishment, Statement.RETURN_GENERATED_KEYS)) {
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
                    // Fallback (özellikle bazı SQLite sürümleri için)
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

        } catch (SQLException e) {
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

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update player stats: " + e.getMessage());
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

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
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

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
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

    public Connection getConnection() {
        return connection;
    }
}