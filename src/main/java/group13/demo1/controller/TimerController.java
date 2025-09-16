package group13.demo1.controller;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.util.List;
import group13.demo1.model.*;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.text.BreakIterator;

import java.time.LocalDateTime;
import group13.demo1.controller.QuickLogController;

public class TimerController {

    @FXML
    private Label timerLabel;

    @FXML
    private Button startStopButton;

    @FXML
    private Label welcomeText;

    @FXML private TextField distractionDescription;
    @FXML private TextField descriptionField;
    @FXML private Label distractionStatus;
    @FXML private VBox distractionBox;

    private final ITimerDAO timerDAO = new SqliteTimerDAO();
    private final QuickLogController quickLogController = new QuickLogController();
    private final DistractionDAO distractionDAO = new DistractionDAO();

    private boolean running = false;
    private long startTime;
    private long elapsedTime = 0;


    private String activeDistraction = null;

    private AnimationTimer timer;

    @FXML
    public void initialize() {
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                long totalElapsed = elapsedTime;
                if (running) totalElapsed += System.currentTimeMillis() - startTime;

                long seconds = (totalElapsed / 1000) % 60;
                long minutes = (totalElapsed / (1000 * 60)) % 60;
                long hours   =  totalElapsed / (1000 * 60 * 60);

                if (hours > 0) {
                    timerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                } else {
                    timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
                }
            }
        };
        timer.start();

        String currentUser = UserSession.getInstance().getUsername();
        welcomeText.setText("Welcome, " + currentUser + "!");
    }

    @FXML
    public void toggleStartStop(ActionEvent event) {
        if (!running)
        {
            startTime = System.currentTimeMillis();
            running = true;
            startStopButton.setText("Pause");
            System.out.println("Timer started: " + LocalDateTime.now());
        } else
        {

            long endMillis     = System.currentTimeMillis();
            long sessionMillis = endMillis - startTime;

            running = false;
            elapsedTime += sessionMillis;
            startStopButton.setText("Start");

            long sessionSeconds = Math.max(0, sessionMillis / 1000);
            LocalDateTime endDateTime   = LocalDateTime.now();
            LocalDateTime startDateTime = endDateTime.minusSeconds(sessionSeconds);


            String label = (activeDistraction != null && !activeDistraction.isBlank())
                    ? activeDistraction.trim()
                    : "Pause";

            String currentUser = UserSession.getInstance().getUsername();
            TimerRecord record = new TimerRecord(
                    currentUser,
                    label,
                    startDateTime,
                    endDateTime,
                    sessionSeconds
            );
            timerDAO.addTimer(record);


            activeDistraction = null;

            System.out.println("Timer paused at " + sessionSeconds + " seconds, label=" + label);
        }
    }

    @FXML
    public void resetTimer(ActionEvent event) {
        // stop running session if any
        if (running) {
            // Edit: Makes sure that the Reset logs the time correctly
            long currentTime = System.currentTimeMillis();
            elapsedTime += (currentTime - startTime); // Adds the current running segment when hasn't been pasued
        }
        running = false;

        long durationBeforeReset = elapsedTime;
        long durationInSeconds = durationBeforeReset / 1000;
        //reset the timer to 0
        elapsedTime = 0;

        startStopButton.setText("Start");
        timerLabel.setText("00:00");
        System.out.println("Timer reset");

        String currentUser = UserSession.getInstance().getUsername();
        TimerRecord record = new TimerRecord(
                currentUser,
                "Reset",
                LocalDateTime.now().minusSeconds(durationInSeconds),
                LocalDateTime.now(),
                durationInSeconds
        );
        timerDAO.addTimer(record);
    }

    @FXML
    public void showQuickLog() {
        distractionBox.setVisible(true);
        distractionBox.setManaged(true);
    }

    @FXML
    public void onClickLogDistraction() {
        String tag = (descriptionField.getText() == null) ? "" : descriptionField.getText().trim();
        String currentUser = UserSession.getInstance().getUsername();

        if (tag.isBlank()) {
            distractionStatus.setText("Please enter a description.");
            return;
        }
        if (currentUser == null || currentUser.isBlank()) {
            distractionStatus.setText("Please log in first.");
            return;
        }


        distractionDAO.addDistraction(tag, currentUser);

        if (running) {

            activeDistraction = tag;
            distractionStatus.setText("Tag set for next session: " + tag);
        } else {

            List<TimerRecord> recent = timerDAO.getTimersForUser(currentUser);
            if (!recent.isEmpty() && "Pause".equalsIgnoreCase(recent.get(0).getLabel())) {
                TimerRecord last = recent.get(0);
                last.setLabel(tag);
                timerDAO.updateTimer(last);
                distractionStatus.setText("Updated the session tag to: " + tag);
            } else {

                activeDistraction = tag;
                distractionStatus.setText("Tag set for next session: " + tag);
            }
        }

        descriptionField.clear();
    }
    private String formatElapsedTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}


//    @FXML
//    public void resetTimer(ActionEvent event) {
//        running = false;
////        elapsedTime = 0;
//        startStopButton.setText("Start");
//        timerLabel.setText("00:00");
//        System.out.println("Timer reset");
//
//        String currentUser = UserSession.getInstance().getUsername();
//        TimerRecord record = new TimerRecord(
//                currentUser,
//                "Reset",
//                LocalDateTime.now(),
//                LocalDateTime.now(),
//                elapsedTime / 1000
//        );
//        timerDAO.addTimer(record);
//    }

    //@FXML
    //public void onClickLogDistraction(ActionEvent event) {
       // System.out.println("Distraction logged!");
   // }


//private void saveTimer(String label, long startTime, long endTime) {
//    TimerRecord record = new TimerRecord(label, startTime, endTime);
//    timerDAO.addTimer(record);
//}