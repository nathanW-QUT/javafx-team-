package group13.demo1.model;

import java.util.List;

public interface ITimerDAO {
    void addTimer(TimerRecord timer);
    void updateTimer(TimerRecord timer);
    void deleteTimer(TimerRecord timer);
    TimerRecord getTimer(int id);
    List<TimerRecord> getAllTimers();
}
