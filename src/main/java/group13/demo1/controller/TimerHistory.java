package group13.demo1.controller;

import group13.demo1.model.MainDistractionDAO;
import group13.demo1.model.MainDistractionHistoryDAO;
import group13.demo1.model.SessionDAO;
import group13.demo1.model.SessionModel;
import group13.demo1.model.SqliteConnection;
import group13.demo1.model.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TimerHistory {

    // Timer sessions
    @FXML private ListView<TimerHistoryLogic.SessionData> sessionList;
    @FXML private Label totalsession;
    @FXML private Label totalTime;
    @FXML private Label session;

    @FXML private Label DateValue, StartValue, EndValue, FocusTimeValue, PausedTimeValue, PauseCountValue;

    private final Connection db = SqliteConnection.getInstance();
    private final TimerHistoryLogic logic = new TimerHistoryLogic();
    private final ObservableList<TimerHistoryLogic.SessionData> sessions = FXCollections.observableArrayList();

    //Main Distraction history
    @FXML private ListView<MainDistractionRow> mdList;
    @FXML private Label mdTotalLabel;
    @FXML private Label mdDetail;

    @FXML private Label mdReason, mdWhenTimeValue, mdMinutesValue, mdNotes;

    private final ObservableList<MainDistractionRow> mdItems = FXCollections.observableArrayList();
    private final MainDistractionHistoryDAO mdDAO = new MainDistractionHistoryDAO(SqliteConnection.getInstance());

    // distraction tab dto
    public static class MainDistractionRow {
        public final int id;
        public final String reason;
        public final String time_of_occurence;
        public final Integer minutes;
        public final String notes;

        public MainDistractionRow(int id, String cause, String when, Integer minutes, String notes)
        {
            this.id = id;
            this.reason = (cause == null || cause.isBlank()) ? "(untitled)" : cause;
            this.time_of_occurence = (when == null || when.isBlank()) ? null : when;
            this.minutes = minutes;
            this.notes = (notes == null || notes.isBlank()) ? null : notes;
        }

        public String listTitle(int index) { return "Distraction " + (index + 1) + " — " + reason; }
    }

    private static final DateTimeFormatter dt_formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @FXML
    public void initialize() {
        String user = (UserSession.getInstance() == null) ? null : UserSession.getInstance().getUsername();

        // Timer sessions
        if (sessionList != null)
        {
            sessionList.setItems(sessions);
            sessionList.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(TimerHistoryLogic.SessionData s, boolean empty) {
                    super.updateItem(s, empty);
                    setText(empty || s == null ? null : logic.listForSession(getIndex(), s));
                }
            });
            sessionList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, s) -> {
                session.setText(s == null ? "(none)" : logic.SelectedSessionText(
                        sessionList.getSelectionModel().getSelectedIndex(), s));
                SelectedSessionDisplay(s);
            });
        }

        loadSessionsForUser(user);

        //Distraction history
        MainDistractionList();
        loadMainDistractions(user);
    }

    private void SelectedSessionDisplay(TimerHistoryLogic.SessionData s) {
        if (s == null) {
            DateValue.setText("—");
            StartValue.setText("—");
            EndValue.setText("—");
            FocusTimeValue.setText("—");
            PausedTimeValue.setText("—");
            PauseCountValue.setText("—");
            return;
        }
        DateValue.setText(s.start == null ? "—" : DateTimeFormatter.ofPattern("MMM d,  yyyy").format(s.start));
        StartValue.setText(s.start == null ? "—" : DateTimeFormatter.ofPattern("hh:mm:ss a").format(s.start));
        EndValue.setText(s.end == null ? "—" : DateTimeFormatter.ofPattern("hh:mm:ss a").format(s.end));
        FocusTimeValue.setText(logic.formatElapsedTime(s.focustime));
        PausedTimeValue.setText(logic.formatElapsedTime(s.pausetime));
        PauseCountValue.setText(Integer.toString(s.pausecount));
    }

    private void loadSessionsForUser(String username) {
        sessions.clear();

        if (username == null || username.isBlank())
        {
            totalsession.setText("Total Sessions: 0");
            totalTime.setText("Total Focus Time: 0s");
            session.setText("(none)");
            SelectedSessionDisplay(null);
            return;
        }

        List<TimerHistoryLogic.SessionData> rows = new ArrayList<>();
        long totalFocus = 0L;

        try {
            List<SessionModel> all = new SessionDAO(db).getAll();


            List<SessionModel> mine = new ArrayList<>();
            for (SessionModel m : all)
            {
                if (m == null) continue;
                if (!username.equals(m.getUsername())) continue;
                boolean nonZero = (m.getTotalRunSeconds() > 0) ||
                        (m.getTotalPauseSeconds() > 0) ||
                        (m.getPauseCount() > 0);
                if (nonZero) mine.add(m);
            }
            mine.sort(Comparator.comparing(SessionModel::getStartTime,
                    (a, b) -> {
                        if (a == null && b == null) return 0;
                        if (a == null) return 1;
                        if (b == null) return -1;
                        return b.compareTo(a);
                    }));

            for (SessionModel m : mine)
            {
                TimerHistoryLogic.SessionData s = new TimerHistoryLogic.SessionData(
                        m.getId(),
                        m.getUsername(),
                        m.getStartTime(),
                        m.getEndTime(),
                        m.getTotalRunSeconds(),
                        m.getTotalPauseSeconds(),
                        m.getPauseCount()
                );
                rows.add(s);
                totalFocus += m.getTotalRunSeconds();
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        sessions.setAll(rows);
        totalsession.setText("Total Sessions: " + sessions.size());
        totalTime.setText("Total Focus Time: " + logic.formatTotal(totalFocus));

        if (sessions.isEmpty()) { session.setText("(none)"); SelectedSessionDisplay(null); }
        else sessionList.getSelectionModel().select(0);
    }

    //Main distraction
    private void MainDistractionList()
    {
        if (mdList == null) return;

        mdList.setItems(mdItems);
        mdList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(MainDistractionRow row, boolean empty) {
                super.updateItem(row, empty);
                setText(empty || row == null ? null : row.listTitle(getIndex()));
            }
        });
        mdList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, row) -> MainDistractionSelectedRecord(row));
    }

    private void MainDistractionSelectedRecord(MainDistractionRow row)
    {
        if (row == null)
        {
            mdReason.setText("—");
            mdWhenTimeValue.setText("—");
            mdMinutesValue.setText("—");
            mdNotes.setText("—");
            if (mdDetail != null) mdDetail.setText("");
            return;
        }
        mdReason.setText(row.reason == null ? "—" : row.reason);
        mdWhenTimeValue.setText(row.time_of_occurence == null ? "—" : row.time_of_occurence);
        mdMinutesValue.setText(row.minutes == null ? "—" : row.minutes + "m");
        mdNotes.setText(row.notes == null ? "—" : row.notes);
        if (mdDetail != null) mdDetail.setText("");
    }

    private void loadMainDistractions(String username)
    {
        if (username == null || username.isBlank() || mdItems == null) return;
        mdItems.clear();
        List<MainDistractionDAO.MainItem> all = mdDAO.getAllForUser(username);
        for (MainDistractionDAO.MainItem it : all)
        {
            mdItems.add(new MainDistractionRow(
                    it.id,
                    it.cause,
                    it.timestamp,
                    it.minutes,
                    it.description
            ));
        }
        if (mdTotalLabel != null) mdTotalLabel.setText("Total Records: " + mdItems.size());
        if (!mdItems.isEmpty() && mdList != null) mdList.getSelectionModel().select(0);
        else MainDistractionSelectedRecord(null);
    }

    @FXML
    private void onConfirm()
    {
        if (sessionList != null && sessionList.getSelectionModel().getSelectedIndex() >= 0)
        {
            sessionList.getSelectionModel().clearSelection();
            session.setText("(none)");
            SelectedSessionDisplay(null);
        }
    }

    @FXML
    private void onDelete() {
        int idx = (sessionList == null) ? -1 : sessionList.getSelectionModel().getSelectedIndex();
        if (idx < 0) return;
        TimerHistoryLogic.SessionData s = sessions.get(idx);
        String sql = "DELETE FROM sessions WHERE id=?";
        try (PreparedStatement ps = db.prepareStatement(sql))
        {
            ps.setInt(1, s.id);
            ps.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        String user = (UserSession.getInstance() == null) ? null : UserSession.getInstance().getUsername();
        loadSessionsForUser(user);
    }
}
