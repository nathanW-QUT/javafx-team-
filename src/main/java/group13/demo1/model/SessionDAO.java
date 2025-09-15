package group13.demo1.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class SessionDAO {
    private Connection connection;
    private static DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public SessionDAO(Connection connection) { this.connection = connection; }

    public void insert(SessionModel s) throws SQLException {
        String sql = "INSERT INTO sessions(username,startTime,endTime,totalRunSeconds,totalPauseSeconds,pauseCount) "
                + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, s.getUsername());
            statement.setString(2, s.getStartTime().format(FMT));
            statement.setString(3, s.getEndTime() == null ? null : s.getEndTime().format(FMT));
            statement.setLong(4, s.getTotalRunSeconds());
            statement.setLong(5, s.getTotalPauseSeconds());
            statement.setInt(6, s.getPauseCount());
            statement.executeUpdate();

            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) s.setId(rs.getInt(1));
        }
    }

    public void update(SessionModel s) throws SQLException {
        String sql = "UPDATE sessions SET endTime=?, totalRunSeconds=?, totalPauseSeconds=?, pauseCount=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, s.getEndTime().format(FMT));
            statement.setLong(2, s.getTotalRunSeconds());
            statement.setLong(3, s.getTotalPauseSeconds());
            statement.setInt(4, s.getPauseCount());
            statement.setInt(5, s.getId());
            statement.executeUpdate();
        }
    }

    public List<SessionModel> getAll() throws SQLException {
        List<SessionModel> list = new ArrayList<>();
        String sql = "SELECT * FROM sessions";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                SessionModel s = new SessionModel(
                        rs.getString("username"),
                        LocalDateTime.parse(rs.getString("startTime"), FMT));
                s.setId(rs.getInt("id"));
                String end = rs.getString("endTime");
                if (end != null) s.setEndTime(LocalDateTime.parse(end, FMT));
                s.setTotalRunSeconds(rs.getLong("totalRunSeconds"));
                s.setTotalPauseSeconds(rs.getLong("totalPauseSeconds"));
                s.setPauseCount(rs.getInt("pauseCount"));
                list.add(s);
            }
        }
        return list;
    }
}

