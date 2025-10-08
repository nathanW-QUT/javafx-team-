package group13.demo1.controller;

import group13.demo1.model.TimerRecord;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Presentation/formatting logic for the Timer History screen.
 * - ViewSession: rows read directly from the DB (focus/pause already aggregated).
 *   (No label/tag here; and we never show the DB row ID in the UI.)
 */
public class TimerHistoryLogic {

    // ---------- Formatting ----------
    private final DateTimeFormatter datePretty = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private final DateTimeFormatter timePretty = DateTimeFormatter.ofPattern("hh:mm:ss a");

    // ---------- DTO used by the UI (read straight from DB) ----------
    public static class ViewSession {
        public final int id;                    // kept internally for delete, but not shown
        public final String username;           // may be empty if DB has no user column
        public final LocalDateTime start;       // may be null if DB row is missing it
        public final LocalDateTime end;         // may be null
        public final long focusSeconds;         // totalRunSeconds from DB
        public final long pauseSeconds;         // totalPauseSeconds from DB
        public final int pauseCount;            // pauseCount from DB

        public ViewSession(int id, String username,
                           LocalDateTime start, LocalDateTime end,
                           long focusSeconds, long pauseSeconds, int pauseCount) {
            this.id = id;
            this.username = username == null ? "" : username;
            this.start = start;
            this.end = end;
            this.focusSeconds = Math.max(0, focusSeconds);
            this.pauseSeconds = Math.max(0, pauseSeconds);
            this.pauseCount = Math.max(0, pauseCount);
        }
    }

    // ---------- Text for the ListView row ----------
    public String listRowForViewSession(int indexZeroBased, ViewSession s) {
        int n = indexZeroBased + 1;
        String date = (s.start == null) ? "" : datePretty.format(s.start);
        String focus = formatTotal(s.focusSeconds);
        // Row: "Session 1 — Oct 8, 2025 — 10s"
        return "Session " + n + "  —  " + date + "  —  " + focus;
    }

    // ---------- Text for the Selected Session label ----------
    public String selectedViewSessionText(int indexZeroBased, ViewSession s) {
        int n = indexZeroBased + 1;
        String date = (s.start == null) ? "" : datePretty.format(s.start);
        String range = formatRange(s.start, s.end);
        String focus = formatElapsedTime(s.focusSeconds);
        String paused = formatElapsedTime(s.pauseSeconds);

        // No ID and no label shown here.
        // Example:
        // "Session 1 — Oct 8, 2025  —  09:26:28 pm → 09:26:43 pm  —  Focus: 00:10 s  —  Paused: 00:05 s  —  Pauses: 2"
        return "Session " + n + "  —  " + date + "  —  " + range
                + "  —  Focus: " + focus + "  —  Paused: " + paused + "  —  Pauses: " + s.pauseCount;
    }

    // ---------- Time formatting helpers ----------
    public String formatRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "(no range)";
        boolean sameDay = start.toLocalDate().equals(end.toLocalDate());
        if (sameDay) {
            return timePretty.format(start) + "  →  " + timePretty.format(end);
        }
        return datePretty.format(start) + " " + timePretty.format(start)
                + "  →  " + datePretty.format(end) + " " + timePretty.format(end);
    }

    public String formatElapsedTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds / 60) % 60;
        long s = seconds % 60;
        if (h > 0)  return String.format("%02dh:%02dm:%02ds", h, m, s);
        if (m > 0)  return String.format("%02dm:%02ds", m, s);
        return String.format("%02ds", s);
    }

    /** Compact total, used in the header ("Total Focus Time: 10s", etc.). */
    public String formatTotal(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (h > 0)  return String.format("%dh %02dm %02ds", h, m, s);
        if (m > 0)  return String.format("%dm %02ds", m, s);
        return String.format("%ds", s);
    }

    // =====================================================================
    // Legacy aggregation over raw TimerRecord rows (kept for compatibility)
    // =====================================================================

    public static class AggregatedSession {
        public final List<Integer> sourceIds = new ArrayList<>();
        public final String username;
        public final String label;
        public final LocalDateTime start;
        public final LocalDateTime end;
        public final long totalSeconds;

        public AggregatedSession(String username, String label,
                                 LocalDateTime start, LocalDateTime end, long totalSeconds,
                                 Collection<Integer> ids) {
            this.username = username;
            this.label = label;
            this.start = start;
            this.end = end;
            this.totalSeconds = totalSeconds;
            this.sourceIds.addAll(ids);
        }
    }

    /** Groups contiguous TimerRecord rows into sessions, splitting on "Reset". */
    public List<AggregatedSession> aggregateIntoSessions(List<TimerRecord> rows) {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        rows.sort(Comparator.comparing(TimerRecord::getStartTime)); // oldest→newest

        List<AggregatedSession> out = new ArrayList<>();
        String currentUser = null;
        LocalDateTime sessStart = null, sessEnd = null;
        long total = 0L;
        String chosenLabel = null;
        List<Integer> ids = new ArrayList<>();

        for (TimerRecord r : rows) {
            currentUser = r.getUsername();

            if ("Reset".equalsIgnoreCase(r.getLabel())) {
                if (sessStart != null && sessEnd != null) {
                    String label = (chosenLabel == null || chosenLabel.isBlank()) ? "Pause" : chosenLabel;
                    out.add(new AggregatedSession(currentUser, label, sessStart, sessEnd, total, ids));
                }
                sessStart = null; sessEnd = null;
                total = 0L; chosenLabel = null; ids = new ArrayList<>();
                continue;
            }

            long seg = Math.max(0, Math.round(r.getElapsedSeconds()));
            if (sessStart == null) { sessStart = r.getStartTime(); sessEnd = r.getEndTime(); }
            else if (r.getEndTime().isAfter(sessEnd)) { sessEnd = r.getEndTime(); }
            total += seg; ids.add(r.getId());

            if (chosenLabel == null && r.getLabel() != null && !r.getLabel().equalsIgnoreCase("Pause")) {
                chosenLabel = r.getLabel();
            }
        }

        if (sessStart != null && sessEnd != null) {
            String label = (chosenLabel == null || chosenLabel.isBlank()) ? "Pause" : chosenLabel;
            out.add(new AggregatedSession(currentUser, label, sessStart, sessEnd, total, ids));
        }

        out.sort((a, b) -> b.start.compareTo(a.start));
        return out;
    }

    public void sortNewestFirst(List<TimerRecord> rows) {
        rows.sort((a, b) -> b.getStartTime().compareTo(a.getStartTime()));
    }

    public String listRowForSession(int indexZeroBased, AggregatedSession s) {
        int n = indexZeroBased + 1;
        return "Session " + n + "  —  " + datePretty.format(s.start) + "  —  " + formatTotal(s.totalSeconds);
    }

    public String selectedSessionText(int indexZeroBased, AggregatedSession s) {
        int n = indexZeroBased + 1;
        return "Session " + n + "  —  " + s.label + "  —  " + formatRange(s.start, s.end)
                + "  —  " + formatElapsedTime(s.totalSeconds);
    }

    public long elapsedTimeFromTimerRecord(TimerRecord t) {
        long secs = Duration.between(t.getStartTime(), t.getEndTime()).getSeconds();
        return Math.max(0, secs);
    }

    /** Sum of elapsedSeconds across raw items (legacy). */
    public long TotalSeconds(List<TimerRecord> items) {
        long totalSecs = 0L;
        for (TimerRecord r : items) totalSecs += r.getElapsedSeconds();
        return totalSecs;
    }
}
