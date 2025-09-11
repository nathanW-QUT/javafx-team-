package group13.demo1.model;

import java.sql.*;

public class DistractionDAO {

    private final Connection connection;

    public DistractionDAO() {
        connection = SqliteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        String query = """
            CREATE TABLE IF NOT EXISTS distraction (
                id INTEGER PRIMARY KEY AUTOINCREMENT
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addDistraction() {
        String query = "INSERT INTO distraction DEFAULT VALUES";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Add distraction failed: " + e.getMessage());
            return false;
        }
    }
}
