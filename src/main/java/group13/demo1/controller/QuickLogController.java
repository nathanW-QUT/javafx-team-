package group13.demo1.controller;
import group13.demo1.model.DistractionDAO;
import group13.demo1.model.UserSession;
import group13.demo1.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;

public class QuickLogController {

    @FXML private Button distractionButton;
    @FXML private Label distractionStatus;
    @FXML private TextField descriptionField;

    private final DistractionDAO distractionDAO = new DistractionDAO();
    private final ITimerDAO timerDAO = new SqliteTimerDAO();

    @FXML
    private void initialize() {
        distractionButton.setOnAction(event -> logDistraction());
    }

    public void logDistraction() {
        String description = descriptionField.getText();
        String currentUser = UserSession.getInstance().getUsername();

        if (currentUser == null || currentUser.isBlank()) {
            distractionStatus.setText("Please log in first.");
            return;
        }
        if (description == null || description.isBlank()) {
            distractionStatus.setText("Please enter a description.");
            return;
        }

        String tag = description.trim();
        boolean success = distractionDAO.addDistraction(tag, currentUser);


        try {
            List<TimerRecord> recent = timerDAO.getTimersForUser(currentUser);
            if (!recent.isEmpty()) {
                TimerRecord last = recent.get(0);
                if ("Pause".equalsIgnoreCase(last.getLabel())) {
                    last.setLabel(tag);
                    timerDAO.updateTimer(last);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not update last timer label: " + e.getMessage());
        }

        if (success) {
            distractionStatus.setText("Distraction logged!");
            descriptionField.clear();
        } else {
            distractionStatus.setText("Failed to log distraction.");

        }
        descriptionField.clear();
    }
}

