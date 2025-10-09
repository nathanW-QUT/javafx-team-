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
        Task t = new Task("bob", "Finish report", "Monthly report", LocalDate.now(), 0);
        int id = dao.addTask(t);
        assertTrue(id > 0);

        List<Task> tasks = dao.getTasksForUser("bob");
        assertEquals(1, tasks.size());
        assertEquals("Finish report", tasks.get(0).getTitle());
    }


    @Test
    void testDeleteTask() {
        int id = dao.addTask(new Task("bob", "Do work", "", LocalDate.now(), 0));

        dao.deleteTask(id);
        List<Task> tasks = dao.getTasksForUser("bob");
        assertTrue(tasks.stream().noneMatch(t -> t.getId() == id));
    }
}
