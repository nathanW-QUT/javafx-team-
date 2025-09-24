package group13.demo1.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SessionTest {

    private static final String USERNAME = "alice";
    private static final String USERNAME_2 = "bob";
    private static final long RUN_SECONDS = 120;
    private static final long RUN_SECONDS_2 = 300;
    private static final long PAUSE_SECONDS = 30;
    private static final long PAUSE_SECONDS_2 = 60;
    private static final int PAUSE_COUNT = 2;
    private static final int PAUSE_COUNT_2 = 5;

    private static final LocalDateTime START =
            LocalDateTime.of(2025, 1, 1, 9, 0);
    private static final LocalDateTime END =
            LocalDateTime.of(2025, 1, 1, 10, 0);

    private SessionModel session;

    @BeforeEach
    public void setUp() {
        session = new SessionModel(USERNAME, START);
        session.setId(1);
        session.setEndTime(END);
        session.setTotalRunSeconds(RUN_SECONDS);
        session.setTotalPauseSeconds(PAUSE_SECONDS);
        session.setPauseCount(PAUSE_COUNT);
    }

    @Test
    public void testGetId() {
        assertEquals(1, session.getId());
    }

    @Test
    public void testSetId() {
        session.setId(2);
        assertEquals(2, session.getId());
    }

    @Test
    public void testGetUsername() {
        assertEquals(USERNAME, session.getUsername());
    }

    @Test
    public void testSetUsername() {
        session.setUsername(USERNAME_2);
        assertEquals(USERNAME_2, session.getUsername());
    }

    @Test
    public void testGetStartTime() {
        assertEquals(START, session.getStartTime());
    }

    @Test
    public void testSetStartTime() {
        LocalDateTime newStart = START.plusHours(1);
        session.setStartTime(newStart);
        assertEquals(newStart, session.getStartTime());
    }

    @Test
    public void testGetEndTime() {
        assertEquals(END, session.getEndTime());
    }

    @Test
    public void testSetEndTime() {
        LocalDateTime newEnd = END.plusHours(1);
        session.setEndTime(newEnd);
        assertEquals(newEnd, session.getEndTime());
    }

    @Test
    public void testGetTotalRunSeconds() {
        assertEquals(RUN_SECONDS, session.getTotalRunSeconds());
    }

    @Test
    public void testSetTotalRunSeconds() {
        session.setTotalRunSeconds(RUN_SECONDS_2);
        assertEquals(RUN_SECONDS_2, session.getTotalRunSeconds());
    }

    @Test
    public void testGetTotalPauseSeconds() {
        assertEquals(PAUSE_SECONDS, session.getTotalPauseSeconds());
    }

    @Test
    public void testSetTotalPauseSeconds() {
        session.setTotalPauseSeconds(PAUSE_SECONDS_2);
        assertEquals(PAUSE_SECONDS_2, session.getTotalPauseSeconds());
    }

    @Test
    public void testGetPauseCount() {
        assertEquals(PAUSE_COUNT, session.getPauseCount());
    }

    @Test
    public void testSetPauseCount() {
        session.setPauseCount(PAUSE_COUNT_2);
        assertEquals(PAUSE_COUNT_2, session.getPauseCount());
    }
}