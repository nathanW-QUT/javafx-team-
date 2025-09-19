package group13.demo1.controller;
import group13.demo1.model.UserSession;
import group13.demo1.HelloApplication;
import group13.demo1.model.ITimerDAO;
import group13.demo1.model.SqliteTimerDAO;
import group13.demo1.model.TimerRecord;
import javafx.collections.ListChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
//
import java.io.IOException;
import java.util.List;

public class TimerHistory {

    @FXML private Label selectedHeader;
    @FXML private ListView<TimerRecord> list;
    @FXML private Label session;
    @FXML private Label totalsession;
    @FXML private Label totalTime;

    private final ITimerDAO dao = new SqliteTimerDAO();
    private final TimerHistoryLogic logic = new TimerHistoryLogic();

    private ObservableList<TimerRecord> items;

    @FXML
    public void initialize()
    {
        String user = UserSession.getInstance().getUsername();
        List<TimerRecord> rows = dao.getTimersForUser(user);
        logic.sortNewestFirst(rows);

        items = FXCollections.observableArrayList(rows);

        list.setCellFactory(lv -> new ListCell<TimerRecord>()
        {
            @Override protected void updateItem(TimerRecord t, boolean empty)
            {
                super.updateItem(t, empty);
                if (empty || t == null)
                {
                    setText(null);
                } else
                {
                    setText(logic.Row(getIndex(), t));
                }
            }
        });

        list.setItems(items);
        list.setPlaceholder(new Label("No timer sessions yet."));


        totalsession.setText(logic.totalsHeaderInitial(items.size()));
        items.addListener((ListChangeListener<TimerRecord>) c -> totalsession.setText(logic.totalsHeaderOnChange(items.size())));


        updateTotal();
        items.addListener((ListChangeListener<TimerRecord>) c -> updateTotal());


        list.getSelectionModel().selectedItemProperty().addListener((obs, oldV, t) -> {
            if (t == null)
            {
                session.setText("(none)");
            } else
            {
                int index = list.getSelectionModel().getSelectedIndex();
                session.setText(logic.SelectedSessionText(index, t));
            }
        });

        if (!items.isEmpty()) list.getSelectionModel().select(0);
        else session.setText("(none)");
    }

    private void updateTotal()
    {
        long totalSecs = logic.TotalSeconds(items);
        if (totalTime != null)
        {
            totalTime.setText("Total Distracted Time: " + logic.formatTotal(totalSecs));
        }
    }

    @FXML
    private void onConfirm()
    {
        if (list.getSelectionModel().getSelectedIndex() >= 0)
        {
            list.getSelectionModel().clearSelection();
            list.getFocusModel().focus(-1);
            session.setText("(none)");
        }
    }

    @FXML
    private void onDelete()
    {
        int i = list.getSelectionModel().getSelectedIndex();
        if (i < 0) return;

        TimerRecord t = items.get(i);
        dao.deleteTimer(t);
        items.remove(i);
        list.refresh();
        updateTotal();

        if (items.isEmpty())
        {
            session.setText("(none)");
            return;
        }
        int next = logic.nextIndexAfterDelete(i, items.size());
        if (next >= 0) list.getSelectionModel().select(next);
    }

    @FXML
    private void onBackHome() throws IOException {
        Stage stage = (Stage) list.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }
}