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

public class EditAccountController {
    @FXML
    private Button nextButton;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;

    private final UserDao userDao = new UserDao();

    @FXML
    private void handleUpdatePassword() throws IOException {
        String username = usernameField.getText().trim();
        String currentPassword = currentPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();

        if (username.isEmpty() || currentPassword.isEmpty() || newPassword.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "All fields are required.").showAndWait();
            return;
        }

        boolean updated = userDao.updatePassword(username, currentPassword, newPassword);
        if (updated) {
            new Alert(Alert.AlertType.INFORMATION, "Password updated successfully!").showAndWait();
            usernameField.clear();
            currentPasswordField.clear();
            newPasswordField.clear();
            UserSession.createSession(username);

            Stage stage = (Stage) nextButton.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
            stage.setScene(scene);

            String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
            scene.getStylesheets().add(stylesheet);
        } else {
            new Alert(Alert.AlertType.ERROR, "Invalid username or password.").showAndWait();
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
