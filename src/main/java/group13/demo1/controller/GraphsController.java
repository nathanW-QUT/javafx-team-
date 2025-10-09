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
import java.sql.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class GraphsController {

    // ---- Pie  ----
    @FXML private PieChart tagPie;

    // ---- Combined Bar (Distractions vs Accomplishments) ----
    @FXML private BarChart<String, Number> dailyBar;
    @FXML private CategoryAxis barXAxis;
    @FXML private NumberAxis   barYAxis;

    // ---- Distractions-only Bar ----
    @FXML private BarChart<String, Number> disBar;
    @FXML private CategoryAxis disBarXAxis;
    @FXML private NumberAxis   disBarYAxis;

    @FXML private LineChart<String, Number> comparisonLine;
    @FXML private CategoryAxis lineXAxis;
    @FXML private NumberAxis   lineYAxis;

    @FXML private Label  emptyState;
    @FXML private Button backBtn;

    private final Connection db = SqliteConnection.getInstance();

    @FXML
    public void initialize() {
        // Reload charts whenever the window regains focus
        Platform.runLater(() -> {
            if (backBtn != null && backBtn.getScene() != null && backBtn.getScene().getWindow() != null) {
                backBtn.getScene().getWindow().focusedProperty().addListener((obs, was, is) -> {
                    if (is) safeReload();
                });
            }
        });
        safeReload();
    }

    private void safeReload() {
        try {
            loadCharts();
        } catch (Exception e) {
            e.printStackTrace();
            showEmpty("Could not load charts.");
        }
    }

    private void loadCharts() {
        String user = (UserSession.getInstance() == null) ? null : UserSession.getInstance().getUsername();
        if (user == null || user.isBlank()) { showEmpty("Please log in to view insights."); return; }

        ensureAccomplishmentTimestampColumn();

        // ---------- Pie ----------
        Map<String, Long> causeCounts = loadMainDistractionTagCounts(user);
        long totalCause = causeCounts.values().stream().mapToLong(Long::longValue).sum();
        if (!causeCounts.isEmpty()) {
            ObservableList<PieChart.Data> pie = FXCollections.observableArrayList();
            causeCounts.forEach((cause, cnt) -> {
                String name = (cause == null || cause.isBlank()) ? "(untitled)" : cause;
                long pct = totalCause == 0 ? 0 : Math.round(cnt * 100.0 / totalCause);
                pie.add(new PieChart.Data(name + " — " + pct + "%", cnt));
            });
            tagPie.setData(pie);
            tagPie.setLegendVisible(false);
            tagPie.setLabelsVisible(true);
            tagPie.setVisible(true);
        } else {
            tagPie.setData(FXCollections.observableArrayList());
            tagPie.setVisible(false);
        }

        // ---------- Last 7 days labels ----------
        List<LocalDate> last7 = last7Dates();
        Map<String, String> keyToLabel = new LinkedHashMap<>();
        ObservableList<String> categories = FXCollections.observableArrayList();
        for (LocalDate d : last7) {
            String key = d.toString();
            String dow = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            String label = key + "\n" + dow;
            keyToLabel.put(key, label);
            categories.add(label);
        }

        // ---------- Daily counts ----------
        Map<String, Long> disDaily = loadMainDistractionDailyCounts(user);
        Map<String, Long> accDaily = loadAccomplishmentDailyCounts(user);

        boolean anyData = totalCause > 0
                || disDaily.values().stream().mapToLong(Long::longValue).sum() > 0
                || accDaily.values().stream().mapToLong(Long::longValue).sum() > 0;
        if (!anyData) { showEmpty("No data to visualize yet."); return; }
        if (emptyState != null) emptyState.setVisible(false);

        // ---------- Combined Bar (Distractions vs Accomplishments) ----------
        if (barXAxis != null) { barXAxis.setLabel("Date"); barXAxis.setCategories(categories); }
        if (barYAxis != null) { barYAxis.setLabel("Count"); barYAxis.setForceZeroInRange(true); }

        XYChart.Series<String, Number> sDis = new XYChart.Series<>(); sDis.setName("Distractions");
        XYChart.Series<String, Number> sAcc = new XYChart.Series<>(); sAcc.setName("Accomplishments");
        for (LocalDate d : last7) {
            String key = d.toString(), label = keyToLabel.get(key);
            sDis.getData().add(new XYChart.Data<>(label, disDaily.getOrDefault(key, 0L)));
            sAcc.getData().add(new XYChart.Data<>(label, accDaily.getOrDefault(key, 0L)));
        }
        dailyBar.setLegendVisible(true);
        dailyBar.setAnimated(false);
        dailyBar.getData().setAll(sDis, sAcc);
        dailyBar.setVisible(true);

        Platform.runLater(() -> {
            sDis.getData().forEach(dp -> { if (dp.getNode()!=null) Tooltip.install(dp.getNode(),
                    new Tooltip("Distractions\n" + dp.getXValue().replace('\n',' ') + " : " + dp.getYValue()));});
            sAcc.getData().forEach(dp -> { if (dp.getNode()!=null) Tooltip.install(dp.getNode(),
                    new Tooltip("Accomplishments\n" + dp.getXValue().replace('\n',' ') + " : " + dp.getYValue()));});
        });

        // ---------- Distractions-only Bar ----------
        if (disBarXAxis != null) { disBarXAxis.setLabel("Date"); disBarXAxis.setCategories(categories); }
        if (disBarYAxis != null) { disBarYAxis.setLabel("Count"); disBarYAxis.setForceZeroInRange(true); }

        XYChart.Series<String, Number> sDisOnly = new XYChart.Series<>();
        for (LocalDate d : last7) {
            String key = d.toString(), label = keyToLabel.get(key);
            sDisOnly.getData().add(new XYChart.Data<>(label, disDaily.getOrDefault(key, 0L)));
        }
        disBar.setAnimated(false);
        disBar.setLegendVisible(false);
        disBar.getData().setAll(sDisOnly);
        disBar.setVisible(true);

        Platform.runLater(() -> {
            sDisOnly.getData().forEach(dp -> { if (dp.getNode()!=null) Tooltip.install(dp.getNode(),
                    new Tooltip("Distractions\n" + dp.getXValue().replace('\n',' ') + " : " + dp.getYValue()));});
        });
    }

    private void ensureAccomplishmentTimestampColumn() {
        try (PreparedStatement info = db.prepareStatement("PRAGMA table_info(accomplishment)");
             ResultSet rs = info.executeQuery()) {
            boolean hasTimestamp = false;
            boolean hasCreatedAt = false;
            while (rs.next()) {
                String name = rs.getString("name");
                if ("timestamp".equalsIgnoreCase(name)) hasTimestamp = true;
                if ("created_at".equalsIgnoreCase(name)) hasCreatedAt = true;
            }
            if (!hasTimestamp) {
                try (Statement st = db.createStatement()) {
                    st.executeUpdate("ALTER TABLE accomplishment ADD COLUMN timestamp DATETIME DEFAULT CURRENT_TIMESTAMP");
                }
                if (hasCreatedAt) {
                    try (Statement st = db.createStatement()) {
                        st.executeUpdate("UPDATE accomplishment SET timestamp = created_at WHERE timestamp IS NULL AND created_at IS NOT NULL");
                    }
                }
            }
        } catch (SQLException ignore) {
        }
    }

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
                while (rs.next()) out.put(rs.getString("cause"), rs.getLong("cnt"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }

    private Map<String, Long> loadMainDistractionDailyCounts(String username) {
        Map<String, Long> map = new LinkedHashMap<>();
        final String sql = """
            SELECT date(timestamp) AS day, COUNT(*) AS cnt
            FROM maindistraction
            WHERE username=? AND date(timestamp) >= date('now','-6 day')
            GROUP BY day
            ORDER BY day
        """;
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString("day"), rs.getLong("cnt"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    private Map<String, Long> loadAccomplishmentDailyCounts(String username) {
        Map<String, Long> map = new LinkedHashMap<>();
        boolean hasTimestamp = false;
        try (PreparedStatement info = db.prepareStatement("PRAGMA table_info(accomplishment)");
             ResultSet rs = info.executeQuery()) {
            while (rs.next()) {
                if ("timestamp".equalsIgnoreCase(rs.getString("name"))) { hasTimestamp = true; break; }
            }
        } catch (SQLException ignored) {}

        if (hasTimestamp) {
            final String sql = """
                SELECT date(timestamp) AS day, COUNT(*) AS cnt
                FROM accomplishment
                WHERE username=? AND date(timestamp) >= date('now','-6 day')
                GROUP BY day
                ORDER BY day
            """;
            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) map.put(rs.getString("day"), rs.getLong("cnt"));
                }
            } catch (SQLException e) { e.printStackTrace(); }
        } else {
            long totalForUser = 0L;
            try (PreparedStatement ps = db.prepareStatement("SELECT COUNT(*) AS cnt FROM accomplishment WHERE username=?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totalForUser = rs.getLong("cnt");
                }
            } catch (SQLException e) { e.printStackTrace(); }
            if (totalForUser > 0) map.put(LocalDate.now().toString(), totalForUser);
        }
        return map;
    }

    private List<LocalDate> last7Dates() {
        List<LocalDate> out = new ArrayList<>(7);
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) out.add(today.minusDays(i));
        return out;
    }

    private void showEmpty(String msg) {
        if (emptyState != null) { emptyState.setText(msg); emptyState.setVisible(true); }
        if (tagPie != null)   tagPie.setVisible(false);
        if (dailyBar != null) dailyBar.setVisible(false);
        if (disBar != null)   disBar.setVisible(false);
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
