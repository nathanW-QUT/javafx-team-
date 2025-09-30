package group13.demo1.model;

import java.sql.*;

public class DistractionDAO {

    private final Connection connection;

    public DistractionDAO() {
        connection = SqliteConnection.getInstance();
        createQuickLogTable();
    }


    private void createQuickLogTable() {
        String query = """
            CREATE TABLE IF NOT EXISTS distraction (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                description TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;
        try (Statement createdStatement = connection.createStatement()) {
            createdStatement.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addDistraction(String description, String username) {
        String query = "INSERT INTO distraction (description, username) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, description);
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Add distraction failed: " + e.getMessage());
            return false;
        }
    }
}