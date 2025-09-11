package group13.demo1.controller;

import group13.demo1.model.ITimerDAO;
import group13.demo1.model.SqliteTimerDAO;
import group13.demo1.model.TimerRecord;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class TimerHistory {

    @FXML private ListView<String> historyList;

    private final ITimerDAO dao = new SqliteTimerDAO();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        historyList.setItems(FXCollections.observableArrayList(
                dao.getAllTimers().stream()
                        .map(this::formatLine)
                        .collect(Collectors.toList())
        ));
    }

    private String formatLine(TimerRecord t) {
        return "ID: " + t.getId()
                + " | Label: " + t.getLabel()
                + " | Start: " + dtf.format(t.getStartTime())
                + " | End: " + dtf.format(t.getEndTime())
                + " | Time Elapsed: " + FormattingElapsedTime(t.getElapsedSeconds());
    }

    private String FormattingElapsedTime(long millis) {
        long s = Math.max(0, millis / 1000);
        long h = s / 3600;
        long m = (s % 3600) / 60;
        long sec = s % 60;
        return (h > 0)
                ? String.format("%02d:%02d:%02d", h, m, sec)
                : String.format("%02d:%02d", m, sec);
    }
}
