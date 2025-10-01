import group13.demo1.model.MainDistractionDAO;
import group13.demo1.model.SqliteConnection;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqliteMainDistractionDAOTest {

    private MainDistractionDAO dao;

    @BeforeEach
    void setUp() {
        dao = new MainDistractionDAO(SqliteConnection.getInstance());
        clearTable();
    }

    @AfterEach
    void tearDown() {
        clearTable();
    }

    private void clearTable() {
        try {
            Connection conn = SqliteConnection.getInstance();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM maindistraction");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to clear maindistraction table: " + e.getMessage());
        }
    }

    @Test
    void testEmptyResultWhenNoRows() {
        List<MainDistractionDAO.MainItem> list = dao.getRecentForUser("Nobody", 4);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void testFilteredByUsername() {
        dao.addMainDistraction("Bruce", "Ate a Snack", 5, "Ate some salt and vinegar chips");
        dao.addMainDistraction("Clark", "Scrolled on Phone", 10, "Looked at cat videos on Tik Tok");
        dao.addMainDistraction("Bruce", "Daydreamed", 3, "Thought about my day tomorrow");

        List<MainDistractionDAO.MainItem> john = dao.getRecentForUser("Bruce", 10);
        List<MainDistractionDAO.MainItem> mary = dao.getRecentForUser("Clark", 10);
        List<MainDistractionDAO.MainItem> none = dao.getRecentForUser("NoSuchUser", 10);

        assertEquals(2, john.size());
        assertEquals(1, mary.size());
        assertTrue(none.isEmpty());
    }

    @Test
    void testNullDescriptionHandled() {
        dao.addMainDistraction("NullDesc", "Other", 12, null);
        List<MainDistractionDAO.MainItem> list = dao.getRecentForUser("NullDesc", 1);
        assertEquals(1, list.size());
        assertNull(list.get(0).description);
        assertEquals("Other", list.get(0).cause);
        assertEquals(12, list.get(0).minutes);
    }

    @Test
    void testAddAndGetRecentForUser() throws InterruptedException {
        String user = "Kiernan";
        dao.addMainDistraction(user, "Scrolled on phone", 7, "Looked at cat videos on Tik Tok");
        Thread.sleep(1000);
        dao.addMainDistraction(user, "Ate a Snack", 3, "Ate some salt and vinegar chips");
        Thread.sleep(1000);
        dao.addMainDistraction(user, "Daydreamed", 5, "Thought about my day tomorrow");

        List<MainDistractionDAO.MainItem> all = dao.getRecentForUser(user, 10);
        assertEquals(3, all.size());
        assertEquals("Daydreamed", all.get(0).cause);
        assertEquals(5, all.get(0).minutes);
        assertEquals("Ate a Snack", all.get(1).cause);
        assertEquals("Scrolled on phone", all.get(2).cause);
    }

    @Test
    void testLimitToFourMostRecent() throws InterruptedException {
        String user = "Bruce";

        dao.addMainDistraction(user, "A1", 1, null);
        Thread.sleep(1000);
        dao.addMainDistraction(user, "A2", 2, null);
        Thread.sleep(1000);
        dao.addMainDistraction(user, "A3", 3, null);
        Thread.sleep(1000);
        dao.addMainDistraction(user, "A4", 4, null);
        Thread.sleep(1000);
        dao.addMainDistraction(user, "A5", 5, null);

        List<MainDistractionDAO.MainItem> last4 = dao.getRecentForUser(user, 4);
        assertEquals(4, last4.size());
        assertEquals("A5", last4.get(0).cause);
        assertEquals("A4", last4.get(1).cause);
        assertEquals("A3", last4.get(2).cause);
        assertEquals("A2", last4.get(3).cause);
    }
}