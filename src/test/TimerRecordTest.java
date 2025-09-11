package group13.demo1.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimerRecordTest {

    private static final String USERNAME = "alice";
    private static final String USERNAME_2 = "bob";
    private static final String LABEL = "Pause";
    private static final String LABEL_2 = "Reset";
    private static final long ELAPSED = 42;
    private static final long ELAPSED_2 = 99;

    private static final LocalDateTime START =
            LocalDateTime.of(2025, 1, 1, 12, 0);
    private static final LocalDateTime END =
            LocalDateTime.of(2025, 1, 1, 12, 5);

    private TimerRecord record;

    @BeforeEach
    public void setUp() {
        record = new TimerRecord(USERNAME, LABEL, START, END, ELAPSED);
        record.setId(1);
    }

    @Test
    public void testGetId() {
        assertEquals(1, record.getId());
    }

    @Test
    public void testSetId() {
        record.setId(2);
        assertEquals(2, record.getId());
    }

    @Test
    public void testGetUsername() {
        assertEquals(USERNAME, record.getUsername());
    }

    @Test
    public void testSetUsername() {
        record.setUsername(USERNAME_2);
        assertEquals(USERNAME_2, record.getUsername());
    }

    @Test
    public void testGetLabel() {
        assertEquals(LABEL, record.getLabel());
    }

    @Test
    public void testSetLabel() {
        record.setLabel(LABEL_2);
        assertEquals(LABEL_2, record.getLabel());
    }

    @Test
    public void testGetStartTime() {
        assertEquals(START, record.getStartTime());
    }

    @Test
    public void testSetStartTime() {
        LocalDateTime newStart = START.plusMinutes(10);
        record.setStartTime(newStart);
        assertEquals(newStart, record.getStartTime());
    }

    @Test
    public void testGetEndTime() {
        assertEquals(END, record.getEndTime());
    }

    @Test
    public void testSetEndTime() {
        LocalDateTime newEnd = END.plusMinutes(10);
        record.setEndTime(newEnd);
        assertEquals(newEnd, record.getEndTime());
    }

    @Test
    public void testGetElapsedSeconds() {
        assertEquals(ELAPSED, record.getElapsedSeconds());
    }

    @Test
    public void testSetElapsedSeconds() {
        record.setElapsedSeconds(ELAPSED_2);
        assertEquals(ELAPSED_2, record.getElapsedSeconds());
    }
}
