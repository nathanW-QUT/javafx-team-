package group13.demo1.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {
    private final Connection connection;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public SessionDAO(Connection conn) {
        this.connection = conn;
    }

    public void insert(SessionModel s) throws SQLException {
        String sql = "INSERT INTO sessions(username,startTime,endTime,totalRunSeconds,totalPauseSeconds,pauseCount) " +
                "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement st = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, s.getUsername());
            st.setString(2, s.getStartTime().format(FMT));
            st.setString(3, s.getEndTime() == null ? null : s.getEndTime().format(FMT));
            st.setLong(4, s.getTotalRunSeconds());
            st.setLong(5, s.getTotalPauseSeconds());
            st.setInt(6, s.getPauseCount());
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) s.setId(rs.getInt(1));
        }
    }

    public void update(SessionModel s) throws SQLException {
        String sql = "UPDATE sessions SET endTime=?, totalRunSeconds=?, totalPauseSeconds=?, pauseCount=? WHERE id=?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, s.getEndTime().format(FMT));
            st.setLong(2, s.getTotalRunSeconds());
            st.setLong(3, s.getTotalPauseSeconds());
            st.setInt(4, s.getPauseCount());
            st.setInt(5, s.getId());
            st.executeUpdate();
        }
    }

    public List<SessionModel> getAll() throws SQLException {
        List<SessionModel> list = new ArrayList<>();
        String sql = "SELECT * FROM sessions";
        try (Statement a = connection.createStatement(); // Bad names, need refactor
             ResultSet rs = a.executeQuery(sql)) {
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
