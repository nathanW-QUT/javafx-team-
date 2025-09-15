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

import java.time.LocalDateTime;

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


    /// Attempring timer session ///
    private SessionModel currentSession;
    private long totalPauseMillis = 0;
    private int pauseCount = 0;
    private long pauseStart = 0;
    private final SessionDAO sessionDAO = new SessionDAO(SqliteConnection.getInstance());

    @FXML
    public void initialize() {
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                long totalElapsed = elapsedTime;
                if (running) totalElapsed += System.currentTimeMillis() - startTime;

                long sec = (totalElapsed / 1000) % 60;
                long min = (totalElapsed / 60000) % 60;
                long hr  = totalElapsed / 3600000;
/// i dont really get this properly but HERE: https://stackoverflow.com/questions/266825/how-to-format-a-duration-in-java-e-g-format-hmmss ///
                if(hr > 0) {
                    timerLabel.setText(String.format("%02d:%02d:%02d", hr, min, sec));
                } else {
                    timerLabel.setText(String.format("%02d:%02d", min, sec));
                }
            }
        };
        timer.start();

        String currentUser = UserSession.getInstance().getUsername();
        welcomeText.setText("Welcome, " + currentUser + "!");
        startSession(currentUser);
    }
    // Session helper
    private void startSession(String username) {
        try {
            currentSession = new SessionModel(username, LocalDateTime.now());
            sessionDAO.insert(currentSession);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void toggleStartStop(ActionEvent event) {
        if (!running)
        {
            startTime = System.currentTimeMillis();
            running = true;
            // lookin for pauses in session
            if (pauseStart > 0) {
                totalPauseMillis += System.currentTimeMillis() - pauseStart;
                pauseStart = 0;
            }
            startStopButton.setText("Pause");
            System.out.println("Timer started: " + LocalDateTime.now());
        } else
        {

            long endMillis     = System.currentTimeMillis();
            long sessionMillis = endMillis - startTime;

            running = false;
            elapsedTime += sessionMillis;
            // if pasued add a counter
            pauseStart = System.currentTimeMillis();
            pauseCount++;
            startStopButton.setText("Start");

            long sessionSeconds = Math.max(0, sessionMillis / 1000);
            LocalDateTime endDateTime   = LocalDateTime.now();
            LocalDateTime startDateTime = endDateTime.minusSeconds(sessionSeconds);


            String label = (activeDistraction != null && !activeDistraction.isBlank())
                    ? activeDistraction.trim()
                    : "Pause";

            String currentUser = UserSession.getInstance().getUsername();
            TimerModel record = new TimerModel(
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
        // stop running *Timer* if any
        running = false;

        long durationBeforeReset = elapsedTime;
        elapsedTime = 0;

        startStopButton.setText("Start");
        timerLabel.setText("00:00");
        System.out.println("Timer reset");

        String currentUser = UserSession.getInstance().getUsername();
        TimerModel record = new TimerModel(
                currentUser,
                "Reset",
                LocalDateTime.now().minusSeconds(durationBeforeReset / 1000),
                LocalDateTime.now(),
                durationBeforeReset / 1000
        );
        timerDAO.addTimer(record);
        saveTimerRecord(durationBeforeReset, "Reset");
        finishSession(durationBeforeReset);

    }
    private void saveTimerRecord(long durationMillis, String defaultLabel) {
        long secs = Math.max(0, durationMillis / 1000);
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusSeconds(secs);
        String label = (activeDistraction != null && !activeDistraction.isBlank())
                ? activeDistraction.trim()
                : defaultLabel;

        TimerModel record = new TimerModel(
                UserSession.getInstance().getUsername(),
                label, start, end, secs
        );
        timerDAO.addTimer(record);
        activeDistraction = null;
    }
    private void finishSession(long totalRunMillis) {
        try {
            if (currentSession != null) {
                currentSession.setEndTime(LocalDateTime.now());
                currentSession.setTotalRunSeconds(totalRunMillis / 1000);
                currentSession.setTotalPauseSeconds(totalPauseMillis / 1000);
                currentSession.setPauseCount(pauseCount);
                sessionDAO.update(currentSession);
                currentSession = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

            List<TimerModel> recent = timerDAO.getTimersForUser(currentUser);
            if (!recent.isEmpty() && "Pause".equalsIgnoreCase(recent.get(0).getLabel())) {
                TimerModel last = recent.get(0);
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