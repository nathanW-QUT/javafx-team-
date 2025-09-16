package group13.demo1.controller;

import group13.demo1.model.UserDao;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class DeleteAccountController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final UserDao userDao = new UserDao();

    @FXML
    private void handleDeleteAccount() {
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
            } else {
                new Alert(Alert.AlertType.ERROR, "Invalid credentials or account not found.").showAndWait();
            }
        }
    }


}
