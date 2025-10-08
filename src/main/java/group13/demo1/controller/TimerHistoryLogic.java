package group13.demo1.controller;

import group13.demo1.model.TimerRecord;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;



public class TimerHistoryLogic {

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
    private final DateTimeFormatter dateFormatted = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private final DateTimeFormatter timeFormatted = DateTimeFormatter.ofPattern("hh:mm:ss a");


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


    public List<AggregatedSession> aggregateIntoSessions(List<TimerRecord> rows) {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();

        rows.sort(Comparator.comparing(TimerRecord::getStartTime)); // oldest→newest

        List<AggregatedSession> out = new ArrayList<>();
        String currentUser = null;
        LocalDateTime sessStart = null;
        LocalDateTime sessEnd = null;
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
                total = 0L;
                chosenLabel = null;
                ids = new ArrayList<>();
                continue;
            }


            long seg = Math.max(0, Math.round(r.getElapsedSeconds()));
            if (sessStart == null) {
                sessStart = r.getStartTime();
                sessEnd   = r.getEndTime();
            } else if (r.getEndTime().isAfter(sessEnd)) {
                sessEnd = r.getEndTime();
            }
            total += seg;
            ids.add(r.getId());

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
        return "Session " + n + "  —  " + dateFormatted.format(s.start) + "  —  " + formatTotal(s.totalSeconds);
    }

    public String selectedSessionText(int indexZeroBased, AggregatedSession s) {
        int n = indexZeroBased + 1;
        return "Session " + n + "  —  " + s.label + "  —  " + formatRange(s.start, s.end) + "  —  " + formatElapsedTime(s.totalSeconds);
    }

    public String formatRange(LocalDateTime start, LocalDateTime end) {
        boolean sameDay = start.toLocalDate().equals(end.toLocalDate());
        if (sameDay) {
            return dateFormatted.format(start) + "  -  " + timeFormatted.format(start) + "  →  " + timeFormatted.format(end);
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

    public long TotalSeconds(List<TimerRecord> items) {
        long totalSecs = 0L;
        for (TimerRecord r : items) totalSecs += r.getElapsedSeconds();
        return totalSecs;
    }

    public String totalsHeaderInitial(int count) { return "Total Timer Sessions: " + count; }
    public String totalsHeaderOnChange(int count) { return "Total Timers: " + count; }

    public int nextIndexAfterDelete(int deletedIndex, int sizeAfterRemoval) {
        if (sizeAfterRemoval <= 0) return -1;
        return Math.min(deletedIndex, sizeAfterRemoval - 1);
    }
}
