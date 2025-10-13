package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.MainDistractionDAO;
import group13.demo1.model.MainDistractionHistoryDAO;
import group13.demo1.model.SqliteConnection;
import group13.demo1.model.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TimerHistory {

    // ---------- TAB 1 (Timer sessions) ----------
    @FXML private ListView<TimerHistoryLogic.SessionData> sessionList;
    @FXML private Label totalsession;
    @FXML private Label totalTime;
    @FXML private Label session;

    @FXML private Label DateValue, StartValue, EndValue, FocusTimeValue, PausedTimeValue, PauseCountValue;

    private final Connection db = SqliteConnection.getInstance();
    private final TimerHistoryLogic logic = new TimerHistoryLogic();
    private final ObservableList<TimerHistoryLogic.SessionData> sessions = FXCollections.observableArrayList();

    // ---------- TAB 2 (Main Distraction history) ----------
    @FXML private ListView<MainDistractionRow> mdList;
    @FXML private Label mdTotalLabel;
    @FXML private Label mdDetail;

    @FXML private Label mdReason, mdWhenTimeValue, mdMinutesValue, mdNotes;

    private final ObservableList<MainDistractionRow> mdItems = FXCollections.observableArrayList();
    private final MainDistractionHistoryDAO mdDAO = new MainDistractionHistoryDAO(SqliteConnection.getInstance());

    // ====== DTO for the Distraction tab======
    public static class MainDistractionRow {
        public final int id;
        public final String reason;
        public final String time_of_occurence;
        public final Integer minutes;
        public final String notes;

        public MainDistractionRow(int id, String cause, String when, Integer minutes, String notes) {
            this.id = id;
            this.reason = (cause == null || cause.isBlank()) ? "(untitled)" : cause;
            this.time_of_occurence = (when == null || when.isBlank()) ? null : when;
            this.minutes = minutes;
            this.notes = (notes == null || notes.isBlank()) ? null : notes;
        }

        public String listTitle(int index) { return "Distraction " + (index + 1) + " — " + reason; }
    }

    private static final DateTimeFormatter dt_formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DistractionTable SessionTable(Connection db) {
        List<String> userCols  = List.of("username","user","user_name","userid","user_id");
        List<String> startCols = List.of("startTime","start","started_at","start_time");
        List<String> endCols   = List.of("endTime","end","ended_at","end_time");
        List<String> runCols   = List.of("totalRunSeconds","runSeconds","focusSeconds","total_run_seconds","elapsedSeconds");
        List<String> pauseCols = List.of("totalPauseSeconds","pauseSeconds","total_pause_seconds");
        List<String> cntCols   = List.of("pauseCount","pauses","pause_count");
        List<String> preferredTables = List.of("sessions","session","timerSessions","timer_sessions","timers");

        try (Statement st = db.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {
            Set<String> allTables = new LinkedHashSet<>();
            while (rs.next()) allTables.add(rs.getString(1));

            List<String> ordered = new ArrayList<>(preferredTables);
            for (String t : allTables) if (!ordered.contains(t)) ordered.add(t);

            for (String table : ordered) {
                if (!allTables.contains(table)) continue;

                Map<String,String> present = new HashMap<>();
                try (Statement st2 = db.createStatement();
                     ResultSet ti = st2.executeQuery("PRAGMA table_info(" + table + ")")) {
                    while (ti.next()) present.put(ti.getString("name").toLowerCase(Locale.ROOT), ti.getString("name"));
                }

                String colId = present.getOrDefault("id", present.getOrDefault("rowid", null));
                String colUser  = firstPresent(present, userCols);
                String colStart = firstPresent(present, startCols);
                String colEnd   = firstPresent(present, endCols);
                String colRun   = firstPresent(present, runCols);
                String colPause = firstPresent(present, pauseCols);
                String colCnt   = firstPresent(present, cntCols);

                if (colStart != null && colRun != null && colPause != null && colCnt != null) {
                    if (colId == null) colId = "rowid";
                    return new DistractionTable(table, colId, colUser, colStart, colEnd, colRun, colPause, colCnt);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private static class DistractionTable {
        final String table;
        final String colId;
        final String colUsername;
        final String colStart;
        final String colEnd;
        final String colRun;    // focus time
        final String colPause;  // paused seconds
        final String colCount;  // pause count
        DistractionTable(String table, String colId, String colUsername,
                  String colStart, String colEnd, String colRun, String colPause, String colCount) {
            this.table = table; this.colId = colId; this.colUsername = colUsername;
            this.colStart = colStart; this.colEnd = colEnd; this.colRun = colRun;
            this.colPause = colPause; this.colCount = colCount;
        }
    }

    private static String firstPresent(Map<String,String> present, List<String> candidates) {
        for (String c : candidates) { String hit = present.get(c.toLowerCase(Locale.ROOT)); if (hit != null) return hit; }
        return null;
    }

    @FXML
    public void initialize() {
        String user = (UserSession.getInstance() == null) ? null : UserSession.getInstance().getUsername();

        // ----- Timer sessions -----
        if (sessionList != null) {
            sessionList.setItems(sessions);
            sessionList.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(TimerHistoryLogic.SessionData s, boolean empty) {
                    super.updateItem(s, empty);
                    setText(empty || s == null ? null : logic.listForSession(getIndex(), s));
                }
            });
            sessionList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, s) -> {
                session.setText(s == null ? "(none)" : logic.SelectedSessionText(
                        sessionList.getSelectionModel().getSelectedIndex(), s));
                SelectedSessionDisplay(s);
            });
        }

        loadSessionsForUser(user);
        // ----- Distraction history -----
        MainDistractionList();
        loadMainDistractions(user);
    }

    private void SelectedSessionDisplay(TimerHistoryLogic.SessionData s) {
        if (s == null) {
            DateValue.setText("—");
            StartValue.setText("—");
            EndValue.setText("—");
            FocusTimeValue.setText("—");
            PausedTimeValue.setText("—");
            PauseCountValue.setText("—");
            return;
        }
        DateValue.setText(s.start == null ? "—" : DateTimeFormatter.ofPattern("MMM d,  yyyy").format(s.start));
        StartValue.setText(s.start == null ? "—" : DateTimeFormatter.ofPattern("hh:mm:ss a").format(s.start));
        EndValue.setText(s.end == null ? "—" : DateTimeFormatter.ofPattern("hh:mm:ss a").format(s.end));
        FocusTimeValue.setText(logic.formatElapsedTime(s.focustime));
        PausedTimeValue.setText(logic.formatElapsedTime(s.pausetime));
        PauseCountValue.setText(Integer.toString(s.pausecount));
    }

    private void loadSessionsForUser(String username) {
        sessions.clear();

        DistractionTable spec = SessionTable(db);
        if (spec == null) {
            totalsession.setText("Total Sessions: 0");
            totalTime.setText("Total Focus Time: 0s");
            session.setText("(none)");
            SelectedSessionDisplay(null);
            return;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
                .append(spec.colId).append(" AS id, ")
                .append(spec.colStart).append(" AS startTime, ")
                .append(spec.colEnd).append(" AS endTime, ")
                .append("COALESCE(").append(spec.colRun).append(",0)  AS runSecs, ")
                .append("COALESCE(").append(spec.colPause).append(",0) AS pauseSecs, ")
                .append("COALESCE(").append(spec.colCount).append(",0) AS pauseCnt ");
        if (spec.colUsername != null) sql.append(", ").append(spec.colUsername).append(" AS username ");
        else sql.append(", '' AS username ");
        sql.append("FROM ").append(spec.table).append(" ");

        boolean filterByUser = (spec.colUsername != null && username != null && !username.isBlank());

        String nonZeroPredicate = "(COALESCE(" + spec.colRun + ",0) > 0 OR " + " COALESCE(" + spec.colPause + ",0) > 0 OR " + " COALESCE(" + spec.colCount + ",0) > 0)";

        if (filterByUser) {
            sql.append("WHERE ").append(spec.colUsername).append("=? AND ").append(nonZeroPredicate).append(" ");
        } else {
            sql.append("WHERE ").append(nonZeroPredicate).append(" ");
        }

        sql.append("ORDER BY ").append(spec.colStart).append(" DESC");

        long totalFocus = 0L;
        List<TimerHistoryLogic.SessionData> rows = new ArrayList<>();

        try (PreparedStatement ps = db.prepareStatement(sql.toString())) {
            if (filterByUser) ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String userCol = rs.getString("username");
                    LocalDateTime start = TimeParse(rs.getString("startTime"));
                    LocalDateTime end   = TimeParse(rs.getString("endTime"));
                    long focus  = rs.getLong("runSecs");
                    long paused = rs.getLong("pauseSecs");
                    int pauses  = rs.getInt("pauseCnt");

                    rows.add(new TimerHistoryLogic.SessionData(id, userCol, start, end, focus, paused, pauses));
                    totalFocus += focus;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        sessions.setAll(rows);
        totalsession.setText("Total Sessions: " + sessions.size());
        totalTime.setText("Total Focus Time: " + logic.formatTotal(totalFocus));

        if (sessions.isEmpty()) { session.setText("(none)"); SelectedSessionDisplay(null); }
        else sessionList.getSelectionModel().select(0);
    }

    private static LocalDateTime TimeParse(String s) {
        try { return (s == null || s.isBlank()) ? null : LocalDateTime.parse(s, dt_formatter); }
        catch (Exception e) { return null; }
    }

    @FXML
    private void onConfirm() {
        if (sessionList != null && sessionList.getSelectionModel().getSelectedIndex() >= 0) {
            sessionList.getSelectionModel().clearSelection();
            session.setText("(none)");
            SelectedSessionDisplay(null);
        }
    }

    @FXML
    private void onDelete() {
        int idx = (sessionList == null) ? -1 : sessionList.getSelectionModel().getSelectedIndex();
        if (idx < 0) return;

        TimerHistoryLogic.SessionData s = sessions.get(idx);

        DistractionTable spec = SessionTable(db);
        if (spec == null) return;

        String sql = "DELETE FROM " + spec.table + " WHERE " + spec.colId + "=?";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setInt(1, s.id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }

        String user = (UserSession.getInstance() == null) ? null : UserSession.getInstance().getUsername();
        loadSessionsForUser(user);
    }

    // ---------------- Main Distraction ----------------
    private void MainDistractionList() {
        if (mdList == null) return;

        mdList.setItems(mdItems);
        mdList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(MainDistractionRow row, boolean empty) {
                super.updateItem(row, empty);
                setText(empty || row == null ? null : row.listTitle(getIndex()));
            }
        });
        mdList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, row) -> MainDistractionSelectedRecord(row));
    }

    private void MainDistractionSelectedRecord(MainDistractionRow row) {
        if (row == null) {
            mdReason.setText("—");
            mdWhenTimeValue.setText("—");
            mdMinutesValue.setText("—");
            mdNotes.setText("—");
            if (mdDetail != null) mdDetail.setText("");
            return;
        }
        mdReason.setText(row.reason == null ? "—" : row.reason);
        mdWhenTimeValue.setText(row.time_of_occurence == null ? "—" : row.time_of_occurence);
        mdMinutesValue.setText(row.minutes == null ? "—" : row.minutes + "m");
        mdNotes.setText(row.notes == null ? "—" : row.notes);
        if (mdDetail != null) mdDetail.setText("");
    }

    private void loadMainDistractions(String username) {
        if (username == null || username.isBlank() || mdItems == null) return;
        mdItems.clear();
        List<MainDistractionDAO.MainItem> all = mdDAO.getAllForUser(username);
        for (MainDistractionDAO.MainItem it : all) {
            mdItems.add(new MainDistractionRow(
                    it.id,
                    it.cause,
                    it.timestamp,
                    it.minutes,
                    it.description
            ));
        }
        if (mdTotalLabel != null) mdTotalLabel.setText("Total Records: " + mdItems.size());
        if (!mdItems.isEmpty() && mdList != null) mdList.getSelectionModel().select(0);
        else MainDistractionSelectedRecord(null);
    }
}
