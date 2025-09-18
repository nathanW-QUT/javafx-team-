package group13.demo1.controller;
import group13.demo1.model.TimerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class TimerHistoryLogicTest {

    private TimerHistoryLogic logic;

    private static final String USER = "vijay";

    private static final LocalDateTime START_SAME =
            LocalDateTime.of(2025, 1, 1, 9, 0, 0);
    private static final LocalDateTime END_SAME =
            LocalDateTime.of(2025, 1, 1, 9, 5, 7);

    private static final LocalDateTime START_CROSS =
            LocalDateTime.of(2025, 1, 1, 23, 50, 0);
    private static final LocalDateTime END_CROSS =
            LocalDateTime.of(2025, 1, 2, 0, 10, 0);

    @BeforeEach
    public void setUp() {
        logic = new TimerHistoryLogic();
        Locale.setDefault(Locale.US);
    }

    @Test
    public void testRow() {
        TimerRecord r = new TimerRecord(USER, "Focus Switch", START_SAME, END_SAME, 307);
        assertEquals("Timer 1  -  Focus Switch", logic.Row(0, r));
    }

    @Test
    public void testFormatElapsedTime() {
        assertEquals("00:12 s",      logic.formatElapsedTime(12));
        assertEquals("02:05 s",      logic.formatElapsedTime(125));
        assertEquals("01h:01m:01s",  logic.formatElapsedTime(3661));
    }

    @Test
    public void testFormatTotal() {
        assertEquals("59s",          logic.formatTotal(59));
        assertEquals("2m 05s",       logic.formatTotal(125));
        assertEquals("1h 01m 01s",   logic.formatTotal(3661));
    }

    @Test
    public void testFormatRange_SameDay() {
        TimerRecord r = new TimerRecord(USER, "Tag", START_SAME, END_SAME, 307);
        String expected = "Jan 1, 2025  -  09:00:00 AM  →  09:05:07 AM";
        assertEquals(expected, logic.formatRange(r));
    }

    @Test
    public void testFormatRange_DifferentDay() {
        long secs = java.time.Duration.between(START_CROSS, END_CROSS).getSeconds();
        TimerRecord r = new TimerRecord(USER, "Tag", START_CROSS, END_CROSS, secs);
        String expected = "2025-01-01 11:50:00 PM  →  2025-01-02 12:10:00 AM";
        assertEquals(expected, logic.formatRange(r));
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
    public void testSelectedSessionText() {
        TimerRecord r = new TimerRecord(USER, "Email", START_SAME, END_SAME, 307);


        String expected = "Timer 3"
                + "  -  Email"
                + "  -  " + logic.formatRange(r)
                + "  -  " + logic.formatElapsedTime(307);

        String actual = logic.SelectedSessionText(2, r);
        assertEquals(expected, actual);
    }

    @Test
    public void testTotalSeconds() {
        TimerRecord r1 = new TimerRecord(USER, "A", START_SAME, END_SAME, 307);
        TimerRecord r2 = new TimerRecord(USER, "B", START_CROSS, END_CROSS,
                java.time.Duration.between(START_CROSS, END_CROSS).getSeconds());

        long expected = r1.getElapsedSeconds() + r2.getElapsedSeconds();
        assertEquals(expected, logic.TotalSeconds(Arrays.asList(r1, r2)));
    }

    @Test
    public void testNextIndexAfterDelete() {
        assertEquals(1,  logic.nextIndexAfterDelete(1, 2));
        assertEquals(1,  logic.nextIndexAfterDelete(2, 2));
        assertEquals(-1, logic.nextIndexAfterDelete(0, 0));
    }
}


