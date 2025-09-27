import group13.demo1.model.Accomplishment;
import group13.demo1.model.SqliteAccomplishmentDAO;
import group13.demo1.model.SqliteConnection;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqliteAccomplishmentDAOTest
{

    private SqliteAccomplishmentDAO dao;

    @BeforeEach
    void setUp()
    {
        dao = new SqliteAccomplishmentDAO();
        clearTable();
    }

    @AfterEach
    void tearDown()
    {
        clearTable();
    }

    private void clearTable()
    {
        try
        {
            Connection conn = SqliteConnection.getInstance();
            Statement stmt = conn.createStatement();
            stmt.execute("DELETE FROM accomplishment");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    void testAddAndGetAccomplishment()
    {
        Accomplishment a = new Accomplishment(0, "Murray", "Test", false);
        dao.addAccomplishment(a);

        List<Accomplishment> all = dao.getAccomplishments();
        assertEquals(1, all.size());
        assertEquals("Murray", all.get(0).getUsername());
        assertEquals("Test", all.get(0).getLabel());
        assertFalse(all.get(0).isCompleted());
    }

    @Test
    void testUpdateAccomplishment()
    {
        Accomplishment a = new Accomplishment(0, "Murray", "Original Label", false);
        dao.addAccomplishment(a);

        Accomplishment saved = dao.getAccomplishments().get(0);
        saved.setLabel("Updated Label");
        saved.setCompleted(true);

        dao.updateAccomplishment(saved);

        Accomplishment updated = dao.getAccomplishmentById(saved.getId());
        assertNotNull(updated);
        assertEquals("Updated Label", updated.getLabel());
        assertTrue(updated.isCompleted());
    }

    @Test
    void testDeleteAccomplishment()
    {
        Accomplishment a = new Accomplishment(0, "Murray", "To Delete", false);
        dao.addAccomplishment(a);

        Accomplishment saved = dao.getAccomplishments().get(0);
        dao.deleteAccomplishment(saved);

        Accomplishment deleted = dao.getAccomplishmentById(saved.getId());
        assertNull(deleted);
        assertTrue(dao.getAccomplishments().isEmpty());
    }

    @Test
    void testGetAccomplishmentByIdNotExists()
    {
        Accomplishment result = dao.getAccomplishmentById(999);
        assertNull(result);
    }

    @Test
    void testGetAccomplishmentsByUsername()
    {
        dao.addAccomplishment(new Accomplishment(0, "John", "Example", false));
        dao.addAccomplishment(new Accomplishment(0, "Mary", "Example 2", true));
        dao.addAccomplishment(new Accomplishment(0, "Jane", "Example 3", false));

        List<Accomplishment> listA = dao.getAccomplishmentsByUsername("John");
        assertEquals(1, listA.size());

        List<Accomplishment> listB = dao.getAccomplishmentsByUsername("Mary");
        assertEquals(1, listB.size());

        List<Accomplishment> listNone = dao.getAccomplishmentsByUsername("doesNotExist");
        assertTrue(listNone.isEmpty());
    }
}