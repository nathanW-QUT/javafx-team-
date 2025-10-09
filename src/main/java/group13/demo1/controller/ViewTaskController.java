package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.IAccomplishmentDAO;
import group13.demo1.model.SqliteAccomplishmentDAO;
import group13.demo1.model.Accomplishment;
import group13.demo1.model.Task;
import group13.demo1.model.TaskDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.List;

public class ViewTaskController {
    @FXML
    private Button nextButton;
    @FXML private Label taskTitle;
    @FXML private Label taskDescription;
    @FXML private Label taskDueDate;
    @FXML private ProgressBar taskProgress;
    @FXML private ListView<CheckBox> subtaskListView;


    private Task currentTask;
    private final TaskDAO taskDao = new TaskDAO();
    private final SqliteAccomplishmentDAO accomplishmentDao = new SqliteAccomplishmentDAO();



    public void loadTask(Task task) {
        this.currentTask = task;
        taskTitle.setText(task.getTitle());
        taskDescription.setText(task.getDescription());
        taskDueDate.setText(task.getDueDate().toString());
        taskProgress.setProgress(task.getCompletion() / 100.0);

        List<String> subtasks = taskDao.getSubtasks(task.getId());
        ObservableList<CheckBox> subtaskItems = FXCollections.observableArrayList();

        for (String sub : subtasks) {
            CheckBox checkBox = new CheckBox(sub);
            checkBox.setSelected(taskDao.isSubtaskCompleted(task.getId(), sub));
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateProgress());
            subtaskItems.add(checkBox);
        }

        subtaskListView.setItems(subtaskItems);
    }


    @FXML
    public void onSaveProgress() {
        for (CheckBox checkBox : subtaskListView.getItems()) {
            taskDao.updateSubtaskStatus(currentTask.getId(), checkBox.getText(), checkBox.isSelected());
        }
        updateProgress();
        int progress = (int) (taskProgress.getProgress() * 100);
        taskDao.updateTaskCompletion(currentTask.getId(), progress);

        if (progress == 100) {
            Accomplishment accomplishment = new Accomplishment(
                    0,
                    currentTask.getUsername(),
                    currentTask.getTitle(),
                    true
            );
            accomplishmentDao.addAccomplishment(accomplishment);
            showAlert("Task Completed", "Congratulations! You’ve completed the task: " + currentTask.getTitle());
        } else {
            showAlert("Progress Saved", "Task progress updated.");
        }
    }

    @FXML
    public void onBack() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Tasks.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }

    private void updateProgress() {
        int total = subtaskListView.getItems().size();
        if (total == 0) {
            taskProgress.setProgress(0);
            return;
        }
        long completed = subtaskListView.getItems().stream().filter(CheckBox::isSelected).count();
        taskProgress.setProgress((double) completed / total);

    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
