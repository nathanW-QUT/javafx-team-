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
/**
 * Controller for Insights page
 * gives bar chart for Distraction Vs Accomplishment,Distraction only bar chart and pie chart
 * for distraction tags
 * when there is no data it shows an empty state("No data to graph yet.")
 */

public class GraphsController {

    //Pie
    @FXML private PieChart tagPie;

    //Combined bar chart (distractions vs accomplishments)
    @FXML private BarChart<String, Number> dailyBar;
    @FXML private CategoryAxis barXAxis;
    @FXML private NumberAxis   barYAxis;

    //Distractions-only bar chart
    @FXML private BarChart<String, Number> disBar;
    @FXML private CategoryAxis disBarXAxis;
    @FXML private NumberAxis   disBarYAxis;

    @FXML private Label  emptyState;
    @FXML private Button backButton;

    /** JDBC connection to SQlite */
    private final Connection db = SqliteConnection.getInstance();


    @FXML
    public void initialize() {
        ReloadGraph();
    }

    /** To reload the graph each time the page refreshes*/
    private void ReloadGraph()
    {
        try
        {
            loadCharts();
        } catch (Exception e)
        {
            e.printStackTrace();
            showEmpty("Could not load charts.");
        }
    }

    /** return count of distraction for a user per cause*/
    private Map<String, Long> loadMainDistractionCounts(String username)
    {
        Map<String, Long> out = new LinkedHashMap<>();
        final String sql = """
            SELECT cause, COUNT(*) AS cnt
            FROM maindistraction
            WHERE username=?
            GROUP BY cause
            ORDER BY cnt DESC
        """;
        try (PreparedStatement ps = db.prepareStatement(sql))
        {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next()) out.put(rs.getString("cause"), rs.getLong("cnt"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }

    /** return count of distraction for a user for the particular day (last 7 days)*/
    private Map<String, Long> loadMainDistractionDailyCounts(String username)
    {
        Map<String, Long> map = new LinkedHashMap<>();
        final String sql = """
            SELECT date(timestamp) AS day, COUNT(*) AS cnt
            FROM maindistraction
            WHERE username=? AND date(timestamp) >= date('now','-6 day')
            GROUP BY day
            ORDER BY day
        """;
        try (PreparedStatement ps = db.prepareStatement(sql))
        {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next()) map.put(rs.getString("day"), rs.getLong("cnt"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    /** return count of accomplishment for a user per day for last 7 days*/
    private Map<String, Long> loadAccomplishmentDailyCounts(String username)
    {
        Map<String, Long> map = new LinkedHashMap<>();
        boolean hasTimestamp = false;
        try (PreparedStatement info = db.prepareStatement("PRAGMA table_info(accomplishment)");
             ResultSet rs = info.executeQuery())
        {
            while (rs.next())
            {
                if ("timestamp".equalsIgnoreCase(rs.getString("name"))) { hasTimestamp = true; break; }
            }
        } catch (SQLException ignored) {}

        if (hasTimestamp)
        {
            final String sql = """
                SELECT date(timestamp) AS day, COUNT(*) AS cnt
                FROM accomplishment
                WHERE username=? AND date(timestamp) >= date('now','-6 day')
                GROUP BY day
                ORDER BY day
            """;
            try (PreparedStatement ps = db.prepareStatement(sql))
            {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery())
                {
                    while (rs.next()) map.put(rs.getString("day"), rs.getLong("cnt"));
                }
            } catch (SQLException e) { e.printStackTrace(); }
        } else
        {
            long totalForUser = 0L;
            try (PreparedStatement ps = db.prepareStatement("SELECT COUNT(*) AS cnt FROM accomplishment WHERE username=?"))
            {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery())
                {
                    if (rs.next()) totalForUser = rs.getLong("cnt");
                }
            } catch (SQLException e) { e.printStackTrace(); }
            if (totalForUser > 0) map.put(LocalDate.now().toString(), totalForUser);
        }
        return map;
    }

    /** gives a list of local-date for the last 7 days*/
    private List<LocalDate> last7Dates()
    {
        List<LocalDate> out = new ArrayList<>(7);
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) out.add(today.minusDays(i));
        return out;
    }

    /** empty state to show when there is no data*/
    private void showEmpty(String msg)
    {
        if (emptyState != null) { emptyState.setText(msg); emptyState.setVisible(true); }
        if (tagPie != null)   tagPie.setVisible(false);
        if (dailyBar != null) dailyBar.setVisible(false);
        if (disBar != null)   disBar.setVisible(false);
    }

    @FXML
    private void onBackHome() throws IOException
    {
        Stage stage = (Stage) backButton.getScene().getWindow();
        FXMLLoader fxml = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
        Scene scene = new Scene(fxml.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String css = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(css);
    }

    /** to read the current user and load data to configure axes(labels), tooltips and charts*/
    private void loadCharts()
    {
        String user = (UserSession.getInstance() == null) ? null : UserSession.getInstance().getUsername();
        if (user == null || user.isBlank()) { showEmpty("Please log in to view insights."); return; }

        // Pie
        Map<String, Long> DistractionCounts = loadMainDistractionCounts(user);
        long totalCounts = DistractionCounts.values().stream().mapToLong(Long::longValue).sum();

        if (!DistractionCounts.isEmpty())
        {
            ObservableList<PieChart.Data> pie = FXCollections.observableArrayList();
            DistractionCounts.forEach((cause, cnt) -> {
                String name = (cause == null || cause.isBlank()) ? "(untitled)" : cause;
                long pie_chart = totalCounts == 0 ? 0 : Math.round(cnt * 100.0 / totalCounts);
                pie.add(new PieChart.Data(name + " — " + pie_chart + "%", cnt));
            });
            tagPie.setData(pie);
            tagPie.setLegendVisible(false);
            tagPie.setLabelsVisible(true);
            tagPie.setVisible(true);
        } else
        {
            tagPie.setData(FXCollections.observableArrayList());
            tagPie.setVisible(false);
        }

        // Last 7 days labels
        List<LocalDate> last7 = last7Dates();
        Map<String, String> Label = new LinkedHashMap<>();
        ObservableList<String> categories = FXCollections.observableArrayList();
        for (LocalDate d : last7)
        {
            String date = d.toString();
            String day = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            String label = date + "\n" + day;
            Label.put(date, label);
            categories.add(label);
        }

        // to map daily counts
        Map<String, Long> distractionDaily = loadMainDistractionDailyCounts(user);
        Map<String, Long> accomplishmentsDaily = loadAccomplishmentDailyCounts(user);

        boolean anyData = totalCounts > 0
                || distractionDaily.values().stream().mapToLong(Long::longValue).sum() > 0
                || accomplishmentsDaily.values().stream().mapToLong(Long::longValue).sum() > 0;
        if (!anyData) { showEmpty("No data to graph yet."); return; }
        if (emptyState != null) emptyState.setVisible(false);

        // Distractions vs Accomplishments
        if (barXAxis != null) { barXAxis.setLabel("Date"); barXAxis.setCategories(categories); }
        if (barYAxis != null) { barYAxis.setLabel("Count"); barYAxis.setForceZeroInRange(true); }

        XYChart.Series<String, Number> Distraction = new XYChart.Series<>(); Distraction.setName("Distractions");
        XYChart.Series<String, Number> Accomplishment = new XYChart.Series<>(); Accomplishment.setName("Accomplishments");

        for (LocalDate d : last7)
        {
            String date = d.toString(), label = Label.get(date);
            Distraction.getData().add(new XYChart.Data<>(label, distractionDaily.getOrDefault(date, 0L)));
            Accomplishment.getData().add(new XYChart.Data<>(label, accomplishmentsDaily.getOrDefault(date, 0L)));
        }

        dailyBar.setLegendVisible(true);
        dailyBar.setAnimated(false);
        dailyBar.getData().setAll(Distraction, Accomplishment);
        dailyBar.setVisible(true);

        Platform.runLater(() -> {
            Distraction.getData().forEach(dp -> { if (dp.getNode()!=null) Tooltip.install(dp.getNode(),
                    new Tooltip("Distractions\n" + dp.getXValue().replace('\n',' ') + " : " + dp.getYValue()));});
            Accomplishment.getData().forEach(dp -> { if (dp.getNode()!=null) Tooltip.install(dp.getNode(),
                    new Tooltip("Accomplishments\n" + dp.getXValue().replace('\n',' ') + " : " + dp.getYValue()));});
        });

        // Distractions-only Bar
        if (disBarXAxis != null) { disBarXAxis.setLabel("Date"); disBarXAxis.setCategories(categories); }
        if (disBarYAxis != null) { disBarYAxis.setLabel("Count"); disBarYAxis.setForceZeroInRange(true); }

        XYChart.Series<String, Number> sDisOnly = new XYChart.Series<>();
        for (LocalDate d : last7)
        {
            String date = d.toString(), label = Label.get(date);
            sDisOnly.getData().add(new XYChart.Data<>(label, distractionDaily.getOrDefault(date, 0L)));
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
}
