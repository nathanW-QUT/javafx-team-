package group13.demo1.controller;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CountdownController {

    @FXML private TextField inputMinutes;
    @FXML private TextField inputSeconds;
    @FXML private Label countdownLabel;
    @FXML private Button startPauseButton;
    @FXML private Button resetButton;

    private long remainingMillis = 0;
    private long lastTick = 0;
    private boolean running = false;
    private AnimationTimer timer;

    @FXML
    public void initialize() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (running) {
                    long nowMillis = System.currentTimeMillis();
                    long delta = nowMillis - lastTick;
                    lastTick = nowMillis;
                    remainingMillis = Math.max(0, remainingMillis - delta);
                    updateLabel();

                    if (remainingMillis == 0) {
                        running = false;
                        startPauseButton.setText("Start");
                        // TODO: play a sound/alert if you want
                    }
                }
            }
        };
        timer.start();
        updateLabel();
    }

    @FXML
    public void startPause(ActionEvent event) {
        if (!running) {
            if (remainingMillis <= 0) {
                // user just set the time
                long mins = parseLongSafe(inputMinutes.getText());
                long secs = parseLongSafe(inputSeconds.getText());
                remainingMillis = (mins * 60 + secs) * 1000;
            }
            lastTick = System.currentTimeMillis();
            running = true;
            startPauseButton.setText("Pause");
        } else {
            running = false;
            startPauseButton.setText("Start");
        }
    }

    @FXML
    public void reset(ActionEvent event) {
        running = false;
        remainingMillis = 0;
        startPauseButton.setText("Start");
        updateLabel();
    }

    private void updateLabel() {
        long totalSeconds = remainingMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        countdownLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private long parseLongSafe(String text) {
        try {
            return Long.parseLong(text.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
