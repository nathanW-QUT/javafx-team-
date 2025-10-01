package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.Task;
import group13.demo1.model.TaskDAO;
import group13.demo1.model.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class CreateTaskController {
    @FXML
    private Button nextButton;

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker dueDatePicker;
    @FXML private TextField subtaskField;
    @FXML private ListView<String> subtaskList;

    private final ObservableList<String> subtasks = FXCollections.observableArrayList();
    private final TaskDAO taskDao = new TaskDAO();

    @FXML
    public void initialize() {
        subtaskList.setItems(subtasks);
    }

    @FXML
    public void onAddSubtask(ActionEvent event) {
        String subtask = subtaskField.getText().trim();
        if (!subtask.isEmpty()) {
            subtasks.add(subtask);
            subtaskField.clear();
        }
    }

    @FXML
    public void onSaveTask(ActionEvent event) throws IOException {
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
        LocalDate dueDate = dueDatePicker.getValue();

        if (title.isEmpty() || dueDate == null) {
            showAlert("Missing Fields", "Please enter a title and due date.");
            return;
        }

        String username = UserSession.getInstance().getUsername();
        Task task = new Task(0, username, title, description, dueDate, 0);
        int taskId = taskDao.addTask(task);
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Tasks.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
        List<String> subtasks = List.of("Subtask 1", "Subtask 2", "Subtask 3");

        taskDao.addSubtasks(taskId, subtasks);



    }

    @FXML
    public void onCancel(ActionEvent event) throws IOException {
        goBack();
    }

    private void goBack() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Tasks.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
