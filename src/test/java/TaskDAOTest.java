import group13.demo1.model.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskDAOTest {

    private TaskDAO dao;

    @BeforeEach
    void setUp() {
        dao = new TaskDAO();
    }

    @Test
    void testAddAndGetTasks() {
        String username = "bob";
        String title = "Finish report";
        String description = "Monthly report";
        LocalDate dueDate = LocalDate.now();

        Task t = new Task(username, title, description, dueDate, 0);
        int id = dao.addTask(t);
        assertTrue(id > 0);

        List<Task> tasks = dao.getTasksForUser(username);

        boolean taskExists = tasks.stream()
                .anyMatch(task -> task.getId() == id &&
                        task.getTitle().equals(title) &&
                        task.getDescription().equals(description) &&
                        task.getDueDate().equals(dueDate));

        assertTrue(taskExists);
    }


    @Test
    void testDeleteTask() {
        int id = dao.addTask(new Task("bob", "Do work", "", LocalDate.now(), 0));

        dao.deleteTask(id);
        List<Task> tasks = dao.getTasksForUser("bob");
        assertTrue(tasks.stream().noneMatch(t -> t.getId() == id));
    }
}
