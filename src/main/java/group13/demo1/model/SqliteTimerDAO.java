package group13.demo1.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class SqliteTimerDAO implements ITimerDAO {
    private final Connection connection;

    public SqliteTimerDAO() {
        connection = SqliteConnection.getInstance();
        createTable();
        createSession();
    }

    //attempt at session
    private void createSession(){
        try (Statement statement = connection.createStatement()){
            // Session table
            String createSessionsTable = """
            CREATE TABLE IF NOT EXISTS sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                startTime TEXT NOT NULL,
                endTime TEXT,
                totalRunSeconds INTEGER,
                totalPauseSeconds INTEGER,
                pauseCount INTEGER
            );
            """;
            statement.execute(createSessionsTable);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            String query = "CREATE TABLE IF NOT EXISTS timers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT NOT NULL," +
                    "label TEXT NOT NULL," +    // Just pause and Reset now
                    "startTime TEXT NOT NULL," +
                    "endTime TEXT NOT NULL," +
                    "totalTime INTEGER NOT NULL" +    // now stores in seconds for clarity
                    ")";
            statement.execute(query);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void addTimer(TimerModel timer) {
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
    public void updateTimer(TimerModel timer) {
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
    public void deleteTimer(TimerModel timer) {
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
    public TimerModel getTimer(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM timers WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                TimerModel t = new TimerModel(
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
    public List<TimerModel> getTimersForUser(String username) {
        List<TimerModel> timers = new ArrayList<>();
        String sql = "SELECT * FROM timers " +
                "WHERE username = ? AND label <> 'Reset' " +
                "ORDER BY startTime DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TimerModel t = new TimerModel(
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
    public List<TimerModel> getAllTimers() {
        List<TimerModel> timers = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM timers");
            while (rs.next()) {
                TimerModel t = new TimerModel(
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
}
