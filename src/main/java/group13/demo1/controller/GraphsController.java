package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.SqliteTimerDAO;
import group13.demo1.model.TimerRecord;
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
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class GraphsController {

    // Pie
    @FXML private PieChart tagPie;

    // Daily counts (line)
    @FXML private LineChart<String, Number> trendChart;
    @FXML private CategoryAxis           trendXAxis;
    @FXML private NumberAxis             trendYAxis;

    // Time-of-day by tag (scatter)
    @FXML private ScatterChart<String, Number> timeChart;
    @FXML private CategoryAxis                 timeXAxis;
    @FXML private NumberAxis                   timeYAxis;

    @FXML private Label  emptyState;
    @FXML private Button backBtn;

    private final SqliteTimerDAO dao = new SqliteTimerDAO();

    @FXML
    public void initialize() {
        String user = (UserSession.getInstance() == null) ? null : UserSession.getInstance().getUsername();
        if (user == null || user.isBlank()) {
            showEmpty("Please log in to view insights.");
            return;
        }


        Map<String, Long> tagCounts = dao.getTagCountsForUser(user);          // label -> count
        if (tagCounts != null && !tagCounts.isEmpty()) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            tagCounts.forEach((tag, cnt) -> pieData.add(new PieChart.Data(tag + " (" + cnt + ")", cnt)));
            tagPie.setData(pieData);
            tagPie.setLegendVisible(false);
            tagPie.setVisible(true);
        } else {
            tagPie.setVisible(false);
        }


        Map<String, Long> daily = dao.getDailyDistractionCounts(user);
        if (daily != null && !daily.isEmpty()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            daily.forEach((day, cnt) -> series.getData().add(new XYChart.Data<>(day, cnt)));
            trendChart.setAnimated(false);
            trendChart.setLegendVisible(false);
            trendYAxis.setLabel("Distractions/day");
            trendXAxis.setLabel("Date (yyyy-MM-dd)");
            trendChart.getData().setAll(series);
            trendChart.setVisible(true);
        } else {
            trendChart.setVisible(false);
        }


        List<TimerRecord> timers = dao.getTimersForUser(user);
        if (timers != null && !timers.isEmpty()) {

            LocalDate latestDay = timers.get(0).getStartTime().toLocalDate();


            Map<String, List<Double>> byTag = new LinkedHashMap<>();
            for (TimerRecord t : timers) {
                LocalDateTime st = t.getStartTime();
                if (!st.toLocalDate().equals(latestDay)) continue;
                String tag = t.getLabel();
                double hour = st.getHour() + (st.getMinute() / 60.0);
                byTag.computeIfAbsent(tag, k -> new ArrayList<>()).add(hour);
            }


            timeXAxis.setLabel("Tag");
            timeYAxis.setLabel("Time of day");
            timeYAxis.setAutoRanging(false);
            timeYAxis.setLowerBound(0);
            timeYAxis.setUpperBound(24);
            timeYAxis.setTickUnit(2);


            timeYAxis.setTickLabelFormatter(new StringConverter<Number>() {
                @Override public String toString(Number n) {
                    int h = (int)Math.round(n.doubleValue());
                    if (h < 0) h = 0;
                    if (h > 24) h = 24;
                    if (h == 0 || h == 24) return "12 am";
                    if (h == 12) return "12 pm";
                    return (h < 12) ? h + " am" : (h - 12) + " pm";
                }
                @Override public Number fromString(String s) { return 0; }
            });


            timeChart.getData().clear();
            for (Map.Entry<String, List<Double>> e : byTag.entrySet()) {
                String tag = e.getKey();
                XYChart.Series<String, Number> s = new XYChart.Series<>();
                s.setName(tag);
                for (Double hour : e.getValue()) {
                    s.getData().add(new XYChart.Data<>(tag, hour));
                }
                timeChart.getData().add(s);
            }
            timeChart.setLegendVisible(true);
            timeChart.setVisible(true);
        } else {
            timeChart.setVisible(false);
        }


        if ((tagCounts == null || tagCounts.isEmpty()) &&
                (daily == null || daily.isEmpty()) &&
                (timers == null || timers.isEmpty())) {
            showEmpty("No distractions recorded yet.");
        } else {
            emptyState.setVisible(false);
        }
    }

    private void showEmpty(String msg) {
        emptyState.setText(msg);
        emptyState.setVisible(true);
        if (tagPie != null)      tagPie.setVisible(false);
        if (trendChart != null)  trendChart.setVisible(false);
        if (timeChart != null)   timeChart.setVisible(false);
    }

    @FXML
    private void onBackHome() throws IOException {
        Stage stage = (Stage) backBtn.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }
}

