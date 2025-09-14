package group13.demo1.model;

import java.sql.*;
import java.time.LocalDateTime;

public class DistractionDAO {

    private final Connection connection;

    public DistractionDAO() {
        connection = SqliteConnection.getInstance();
        createTable();
    }


    private void createTable() {
        String query = """
            CREATE TABLE IF NOT EXISTS distraction (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
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

    public boolean addDistraction(String description, String username) {
        String query = "INSERT INTO distraction (description, username) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, description);
            stmt.setString(2, username);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Add distraction failed: " + e.getMessage());
            return false;
        }
    }
}
