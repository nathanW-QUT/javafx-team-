package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.UserDao;
import group13.demo1.model.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class DeleteAccountController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML
    private Button nextButton;

    private final UserDao userDao = new UserDao();

    @FXML
    private void handleDeleteAccount() throws IOException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Please enter both username and password.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this account?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Deletion");
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            boolean deleted = userDao.deleteAccount(username, password);
            if (deleted) {
                new Alert(Alert.AlertType.INFORMATION, "Account deleted successfully.").showAndWait();
                usernameField.clear();
                passwordField.clear();

                    Stage stage = (Stage) nextButton.getScene().getWindow();
                    FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
                    Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
                    stage.setScene(scene);
                    String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
                    scene.getStylesheets().add(stylesheet);

            } else {
                new Alert(Alert.AlertType.ERROR, "Invalid credentials or account not found.").showAndWait();
            }
        }
    }
    @FXML
    private void onClickback() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }
}
