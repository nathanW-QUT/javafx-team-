package group13.demo1.model;
import java.time.LocalDateTime;

public class Distraction {
    private int id;
    private String username;
    private String description;


    public Distraction(int id, String username, String description, LocalDateTime timestamp) {
        this.id = id;
        this.username = username;
        this.description = description;

    }

    public Distraction(String username, String description, LocalDateTime timestamp) {
        this.username = username;
        this.description = description;

    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getDescription() { return description; }


    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setDescription(String description) { this.description = description; }

}
