package group13.demo1.model;

import java.time.LocalDateTime;

public class TimerModel {
    private int id;
    private String username;
    private String label; // "Pause' or 'Reset'
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long totalTime; // stored as seconds now for ease

    public TimerModel(String username, String label, LocalDateTime startTime, LocalDateTime endTime, long totalTime) {
        this.username = username;
        this.label = label;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalTime = totalTime;
    }


    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getLabel() { return label; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public long getElapsedSeconds() { return totalTime; }


    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setLabel(String label) { this.label = label; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public void setElapsedSeconds(long totalTime) { this.totalTime = totalTime; }


    /**
     * This is used to show data in the terminal and can be used in debugs
      * @return string
     */
    @Override
    public String toString() {
        return "TimerRecord{" +
                "id:" + id +
                ", username:" + username +
                ", label:" + label +
                ", startTime:" + startTime +
                ", endTime:" + endTime +
                ", elapsedSeconds:" + totalTime +
                '}';
    }
}

