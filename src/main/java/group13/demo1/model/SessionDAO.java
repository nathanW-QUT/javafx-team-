package group13.demo1.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {
    private final Connection connection;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public SessionDAO(Connection connection) {
        this.connection = connection;
    }

    public void insert(SessionModel s) throws SQLException {
        String sql = "INSERT INTO sessions(username,startTime,endTime,totalRunSeconds,totalPauseSeconds,pauseCount) " +
                "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, s.getUsername());
            preparedStatement.setString(2, s.getStartTime().format(FMT));
            preparedStatement.setString(3, s.getEndTime() == null ? null : s.getEndTime().format(FMT));
            preparedStatement.setLong(4, s.getTotalRunSeconds());
            preparedStatement.setLong(5, s.getTotalPauseSeconds());
            preparedStatement.setInt(6, s.getPauseCount());
            preparedStatement.executeUpdate();
            ResultSet preparedStatementGeneratedKeys = preparedStatement.getGeneratedKeys();
            if (preparedStatementGeneratedKeys.next()) s.setId(preparedStatementGeneratedKeys.getInt(1));
        }
    }

    public void update(SessionModel sessionModel) throws SQLException {
        String sql = "UPDATE sessions SET endTime=?, totalRunSeconds=?, totalPauseSeconds=?, pauseCount=? WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, sessionModel.getEndTime().format(FMT));
            preparedStatement.setLong(2, sessionModel.getTotalRunSeconds());
            preparedStatement.setLong(3, sessionModel.getTotalPauseSeconds());
            preparedStatement.setInt(4, sessionModel.getPauseCount());
            preparedStatement.setInt(5, sessionModel.getId());
            preparedStatement.executeUpdate();
        }
    }

    public List<SessionModel> getAll() throws SQLException {
        List<SessionModel> list = new ArrayList<>();
        String sql = "SELECT * FROM sessions";
        try (Statement connectionStatement = connection.createStatement(); // Bad names, need refactor
             ResultSet resultSet = connectionStatement.executeQuery(sql)) {
            while (resultSet.next()) {
                SessionModel model = new SessionModel(
                        resultSet.getString("username"),
                        LocalDateTime.parse(resultSet.getString("startTime"), FMT));
                model.setId(resultSet.getInt("id"));
                String end = resultSet.getString("endTime");
                if (end != null)
                    model.setEndTime(LocalDateTime.parse(end, FMT));
                model.setTotalRunSeconds(resultSet.getLong("totalRunSeconds"));
                model.setTotalPauseSeconds(resultSet.getLong("totalPauseSeconds"));
                model.setPauseCount(resultSet.getInt("pauseCount"));
                list.add(model);
            }
        }
        return list;
    }
}
