package group13.demo1.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class MainDistractionDAO {

    private final Connection connection;

    public MainDistractionDAO(Connection connection) {
        this.connection = connection;
        createMainDistractionTable();
    }

    /**
     * Creates the main distraction table if the table does not yet exist in the database
     */

    private void createMainDistractionTable() {
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

    /**
     * Adds an entry into the distraction table for the user
     *
     * @param username owners username of the unique session
     * @param cause the cause of the distraction
     * @param minutes how many minutes the distraction was
     * @param description a further description of the distraction
     */

    public void addMainDistraction(String username, String cause, int minutes, String description) {
        String sql = "INSERT INTO maindistraction (username, cause, minutes, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, cause);
            preparedStatement.setInt(3, minutes);
            preparedStatement.setString(4, description);
            preparedStatement.setString(5, LocalDateTime.now().toString());
            preparedStatement.executeUpdate();
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


        /**
         * Creates a new main item
         * @param id a unique database id
         * @param cause the cause of the distraction
         * @param minutes how many minutes the distraction was
         * @param description a further description of the distraction
         * @param timestamp the exact time that the distraction was logged
         */
        public MainItem(int id, String cause, int minutes, String description, String timestamp) {
            this.id = id;
            this.cause = cause;
            this.minutes = minutes;
            this.description = description;
            this.timestamp = timestamp;
        }

    }

    /**
     * This function returns the four most recent distractions logged by the user
     * @param username the username filter
     * @param limit the maximum number of allowed records to be returned
     * @return a list of the four most recent distractions from newest to oldest
     */
    public List<MainItem> getRecentForUser(String username, int limit) {

        final String sql =
                "SELECT id, cause, minutes, description, timestamp " +
                        "FROM maindistraction WHERE username = ? " +
                        "ORDER BY datetime(timestamp) DESC LIMIT ?";

        List<MainItem> rows = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setInt(2, limit);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(new MainItem(
                            resultSet.getInt("id"),
                            resultSet.getString("cause"),
                            resultSet.getInt("minutes"),
                            resultSet.getString("description"),
                            resultSet.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Load failed: " + e.getMessage());
        }
        return rows;
    }



}