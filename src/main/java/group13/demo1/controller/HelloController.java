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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HelloController  {

    @FXML
    private Label welcomeText;
    @FXML
    private Button nextButton;



    @FXML private BarChart<String, Number> homeDisBar;
    @FXML private CategoryAxis homeDisBarXAxis;
    @FXML private NumberAxis homeDisBarYAxis;


    @FXML
    public void initialize() {
        initializeHomeDistractionsChart();
    }

    @FXML
    protected void onHomeButtonClick() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }

    @FXML
    private void onClickLogDistraction() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Distraction.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }

    @FXML
    private void onClickLogOut() throws IOException {
        UserSession.clearSession();
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }

    @FXML
    private void onClickGoTimer() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Timer.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }

    @FXML
    private void onClickGoTasks() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Tasks.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }

    @FXML
    private void onClickGoAccomplishment() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Accomplishment.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }

    @FXML
    private void onClickGoHistory() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("TimerHistory.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }

    @FXML
    private void onClickGoGraphs() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxml = new FXMLLoader(HelloApplication.class.getResource("Graphs.fxml"));
        Scene scene = new Scene(fxml.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String css = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(css);
    }

    @FXML
    protected void onLoginButtonClicked() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }

    @FXML
    protected void onRegisterButtonClicked() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Register.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }

    @FXML
    protected void onCountdownButtonClicked() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Countdown.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }

    @FXML
    protected void onClickGoDistractionPage() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("DistractionPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }
    @FXML
    private void onClickAccount() throws IOException {
            Stage stage = (Stage) nextButton.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Account.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
            stage.setScene(scene);
            String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
            scene.getStylesheets().add(stylesheet);
    }



    private void initializeHomeDistractionsChart() {
        if (homeDisBar == null) return; // this controller is also used by other FXML files

        String user = (UserSession.getInstance() == null)
                ? null : UserSession.getInstance().getUsername();
        if (user == null || user.isBlank()) {
            homeDisBar.setVisible(false);
            return;
        }

        // last-7-days labels
        List<LocalDate> last7 = last7Dates();
        Map<String, String> labelByIso = new LinkedHashMap<>();
        ObservableList<String> categories = FXCollections.observableArrayList();
        for (LocalDate d : last7) {
            String iso = d.toString();
            String day = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            String label = iso + "\n" + day;
            labelByIso.put(iso, label);
            categories.add(label);
        }

        Map<String, Long> distractionDaily = loadMainDistractionDailyCounts(user);

        if (homeDisBarXAxis != null) {
            homeDisBarXAxis.setLabel("Date");
            homeDisBarXAxis.setCategories(categories);
        }
        if (homeDisBarYAxis != null) {
            homeDisBarYAxis.setLabel("Count");
            homeDisBarYAxis.setForceZeroInRange(true);
        }

        XYChart.Series<String, Number> s = new XYChart.Series<>();
        for (LocalDate d : last7) {
            String iso = d.toString();
            String label = labelByIso.get(iso);
            s.getData().add(new XYChart.Data<>(label, distractionDaily.getOrDefault(iso, 0L)));
        }

        homeDisBar.setAnimated(false);
        homeDisBar.setLegendVisible(false);
        homeDisBar.getData().setAll(s);
        homeDisBar.setVisible(true);

        Platform.runLater(() -> s.getData().forEach(dp -> {
            if (dp.getNode() != null) {
                Tooltip.install(dp.getNode(),
                        new Tooltip("Distractions\n" + dp.getXValue().replace('\n', ' ')
                                + " : " + dp.getYValue()));
            }
        }));
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
        try (PreparedStatement ps = SqliteConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("day"), rs.getLong("cnt"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    private List<LocalDate> last7Dates() {
        List<LocalDate> out = new ArrayList<>(7);
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) out.add(today.minusDays(i));
        return out;
    }
}
