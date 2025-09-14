package group13.demo1.controller;

import group13.demo1.model.DistractionDAO;
import group13.demo1.model.MainDistractionDAO;
import group13.demo1.model.SqliteConnection;
import group13.demo1.model.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class MainDistractionController {
    @FXML
    private ComboBox<String> CauseBox;
    @FXML
    private ComboBox<String> MinutesBox;
    @FXML
    private TextField descriptionField;
    @FXML
    private Button nextButton;

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

        // Clear form after logging
        CauseBox.setValue(null);
        MinutesBox.setValue(null);
        descriptionField.clear();

        System.out.println("Distraction logged successfully!");

    }

}


