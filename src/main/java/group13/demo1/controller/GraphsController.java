package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.SqliteTimerDAO;
import group13.demo1.model.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * controller for the Insights/Graphs page.
 * showing:
 * a pie chart of distraction tags.
 * a line chart of distractions over the day.
 */
public class GraphsController {
    @FXML private PieChart tagPie;
    @FXML private LineChart<Number, Number> trendChart;
    @FXML private NumberAxis                timeAxis;   // X
    @FXML private NumberAxis                trendYAxis; // Y

    @FXML private Label  emptyState;
    @FXML private Button backBtn;

    /**DAO for timer and distraction data. */
    private final SqliteTimerDAO dao = new SqliteTimerDAO();

    /**
     * to build pie chart and line chart for the currently logged-in user.
     */
    @FXML
    public void initialize()
    {
        String user = (UserSession.getInstance() == null) ? null : UserSession.getInstance().getUsername();
        if (user == null || user.isBlank())
        {
            showEmpty("Please log in to view insights.");
            return;
        }

        // Pie data
        Map<String, Long> tagCounts = dao.getTagCountsForUser(user);
        List<LocalDateTime> times = dao.getTodayDistractionTimes(user);
        if (times == null || times.isEmpty())
        {
            times = dao.getMostRecentDayDistractionTimes(user);
        }

        if ((tagCounts == null || tagCounts.isEmpty()) && (times == null || times.isEmpty()))
        {
            showEmpty("No distractions recorded yet.");
            return;
        }
        emptyState.setVisible(false);

        // pie chart
        if (tagCounts != null && !tagCounts.isEmpty())
        {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            tagCounts.forEach((tag, cnt) -> pieData.add(new PieChart.Data(tag + " (" + cnt + ")", cnt)));
            tagPie.setData(pieData);
            tagPie.setVisible(true);
        } else
        {
            tagPie.setVisible(false);
        }

        // line chart
        setupTimeAxis();
        plotCumulativeTimeline(times);
    }

    /**
     * to get the time (X) and count (Y) axis values for the line chart.
     */
    private void setupTimeAxis()
    {
        timeAxis.setAutoRanging(false);
        timeAxis.setLowerBound(0);
        timeAxis.setUpperBound(24 * 60 * 60);
        timeAxis.setTickUnit(6 * 60 * 60);
        timeAxis.setLabel("Time");

        timeAxis.setTickLabelFormatter(new StringConverter<Number>()
        {
            @Override public String toString(Number n)
            {
                long s = n.longValue();
                long h = (s / 3600) % 24;
                String am_pm = (h < 12) ? "am" : "pm";
                long hh = (h % 12 == 0) ? 12 : (h % 12);
                return String.format("%d %s", hh, am_pm);
            }
            @Override public Number fromString(String s) { return 0; }
        });

        trendYAxis.setAutoRanging(false);
        trendYAxis.setLowerBound(0);
        trendYAxis.setUpperBound(1);
        trendYAxis.setTickUnit(1);
        trendYAxis.setLabel("No of Distractions");

        trendChart.setAnimated(false);
        trendChart.setCreateSymbols(true);
        trendChart.setLegendVisible(false);
        trendChart.setVisible(true);
    }

    /**
     * plots a continuous series of line for the registered timestamps.
     * to create a rising step line.
     */
    private void plotCumulativeTimeline(List<LocalDateTime> times)
    {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        if (times != null && !times.isEmpty())
        {
            times.sort(Comparator.naturalOrder());
            int count = 0;
            for (LocalDateTime t : times)
            {
                count++;
                long seconds = t.getHour() * 3600L + t.getMinute() * 60L + t.getSecond();
                series.getData().add(new XYChart.Data<>(seconds, count));
            }
            trendYAxis.setUpperBound(Math.max(2, count + 1));
            trendYAxis.setTickUnit(Math.max(1, Math.ceil((count + 1) / 5.0)));
        } else
        {
            series.getData().add(new XYChart.Data<>(0, 0));
            series.getData().add(new XYChart.Data<>(24 * 3600, 0));
            trendYAxis.setUpperBound(1);
            trendYAxis.setTickUnit(1);
        }

        trendChart.getData().setAll(series);
    }

    /**
     * to show default page when there is no registered history data
     */
    private void showEmpty(String msg)
    {
        emptyState.setText(msg);
        emptyState.setVisible(true);
        if (tagPie != null)     tagPie.setVisible(false);
        if (trendChart != null) trendChart.setVisible(false);
    }

    /**
     * helps in navigating back to the home screen.
     */
    @FXML
    private void onBackHome() throws IOException
    {
        Stage stage = (Stage) backBtn.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }
}





