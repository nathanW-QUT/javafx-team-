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

    @FXML private ListView<TimerRecord> list;
    @FXML private Label selectedLabel;
    @FXML private Label totalLabel;


    private final ITimerDAO dao = new SqliteTimerDAO();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private ObservableList<TimerRecord> items;

    @FXML
    public void initialize() {
        String user = UserSession.getInstance().getUsername();


        List<TimerRecord> rows = dao.getTimersForUser(user);
        rows.sort((a, b) -> b.getStartTime().compareTo(a.getStartTime()));

        items = FXCollections.observableArrayList(rows);
        list.setItems(items);
        totalLabel.setText("Total Distractions: " + items.size());
        items.addListener((ListChangeListener<TimerRecord>) c ->
                totalLabel.setText("Total Distractions: " + items.size()));

        list.setPlaceholder(new Label("No timer sessions yet."));

        // Simple row text
        list.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(TimerRecord t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) {
                    setText(null);
                    return;
                }
                int n = getIndex() + 1;                 // 1-based display number
                setText("Distraction " + n + "  •  " + t.getLabel());
            }
        });

        list.getSelectionModel().selectedItemProperty().addListener((obs, oldV, t) -> {
            if (t == null) {
                selectedLabel.setText("(none)");
            } else {
                int n = list.getSelectionModel().getSelectedIndex() + 1;  // 1-based
                long secs = elapsedSecondsFromTimes(t);
                selectedLabel.setText(
                        "Distraction " + n + "  •  " + t.getLabel() + "  •  " + formatElapsedTime(secs) + "\n" +
                                dtf.format(t.getStartTime()) + "  →  " + dtf.format(t.getEndTime())
                );
            }
        });

        if (!items.isEmpty()) list.getSelectionModel().select(0);
        else selectedLabel.setText("(none)");
    }

    @FXML
    private void onConfirm() {
        int i = list.getSelectionModel().getSelectedIndex();
        if (i < 0) return;
        if (i < items.size() - 1) list.getSelectionModel().select(i + 1);
    }

    @FXML
    private void onDelete() {
        int i = list.getSelectionModel().getSelectedIndex();
        if (i < 0) return;
        TimerRecord t = items.get(i);

        dao.deleteTimer(t);
        items.remove(i);
        list.refresh();

        if (items.isEmpty()) {
            selectedLabel.setText("(none)");
            return;
        }
        list.getSelectionModel().select(Math.min(i, items.size() - 1));
    }


    @FXML
    private void onBackHome() throws IOException {
        Stage stage = (Stage) list.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }

    private long elapsedSecondsFromTimes(TimerRecord t) {
        long secs = Duration.between(t.getStartTime(), t.getEndTime()).getSeconds();
        return Math.max(0, secs);
    }

    private String formatElapsedTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return (h > 0)
                ? String.format("%02d:%02d:%02d", h, m, s)
                : String.format("%02d:%02d", m, s);
    }
}
