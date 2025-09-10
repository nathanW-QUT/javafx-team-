package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.DistractionDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.io.IOException;

public class QuickLogController {

    @FXML private Button distractionButton;
    @FXML private Label distractionStatus;

    private final DistractionDAO distractionDao = new DistractionDAO();

    @FXML
    private void initialize() {
        distractionButton.setOnAction(event -> logDistraction());
    }

    public void logDistraction() {
        boolean success = distractionDao.addDistraction();
        if (success) {
            distractionStatus.setText("Distraction logged!");
        } else {
            distractionStatus.setText("Failed to log distraction.");
        }
    }
}


