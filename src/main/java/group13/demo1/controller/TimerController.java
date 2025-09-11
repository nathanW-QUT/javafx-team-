package group13.demo1.controller;

import group13.demo1.model.*;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.time.LocalDateTime;

public class TimerController {


    @FXML
    private Label timerLabel;

    @FXML
    private Button startStopButton;

    @FXML
    private Label welcomeText;

    private ITimerDAO timerDAO;
    private boolean running = false;
    private long startTime; //  nanoseconds
    private long elapsedTime = 0; //  milliseconds

    private AnimationTimer timer;

    public TimerController() {
        timerDAO = new SqliteTimerDAO();
    }

    @FXML
    public void initialize() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long totalElapsed = elapsedTime;
                if (running) {
                    totalElapsed += System.currentTimeMillis() - startTime;
                }

                long seconds = (totalElapsed / 1000) % 60;
                long minutes = (totalElapsed / (1000 * 60)) % 60;
                long hours = totalElapsed / (1000 * 60 * 60);

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
        if (!running) {
            startTime = System.currentTimeMillis();
            running = true;
            startStopButton.setText("Pause");
            System.out.println("Timer started at " + LocalDateTime.now());
        } else {
            long endMillis = System.currentTimeMillis();
            running = false;
            elapsedTime += endMillis - startTime;
            startStopButton.setText("Start");

            LocalDateTime startDateTime = LocalDateTime.now().minusSeconds(elapsedTime / 1000);
            LocalDateTime endDateTime = LocalDateTime.now();

            String currentUser = UserSession.getInstance().getUsername();
            TimerRecord record = new TimerRecord(
                    currentUser,
                    "Pause",
                    startDateTime,
                    endDateTime,
                    elapsedTime / 1000 // stored in seconds instead of mili
            );
            timerDAO.addTimer(record);

            System.out.println("Timer paused at " + (elapsedTime / 1000) + " seconds");
        }
    }


    @FXML
    public void resetTimer(ActionEvent event) {
        running = false;
        elapsedTime = 0;
        startStopButton.setText("Start");
        timerLabel.setText("00:00");
        System.out.println("Timer reset");

        String currentUser = UserSession.getInstance().getUsername();
        TimerRecord record = new TimerRecord(
                currentUser,
                "Reset",
                LocalDateTime.now(),
                LocalDateTime.now(),
                0
        );
        timerDAO.addTimer(record);
    }

    @FXML
    public void onClickLogDistraction(ActionEvent event) {
        System.out.println("Distraction logged!");
    }



    private String formatElapsedTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
//private void saveTimer(String label, long startTime, long endTime) {
//    TimerRecord record = new TimerRecord(label, startTime, endTime);
//    timerDAO.addTimer(record);
//}