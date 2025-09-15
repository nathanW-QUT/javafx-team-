package group13.demo1.controller;

import group13.demo1.HelloApplication;
import group13.demo1.model.UserDao;
import group13.demo1.model.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import group13.demo1.model.SqliteConnection;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeleteAccountController {

    public UserDao userDao;
    @FXML
    private Button nextButton;
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;
    @FXML
    private void onClickLogOut() throws IOException {
        UserSession.clearSession();
        Stage stage = (Stage) nextButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), HelloApplication.WIDTH, HelloApplication.HEIGHT);
        stage.setScene(scene);
    }

    @FXML
    public void handleDeleteAccount() {


        String username = usernameField.getText().trim();
        String  password = passwordField.getText().trim();







        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(AlertType.ERROR, "Please fill in both fields.");
            alert.showAndWait();
            return;
        }

        // Confirm deletion
        Alert confirm = new Alert(AlertType.CONFIRMATION,
                "Are you sure you want to delete this account?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            Connection conn = SqliteConnection.getInstance();

            try {
                // Verify account exists
                String checkSql = "SELECT * FROM users WHERE username = ? AND password = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, username);
                checkStmt.setString(2, password); // in production, use hashed passwords
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Delete account
                    String deleteSql = "DELETE FROM users WHERE username = ?";
                    PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                    deleteStmt.setString(1, username);
                    deleteStmt.executeUpdate();

                    Alert success = new Alert(AlertType.INFORMATION, "Account deleted successfully.");
                    success.showAndWait();

                    usernameField.clear();
                    passwordField.clear();
                } else {
                    Alert error = new Alert(AlertType.ERROR, "Username or password incorrect.");
                    error.showAndWait();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Alert error = new Alert(AlertType.ERROR, "Database error: " + e.getMessage());
                error.showAndWait();
            }
        }
    }
}
