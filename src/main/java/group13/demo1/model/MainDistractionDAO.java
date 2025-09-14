package group13.demo1.model;

import java.sql.*;
import java.time.LocalDateTime;

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
}
