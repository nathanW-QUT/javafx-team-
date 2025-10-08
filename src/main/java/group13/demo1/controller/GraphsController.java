package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.SqliteConnection;
import group13.demo1.model.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class GraphsController {

    // FXML nodes (same fx:id as your FXML)
    @FXML private PieChart tagPie;

    @FXML private BarChart<String, Number> comboBar;
    @FXML private CategoryAxis dayAxis;
    @FXML private NumberAxis   countAxis;

    // kept for compatibility — not used by the current FXML but do not remove
    @FXML private LineChart<String, Number> comparisonLine;
    @FXML private CategoryAxis lineXAxis;
    @FXML private NumberAxis   lineYAxis;

    @FXML private Label  emptyState;
    @FXML private Button backBtn;

    private final Connection db = SqliteConnection.getInstance();

    @FXML
    public void initialize() {
        // If axes didn’t inject (rare), fetch them from the chart safely.
        try {
            if (comboBar != null) {
                if (dayAxis == null && comboBar.getXAxis() instanceof CategoryAxis ax) dayAxis = ax;
                if (countAxis == null && comboBar.getYAxis() instanceof NumberAxis ax)  countAxis = ax;
            }
            if (comparisonLine != null) {
                if (lineXAxis == null && comparisonLine.getXAxis() instanceof CategoryAxis ax) lineXAxis = ax;
                if (lineYAxis == null && comparisonLine.getYAxis() instanceof NumberAxis ax)  lineYAxis = ax;
            }
        } catch (Exception ignore) {}

        String user = (UserSession.getInstance() == null)
                ? null : UserSession.getInstance().getUsername();
        if (user == null || user.isBlank()) {
            showEmpty("Please log in to view insights.");
            return;
        }

        // ---------- Pie: main distraction tags (with %) ----------
        Map<String, Long> causeCounts = loadMainDistractionTagCounts(user);
        long total = causeCounts.values().stream().mapToLong(Long::longValue).sum();
        if (!causeCounts.isEmpty()) {
            ObservableList<PieChart.Data> pie = FXCollections.observableArrayList();
            causeCounts.forEach((cause, cnt) -> {
                long pct = (total == 0) ? 0 : Math.round(cnt * 100.0 / total);
                pie.add(new PieChart.Data(cause + " — " + pct + "%", cnt));
            });
            tagPie.setData(pie);
            tagPie.setLegendVisible(false);
            tagPie.setLabelsVisible(true);
            tagPie.setVisible(true);
        } else {
            tagPie.setData(FXCollections.observableArrayList());
            tagPie.setVisible(false);
        }

        // ---------- Counters ----------
        long disCount = loadDistractionCountLast7Days(user);
        long accCount = loadAccomplishmentCount(user);

        if (causeCounts.isEmpty() && disCount == 0 && accCount == 0) {
            showEmpty("No data to visualize yet.");
            return;
        }
        if (emptyState != null) emptyState.setVisible(false);

        // ---------- Bar: Distractions vs Accomplishments ----------
        if (dayAxis != null) {
            dayAxis.getCategories().setAll("Distractions", "Accomplishments");
            dayAxis.setLabel("");
        }
        if (countAxis != null) countAxis.setLabel("Count");

        XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
        barSeries.getData().add(new XYChart.Data<>("Distractions", disCount));
        barSeries.getData().add(new XYChart.Data<>("Accomplishments", accCount));

        comboBar.setAnimated(false);
        comboBar.setLegendVisible(false);
        comboBar.getData().setAll(barSeries);
        comboBar.setVisible(true);

        addBarValueLabels(barSeries); // labels + tooltips without using protected API
    }

    /** Add text labels above bars + tooltips (no protected API used). */
    private void addBarValueLabels(XYChart.Series<String, Number> series) {
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> d : series.getData()) {
                if (d.getNode() == null) continue;

                Tooltip.install(d.getNode(), new Tooltip(String.valueOf(d.getYValue())));
                var label = new javafx.scene.text.Text(String.valueOf(d.getYValue()));
                label.getStyleClass().add("bar-value");

                d.getNode().parentProperty().addListener((obs, oldP, newP) -> {
                    if (newP instanceof javafx.scene.Group g && !g.getChildren().contains(label)) {
                        g.getChildren().add(label);
                    }
                });
                if (d.getNode().getParent() instanceof javafx.scene.Group g && !g.getChildren().contains(label)) {
                    g.getChildren().add(label);
                }
                d.getNode().boundsInParentProperty().addListener((obs, ob, nb) -> {
                    double x = nb.getMinX() + nb.getWidth() / 2.0 - label.getLayoutBounds().getWidth() / 2.0;
                    double y = nb.getMinY() - 6;
                    label.setLayoutX(x);
                    label.setLayoutY(y);
                });
            }
        });
    }

    // ---------------- data helpers ----------------
    private Map<String, Long> loadMainDistractionTagCounts(String username) {
        Map<String, Long> out = new LinkedHashMap<>();
        final String sql = """
            SELECT cause, COUNT(*) AS cnt
            FROM maindistraction
            WHERE username=?
            GROUP BY cause
            ORDER BY cnt DESC
        """;
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String cause = rs.getString("cause");
                    if (cause == null || cause.isBlank()) cause = "(untitled)";
                    out.put(cause, rs.getLong("cnt"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }

    private long loadDistractionCountLast7Days(String username) {
        final String sql = """
            SELECT COUNT(*) AS cnt
            FROM timers
            WHERE username=?
              AND label NOT IN ('Reset','Pause')
              AND COALESCE(totalTime,0) > 0
              AND date(startTime) >= date('now','-6 day')
        """;
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getLong("cnt") : 0L; }
        } catch (SQLException e) { e.printStackTrace(); return 0L; }
    }

    private long loadAccomplishmentCount(String username) {
        boolean hasTimestamp = false;
        try (PreparedStatement info = db.prepareStatement("PRAGMA table_info(accomplishment)");
             ResultSet rs = info.executeQuery()) {
            while (rs.next()) if ("timestamp".equalsIgnoreCase(rs.getString("name"))) { hasTimestamp = true; break; }
        } catch (SQLException ignore) {}

        String sql = hasTimestamp
                ? "SELECT COUNT(*) AS cnt FROM accomplishment WHERE username=? AND date(timestamp) >= date('now','-6 day')"
                : "SELECT COUNT(*) AS cnt FROM accomplishment WHERE username=?";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getLong("cnt") : 0L; }
        } catch (SQLException e) { e.printStackTrace(); return 0L; }
    }

    private void showEmpty(String msg) {
        if (emptyState != null) { emptyState.setText(msg); emptyState.setVisible(true); }
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
