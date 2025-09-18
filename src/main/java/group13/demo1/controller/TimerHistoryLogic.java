package group13.demo1.controller;

import group13.demo1.model.TimerRecord;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TimerHistoryLogic {

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a");

    /** Sort newest first by startTime (desc). */
    public void sortNewestFirst(List<TimerRecord> rows) {
        rows.sort((a, b) -> b.getStartTime().compareTo(a.getStartTime()));
    }

    /** Build the left list label: "Timer N  -  Label". */
    public String buildRowLabel(int indexZeroBased, TimerRecord t) {
        int n = indexZeroBased + 1;
        return "Timer " + n + "  -  " + t.getLabel();
    }

    /** Return the formatted string for the selected session line. */
    public String buildSelectedSessionText(int indexZeroBased, TimerRecord t) {
        int n = indexZeroBased + 1;
        long secs = elapsedSecondsFromTimes(t);
        String range = formatRange(t);
        return "Timer " + n + "  -  " + t.getLabel() + "  -  " + range + "  -  " + formatElapsedTime(secs);
    }

    /** Same-day vs cross-day formatting. */
    public String formatRange(TimerRecord t) {
        boolean sameDay = t.getStartTime().toLocalDate().equals(t.getEndTime().toLocalDate());
        if (sameDay) {
            return dateFmt.format(t.getStartTime()) + "  -  "
                    + timeFmt.format(t.getStartTime()) + "  →  "
                    + timeFmt.format(t.getEndTime());
        } else {
            return dtf.format(t.getStartTime()) + "  →  " + dtf.format(t.getEndTime());
        }
    }

    /** Safe elapsed seconds from start/end. */
    public long elapsedSecondsFromTimes(TimerRecord t) {
        long secs = Duration.between(t.getStartTime(), t.getEndTime()).getSeconds();
        return Math.max(0, secs);
    }

    /** Selected-line elapsed time format. */
    public String formatElapsedTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return (h > 0)
                ? String.format("%02dh:%02dm:%02ds", h, m, s)
                : String.format("%02d:%02d s", m, s);
    }

    /** Footer total-time format. */
    public String formatTotal(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (h > 0)  return String.format("%dh %02dm %02ds", h, m, s);
        if (m > 0)  return String.format("%dm %02ds", m, s);
        return String.format("%ds", s);
    }

    /** Sum of elapsed seconds across items (uses stored seconds on record). */
    public long computeTotalSeconds(List<TimerRecord> items) {
        long totalSecs = 0L;
        for (TimerRecord r : items) totalSecs += r.getElapsedSeconds();
        return totalSecs;
    }

    /** Header text helpers to match your labels. */
    public String totalsHeaderInitial(int count) { return "Total Timer Sessions: " + count; }
    public String totalsHeaderOnChange(int count) { return "Total Timers: " + count; }

    /** After delete, which index should be selected next? (-1 means none) */
    public int nextIndexAfterDelete(int deletedIndex, int sizeAfterRemoval) {
        if (sizeAfterRemoval <= 0) return -1;
        return Math.min(deletedIndex, sizeAfterRemoval - 1);
    }
}
