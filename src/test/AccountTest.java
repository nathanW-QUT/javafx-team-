
import group13.demo1.model.UserDao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;



public class AccountTest {

    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "testPass";
    private static final String WRONG_PASSWORD = "wrongPass";
    private static final String NEW_PASSWORD = "newPass";
    private static final String NON_EXISTENT = "ghost";

    private UserDao userDao;

    @BeforeEach
    public void setUp() {
        userDao = new UserDao();
    }

    @Test
    public void testAddUser() {
        assertTrue(userDao.addUser(USERNAME, PASSWORD));
        assertTrue(userDao.validateLogin(USERNAME, PASSWORD));
    }

    @Test
    public void testRemoveUser() {
        userDao.addUser(USERNAME, PASSWORD);
        assertTrue(userDao.deleteAccount(USERNAME, PASSWORD));
        assertFalse(userDao.validateLogin(USERNAME, PASSWORD));
    }

    @Test
    public void testLoginValid() {
        userDao.addUser(USERNAME, PASSWORD);
        assertTrue(userDao.validateLogin(USERNAME, PASSWORD));
    }

    @Test
    public void testLoginInvalidPassword() {
        userDao.addUser(USERNAME, PASSWORD);
        assertFalse(userDao.validateLogin(USERNAME, WRONG_PASSWORD));
    }

    @Test
    public void testLoginUserDoesNotExist() {
        assertFalse(userDao.validateLogin(NON_EXISTENT, PASSWORD));
    }

    @Test
    public void testAddUserThatExists() {
        userDao.addUser(USERNAME, PASSWORD);
        assertFalse(userDao.addUser(USERNAME, PASSWORD));
    }

    @Test
    public void testRemoveUserThatDoesNotExist() {
        assertFalse(userDao.deleteAccount(NON_EXISTENT, NON_EXISTENT));
    }

    @Test
    public void testEditUser() {
        userDao.addUser(USERNAME, PASSWORD);
        assertTrue(userDao.updatePassword(USERNAME, PASSWORD, NEW_PASSWORD));
        assertTrue(userDao.validateLogin(USERNAME, NEW_PASSWORD));
    }

    @Test
    public void testEditUserThatDoesNotExist() {
        assertFalse(userDao.updatePassword(NON_EXISTENT, NON_EXISTENT, NEW_PASSWORD));
    }
}
