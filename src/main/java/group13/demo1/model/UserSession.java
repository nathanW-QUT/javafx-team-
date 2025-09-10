package group13.demo1.model;

public class UserSession {

    private static UserSession instance;

    private String username;

    private UserSession(String username) {
        this.username = username;
    }

    public static void createSession(String username) {
        instance = new UserSession(username);
    }

    public static UserSession getInstance() {
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public static void clearSession() {
        instance = null;
    }
}



