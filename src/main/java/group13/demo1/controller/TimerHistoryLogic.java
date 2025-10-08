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

    // ---------- DTO straight from DB ----------
    public static class ViewSession {
        public final int id;
        public final String username;
        public final LocalDateTime start;
        public final LocalDateTime end;
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

    // ---------- Text for the session table ----------
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

        return "Session " + n + "  —  " + date + "  —  " + range + "  —  Focus: " + focus + "  —  Paused: " + paused + "  —  Pauses: " + s.pauseCount;
    }

    // ---------- Time formatting helpers ----------
    public String formatRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "(no range)";
        boolean sameDay = start.toLocalDate().equals(end.toLocalDate());
        if (sameDay) {
            return timePretty.format(start) + "  →  " + timePretty.format(end);
        }
        return datePretty.format(start) + " " + timePretty.format(start) + "  →  " + datePretty.format(end) + " " + timePretty.format(end);
    }

    public String formatElapsedTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds / 60) % 60;
        long s = seconds % 60;
        if (h > 0)  return String.format("%02dh:%02dm:%02ds", h, m, s);
        if (m > 0)  return String.format("%02dm:%02ds", m, s);
        return String.format("%02ds", s);
    }


    public String formatTotal(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (h > 0)  return String.format("%dh %02dm %02ds", h, m, s);
        if (m > 0)  return String.format("%dm %02ds", m, s);
        return String.format("%ds", s);
    }


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

    public void sortNewestFirst(List<TimerRecord> rows) {
        rows.sort((a, b) -> b.getStartTime().compareTo(a.getStartTime()));
    }

    public long TotalSeconds(List<TimerRecord> items) {
        long totalSecs = 0L;
        for (TimerRecord r : items) totalSecs += r.getElapsedSeconds();
        return totalSecs;
    }
}