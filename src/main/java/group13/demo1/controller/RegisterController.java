package group13.demo1.controller;
import group13.demo1.HelloApplication;
import group13.demo1.model.UserDao;
import group13.demo1.model.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.io.IOException;


public class RegisterController {
    @FXML
    private Button nextButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private final UserDao userDao = new UserDao();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            statusLabel.setText("Please enter both fields.");
            return;
        }

        boolean success = userDao.addUser(username, password);
        if (success) {
            UserSession.createSession(username);
            statusLabel.setText("Account created! Logging in...");


            try {
                Stage stage = (Stage) nextButton.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
                stage.setScene(scene);
                String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
                scene.getStylesheets().add(stylesheet);
            } catch (IOException e) {
                e.printStackTrace();
                statusLabel.setText("Error loading main page.");
            }

        } else {
            statusLabel.setText("Username already taken.");
        }
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) nextButton.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}