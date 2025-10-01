package group13.demo1.model;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqliteAccomplishmentDAOTest {

    private SqliteAccomplishmentDAO dao;

    @BeforeEach
    void setUp() {
        dao = new SqliteAccomplishmentDAO();
        clearTable();
    }

    @AfterEach
    void tearDown() {
        clearTable();
    }

    private void clearTable() {
        try {
            Connection conn = SqliteConnection.getInstance();
            Statement stmt = conn.createStatement();
            stmt.execute("DELETE FROM accomplishment");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testAddAndGetAccomplishment() {
        Accomplishment a = new Accomplishment(0, "testuser", "Test Label", false);
        dao.addAccomplishment(a);

        List<Accomplishment> all = dao.getAccomplishments();
        assertEquals(1, all.size());
        assertEquals("testuser", all.get(0).getUsername());
        assertEquals("Test Label", all.get(0).getLabel());
        assertFalse(all.get(0).isCompleted());
    }

    @Test
    void testUpdateAccomplishment() {
        Accomplishment a = new Accomplishment(0, "user1", "Initial Label", false);
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
    void testDeleteAccomplishment() {
        Accomplishment a = new Accomplishment(0, "user1", "To Delete", false);
        dao.addAccomplishment(a);

        Accomplishment saved = dao.getAccomplishments().get(0);
        dao.deleteAccomplishment(saved);

        Accomplishment deleted = dao.getAccomplishmentById(saved.getId());
        assertNull(deleted);
        assertTrue(dao.getAccomplishments().isEmpty());
    }

    @Test
    void testGetAccomplishmentByIdNotExists() {
        Accomplishment result = dao.getAccomplishmentById(999);
        assertNull(result);
    }

    @Test
    void testGetAccomplishmentsByUsername() {
        dao.addAccomplishment(new Accomplishment(0, "userA", "Label1", false));
        dao.addAccomplishment(new Accomplishment(0, "userA", "Label2", true));
        dao.addAccomplishment(new Accomplishment(0, "userB", "Other", false));

        List<Accomplishment> listA = dao.getAccomplishmentsByUsername("userA");
        assertEquals(2, listA.size());

        List<Accomplishment> listB = dao.getAccomplishmentsByUsername("userB");
        assertEquals(1, listB.size());

        List<Accomplishment> listNone = dao.getAccomplishmentsByUsername("doesNotExist");
        assertTrue(listNone.isEmpty());
    }
}
