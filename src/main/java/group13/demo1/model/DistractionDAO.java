package group13.demo1.model;

import java.sql.*;
import java.time.LocalDateTime;

public class DistractionDAO {

    private final Connection connection;

    public DistractionDAO() {
        this(SqliteConnection.getInstance());
    }

    public DistractionDAO(Connection connection) {
        this.connection = connection;
        createQuickLogTable();
    }


    /**
     * Creates the quick log distraction table if the table does not yet exist in the database
     */
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

    /**
     * Adds a new distraction to the quick log database
     * @param description adds a quick description to the quick logged distraction
     * @param username adds the username of the session user to the database
     * @return returns true if the insert succeeds, elsewise false
     */
    public boolean addDistraction(String description, String username) {
        String query = "INSERT INTO distraction (description, username, timestamp) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, description );
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, LocalDateTime.now().toString());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Add distraction failed: " + e.getMessage());
            return false;
        }
    }
}