package group13.demo1.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MainDistractionDAO {

    private final Connection connection;

    public MainDistractionDAO(Connection connection) {
        this.connection = connection;
        createTable();
    }


    private void createTable() {
        String query = """
            CREATE TABLE IF NOT EXISTS maindistraction (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                cause TEXT NOT NULL,
                minutes INTEGER NOT NULL,
                description TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addMainDistraction(String username, String cause, int minutes, String description) {
        String sql = "INSERT INTO maindistraction (username, cause, minutes, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, cause);
            ps.setInt(3, minutes);
            ps.setString(4, description);
            ps.executeUpdate();
            System.out.println("Main distraction logged for " + username);
        } catch (SQLException e) {
            System.out.println("Add main distraction failed: " + e.getMessage());
        }
    }

    public static class MainItem {
        public final int id;
        public final String cause;
        public final int minutes;
        public final String description;
        public final String timestamp;



        public MainItem(int id, String cause, int minutes, String description, String timestamp) {
            this.id = id;
            this.cause = cause;
            this.minutes = minutes;
            this.description = description;
            this.timestamp = timestamp;
        }

    }

    public List<MainItem> getRecentForUser(String username, int limit) {

        final String sql =
                "SELECT id, cause, minutes, description, timestamp " +
                        "FROM maindistraction WHERE username = ? " +
                        "ORDER BY datetime(timestamp) DESC LIMIT ?";

        List<MainItem> rows = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new MainItem(
                            rs.getInt("id"),
                            rs.getString("cause"),
                            rs.getInt("minutes"),
                            rs.getString("description"),
                            rs.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Load failed: " + e.getMessage());
        }
        return rows;
    }



}