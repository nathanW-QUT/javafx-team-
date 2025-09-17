package group13.demo1.controller;
import group13.demo1.model.UserSession;
import group13.demo1.HelloApplication;
import group13.demo1.model.ITimerDAO;
import group13.demo1.model.SqliteTimerDAO;
import group13.demo1.model.TimerRecord;
import javafx.collections.ListChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TimerHistory {

    @FXML private Label selectedHeader;
    @FXML private ListView<TimerRecord> list;
    @FXML private Label selectedLabel;
    @FXML private Label totalLabel;
    @FXML private Label totalTimeLabel;

    private final ITimerDAO dao = new SqliteTimerDAO();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
    private final DateTimeFormatter dateformatted = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private final DateTimeFormatter timeformatted = DateTimeFormatter.ofPattern("hh:mm:ss a");

    private ObservableList<TimerRecord> items;

    @FXML
    public void initialize() {
        String user = UserSession.getInstance().getUsername();

        List<TimerRecord> rows = dao.getTimersForUser(user);
        rows.sort((a, b) -> b.getStartTime().compareTo(a.getStartTime()));

        items = FXCollections.observableArrayList(rows);

        list.setCellFactory(lv -> new ListCell<TimerRecord>() {
            @Override protected void updateItem(TimerRecord t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null)
                {
                    setText(null);
                } else
                {
                    int n = getIndex() + 1;
                    setText("Timer " + n + "  •  " + t.getLabel());
                }
            }
        });

        list.setItems(items);
        list.setPlaceholder(new Label("No timer sessions yet."));

        totalLabel.setText("Total Timer Sessions: " + items.size());
        items.addListener((ListChangeListener<TimerRecord>) c ->
                totalLabel.setText("Total Timers: " + items.size()));
        updateTotals();
        items.addListener((ListChangeListener<TimerRecord>) c -> updateTotals());

        list.getSelectionModel().selectedItemProperty().addListener((obs, oldV, t) -> {
            if (t == null) {
                selectedLabel.setText("(none)");
            } else {
                int n = list.getSelectionModel().getSelectedIndex() + 1;
                long secs = elapsedSecondsFromTimes(t);


                String range;
                boolean sameDay = t.getStartTime().toLocalDate().equals(t.getEndTime().toLocalDate());
                if (sameDay) {

                    range = dateformatted.format(t.getStartTime()) + "  •  "
                            + timeformatted.format(t.getStartTime()) + "  →  "
                            + timeformatted.format(t.getEndTime());
                } else {

                    range = dtf.format(t.getStartTime()) + "  →  " + dtf.format(t.getEndTime());
                }

                selectedLabel.setText(
                        "Timer " + n + "  •  " + t.getLabel() + "  •  " + range + "  •  " + formatElapsedTime(secs)
                );
            }
        });

        if (!items.isEmpty()) list.getSelectionModel().select(0);
        else selectedLabel.setText("(none)");
    }
    private void updateTotals() {
        long totalSecs = 0L;
        for (TimerRecord r : items) {
            totalSecs += r.getElapsedSeconds(); // already stored in seconds
        }
        if (totalTimeLabel != null) {
            totalTimeLabel.setText("Total Distraction Time: " + formatTotal(totalSecs));
        }
    }

    @FXML
    private void onConfirm()
    {

        if (list.getSelectionModel().getSelectedIndex() >= 0)
        {
            list.getSelectionModel().clearSelection();
            list.getFocusModel().focus(-1);
            selectedLabel.setText("(none)");
        }
    }

    @FXML
    private void onDelete()
    {
        int i = list.getSelectionModel().getSelectedIndex();
        if (i < 0) return;
        TimerRecord t = items.get(i);

        dao.deleteTimer(t);
        items.remove(i);
        list.refresh();
        updateTotals();

        if (items.isEmpty())
        {
            selectedLabel.setText("(none)");
            return;
        }
        list.getSelectionModel().select(Math.min(i, items.size() - 1));
    }

    @FXML
    private void onBackHome() throws IOException
    {
        Stage stage = (Stage) list.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }

    private long elapsedSecondsFromTimes(TimerRecord t)
    {
        long secs = Duration.between(t.getStartTime(), t.getEndTime()).getSeconds();
        return Math.max(0, secs);
    }

    private String formatElapsedTime(long seconds)
    {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return (h > 0)
                ? String.format("%02dh:%02dm:%02ds", h, m, s)
                : String.format("%02d:%02d s", m, s);
    }

    private String formatTotal(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;

        if (h > 0)  return String.format("%dh %02dm %02ds", h, m, s);
        if (m > 0)  return String.format("%dm %02ds", m, s);
        return String.format("%ds", s);
    }
}
