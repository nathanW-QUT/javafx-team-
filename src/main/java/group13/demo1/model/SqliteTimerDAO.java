package group13.demo1.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.temporal.ChronoUnit;

public class SqliteTimerDAO implements ITimerDAO {
    private final Connection connection;

    public SqliteTimerDAO() {
        connection = SqliteConnection.getInstance();
        createTable();
        createSessionTable();
    }


    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            String query = "CREATE TABLE IF NOT EXISTS timers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT NOT NULL," +
                    "label TEXT NOT NULL," +    // Just pause and Reset now
                    "startTime TEXT NOT NULL," +
                    "endTime TEXT NOT NULL," +
                    "totalTime INTEGER NOT NULL" +    // now stores in seconds instead of milli for clarity
                    ")";
            statement.execute(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void createSessionTable() {
        try (Statement statement = connection.createStatement()) {
            String query = "CREATE TABLE IF NOT EXISTS sessions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT NOT NULL," +
                    "startTime TEXT NOT NULL," +
                    "endTime TEXT," +
                    "totalRunSeconds INTEGER," +
                    "totalPauseSeconds INTEGER,"+
                    "pauseCount INTEGER"+
                    ")";
            statement.execute(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void addTimer(TimerRecord timer) {
        try {
            String query = "INSERT INTO timers (username, label, startTime, endTime, totalTime) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, timer.getUsername());
            ps.setString(2, timer.getLabel());
            ps.setString(3, timer.getStartTime().toString());
            ps.setString(4, timer.getEndTime().toString());
            ps.setLong(5, timer.getElapsedSeconds());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                timer.setId(keys.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTimer(TimerRecord timer) {
        try {
            String query = "UPDATE timers SET label=?, startTime=?, endTime=?, totalTime=? WHERE id=?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, timer.getLabel());
            ps.setString(2, timer.getStartTime().toString()); // store as text
            ps.setString(3, timer.getEndTime().toString());
            ps.setLong(4, timer.getElapsedSeconds());
            ps.setInt(5, timer.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void deleteTimer(TimerRecord timer) {
        try {
            String query = "DELETE FROM timers WHERE id=?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, timer.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public TimerRecord getTimer(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM timers WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                TimerRecord t = new TimerRecord(
                        rs.getString("username"),
                        rs.getString("label"),
                        LocalDateTime.parse(rs.getString("startTime")),
                        LocalDateTime.parse(rs.getString("endTime")),
                        rs.getLong("totalTime")
                );
                t.setId(id);
                return t;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public List<TimerRecord> getTimersForUser(String username) {
        List<TimerRecord> timers = new ArrayList<>();
        String sql = "SELECT * FROM timers " +
                "WHERE username = ? AND label <> 'Reset' " +
                "ORDER BY startTime DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    TimerRecord t = new TimerRecord(
                            rs.getString("username"),
                            rs.getString("label"),
                            LocalDateTime.parse(rs.getString("startTime")),
                            LocalDateTime.parse(rs.getString("endTime")),
                            rs.getLong("totalTime")
                    );
                    t.setId(rs.getInt("id"));
                    timers.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return timers;
    }




    @Override
    public List<TimerRecord> getAllTimers() {
        List<TimerRecord> timers = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM timers");
            while (rs.next()) {
                TimerRecord t = new TimerRecord(
                        rs.getString("username"),
                        rs.getString("label"),
                        LocalDateTime.parse(rs.getString("startTime")),
                        LocalDateTime.parse(rs.getString("endTime")),
                        rs.getLong("totalTime")
                );
                t.setId(rs.getInt("id"));
                timers.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timers;
    }

    public Map<String, Long> getTagCountsForUser(String username) {
        Map<String, Long> map = new LinkedHashMap<>();
        String sql = "SELECT label, COUNT(*) AS cnt " +
                "FROM timers WHERE username=? AND label <> 'Reset' " +
                "GROUP BY label ORDER BY cnt DESC";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, username);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("label"), rs.getLong("cnt"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    public Map<String, Long> getDailyDistractionCounts(String username) {
        Map<String, Long> map = new LinkedHashMap<>();
        String sql = "SELECT substr(startTime,1,10) AS day, COUNT(*) AS cnt " +
                "FROM timers WHERE username=? AND label <> 'Reset' " +
                "GROUP BY day ORDER BY day";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, username);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {

                    map.put(rs.getString("day"), rs.getLong("cnt"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }


    public Map<String, Long> getHourlyDistractionCounts(String username, int lastNHours) {
        Map<String, Long> map = new LinkedHashMap<>();

        int hours = Math.max(1, lastNHours);
        LocalDateTime nowHour = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        LocalDateTime start   = nowHour.minusHours(hours - 1);


        String sql =
                "SELECT replace(substr(startTime,1,13),'T',' ') || ':00' AS hour_bucket, COUNT(*) AS cnt " +
                        "FROM timers " +
                        "WHERE username=? AND label <> 'Reset' AND startTime >= ? " +
                        "GROUP BY hour_bucket " +
                        "ORDER BY hour_bucket";

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, username);
            st.setString(2, start.toString());
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("hour_bucket"), rs.getLong("cnt"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }


}