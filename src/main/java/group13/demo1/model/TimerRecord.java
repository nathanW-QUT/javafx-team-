package group13.demo1.model;

import java.time.LocalDateTime;

public class TimerRecord {
    private int id;
    private String label;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long elapsedTime; // milliseconds

    public TimerRecord(String label, LocalDateTime startTime, LocalDateTime endTime, long elapsedTime) {
        this.label = label;
        this.startTime = startTime;
        this.endTime = endTime;
        this.elapsedTime = elapsedTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public long getElapsedTime() { return elapsedTime; }
    public void setElapsedTime(long elapsedTime) { this.elapsedTime = elapsedTime; }
}
