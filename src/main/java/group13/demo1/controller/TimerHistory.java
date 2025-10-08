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
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Timer History screen.
 *  - Tab 1:Timer sessions (unchanged).
 *  - Tab 2: Main Distraction history.
 */
public class TimerHistory {

    // ---------- TAB 1 (Sessions) ----------
    @FXML private ListView<TimerHistoryLogic.AggregatedSession> sessionList;
    @FXML private Label session;
    @FXML private Label totalsession;
    @FXML private Label totalTime;


    @FXML private ListView<TimerRecord> list;

    private final ITimerDAO dao = new SqliteTimerDAO();
    private final TimerHistoryLogic logic = new TimerHistoryLogic();

    private ObservableList<TimerRecord> rawItems;
    private ObservableList<TimerHistoryLogic.AggregatedSession> sessions;

    // ---------- TAB 2 (Main Distraction history) ----------
    @FXML private ListView<MainDistractionRow> mdList;
    @FXML private Label mdTotalLabel;
    @FXML private Label mdDetail;

    private final Connection db = SqliteConnection.getInstance();
    private final ObservableList<MainDistractionRow> mdItems = FXCollections.observableArrayList();


    public static class MainDistractionRow {
        public final int id;
        public final String reason;
        public final String time_of_occurence;     // nullable – formatted timestamp string
        public final Integer minutes; // nullable
        public final String notes;    // nullable

        public MainDistractionRow(int id, String cause, String when, Integer minutes, String notes) {
            this.id = id;
            this.reason = (cause == null || cause.isBlank()) ? "(untitled)" : cause;
            this.time_of_occurence = (when == null || when.isBlank()) ? null : when;
            this.minutes = minutes;
            this.notes = (notes == null || notes.isBlank()) ? null : notes;
        }

        public String listTitle(int indexZeroBased) {
            return "Distraction " + (indexZeroBased + 1) + " — " + reason;
        }

        public String detailText() {
            StringBuilder sb = new StringBuilder();
            sb.append("reason: ").append(reason);
            if (reason != null)   sb.append("\nTime of Occurence: ").append(time_of_occurence);
            if (minutes != null) sb.append("\nDuration: ").append(minutes).append("m");
            if (notes != null)  sb.append("\nDescription: ").append(notes);
            return sb.toString();
        }
    }

    @FXML
    public void initialize() {
        // ----- sessions list -----
        String user = UserSession.getInstance().getUsername();
        List<TimerRecord> rows = dao.getTimersForUser(user);
        logic.sortNewestFirst(rows);

        rawItems = FXCollections.observableArrayList(rows);

        List<TimerHistoryLogic.AggregatedSession> agg =
                logic.aggregateIntoSessions(new ArrayList<>(rows));
        sessions = FXCollections.observableArrayList(agg);

        if (sessionList != null) {
            sessionList.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(TimerHistoryLogic.AggregatedSession s, boolean empty) {
                    super.updateItem(s, empty);
                    setText(empty || s == null ? null : logic.listRowForSession(getIndex(), s));
                }
            });
            sessionList.setItems(sessions);
        }

        totalsession.setText("Total Sessions: " + sessions.size());

        updateTotal();
        rawItems.addListener((ListChangeListener<TimerRecord>) c -> updateTotal());

        if (sessionList != null) {
            sessionList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, s) -> {
                session.setText(s == null ? "(none)" :
                        logic.selectedSessionText(sessionList.getSelectionModel().getSelectedIndex(), s));
            });
        }
        if (sessions.isEmpty()) session.setText("(none)");
        else sessionList.getSelectionModel().select(0);

        // -----main distraction history -----
        wireMainDistractionList();
        loadMainDistractions(user);
    }


    private void updateTotal() {
        long totalSecs = logic.TotalSeconds(rawItems);
        if (totalTime != null) {
            totalTime.setText("Total Distracted Time: " + logic.formatTotal(totalSecs));
        }
    }

    @FXML
    private void onConfirm() {
        if (sessionList != null && sessionList.getSelectionModel().getSelectedIndex() >= 0) {
            sessionList.getSelectionModel().clearSelection();
            session.setText("(none)");
        }
    }

    @FXML
    private void onDelete() {
        int i = (sessionList == null) ? -1 : sessionList.getSelectionModel().getSelectedIndex();
        if (i < 0) return;

        TimerHistoryLogic.AggregatedSession agg = sessions.get(i);


        for (Integer id : agg.sourceIds) {
            TimerRecord stub = new TimerRecord(agg.username, agg.label, agg.start, agg.end, agg.totalSeconds);
            stub.setId(id);
            dao.deleteTimer(stub);
        }


        String user = UserSession.getInstance().getUsername();
        List<TimerRecord> rows = dao.getTimersForUser(user);
        rawItems.setAll(rows);

        List<TimerHistoryLogic.AggregatedSession> newAgg =
                logic.aggregateIntoSessions(new ArrayList<>(rows));
        sessions.setAll(newAgg);
        totalsession.setText("Total Sessions: " + sessions.size());

        if (sessions.isEmpty()) {
            session.setText("(none)");
            return;
        }
        int next = Math.min(i, sessions.size() - 1);
        if (next >= 0) sessionList.getSelectionModel().select(next);
    }

    // ---------------- Main Distraction ----------------
    private void wireMainDistractionList() {
        if (mdList == null) return;

        mdList.setItems(mdItems);
        mdList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(MainDistractionRow row, boolean empty) {
                super.updateItem(row, empty);
                setText(empty || row == null ? null : row.listTitle(getIndex()));
            }
        });

        mdList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, row) -> {
            if (mdDetail == null) return;
            mdDetail.setText(row == null ? "" : row.detailText());
        });
    }

    private void loadMainDistractions(String username) {
        if (username == null || username.isBlank() || mdItems == null) return;
        mdItems.clear();


        final String sql =
                "SELECT * FROM maindistraction WHERE username=? ORDER BY id DESC";

        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                while (rs.next()) {
                    int id = safeGetInt(rs, md, "id", "rowid");
                    String cause = safeGet(rs, md, "cause", "tag", "title");


                    String when =
                            firstNonBlank(
                                    safeGet(rs, md, "created_at"),
                                    safeGet(rs, md, "timestamp"),
                                    safeGet(rs, md, "date"),
                                    safeGet(rs, md, "loggedAt"),
                                    safeGet(rs, md, "time")
                            );


                    Integer minutes = firstNonNullInt(
                            safeGetIntNullable(rs, md, "minutes"),
                            safeGetIntNullable(rs, md, "duration"),
                            safeGetIntNullable(rs, md, "durationMinutes"),
                            safeGetIntNullable(rs, md, "duration_min")
                    );


                    String notes =
                            firstNonBlank(
                                    safeGet(rs, md, "notes"),
                                    safeGet(rs, md, "note"),
                                    safeGet(rs, md, "description"),
                                    safeGet(rs, md, "details")
                            );

                    mdItems.add(new MainDistractionRow(id, cause, when, minutes, notes));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (mdTotalLabel != null) mdTotalLabel.setText("Total Records: " + mdItems.size());
        if (!mdItems.isEmpty() && mdList != null) mdList.getSelectionModel().select(0);
    }


    private static String safeGet(ResultSet rs, ResultSetMetaData md, String... cols) throws SQLException {
        for (String c : cols) {
            if (hasColumn(md, c)) {
                String v = rs.getString(c);
                if (v != null && !v.isBlank()) return v;
            }
        }
        return null;
    }

    private static Integer safeGetInt(ResultSet rs, ResultSetMetaData md, String... cols) throws SQLException {
        for (String c : cols) {
            if (hasColumn(md, c)) {
                try {
                    return rs.getInt(c);
                } catch (SQLException ignore) {}
            }
        }
        return 0;
    }

    private static Integer safeGetIntNullable(ResultSet rs, ResultSetMetaData md, String col) throws SQLException {
        if (!hasColumn(md, col)) return null;
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    private static boolean hasColumn(ResultSetMetaData md, String name) throws SQLException {
        int n = md.getColumnCount();
        for (int i = 1; i <= n; i++) {
            if (name.equalsIgnoreCase(md.getColumnLabel(i)) || name.equalsIgnoreCase(md.getColumnName(i))) {
                return true;
            }
        }
        return false;
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }

    private static Integer firstNonNullInt(Integer... vals) {
        for (Integer v : vals) if (v != null) return v;
        return null;
    }


    @FXML
    private void onBackHome() throws IOException {
        Stage stage = (Stage) (sessionList != null ? sessionList.getScene().getWindow() : list.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }
}
