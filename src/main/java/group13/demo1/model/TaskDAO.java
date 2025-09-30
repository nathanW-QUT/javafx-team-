package group13.demo1.model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private final Connection connection;

    public TaskDAO() {
        connection = SqliteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT NOT NULL," +
                    "title TEXT NOT NULL," +
                    "description TEXT," +
                    "due_date TEXT," +
                    "completion INTEGER DEFAULT 0)");
            stmt.execute("CREATE TABLE IF NOT EXISTS subtasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "task_id INTEGER NOT NULL," +
                    "name TEXT NOT NULL," +
                    "completed INTEGER DEFAULT 0," +
                    "FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int addTask(Task task) {
        String sql = "INSERT INTO tasks (username, title, description, due_date, completion) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, task.getUsername());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setString(4, task.getDueDate() != null ? task.getDueDate().toString() : null);
            ps.setInt(5, task.getCompletion());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Task> getTasksForUser(String username) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String dueDateStr = rs.getString("due_date");
                LocalDate dueDate = (dueDateStr != null) ? LocalDate.parse(dueDateStr) : null;

                Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("title"),
                        rs.getString("description"),
                        dueDate,
                        rs.getInt("completion")
                );
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public void updateTaskCompletion(int taskId, int completion) {
        String sql = "UPDATE tasks SET completion = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, completion);
            ps.setInt(2, taskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTask(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addSubtasks(int taskId, List<String> subtasks) {
        String sql = "INSERT INTO subtasks (task_id, name, completed) VALUES (?, ?, 0)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (String subtask : subtasks) {
                pstmt.setInt(1, taskId);
                pstmt.setString(2, subtask);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getSubtasks(int taskId) {
        List<String> subtasks = new ArrayList<>();
        String sql = "SELECT name FROM subtasks WHERE task_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                subtasks.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subtasks;
    }

    public boolean isSubtaskCompleted(int taskId, String name) {
        String sql = "SELECT completed FROM subtasks WHERE task_id = ? AND name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.setString(2, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("completed") == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateSubtaskStatus(int taskId, String name, boolean completed) {
        String sql = "UPDATE subtasks SET completed = ? WHERE task_id = ? AND name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, completed ? 1 : 0);
            pstmt.setInt(2, taskId);
            pstmt.setString(3, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
