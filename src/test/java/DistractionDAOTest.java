import group13.demo1.model.DistractionDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class DistractionDAOTest {

    private DistractionDAO dao;


    @BeforeEach
    public void setUp() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        dao = new DistractionDAO(connection);
    }

    @Test
    public void testAddDistraction() {
        boolean result = dao.addDistraction("Test quick log", "Kiernan");
        assertTrue(result, "Distraction should be added successfully");
    }
}
