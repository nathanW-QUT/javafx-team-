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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Graphs page:
 *  • Pie: counts of Main Distraction tags (maindistraction.cause)
 *  • Bar: Distractions vs Accomplishments (counts, not per-day)
 *  • Line: same two counts, as a simple line with two points
 */
public class GraphsController {

    // Pie: Main Distraction tags
    @FXML private PieChart tagPie;

    // Bar: Distractions vs Accomplishments
    @FXML private BarChart<String, Number> comboBar;
    @FXML private CategoryAxis dayAxis;
    @FXML private NumberAxis countAxis;

    // Line: Distractions vs Accomplishments
    @FXML private LineChart<String, Number> comparisonLine;
    @FXML private CategoryAxis lineXAxis;
    @FXML private NumberAxis lineYAxis;

    @FXML private Label  emptyState;
    @FXML private Button backBtn;

    private final Connection db = SqliteConnection.getInstance();

    @FXML
    public void initialize() {
        String user = (UserSession.getInstance() == null) ? null : UserSession.getInstance().getUsername();
        if (user == null || user.isBlank()) {
            showEmpty("Please log in to view insights.");
            return;
        }

        // ---------- Pie: Main distraction tags ----------
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

        // ---------- Counts ----------
        long disCount = loadDistractionCountLast7Days(user);
        long accCount = loadAccomplishmentCount(user);

        boolean hasAny = !causeCounts.isEmpty() || disCount > 0 || accCount > 0;
        if (!hasAny) {
            showEmpty("No data to visualize yet.");
            return;
        }
        emptyState.setVisible(false);

        // ---------- Bar ----------
        dayAxis.getCategories().setAll("Distractions", "Accomplishments");
        dayAxis.setLabel("");
        countAxis.setLabel("Count");

        XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
        barSeries.getData().add(new XYChart.Data<>("Distractions", disCount));
        barSeries.getData().add(new XYChart.Data<>("Accomplishments", accCount));
        comboBar.setAnimated(false);
        comboBar.setLegendVisible(false);
        comboBar.getData().setAll(barSeries);
        comboBar.setVisible(true);

        // ---------- Line ----------
        lineXAxis.getCategories().setAll("Distractions", "Accomplishments");
        lineYAxis.setLabel("Count");
        XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
        lineSeries.getData().add(new XYChart.Data<>("Distractions", disCount));
        lineSeries.getData().add(new XYChart.Data<>("Accomplishments", accCount));
        comparisonLine.setLegendVisible(false);
        comparisonLine.getData().setAll(lineSeries);
        comparisonLine.setCreateSymbols(true); // show the two points
        comparisonLine.setVisible(true);
    }


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


    private long loadDistractionCountLast7Days(String username) {
        final String sql =
                "SELECT COUNT(*) AS cnt FROM timers " +
                        "WHERE username=? " +
                        "AND label NOT IN ('Reset','Pause') " +
                        "AND COALESCE(elapsedSeconds, 0) > 0 " +
                        "AND date(startTime) >= date('now','-6 day')";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("cnt") : 0L;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0L;
        }
    }


    private long loadAccomplishmentCount(String username) {
        boolean hasTimestamp = false;
        try (PreparedStatement info = db.prepareStatement("PRAGMA table_info(accomplishment)");
             ResultSet rs = info.executeQuery()) {
            while (rs.next()) {
                if ("timestamp".equalsIgnoreCase(rs.getString("name"))) { hasTimestamp = true; break; }
            }
        } catch (SQLException ignore) {}

        String sql = hasTimestamp
                ? "SELECT COUNT(*) AS cnt FROM accomplishment WHERE username=? AND date(timestamp) >= date('now','-6 day')"
                : "SELECT COUNT(*) AS cnt FROM accomplishment WHERE username=?";

        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("cnt") : 0L;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0L;
        }
    }


    private void showEmpty(String msg) {
        emptyState.setText(msg);
        emptyState.setVisible(true);
        if (tagPie != null)   tagPie.setVisible(false);
        if (comboBar != null) comboBar.setVisible(false);
        if (comparisonLine != null) comparisonLine.setVisible(false);
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
