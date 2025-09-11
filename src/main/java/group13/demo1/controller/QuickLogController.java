package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.DistractionDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.io.IOException;
import java.time.LocalDateTime;

public class QuickLogController {

    @FXML private Button distractionButton;
    @FXML private Label distractionStatus;
    @FXML private TextField descriptionField;

    private final DistractionDAO distractionDao = new DistractionDAO();

    public void logDistraction() {
        String description = descriptionField.getText();

        if (description == null || description.isBlank()) {
            distractionStatus.setText("Please enter a description.");
            return;
        }

        boolean success = distractionDao.addDistraction(description);

        if (success) {
            distractionStatus.setText("Distraction logged!");
            descriptionField.clear();
        } else {
            distractionStatus.setText("Failed to log distraction.");
        }
    }
}


