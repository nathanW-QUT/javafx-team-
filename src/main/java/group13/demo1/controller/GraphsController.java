package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.SqliteTimerDAO;
import group13.demo1.model.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class GraphsController {

    @FXML private PieChart tagPie;
    @FXML private BarChart<String, Number> tagBar;
    @FXML private CategoryAxis tagBarXAxis;
    @FXML private NumberAxis tagBarYAxis;

    @FXML private LineChart<String, Number> trendChart;
    @FXML private CategoryAxis trendXAxis;
    @FXML private NumberAxis trendYAxis;

    @FXML private TableView<TagCount> rankingTable;
    @FXML private TableColumn<TagCount, String> tagCol;
    @FXML private TableColumn<TagCount, Number> countCol;

    @FXML private Label emptyState;
    @FXML private Button backBtn;

    private final SqliteTimerDAO dao = new SqliteTimerDAO();

    public static class TagCount {
        private final String tag;
        private final long count;
        public TagCount(String tag, long count) { this.tag = tag; this.count = count; }
        public String getTag()   { return tag; }
        public long getCount()   { return count; }
    }

    @FXML
    public void initialize() {
        String user = UserSession.getInstance() == null ? null : UserSession.getInstance().getUsername();
        if (user == null || user.isBlank()) {
            showEmpty("Please log in to view insights.");
            return;
        }

        Map<String, Long> tagCounts = dao.getTagCountsForUser(user);          // ordered desc
        Map<String, Long> daily     = dao.getDailyDistractionCounts(user);    // yyyy-MM-dd -> count

        if (tagCounts.isEmpty() && daily.isEmpty()) {
            showEmpty("No distractions recorded yet.");
            return;
        }
        emptyState.setVisible(false);

        // Ranking table
        tagCol.setCellValueFactory(new PropertyValueFactory<>("tag"));
        countCol.setCellValueFactory(new PropertyValueFactory<>("count"));
        ObservableList<TagCount> rows = FXCollections.observableArrayList();
        tagCounts.forEach((k,v) -> rows.add(new TagCount(k, v)));
        rankingTable.setItems(rows);

        // Pie chart (tag split)
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        tagCounts.forEach((k,v) -> pieData.add(new PieChart.Data(k + " (" + v + ")", v)));
        tagPie.setData(pieData);
        tagPie.setLegendVisible(false);

        // Bar chart (same data, better for ranking)
        XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
        tagCounts.forEach((k,v) -> barSeries.getData().add(new XYChart.Data<>(k, v)));
        tagBar.getData().setAll(barSeries);
        tagBar.setLegendVisible(false);
        tagBarYAxis.setLabel("Count");

        // Trend line (per day)
        XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
        daily.forEach((day, cnt) -> lineSeries.getData().add(new XYChart.Data<>(day, cnt)));
        trendChart.getData().setAll(lineSeries);
        trendChart.setLegendVisible(false);
        trendYAxis.setLabel("Distractions/day");
    }

    private void showEmpty(String msg) {
        emptyState.setText(msg);
        emptyState.setVisible(true);
        tagPie.setVisible(false);
        tagBar.setVisible(false);
        trendChart.setVisible(false);
        rankingTable.setVisible(false);
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
