package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.UserSession;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.io.IOException;

public class Timer {

    @FXML
    private Button nextButton; // Log Distraction button

    @FXML
    private Button startStopButton; // toggles start/stop

    @FXML
    private Button resetButton;

    @FXML
    private Label timerLabel;
    @FXML
    private Label welcomeText;


    private int secondsElapsed = 0;
    private Timeline timeline;


    @FXML
    private void initialize() {
        timerLabel.setText("00:00");
        startStopButton.setText("Start");

    }


    // I need to make it so that the duration is further out, maybe a public variable
    // so that I am able to keep track of the duration of the timer despite pauses

    @FXML
    private void toggleStartStop() {

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (timeline != null && timeline.getStatus() == Timeline.Status.RUNNING) {

            timeline.stop();
            System.out.println("Timer paused at: "+ now.format(formatter));
            startStopButton.setText("Start");
        } else {

            System.out.println("Timer started at: " + now.format(formatter));


            timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                secondsElapsed++;
                int minutes = secondsElapsed / 60;
                int seconds = secondsElapsed % 60;
                timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();

            startStopButton.setText("Pause");
        }
    }

    @FXML
    private void resetTimer() {
        if (timeline != null) {
            timeline.stop();
        }
        secondsElapsed = 0;
        timerLabel.setText("00:00");
        startStopButton.setText("Start");
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
}
