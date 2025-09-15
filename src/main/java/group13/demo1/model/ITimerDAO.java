package group13.demo1.model;

import java.util.List;

public interface ITimerDAO {
    void addTimer(TimerModel timer);
    void updateTimer(TimerModel timer);
    void deleteTimer(TimerModel timer);
    TimerModel getTimer(int id);
    List<TimerModel> getAllTimers();
    List<TimerModel> getTimersForUser(String username);
}
