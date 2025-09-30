package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.SqliteTimerDAO;
import group13.demo1.model.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class GraphsController {

    @FXML private PieChart tagPie;

    @FXML private LineChart<String, Number> trendChart;
    @FXML private CategoryAxis trendXAxis;
    @FXML private NumberAxis trendYAxis;

    @FXML private Label emptyState;
    @FXML private Button backBtn;

    private final SqliteTimerDAO dao = new SqliteTimerDAO();

    @FXML
    public void initialize() {
        String user = (UserSession.getInstance() == null) ? null : UserSession.getInstance().getUsername();
        if (user == null || user.isBlank()) {
            showEmpty("Please log in to view insights.");
            return;
        }

        // Data from DB (already ordered appropriately in DAO)
        Map<String, Long> tagCounts = dao.getTagCountsForUser(user);          // label -> count
        Map<String, Long> daily     = dao.getDailyDistractionCounts(user);    // yyyy-MM-dd -> count

        if ((tagCounts == null || tagCounts.isEmpty()) &&
                (daily == null || daily.isEmpty())) {
            showEmpty("No distractions recorded yet.");
            return;
        }

        emptyState.setVisible(false);

        // Pie chart (tag distribution)
        if (tagCounts != null && !tagCounts.isEmpty()) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            tagCounts.forEach((tag, cnt) -> pieData.add(new PieChart.Data(tag + " (" + cnt + ")", cnt)));
            tagPie.setData(pieData);
            tagPie.setVisible(true);
        } else {
            tagPie.setVisible(false);
        }

        // Line chart (daily trend)
        if (daily != null && !daily.isEmpty()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            daily.forEach((day, cnt) -> series.getData().add(new XYChart.Data<>(day, cnt)));
            trendChart.setAnimated(false);
            trendChart.setLegendVisible(false);
            trendYAxis.setLabel("Distractions/day");
            trendChart.getData().setAll(series);
            trendChart.setVisible(true);
        } else {
            trendChart.setVisible(false);
        }
    }

    private void showEmpty(String msg) {
        emptyState.setText(msg);
        emptyState.setVisible(true);
        if (tagPie != null) tagPie.setVisible(false);
        if (trendChart != null) trendChart.setVisible(false);
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
