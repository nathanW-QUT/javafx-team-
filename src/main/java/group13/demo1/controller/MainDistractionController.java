package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.MainDistractionDAO;
import group13.demo1.model.SqliteConnection;
import group13.demo1.model.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.List;

import java.io.IOException;

public class MainDistractionController {
    @FXML
    private ComboBox<String> CauseBox;
    @FXML
    private ComboBox<String> MinutesBox;
    @FXML
    private TextField descriptionField;
    @FXML
    private Button logButton;
    @FXML private ListView<MainDistractionDAO.MainItem> recentMainDistractions;

    private final MainDistractionDAO mainDistractionDAO = new MainDistractionDAO(SqliteConnection.getInstance());


    @FXML
    private void onClickLogDistraction() {
        String cause = CauseBox.getValue();
        String minutesStr = MinutesBox.getValue();
        String description = descriptionField.getText();

        if (cause == null || minutesStr == null) {
            System.out.println("Please select a cause and minutes.");
            return;
        }


        int minutes = Integer.parseInt(minutesStr);
        String username = UserSession.getInstance().getUsername();

        mainDistractionDAO.addMainDistraction(username, cause, minutes, description);
        CauseBox.setValue(null);
        MinutesBox.setValue(null);
        descriptionField.clear();

        System.out.println("Distraction logged successfully!");

    }

    @FXML
    private void initialize() {
        if (recentMainDistractions != null) {
            loadRecentDistractions();
        }
    }

    private void loadRecentDistractions() {
        String username = UserSession.getInstance().getUsername();
        List<MainDistractionDAO.MainItem> last4Items = mainDistractionDAO.getRecentForUser(username, 4);


        recentMainDistractions.getItems().setAll(last4Items);

        recentMainDistractions.setCellFactory(list -> new ListCell<>() {
            @Override

            protected void updateItem(MainDistractionDAO.MainItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);


                } else {
                    String desc = (item.description == null || item.description.isBlank())
                            ? ""
                            : " — " + item.description;
                    setText(item.timestamp + " — " + item.cause + " (" + item.minutes + "m)" + desc);
                }
            }
        });
    }

    @FXML
    private void onClickGoToLogger() throws IOException {
        Stage stage = (Stage) ((recentMainDistractions != null)
                ? recentMainDistractions.getScene().getWindow()
                : logButton.getScene().getWindow());

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Distraction.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }




}