package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.ITimerDAO;
import group13.demo1.model.SqliteConnection;
import group13.demo1.model.SqliteTimerDAO;
import group13.demo1.model.TimerRecord;
import group13.demo1.model.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Timer History screen with two tabs:
 *  • Timer sessions (aggregated between Resets; first non-”Pause” tag becomes the session label)
 *  • Distraction history (rows from maindistraction)
 */
public class TimerHistory {

    // TAB 1: sessions
    @FXML private ListView<TimerHistoryLogic.AggregatedSession> sessionsList;
    @FXML private Label totalSessionsLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Label sessionDetail;
    @FXML private Button deleteBtn;

    // TAB 2: main distractions
    @FXML private ListView<String> mdList;
    @FXML private Label mdTotalLabel;
    @FXML private Label mdDetail;

    private final ITimerDAO dao = new SqliteTimerDAO();
    private final TimerHistoryLogic logic = new TimerHistoryLogic();
    private final Connection db = SqliteConnection.getInstance();

    private ObservableList<TimerRecord> rawItems; // raw timers
    private ObservableList<TimerHistoryLogic.AggregatedSession> sessions;

    @FXML
    public void initialize() {
        // ---- Load sessions (aggregate from timers) ----
        String user = UserSession.getInstance().getUsername();
        List<TimerRecord> rows = dao.getTimersForUser(user);
        logic.sortNewestFirst(rows);

        rawItems = FXCollections.observableArrayList(rows);
        List<TimerHistoryLogic.AggregatedSession> agg = logic.aggregateIntoSessions(new ArrayList<>(rows));
        sessions = FXCollections.observableArrayList(agg);

        sessionsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(TimerHistoryLogic.AggregatedSession s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : logic.listRowForSession(getIndex(), s));
            }
        });
        sessionsList.setItems(sessions);

        totalSessionsLabel.setText("Total Sessions: " + sessions.size());
        updateTotal();
        rawItems.addListener((ListChangeListener<TimerRecord>) c -> updateTotal());

        sessionsList.getSelectionModel().selectedItemProperty().addListener((obs, ov, s) -> {
            deleteBtn.setDisable(s == null);
            if (s == null) {
                sessionDetail.setText("(none)");
            } else {
                int index = sessionsList.getSelectionModel().getSelectedIndex();
                sessionDetail.setText(logic.selectedSessionText(index, s));
            }
        });
        if (!sessions.isEmpty()) sessionsList.getSelectionModel().select(0);
        else sessionDetail.setText("(none)");

        // ---- Load main distraction history list ----
        loadMainDistractions(user);

        mdList.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            mdDetail.setText(nv == null ? "(none)" : nv);
        });
        if (!mdList.getItems().isEmpty()) mdList.getSelectionModel().select(0);
        else mdDetail.setText("(none)");
    }

    private void loadMainDistractions(String username) {
        ObservableList<String> items = FXCollections.observableArrayList();
        final String sql = "SELECT timestamp, cause, minutes, description " +
                "FROM maindistraction WHERE username=? " +
                "ORDER BY datetime(timestamp) DESC";
        int count = 0;
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ts   = rs.getString("timestamp");
                    String cause = rs.getString("cause");
                    int minutes  = rs.getInt("minutes");
                    String desc  = rs.getString("description");
                    if (cause == null || cause.isBlank()) cause = "(untitled)";
                    String row = String.format("%s — %s (%dm)%s",
                            ts, cause, minutes,
                            (desc == null || desc.isBlank()) ? "" : " — " + desc);
                    items.add(row);
                    count++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        mdList.setItems(items);
        mdTotalLabel.setText("Total Records: " + count);
    }

    private void updateTotal() {
        long totalSecs = logic.TotalSeconds(rawItems);
        totalTimeLabel.setText("Total Distracted Time: " + logic.formatTotal(totalSecs));
    }

    /** Clear selection in the sessions tab (name matches FXML). */
    @FXML
    private void onConfirmSession() {
        sessionsList.getSelectionModel().clearSelection();
        deleteBtn.setDisable(true);
        sessionDetail.setText("(none)");
    }

    /** Delete the whole aggregated session (all underlying timer rows). */
    @FXML
    private void onDelete() {
        int i = sessionsList.getSelectionModel().getSelectedIndex();
        if (i < 0) return;

        TimerHistoryLogic.AggregatedSession agg = sessions.get(i);

        // Delete all underlying timer rows
        for (Integer id : agg.sourceIds) {
            TimerRecord stub = new TimerRecord(agg.username, agg.label, agg.start, agg.end, agg.totalSeconds);
            stub.setId(id);
            dao.deleteTimer(stub);
        }

        // Refresh lists
        String user = UserSession.getInstance().getUsername();
        List<TimerRecord> rows = dao.getTimersForUser(user);
        rawItems.setAll(rows);

        List<TimerHistoryLogic.AggregatedSession> newAgg = logic.aggregateIntoSessions(new ArrayList<>(rows));
        sessions.setAll(newAgg);
        totalSessionsLabel.setText("Total Sessions: " + sessions.size());

        if (sessions.isEmpty()) {
            onConfirmSession();
            return;
        }
        int next = Math.min(i, sessions.size() - 1);
        if (next >= 0) sessionsList.getSelectionModel().select(next);
    }

    @FXML
    private void onBackHome() throws IOException {
        Stage stage = (Stage) sessionsList.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }
}
