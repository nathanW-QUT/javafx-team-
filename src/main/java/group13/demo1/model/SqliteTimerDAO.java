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
        createSessionTable();
    }

    /**
     * Creates the initial Timer table in the db (first created in development
     */
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

    /**
     * Createing the higher level session (of potentially many timers)
     */
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
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, timer.getLabel());
            preparedStatement.setString(2, timer.getStartTime().toString()); // store as text
            preparedStatement.setString(3, timer.getEndTime().toString());
            preparedStatement.setLong(4, timer.getElapsedSeconds());
            preparedStatement.setInt(5, timer.getId());
            preparedStatement.executeUpdate();
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
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM timers WHERE id=?");
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                TimerRecord timerRecord = new TimerRecord(
                        resultSet.getString("username"),
                        resultSet.getString("label"),
                        LocalDateTime.parse(resultSet.getString("startTime")),
                        LocalDateTime.parse(resultSet.getString("endTime")),
                        resultSet.getLong("totalTime")
                );
                timerRecord.setId(id);
                return timerRecord;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This function selects the timer recorded in the db, by username
     * for the purposes of displaying that information on page
     * @param username for the currently logged user
     * @return timers
     */
    @Override
    public List<TimerRecord> getTimersForUser(String username) {
        List<TimerRecord> timers = new ArrayList<>();
        String sql = "SELECT * FROM timers " +
                "WHERE username = ? AND label <> 'Reset' " +
                "ORDER BY startTime DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    TimerRecord timerRecord = new TimerRecord(
                            resultSet.getString("username"),
                            resultSet.getString("label"),
                            LocalDateTime.parse(resultSet.getString("startTime")),
                            LocalDateTime.parse(resultSet.getString("endTime")),
                            resultSet.getLong("totalTime")
                    );
                    timerRecord.setId(resultSet.getInt("id"));
                    timers.add(timerRecord);
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
}
