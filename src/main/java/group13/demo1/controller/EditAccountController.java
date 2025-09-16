package group13.demo1.controller;

import group13.demo1.model.UserDao;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class EditAccountController {

    @FXML private TextField usernameField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;

    private final UserDao userDao = new UserDao();

    @FXML
    private void handleUpdatePassword() {
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
        } else {
            new Alert(Alert.AlertType.ERROR, "Invalid username or password.").showAndWait();
        }
    }




}
