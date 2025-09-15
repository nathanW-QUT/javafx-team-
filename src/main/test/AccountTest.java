import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import group13.demo1.controller.*;
import group13.demo1.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.IOException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountTest {

    private Login loginController;
    private RegisterController registerController;
    private DeleteAccountController deleteController;

    private UserDao userDao;

    private final String TEST_USERNAME = "testuser";
    private final String TEST_PASSWORD = "password123";
    private final String WRONG_PASSWORD = "wrongpass";

    @BeforeAll
    public void setupDatabase() throws SQLException {
        Connection conn = SqliteConnection.getInstance();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS users");
        }
        userDao = new UserDao();
        loginController = new Login();
        loginController.userDao = userDao;

        registerController = new RegisterController();
        registerController.userDao = userDao;

        deleteController = new DeleteAccountController();
        deleteController.userDao = userDao;
    }

    @BeforeEach
    public void cleanDatabase() throws SQLException {
        Connection conn = SqliteConnection.getInstance();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM users");
        }
    }

    @Test
    public void testRegisterAccountSuccess() {
        boolean created = registerController.userDao.addUser(TEST_USERNAME, TEST_PASSWORD);
        assertTrue(created);
    }

    @Test
    public void testRegisterAccountFailsIfExists() {
        registerController.userDao.addUser(TEST_USERNAME, TEST_PASSWORD);
        boolean createdAgain = registerController.userDao.addUser(TEST_USERNAME, TEST_PASSWORD);
        assertFalse(createdAgain);
    }

    @Test
    public void testLoginWithValidCredentials() {
        registerController.userDao.addUser(TEST_USERNAME, TEST_PASSWORD);
        boolean validLogin = loginController.userDao.validateLogin(TEST_USERNAME, TEST_PASSWORD);
        assertTrue(validLogin);
    }

    @Test
    public void testLoginWithInvalidCredentials() {
        registerController.userDao.addUser(TEST_USERNAME, TEST_PASSWORD);
        boolean invalidLogin = loginController.userDao.validateLogin(TEST_USERNAME, WRONG_PASSWORD);
        assertFalse(invalidLogin);
    }

    @Test
    public void testLoginWithNonExistentUser() {
        boolean result = loginController.userDao.validateLogin("nonexistent", "anyPassword");
        assertFalse(result);
    }

    @Test
    public void testDeleteExistingAccountSuccess() throws SQLException {
        registerController.userDao.addUser(TEST_USERNAME, TEST_PASSWORD);
        boolean canDelete = deleteController.userDao.validateLogin(TEST_USERNAME, TEST_PASSWORD);
        assertTrue(canDelete);

        String query = "DELETE FROM users WHERE username = '" + TEST_USERNAME + "'";
        SqliteConnection.getInstance().createStatement().executeUpdate(query);

        boolean stillExists = deleteController.userDao.validateLogin(TEST_USERNAME, TEST_PASSWORD);
        assertFalse(stillExists);
    }

    @Test
    public void testDeleteNonExistentAccountFails() {
        boolean canDelete = deleteController.userDao.validateLogin(TEST_USERNAME, TEST_PASSWORD);
        assertFalse(canDelete);
    }

    @Test
    public void testDeleteWithWrongPasswordFails() {
        registerController.userDao.addUser(TEST_USERNAME, TEST_PASSWORD);
        boolean canDelete = deleteController.userDao.validateLogin(TEST_USERNAME, WRONG_PASSWORD);
        assertFalse(canDelete);
    }




}
