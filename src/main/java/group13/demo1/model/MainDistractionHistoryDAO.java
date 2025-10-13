package group13.demo1.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only DAO for History: returns all main distractions for a user,
 * newest first. Uses existing "maindistraction" table and the
 * MainDistractionDAO.MainItem DTO.
 */
public class MainDistractionHistoryDAO {
    private final Connection connection;

    public MainDistractionHistoryDAO() {
        this(SqliteConnection.getInstance());
    }
    public MainDistractionHistoryDAO(Connection connection) {
        this.connection = connection;
    }

    public List<MainDistractionDAO.MainItem> getAllForUser(String username) {
        List<MainDistractionDAO.MainItem> rows = new ArrayList<>();
        final String sql =
                "SELECT id, cause, minutes, description, timestamp " +
                        "FROM maindistraction WHERE username=? " +
                        "ORDER BY datetime(timestamp) DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new MainDistractionDAO.MainItem(
                            rs.getInt("id"),
                            rs.getString("cause"),
                            rs.getInt("minutes"),
                            rs.getString("description"),
                            rs.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }
}

