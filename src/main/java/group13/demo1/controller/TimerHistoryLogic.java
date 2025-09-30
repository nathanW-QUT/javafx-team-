package group13.demo1.controller;

import group13.demo1.model.TimerRecord;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * logic part for the History page.
 * helping in formatting and sorting etc.
 */
public class TimerHistoryLogic {

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
    private final DateTimeFormatter dateFormatted = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private final DateTimeFormatter timeFormatted = DateTimeFormatter.ofPattern("hh:mm:ss a");

    /** sorting timer by showing newest first. */
    public void sortNewestFirst(List<TimerRecord> rows)
    {
        rows.sort((a, b) -> b.getStartTime().compareTo(a.getStartTime()));
    }


    public String Row(int indexZeroBased, TimerRecord t)
    {
        int n = indexZeroBased + 1;
        return "Timer " + n + "  -  " + t.getLabel();
    }

    /** shows the full details of selected timer */
    public String SelectedSessionText(int indexZeroBased, TimerRecord t)
    {
        int n = indexZeroBased + 1;
        long secs = elapsedTimeFromTimerRecord(t);
        String range = formatRange(t);
        return "Timer " + n + "  -  " + t.getLabel() + "  -  " + range + "  -  " + formatElapsedTime(secs);
    }

    /** formates the start and end time for each timers */
    public String formatRange(TimerRecord t)
    {
        boolean sameDay = t.getStartTime().toLocalDate().equals(t.getEndTime().toLocalDate());
        if (sameDay)
        {
            return dateFormatted.format(t.getStartTime()) + "  -  "
                    + timeFormatted.format(t.getStartTime()) + "  →  "
                    + timeFormatted.format(t.getEndTime());
        } else
        {
            return dtf.format(t.getStartTime()) + "  →  " + dtf.format(t.getEndTime());
        }
    }

    /** calculates the time elapsed for each timer*/
    public long elapsedTimeFromTimerRecord(TimerRecord t)
    {
        long secs = Duration.between(t.getStartTime(), t.getEndTime()).getSeconds();
        return Math.max(0, secs);
    }

    /** helps in formatting  the elapsed time as "mm:ss s" or "hh:mm:ss. */
    public String formatElapsedTime(long seconds)
    {
        long h = seconds / 3600;
        long m = (seconds / 60) % 60;
        long s = seconds % 60;

        if (h == 0)
        {
            return String.format("%02d:%02d s", m, s);
        }
        return String.format("%02dh:%02dm:%02ds", h, m, s);
    }

    /** formates total time spent in timers*/
    public String formatTotal(long seconds)
    {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (h > 0)  return String.format("%dh %02dm %02ds", h, m, s);
        if (m > 0)  return String.format("%dm %02ds", m, s);
        return String.format("%ds", s);
    }

    /** gives total time spent adding all timer sessions*/
    public long TotalSeconds(List<TimerRecord> items)
    {
        long totalSecs = 0L;
        for (TimerRecord r : items) totalSecs += r.getElapsedSeconds();
        return totalSecs;
    }

    /** default header display for total distraction counts */
    public String totalsHeaderInitial(int count) { return "Total Timer Sessions: " + count; }

    /** header display for distraction counts when the number of distraction changes */
    public String totalsHeaderOnChange(int count) { return "Total Timers: " + count; }

    /** gives the next index after deleting a timer */
    public int nextIndexAfterDelete(int deletedIndex, int sizeAfterRemoval)
    {
        if (sizeAfterRemoval <= 0) return -1;
        return Math.min(deletedIndex, sizeAfterRemoval - 1);
    }
}