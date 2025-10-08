import group13.demo1.controller.TimerHistoryLogic;
import group13.demo1.controller.TimerHistoryLogic.ViewSession;
import group13.demo1.model.TimerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TimerHistoryLogicTest {

    private TimerHistoryLogic logic;

    // Test data
    private static final LocalDateTime START_SAME =
            LocalDateTime.of(2025, 1, 1, 9, 0, 0);
    private static final LocalDateTime END_SAME =
            LocalDateTime.of(2025, 1, 1, 9, 5, 7);

    private static final LocalDateTime START_DIFF =
            LocalDateTime.of(2025, 1, 1, 23, 50, 0);
    private static final LocalDateTime END_DIFF =
            LocalDateTime.of(2025, 1, 2, 0, 10, 0);


    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.US);

    @BeforeEach
    public void setUp() {
        Locale.setDefault(Locale.US);
        logic = new TimerHistoryLogic();
    }

    private TimerRecord rec(String label, LocalDateTime s, LocalDateTime e, long secs) {
        return new TimerRecord("vijay", label, s, e, secs);
    }

    private ViewSession view(LocalDateTime s, LocalDateTime e, long focus, long paused, int pauseCount) {
        return new ViewSession(99, "vijay", s, e, focus, paused, pauseCount);
    }



    @Test
    public void testSortNewestFirst() {
        TimerRecord a = rec("A",
                LocalDateTime.of(2025, 1, 1, 8, 0),
                LocalDateTime.of(2025, 1, 1, 8, 5), 300);
        TimerRecord b = rec("B",
                LocalDateTime.of(2025, 1, 1, 9, 0),
                LocalDateTime.of(2025, 1, 1, 9, 5), 300);
        TimerRecord c = rec("C",
                LocalDateTime.of(2025, 1, 1, 10, 0),
                LocalDateTime.of(2025, 1, 1, 10, 5), 300);

        List<TimerRecord> rows = new ArrayList<>(Arrays.asList(a, b, c));
        logic.sortNewestFirst(rows);

        assertSame(c, rows.get(0));
        assertSame(b, rows.get(1));
        assertSame(a, rows.get(2));
    }

    @Test
    public void testFormatElapsedTime() {
        assertEquals("00s",          logic.formatElapsedTime(0));
        assertEquals("12s",          logic.formatElapsedTime(12));
        assertEquals("02m:05s",      logic.formatElapsedTime(125));
        assertEquals("01h:01m:01s",  logic.formatElapsedTime(3661));
    }

    @Test
    public void testFormatTotal() {
        assertEquals("59s",          logic.formatTotal(59));
        assertEquals("2m 05s",       logic.formatTotal(125));
        assertEquals("1h 01m 01s",   logic.formatTotal(3661));
    }

    @Test
    public void testTotalSeconds() {
        TimerRecord r1 = rec("X", START_SAME, END_SAME,
                Duration.between(START_SAME, END_SAME).getSeconds());
        TimerRecord r2 = rec("Y", START_DIFF, END_DIFF,
                Duration.between(START_DIFF, END_DIFF).getSeconds());
        long expected = r1.getElapsedSeconds() + r2.getElapsedSeconds();
        assertEquals(expected, logic.TotalSeconds(Arrays.asList(r1, r2)));
    }



    @Test
    public void testListRowForViewSession() {
        ViewSession v = view(START_SAME, END_SAME, 307, 30, 2);

        String row = logic.listRowForViewSession(0, v);


        String expectedDate = DATE_FMT.format(START_SAME);
        String expectedFocus = logic.formatTotal(307);

        assertTrue(row.startsWith("Session 1"), "Row should start with Session index");
        assertTrue(row.contains(expectedDate), "Row should contain formatted date");
        assertTrue(row.endsWith(expectedFocus), "Row should end with formatted total focus");
    }

    @Test
    public void testSelectedViewSessionText() {
        ViewSession v = view(START_SAME, END_SAME, 307, 30, 2);

        String actual = logic.selectedViewSessionText(2, v);

        String dateStr = DATE_FMT.format(START_SAME);
        String range   = TIME_FMT.format(START_SAME) + "  \u2192  " + TIME_FMT.format(END_SAME);
        String focus   = logic.formatElapsedTime(307);
        String paused  = logic.formatElapsedTime(30);


        assertTrue(actual.startsWith("Session 3"), "Should start with Session index 3");
        assertTrue(actual.contains("  —  " + dateStr + "  —  "), "Should show formatted date");
        assertTrue(actual.contains(range), "Should show time range");
        assertTrue(actual.contains("Focus: " + focus), "Should show focus time");
        assertTrue(actual.contains("Paused: " + paused), "Should show paused time");
        assertTrue(actual.endsWith("Pauses: 2"), "Should end with pause count");
    }

    @Test
    public void testFormatRange_SameDay_Dynamic() {
        String actual = logic.formatRange(START_SAME, END_SAME);
        String startT = TIME_FMT.format(START_SAME);
        String endT   = TIME_FMT.format(END_SAME);
        assertEquals(startT + "  \u2192  " + endT, actual);
    }

    @Test
    public void testFormatRange_DifferentDay_Dynamic() {
        String actual = logic.formatRange(START_DIFF, END_DIFF);

        // Build expected dynamically from the LocalDateTimes
        boolean sameDay = START_DIFF.toLocalDate().equals(END_DIFF.toLocalDate());
        assertFalse(sameDay);

        String expected = DATE_FMT.format(START_DIFF) + " " + TIME_FMT.format(START_DIFF) + "  \u2192  " + DATE_FMT.format(END_DIFF) + " " + TIME_FMT.format(END_DIFF);

        assertEquals(expected, actual);
    }
}
