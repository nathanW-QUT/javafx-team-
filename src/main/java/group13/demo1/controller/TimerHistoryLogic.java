package group13.demo1.controller;

import group13.demo1.model.TimerRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class TimerHistoryLogic {

    // Formatting tools
    private final DateTimeFormatter dateformatted = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private final DateTimeFormatter timeformatted = DateTimeFormatter.ofPattern("hh:mm:ss a");

    // dto from th the sessiondata database
    public static class SessionData {
        public final int id;
        public final String username;
        public final LocalDateTime start;
        public final LocalDateTime end;
        public final long focustime;   // totalRunSeconds
        public final long pausetime;   // totalPauseSeconds
        public final int pausecount;      // pauseCount

        public SessionData(int id, String username, LocalDateTime start, LocalDateTime end,
                           long focusSeconds, long pauseSeconds, int pauseCount)
        {
            this.id = id;
            this.username = (username == null) ? "" : username;
            this.start = start;
            this.end = end;
            this.focustime = Math.max(0, focusSeconds);
            this.pausetime = Math.max(0, pauseSeconds);
            this.pausecount = Math.max(0, pauseCount);
        }
    }

    // displayed row text for list in timer history(session)
    public String listForSession(int index, SessionData s)
    {
        int n = index + 1;
        String date = (s.start == null) ? "" : dateformatted.format(s.start);
        String focus = formatTotal(s.focustime);
        return "Session " + n + "  —  " + date + "  —  " + focus;
    }

    // session description for the selected row
    public String SelectedSessionText(int index, SessionData s)
    {
        int n = index + 1;
        String date = (s.start == null) ? "" : dateformatted.format(s.start);
        String range = formatRange(s.start, s.end);
        String focus = formatElapsedTime(s.focustime);
        String paused = formatElapsedTime(s.pausetime);
        return "Session " + n + "  —  " + date + "  —  " + range + "  —  Focus: " + focus + "  —  Paused: " + paused + "  —  Pauses: " + s.pausecount;
    }

    // to format time into a more readable format
    public String formatRange(LocalDateTime start, LocalDateTime end)
    {
        if (start == null || end == null) return "(no range)";
        boolean sameDay = start.toLocalDate().equals(end.toLocalDate());
        if (sameDay)
        {
            return timeformatted.format(start) + "  →  " + timeformatted.format(end);
        }
        return dateformatted.format(start) + " " + timeformatted.format(start) + "  →  " + dateformatted.format(end) + " " + timeformatted.format(end);
    }

    public String formatElapsedTime(long seconds)
    {
        long h = seconds / 3600;
        long m = (seconds / 60) % 60;
        long s = seconds % 60;
        if (h > 0)  return String.format("%02dh:%02dm:%02ds", h, m, s);
        if (m > 0)  return String.format("%02dm:%02ds", m, s);
        return String.format("%02ds", s);
    }

    public String formatTotal(long seconds)
    {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (h > 0)  return String.format("%dh %02dm %02ds", h, m, s);
        if (m > 0)  return String.format("%dm %02ds", m, s);
        return String.format("%ds", s);
    }

    public void sortNewestFirst(List<TimerRecord> rows)
    {
        if (rows == null) return;
        rows.sort((a, b) -> {
            LocalDateTime sa = (a == null) ? null : a.getStartTime();
            LocalDateTime sb = (b == null) ? null : b.getStartTime();
            if (sa == null && sb == null) return 0;
            if (sa == null) return 1;
            if (sb == null) return -1;
            return sb.compareTo(sa);
        });
    }

    public long TotalSeconds(List<TimerRecord> items)
    {
        if (items == null) return 0L;
        long totalSecs = 0L;
        for (TimerRecord r : items)
        {
            if (r != null) totalSecs += Math.max(0, r.getElapsedSeconds());
        }
        return totalSecs;
    }
}
