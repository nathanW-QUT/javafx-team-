package group13.demo1.controller;

import group13.demo1.HelloApplication;
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

/**
 * Timer History screen (sessions + main distraction history).
 * Sessions are read directly from the DB table that contains:
 *   startTime, endTime, totalRunSeconds, totalPauseSeconds, pauseCount
 * The table/column names are auto-detected (common variants supported).
 * No label/tag is displayed anywhere.
 */
public class TimerHistory {

    // ---------- TAB 1 (Timer sessions) ----------
    @FXML private ListView<TimerHistoryLogic.ViewSession> sessionList;
    @FXML private Label totalsession;
    @FXML private Label totalTime;
    @FXML private Label session;
    @FXML private Button deleteBtn;

    private final Connection db = SqliteConnection.getInstance();
    private final TimerHistoryLogic logic = new TimerHistoryLogic();
    private final ObservableList<TimerHistoryLogic.ViewSession> sessions = FXCollections.observableArrayList();

    // ---------- TAB 2 (Main Distraction history) ----------
    @FXML private ListView<MainDistractionRow> mdList;
    @FXML private Label mdTotalLabel;
    @FXML private Label mdDetail;

    private final ObservableList<MainDistractionRow> mdItems = FXCollections.observableArrayList();

    // ====== DTO for the Distraction tab (unchanged) ======
    public static class MainDistractionRow {
        public final int id;
        public final String reason;
        public final String time_of_occurence;   // nullable – formatted timestamp string
        public final Integer minutes;            // nullable
        public final String notes;               // nullable

        public MainDistractionRow(int id, String cause, String when, Integer minutes, String notes) {
            this.id = id;
            this.reason = (cause == null || cause.isBlank()) ? "(untitled)" : cause;
            this.time_of_occurence = (when == null || when.isBlank()) ? null : when;
            this.minutes = minutes;
            this.notes = (notes == null || notes.isBlank()) ? null : notes;
        }

        public String listTitle(int indexZeroBased) { return "Distraction " + (indexZeroBased + 1) + " — " + reason; }

        public String detailText() {
            StringBuilder sb = new StringBuilder();
            sb.append("reason: ").append(reason);
            if (reason != null)   sb.append("\nTime of Occurence: ").append(time_of_occurence);
            if (minutes != null)  sb.append("\nDuration: ").append(minutes).append("m");
            if (notes != null)    sb.append("\nDescription: ").append(notes);
            return sb.toString();
        }
    }

    // ========= Auto-detect table/columns for the sessions view =========
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static class TableSpec {
        final String table;
        final String colId;
        final String colUsername;
        final String colStart;
        final String colEnd;
        final String colRun;    // focus seconds
        final String colPause;  // paused seconds
        final String colCount;  // pause count

        TableSpec(String table, String colId, String colUsername,
                  String colStart, String colEnd, String colRun, String colPause, String colCount) {
            this.table = table;
            this.colId = colId;
            this.colUsername = colUsername;
            this.colStart = colStart;
            this.colEnd = colEnd;
            this.colRun = colRun;
            this.colPause = colPause;
            this.colCount = colCount;
        }
    }

    private TableSpec detectSessionTableSpec(Connection db) {
        // Accept common variations of column names
        List<String> userCols  = List.of("username","user","user_name","userid","user_id");
        List<String> startCols = List.of("startTime","start","started_at","start_time");
        List<String> endCols   = List.of("endTime","end","ended_at","end_time");
        List<String> runCols   = List.of("totalRunSeconds","runSeconds","focusSeconds","total_run_seconds","elapsedSeconds");
        List<String> pauseCols = List.of("totalPauseSeconds","pauseSeconds","total_pause_seconds");
        List<String> cntCols   = List.of("pauseCount","pauses","pause_count");

        // Prefer obvious table names first
        List<String> preferredTables = List.of("sessions","session","timerSessions","timer_sessions","timers");

        try (Statement st = db.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {
            Set<String> allTables = new LinkedHashSet<>();
            while (rs.next()) allTables.add(rs.getString(1));

            // Reorder so preferred tables are checked first
            List<String> ordered = new ArrayList<>(preferredTables);
            for (String t : allTables) if (!ordered.contains(t)) ordered.add(t);

            for (String table : ordered) {
                if (!allTables.contains(table)) continue;

                Map<String,String> present = new HashMap<>();
                try (Statement st2 = db.createStatement();
                     ResultSet ti = st2.executeQuery("PRAGMA table_info(" + table + ")")) {
                    while (ti.next()) {
                        String name = ti.getString("name");
                        present.put(name.toLowerCase(Locale.ROOT), name);
                    }
                }

                String colId = present.getOrDefault("id", present.getOrDefault("rowid", null));
                String colUser  = firstPresent(present, userCols);
                String colStart = firstPresent(present, startCols);
                String colEnd   = firstPresent(present, endCols);
                String colRun   = firstPresent(present, runCols);
                String colPause = firstPresent(present, pauseCols);
                String colCnt   = firstPresent(present, cntCols);

                // Require the three key metrics + start time to exist
                if (colStart != null && colRun != null && colPause != null && colCnt != null) {
                    if (colId == null) colId = "rowid"; // SQLite fallback
                    return new TableSpec(table, colId, colUser, colStart, colEnd, colRun, colPause, colCnt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // not found
    }

    private static String firstPresent(Map<String,String> present, List<String> candidates) {
        for (String c : candidates) {
            String hit = present.get(c.toLowerCase(Locale.ROOT));
            if (hit != null) return hit;
        }
        return null;
    }

    // ======================================================

    @FXML
    public void initialize() {
        String user = (UserSession.getInstance() == null) ? null : UserSession.getInstance().getUsername();

        // ----- Timer sessions (auto-detected table) -----
        if (sessionList != null) {
            sessionList.setItems(sessions);
            sessionList.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(TimerHistoryLogic.ViewSession s, boolean empty) {
                    super.updateItem(s, empty);
                    setText(empty || s == null ? null : logic.listRowForViewSession(getIndex(), s));
                }
            });
            sessionList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, s) -> {
                session.setText(s == null ? "(none)" :
                        logic.selectedViewSessionText(sessionList.getSelectionModel().getSelectedIndex(), s));
            });
        }

        loadSessionsForUser(user);

        // ----- Distraction history (unchanged) -----
        wireMainDistractionList();
        loadMainDistractions(user);
    }

    private void loadSessionsForUser(String username) {
        sessions.clear();

        TableSpec spec = detectSessionTableSpec(db);
        if (spec == null) {
            totalsession.setText("Total Sessions: 0");
            totalTime.setText("Total Focus Time: 0s");
            session.setText("(none)");
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
        if (filterByUser) sql.append("WHERE ").append(spec.colUsername).append("=? ");

        sql.append("ORDER BY ").append(spec.colStart).append(" DESC");

        long totalFocus = 0L;
        List<TimerHistoryLogic.ViewSession> rows = new ArrayList<>();

        try (PreparedStatement ps = db.prepareStatement(sql.toString())) {
            if (filterByUser) ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String userCol = rs.getString("username");
                    LocalDateTime start = safeParse(rs.getString("startTime"));
                    LocalDateTime end   = safeParse(rs.getString("endTime"));
                    long focus  = rs.getLong("runSecs");
                    long paused = rs.getLong("pauseSecs");
                    int pauses  = rs.getInt("pauseCnt");

                    rows.add(new TimerHistoryLogic.ViewSession(id, userCol, start, end, focus, paused, pauses));
                    totalFocus += focus;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sessions.setAll(rows);
        totalsession.setText("Total Sessions: " + sessions.size());
        totalTime.setText("Total Focus Time: " + logic.formatTotal(totalFocus));

        if (sessions.isEmpty()) session.setText("(none)");
        else sessionList.getSelectionModel().select(0);
    }

    private static LocalDateTime safeParse(String s) {
        try { return (s == null || s.isBlank()) ? null : LocalDateTime.parse(s, ISO); }
        catch (Exception e) { return null; }
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
        int idx = (sessionList == null) ? -1 : sessionList.getSelectionModel().getSelectedIndex();
        if (idx < 0) return;

        TimerHistoryLogic.ViewSession s = sessions.get(idx);

        TableSpec spec = detectSessionTableSpec(db);
        if (spec == null) return;

        String sql = "DELETE FROM " + spec.table + " WHERE " + spec.colId + "=?";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setInt(1, s.id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String user = (UserSession.getInstance() == null) ? null : UserSession.getInstance().getUsername();
        loadSessionsForUser(user);
    }

    // ---------------- Main Distraction (unchanged) ----------------
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

        final String sql = "SELECT * FROM maindistraction WHERE username=? ORDER BY id DESC";

        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                while (rs.next()) {
                    int id = safeGetInt(rs, md, "id", "rowid");
                    String cause = safeGet(rs, md, "cause", "tag", "title");
                    String when = firstNonBlank(
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
                    String notes = firstNonBlank(
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
        for (String c : cols) if (hasColumn(md, c)) {
            String v = rs.getString(c);
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
    private static Integer safeGetInt(ResultSet rs, ResultSetMetaData md, String... cols) throws SQLException {
        for (String c : cols) if (hasColumn(md, c)) {
            try { return rs.getInt(c); } catch (SQLException ignore) {}
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
            if (name.equalsIgnoreCase(md.getColumnLabel(i)) || name.equalsIgnoreCase(md.getColumnName(i))) return true;
        }
        return false;
    }
    private static String firstNonBlank(String... vals) { for (String v : vals) if (v != null && !v.isBlank()) return v; return null; }
    private static Integer firstNonNullInt(Integer... vals) { for (Integer v : vals) if (v != null) return v; return null; }

    @FXML
    private void onBackHome() throws IOException {
        Stage stage = (Stage) (sessionList != null ? sessionList.getScene().getWindow() : mdList.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }
}
