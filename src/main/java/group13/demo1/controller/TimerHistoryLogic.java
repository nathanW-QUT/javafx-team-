package group13.demo1.controller;

import group13.demo1.model.TimerRecord;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Presentation logic for the Timer History page.
 *
 * Aggregates raw timer rows into "sessions": every group of rows between two "Reset"
 * records is merged into a single display item. The session label is the first
 * non-"Pause" label encountered in that group; if none is found, it shows "Pause".
 *
 * Also exposes the small formatting helpers used by the UI.
 */
public class TimerHistoryLogic {

    private final DateTimeFormatter dtf           = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
    private final DateTimeFormatter dateFormatted = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private final DateTimeFormatter timeFormatted = DateTimeFormatter.ofPattern("hh:mm:ss a");

    /** One aggregated session + the ids of the source timer rows. */
    public static class AggregatedSession {
        public final List<Integer> sourceIds = new ArrayList<>();
        public final String username;
        public final String label;            // chosen session label
        public final LocalDateTime start;     // first start in the group
        public final LocalDateTime end;       // last end in the group
        public final long totalSeconds;       // sum of elapsed seconds across the group

        public AggregatedSession(String username,
                                 String label,
                                 LocalDateTime start,
                                 LocalDateTime end,
                                 long totalSeconds,
                                 Collection<Integer> ids) {
            this.username = username;
            this.label = label;
            this.start = start;
            this.end = end;
            this.totalSeconds = totalSeconds;
            this.sourceIds.addAll(ids);
        }
    }

    /**
     * Merge raw rows into sessions (everything between two "Reset" rows).
     * Works chronologically; the UI will reverse to newest-first later.
     */
    public List<AggregatedSession> aggregateIntoSessions(List<TimerRecord> rows) {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();

        // Ascending order for accumulation.
        rows.sort(Comparator.comparing(TimerRecord::getStartTime));

        List<AggregatedSession> out = new ArrayList<>();

        String currentUser = null;
        LocalDateTime sessStart = null;
        LocalDateTime sessEnd   = null;
        long total = 0L;
        String chosenLabel = null;  // first non-"Pause" seen in the group
        List<Integer> ids = new ArrayList<>();

        for (TimerRecord r : rows) {
            currentUser = r.getUsername();

            // Reset ends the current session; flush then clear accumulators
            if ("Reset".equalsIgnoreCase(r.getLabel())) {
                if (sessStart != null && sessEnd != null) {
                    String label = (chosenLabel == null || chosenLabel.isBlank()) ? "Pause" : chosenLabel;
                    out.add(new AggregatedSession(currentUser, label, sessStart, sessEnd, total, ids));
                }
                sessStart = null;
                sessEnd   = null;
                total = 0L;
                chosenLabel = null;
                ids = new ArrayList<>();
                continue;
            }

            // Accumulate this timer slice
            if (sessStart == null) {
                sessStart = r.getStartTime();
                sessEnd   = r.getEndTime();
            } else if (r.getEndTime().isAfter(sessEnd)) {
                sessEnd = r.getEndTime();
            }

            total += Math.max(0, r.getElapsedSeconds());
            ids.add(r.getId());

            // First non-"Pause" becomes the session label
            if (chosenLabel == null && r.getLabel() != null && !r.getLabel().equalsIgnoreCase("Pause")) {
                chosenLabel = r.getLabel();
            }
        }

        // Flush trailing session if the timeline didn't end with a Reset
        if (sessStart != null && sessEnd != null) {
            String label = (chosenLabel == null || chosenLabel.isBlank()) ? "Pause" : chosenLabel;
            out.add(new AggregatedSession(currentUser, label, sessStart, sessEnd, total, ids));
        }

        // Newest first for display
        out.sort((a, b) -> b.start.compareTo(a.start));
        return out;
    }

    /* ---------- Formatting helpers used by TimerHistory controller ---------- */

    public void sortNewestFirst(List<TimerRecord> rows) {
        rows.sort((a, b) -> b.getStartTime().compareTo(a.getStartTime()));
    }

    /** Legacy row label for a raw TimerRecord list cell (still used in tests). */
    public String Row(int indexZeroBased, TimerRecord t) {
        int n = indexZeroBased + 1;
        return "Timer " + n + "  -  " + t.getLabel();
    }

    /** Text for the aggregated sessions list cell. */
    public String listRowForSession(int indexZeroBased, AggregatedSession s) {
        int n = indexZeroBased + 1;
        return "Session " + n + "  —  " + dateFormatted.format(s.start) + "  —  " + formatTotal(s.totalSeconds);
    }

    /** Text for the right-side “Selected session” detail line. */
    public String selectedSessionText(int indexZeroBased, AggregatedSession s) {
        int n = indexZeroBased + 1;
        return "Session " + n + "  —  " + s.label + "  —  "
                + formatRange(s.start, s.end) + "  —  " + formatElapsedTime(s.totalSeconds);
    }

    public String formatRange(LocalDateTime start, LocalDateTime end) {
        boolean sameDay = start.toLocalDate().equals(end.toLocalDate());
        if (sameDay) {
            return dateFormatted.format(start) + "  -  "
                    + timeFormatted.format(start) + "  →  "
                    + timeFormatted.format(end);
        } else {
            return dtf.format(start) + "  →  " + dtf.format(end);
        }
    }

    public long elapsedTimeFromTimerRecord(TimerRecord t) {
        long secs = Duration.between(t.getStartTime(), t.getEndTime()).getSeconds();
        return Math.max(0, secs);
    }

    public String formatElapsedTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds / 60) % 60;
        long s = seconds % 60;
        if (h == 0) return String.format("%02d:%02d s", m, s);
        return String.format("%02dh:%02dm:%02ds", h, m, s);
    }

    public String formatTotal(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (h > 0)  return String.format("%dh %02dm %02ds", h, m, s);
        if (m > 0)  return String.format("%dm %02ds", m, s);
        return String.format("%ds", s);
    }

    /** Sum elapsed seconds straight from raw rows (kept for the total label). */
    public long TotalSeconds(List<TimerRecord> items) {
        long totalSecs = 0L;
        for (TimerRecord r : items) totalSecs += r.getElapsedSeconds();
        return totalSecs;
    }

    // Old header helpers still here for compatibility (not used by new UI)
    public String totalsHeaderInitial(int count) { return "Total Timer Sessions: " + count; }
    public String totalsHeaderOnChange(int count) { return "Total Timers: " + count; }

    public int nextIndexAfterDelete(int deletedIndex, int sizeAfterRemoval) {
        if (sizeAfterRemoval <= 0) return -1;
        return Math.min(deletedIndex, sizeAfterRemoval - 1);
    }
}
