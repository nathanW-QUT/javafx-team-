package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.SqliteConnection;
import group13.demo1.model.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Graphs page:
 *  • Pie: counts of Main Distraction tags (maindistraction.cause)
 *  • Bar: weekly counts (Mon..Sun) with two series:
 *        Distractions/day (from maindistraction) and Accomplishments/day (if accomplishment.timestamp exists)
 *
 *  Use the Refresh button to reload data. Data also loads at initialize().
 */
public class GraphsController {

    // Pie: Main Distraction tags
    @FXML private PieChart tagPie;

    // Bar: weekly counts
    @FXML private BarChart<String, Number> comboBar;
    @FXML private CategoryAxis dayAxis;
    @FXML private NumberAxis countAxis;

    @FXML private Label noteLabel;
    @FXML private Label emptyState;
    @FXML private Button backBtn;
    @FXML private Button refreshBtn;

    private final Connection db = SqliteConnection.getInstance();

    // Monday-first labels for x-axis
    private static final List<String> MON_TO_SUN = List.of(
            "Mon","Tue","Wed","Thu","Fri","Sat","Sun"
    );

    @FXML
    public void initialize() {
        refreshBtn.setOnAction(e -> refresh());
        refresh();
    }

    /** Rebuilds both charts from the database. */
    private void refresh() {
        String user = (UserSession.getInstance() == null) ? null : UserSession.getInstance().getUsername();
        if (user == null || user.isBlank()) {
            showEmpty("Please log in to view insights.");
            return;
        }

        // --- Pie: Main Distraction tags (cause) ---
        Map<String, Long> causeCounts = loadMainDistractionTagCounts(user);
        if (!causeCounts.isEmpty()) {
            ObservableList<PieChart.Data> pie = FXCollections.observableArrayList();
            causeCounts.forEach((cause, cnt) -> pie.add(new PieChart.Data(cause + " (" + cnt + ")", cnt)));
            tagPie.setData(pie);
            tagPie.setLegendVisible(false);
            tagPie.setVisible(true);
        } else {
            tagPie.setData(FXCollections.observableArrayList());
            tagPie.setVisible(false);
        }

        // --- Bar: Weekly counts Mon..Sun (Main Distractions + (optional) Accomplishments) ---
        Map<String, Long> disWeek = loadMainDistractionWeekCounts(user);     // Mon..Sun from maindistraction
        Map<String, Long> accWeek = tryLoadAccomplishmentsWeekCounts(user);  // Mon..Sun or empty if no timestamp col

        boolean hasAny = !causeCounts.isEmpty()
                || disWeek.values().stream().mapToLong(Long::longValue).sum() > 0
                || accWeek.values().stream().mapToLong(Long::longValue).sum() > 0;

        if (!hasAny) {
            showEmpty("No data to visualize yet.");
            return;
        }
        emptyState.setVisible(false);

        dayAxis.getCategories().setAll(MON_TO_SUN);
        countAxis.setLabel("Count");
        dayAxis.setLabel("Day of week");

        XYChart.Series<String, Number> sDis = new XYChart.Series<>();
        sDis.setName("Distractions");
        XYChart.Series<String, Number> sAcc = new XYChart.Series<>();
        sAcc.setName("Accomplishments");

        for (String d : MON_TO_SUN) {
            sDis.getData().add(new XYChart.Data<>(d, disWeek.getOrDefault(d, 0L)));
            sAcc.getData().add(new XYChart.Data<>(d, accWeek.getOrDefault(d, 0L)));
        }

        comboBar.setAnimated(false);
        comboBar.setLegendVisible(true);
        comboBar.getData().setAll(sDis, sAcc);

        if (accWeek.values().stream().mapToLong(Long::longValue).sum() == 0 && !hasAccomplishmentTimestamp()) {
            noteLabel.setText("Accomplishments/week unavailable (no 'timestamp' column in 'accomplishment').");
            noteLabel.setVisible(true);
        } else {
            noteLabel.setVisible(false);
        }
    }

    // ----------------- Data helpers -----------------

    /** Counts of main distraction tags by 'cause' for the user (all-time). */
    private Map<String, Long> loadMainDistractionTagCounts(String username) {
        Map<String, Long> map = new LinkedHashMap<>();
        final String sql =
                "SELECT cause, COUNT(*) AS cnt " +
                        "FROM maindistraction WHERE username=? " +
                        "GROUP BY cause ORDER BY cnt DESC";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String cause = rs.getString("cause");
                    long cnt = rs.getLong("cnt");
                    if (cause == null || cause.isBlank()) cause = "(untitled)";
                    map.put(cause, cnt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /** Weekly counts (Mon..Sun) from maindistraction in the *last 7 days*. */
    private Map<String, Long> loadMainDistractionWeekCounts(String username) {
        Map<String, Long> map = zeroWeekMap();
        final String sql =
                "SELECT strftime('%w', timestamp) AS dow, COUNT(*) AS cnt " +
                        "FROM maindistraction " +
                        "WHERE username=? AND date(timestamp) >= date('now','-6 day') " +
                        "GROUP BY dow";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int dowSql = rs.getInt("dow"); // 0=Sun..6=Sat
                    String name = mapDowToMonFirstName(dowSql);
                    long cnt = rs.getLong("cnt");
                    map.put(name, map.get(name) + cnt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /** Weekly counts (Mon..Sun) for accomplishments in the *last 7 days* if 'timestamp' exists. */
    private Map<String, Long> tryLoadAccomplishmentsWeekCounts(String username) {
        Map<String, Long> map = zeroWeekMap();
        if (!hasAccomplishmentTimestamp()) return map;

        final String sql =
                "SELECT strftime('%w', timestamp) AS dow, COUNT(*) AS cnt " +
                        "FROM accomplishment WHERE username=? " +
                        "AND date(timestamp) >= date('now','-6 day') " +
                        "GROUP BY dow";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int dowSql = rs.getInt("dow"); // 0=Sun..6=Sat
                    String name = mapDowToMonFirstName(dowSql);
                    long cnt = rs.getLong("cnt");
                    map.put(name, map.get(name) + cnt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    private boolean hasAccomplishmentTimestamp() {
        try (PreparedStatement info = db.prepareStatement("PRAGMA table_info(accomplishment)");
             ResultSet rs = info.executeQuery()) {
            while (rs.next()) {
                if ("timestamp".equalsIgnoreCase(rs.getString("name"))) return true;
            }
        } catch (SQLException ignore) {}
        return false;
    }

    private Map<String, Long> zeroWeekMap() {
        Map<String, Long> m = new LinkedHashMap<>();
        for (String d : MON_TO_SUN) m.put(d, 0L);
        return m;
    }

    /** Convert SQLite %w (0=Sun..6=Sat) -> "Mon".."Sun". */
    private String mapDowToMonFirstName(int sqliteDow) {
        switch (sqliteDow) {
            case 1: return "Mon";
            case 2: return "Tue";
            case 3: return "Wed";
            case 4: return "Thu";
            case 5: return "Fri";
            case 6: return "Sat";
            default: return "Sun";
        }
    }

    // ----------------- UI helpers -----------------

    private void showEmpty(String msg) {
        emptyState.setText(msg);
        emptyState.setVisible(true);
        if (tagPie != null)   tagPie.setVisible(false);
        if (comboBar != null) comboBar.setVisible(false);
        if (noteLabel != null) noteLabel.setVisible(false);
    }

    @FXML
    private void onBackHome() throws IOException {
        Stage stage = (Stage) backBtn.getScene().getWindow();
        FXMLLoader fxml = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
        Scene scene = new Scene(fxml.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String css = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(css);
    }
}


