package group13.demo1.controller;

import group13.demo1.model.TimerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TimerHistoryLogicTest {

    private TimerHistoryLogic logic;

    private static final String USER = "alice";

    private static final LocalDateTime START_SAME =
            LocalDateTime.of(2025, 1, 1, 9, 0, 0);
    private static final LocalDateTime END_SAME =
            START_SAME.plusMinutes(5).plusSeconds(7); // 307s

    private static final LocalDateTime START_CROSS =
            LocalDateTime.of(2025, 1, 1, 23, 50, 0);
    private static final LocalDateTime END_CROSS =
            LocalDateTime.of(2025, 1, 2, 0, 10, 0);   // crosses midnight

    @BeforeEach
    public void setUp() {
        logic = new TimerHistoryLogic();
        // Ensure stable English month names for "MMM"
        Locale.setDefault(Locale.US);
    }

    @Test
    public void testElapsedSecondsFromTimes() {
        TimerRecord r = new TimerRecord(USER, "Pause", START_SAME, END_SAME, 307);
        long expected = Duration.between(START_SAME, END_SAME).getSeconds();
        assertEquals(expected, logic.elapsedSecondsFromTimes(r));
    }

    @Test
    public void testFormatElapsedTime_NoHours() {
        assertEquals("00:12 s", logic.formatElapsedTime(12));
        assertEquals("02:05 s", logic.formatElapsedTime(125)); // 2m05s
    }

    @Test
    public void testFormatElapsedTime_WithHours() {
        assertEquals("01h:01m:01s", logic.formatElapsedTime(3661));
        assertEquals("10h:00m:00s", logic.formatElapsedTime(36000));
    }

    @Test
    public void testFormatTotal() {
        assertEquals("59s", logic.formatTotal(59));
        assertEquals("2m 05s", logic.formatTotal(125));
        assertEquals("1h 01m 01s", logic.formatTotal(3661));
    }

    @Test
    public void testFormatRange_SameDay() {
        TimerRecord r = new TimerRecord(USER, "Tag", START_SAME, END_SAME, 307);
        String out = logic.formatRange(r);
        // Starts with "Jan 1, 2025  -  09:00:00 AM  →  09:05:07 AM"
        assertTrue(out.startsWith("Jan 1, 2025  -  09:00:00 AM"));
        assertTrue(out.endsWith("09:05:07 AM"));
        assertTrue(out.contains("→"));
    }

    @Test
    public void testFormatRange_CrossDay() {
        long secs = Duration.between(START_CROSS, END_CROSS).getSeconds();
        TimerRecord r = new TimerRecord(USER, "Tag", START_CROSS, END_CROSS, secs);
        String out = logic.formatRange(r);
        // Should include full datetime for both ends and the arrow
        assertTrue(out.contains("2025-01-01 11:50:00 PM"));
        assertTrue(out.contains("2025-01-02 12:10:00 AM"));
        assertTrue(out.contains("→"));
    }

    @Test
    public void testSortNewestFirst() {
        TimerRecord a = new TimerRecord(USER, "A",
                LocalDateTime.of(2025, 1, 1, 8, 0, 0),
                LocalDateTime.of(2025, 1, 1, 8, 5, 0), 300);

        TimerRecord b = new TimerRecord(USER, "B",
                LocalDateTime.of(2025, 1, 1, 9, 0, 0),
                LocalDateTime.of(2025, 1, 1, 9, 5, 0), 300);

        TimerRecord c = new TimerRecord(USER, "C",
                LocalDateTime.of(2025, 1, 1, 10, 0, 0),
                LocalDateTime.of(2025, 1, 1, 10, 5, 0), 300);

        List<TimerRecord> rows = new ArrayList<>(Arrays.asList(a, b, c));
        logic.sortNewestFirst(rows);

        assertSame(c, rows.get(0));
        assertSame(b, rows.get(1));
        assertSame(a, rows.get(2));
    }

    @Test
    public void testBuildRowLabel() {
        TimerRecord r = new TimerRecord(USER, "Focus Switch", START_SAME, END_SAME, 307);
        assertEquals("Timer 1  -  Focus Switch", logic.buildRowLabel(0, r));
    }

    @Test
    public void testBuildSelectedSessionText() {
        TimerRecord r = new TimerRecord(USER, "Email", START_SAME, END_SAME, 307);
        String text = logic.buildSelectedSessionText(2, r); // index 2 => "Timer 3"
        assertTrue(text.startsWith("Timer 3  -  Email  -  "));
        assertTrue(text.contains(" -  00:05 s") || text.contains(" -  00:05:07")); // formatted duration tail
    }

    @Test
    public void testComputeTotalSeconds() {
        TimerRecord r1 = new TimerRecord(USER, "A", START_SAME, END_SAME, 307);
        TimerRecord r2 = new TimerRecord(USER, "B", START_CROSS, END_CROSS,
                Duration.between(START_CROSS, END_CROSS).getSeconds());
        List<TimerRecord> list = Arrays.asList(r1, r2);

        long expected = r1.getElapsedSeconds() + r2.getElapsedSeconds();
        assertEquals(expected, logic.computeTotalSeconds(list));
    }

    @Test
    public void testNextIndexAfterDelete() {
        // delete middle item (index 1) from size 3 -> sizeAfter=2 -> next=1 (now last)
        assertEquals(1, logic.nextIndexAfterDelete(1, 2));

        // delete last (index 2) from size 3 -> sizeAfter=2 -> next=1
        assertEquals(1, logic.nextIndexAfterDelete(2, 2));

        // list becomes empty
        assertEquals(-1, logic.nextIndexAfterDelete(0, 0));
    }
}
