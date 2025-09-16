import group13.demo1.dao.SqliteAccomplishmentDAO;
import group13.demo1.model.Accomplishment;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqliteAccomplishmentDAOTest
{
    private Connection connection;
    private SqliteAccomplishmentDAOTest dao;

    @BeforeEach
    void setup() throws Exception
    {
        connection = DriverManager.getConnection("jdbc:sqlite::memory");
        dao = new SqliteAccomplishmentDAOTest(connection);
        dao.createTable();
    }

    @AfterEach
    void tearDown() throws Exception
    {
        connection.close();
    }

    @Test
    void testAddAccomplishment()
    {
        Accomplishment accomplishment = new Accomplishment("Test Task");
        accomplishment.setCompleted(false);

        dao.addAccomplishment("Murray", accomplishment);

        List<Accomplishment> list = dao.getAccomplishmentByUsername("Murray");
        assertEquals(1, list.size());
        assertEquals("Test Task", list.get(0).getDescription());
    }

    @Test
    void testUpdateAccomplishment()
    {
        Accomlishment accomplishment = new Accomplishment("Original");
        dao.addAccomplishment("Murray", accomplishment);

        Accomplishment fetched = dao.getAccomplishmentByUsername("Murray").get(0);
        fetched.setDescription("Updated");
        fetched.setCompleted(true);

        dao.testUpdateAccomplishment(fetched);

        Accomplishment updated = dao.getAccomplishmentById(fetched.getId());
        assertEquals("Updated", updated.getDescription());
        assertTrue(updated.isCompleted());
    }

    @Test
    void testDeleteAccomplishment()
    {
        Accomplishment accomplishment = new Accomplishment("Temp");
        dao.addAccomplishment("Murray", accomplishment);

        Accomplishment fetched = dao.getAccomplishmentByUsername("Murray").get(0);
        dao.deleteAccomplishment(fetched.getId());

        List<Accomplishment> list = dao.getAccomplishmentByUsername("Murray");
        assertTrue(list.isEmpty());
    }
}