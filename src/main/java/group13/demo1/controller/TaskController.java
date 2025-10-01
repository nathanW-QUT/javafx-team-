package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class TaskController{
    @FXML
    private Button nextButton;
@FXML private TextField titleField;
@FXML private TextArea descriptionField;
@FXML private DatePicker dueDatePicker;
@FXML private TilePane tasksTilePane;
@FXML private VBox taskDetailsBox;

@FXML private Label taskDescriptionLabel;
@FXML private Label taskDueDateLabel;
@FXML private ProgressBar taskCompletionBar;
@FXML private CheckBox completionCheckBox;

private final TaskDAO taskDao = new TaskDAO();
private Task currentTask;

@FXML
public void initialize() {
    loadTasks();
}

@FXML
private void onCreateTask() {
    String username = UserSession.getInstance().getUsername();
    String title = titleField.getText();
    String description = descriptionField.getText();
    LocalDate dueDate = dueDatePicker.getValue();

    if (title.isEmpty() || dueDate == null) return;

    Task task = new Task(0, username, title, description, dueDate, 0);
    taskDao.addTask(task);
    clearForm();
    loadTasks();
}

    private void loadTasks() {
        tasksTilePane.getChildren().clear();
        List<Task> tasks = taskDao.getTasksForUser(UserSession.getInstance().getUsername());
        for (Task task : tasks) {
            Button taskButton = new Button(task.getTitle());
            taskButton.setPrefWidth(250);
            taskButton.setOnAction(e -> openViewTaskPage(task));
            tasksTilePane.getChildren().add(taskButton);
        }
    }

    private void openViewTaskPage(Task task) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/group13/demo1/ViewTask.fxml"));
                Parent root = loader.load();

                ViewTaskController controller = loader.getController();
                controller.loadTask(task);

                Scene scene = new Scene(root, HelloApplication.WIDTH, 640);
                String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
                scene.getStylesheets().add(stylesheet);

                Stage stage = (Stage) tasksTilePane.getScene().getWindow();
                stage.setScene(scene);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }

}



@FXML
private void onCreateTaskPage() throws IOException {

    Stage stage = (Stage) nextButton.getScene().getWindow();
    FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("CreateTask.fxml"));
    Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, 640);
    stage.setScene(scene);
    String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
    scene.getStylesheets().add(stylesheet);


}

@FXML
private void onToggleCompletion() {
    if (currentTask != null) {
        currentTask.setCompletion(completionCheckBox.isSelected() ? 100 : 0);
        taskDao.updateTaskCompletion(currentTask.getId(), currentTask.getCompletion());
        taskCompletionBar.setProgress(currentTask.getCompletion() / 100.0);
    }
}

private void clearForm() {
    titleField.clear();
    descriptionField.clear();
    dueDatePicker.setValue(null);
}


}