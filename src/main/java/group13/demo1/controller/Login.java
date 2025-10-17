package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.UserDao;
import group13.demo1.model.UserSession;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class Login {
    @FXML
    private Button nextButton;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private final UserDao userDao = new UserDao();

    @FXML
    private VBox loginBox;

    @FXML
    public void initialize()
    {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.2), loginBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    @FXML
    private void onClickLogOut() throws IOException {

        UserSession.clearSession();

        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }
    @FXML
    private void handleLogin() throws IOException{
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (userDao.validateLogin(username, password)) {
            UserSession.createSession(username);
            statusLabel.setText("Login successful!");
            Stage stage = (Stage) nextButton.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
            stage.setScene(scene);

            String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
            scene.getStylesheets().add(stylesheet);
        } else {
            statusLabel.setStyle("-fx-text-fill: red");
            statusLabel.setText("Invalid username or password.");
            playShakeAnimation(usernameField);
            playShakeAnimation(passwordField);
        }
    }

    private void playShakeAnimation(Node node)
    {
        TranslateTransition shake = new TranslateTransition(Duration.seconds(0.08), node);
        shake.setFromX(0);
        shake.setToX(8);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    @FXML
    private void onClickEditPassword() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("EditAccount.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }
    @FXML
    private void onDeleteAccount() throws IOException {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("DeleteAccount.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
        String stylesheet = HelloApplication.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
    }
}
