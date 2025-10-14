
import group13.demo1.model.SqliteConnection;
import group13.demo1.model.UserDao;


import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class UserDaoTest {

    private UserDao dao;
    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "testPass";

    @BeforeEach
    void setUp() {
        dao = new UserDao();
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
            stmt.execute("DELETE FROM users");
        } catch (Exception ignored) {}
    }

    @Test
    void testAddUser() {
        assertTrue(dao.addUser(USERNAME, PASSWORD));
        assertTrue(dao.validateLogin(USERNAME, PASSWORD));
    }

    @Test
    void testDuplicateUserFails() {
        dao.addUser(USERNAME, PASSWORD);
        assertFalse(dao.addUser(USERNAME, PASSWORD));
    }

    @Test
    void testValidateWrongPassword() {
        dao.addUser(USERNAME, PASSWORD);
        assertFalse(dao.validateLogin(USERNAME, "wrong"));
    }

    @Test
    void testUpdatePassword() {
        dao.addUser(USERNAME, PASSWORD);
        assertTrue(dao.updatePassword(USERNAME, PASSWORD, "newpass"));
        assertTrue(dao.validateLogin(USERNAME, "newpass"));
    }

    @Test
    void testDeleteAccount() {
        dao.addUser(USERNAME, PASSWORD);
        assertTrue(dao.deleteAccount(USERNAME, PASSWORD));
        assertFalse(dao.validateLogin(USERNAME, PASSWORD));
    }
}
