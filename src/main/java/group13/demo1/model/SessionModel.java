package group13.demo1.model;

import java.time.LocalDateTime;

public class SessionModel {
    private int id;
    private String username;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long totalRunSeconds;
    private long totalPauseSeconds;
    private int pauseCount;

    public SessionModel(String username, LocalDateTime startTime) {
        this.username = username;
        this.startTime = startTime;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public long getTotalRunSeconds() { return totalRunSeconds; }
    public long getTotalPauseSeconds() { return totalPauseSeconds; }
    public int getPauseCount() { return pauseCount; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public void setTotalRunSeconds(long totalRunSeconds) { this.totalRunSeconds = totalRunSeconds; }
    public void setTotalPauseSeconds(long totalPauseSeconds) { this.totalPauseSeconds = totalPauseSeconds; }
    public void setPauseCount(int pauseCount) { this.pauseCount = pauseCount; }

}
