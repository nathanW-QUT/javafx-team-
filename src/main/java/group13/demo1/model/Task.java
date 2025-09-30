package group13.demo1.model;

import java.time.LocalDate;

public class Task {
    private int id;
    private String username;
    private String title;
    private String description;
    private LocalDate dueDate;
    private int completion;

    public Task(int id, String username, String title, String description, LocalDate dueDate, int completion) {
        this.id = id;
        this.username = username;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.completion = completion;
    }

    public Task(String username, String title, String description, LocalDate dueDate, int completion) {
        this.username = username;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.completion = completion;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public int getCompletion() { return completion; }

    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setCompletion(int completion) { this.completion = completion; }
}

