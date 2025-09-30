package group13.demo1.controller;

import group13.demo1.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert;

import java.util.List;

public class AccomplishmentController
{
    @FXML
    private ListView<String> accomplishmentList;

    @FXML
    private TextField accomplishmentLabel;

    @FXML
    private Label statusLabel;

    private final IAccomplishmentDAO accomplishmentDAO = new SqliteAccomplishmentDAO();

    @FXML
    public void initialize()
    {
        loadAccomplishments();
    }

    private void loadAccomplishments()
    {
        String username = UserSession.getInstance().getUsername();
        accomplishmentList.getItems().clear();
        List<Accomplishment> accomplishments = accomplishmentDAO.getAccomplishmentsByUsername(username);

        for (Accomplishment accomplishment : accomplishments)
        {
            String display = accomplishment.getLabel() + " " + (accomplishment.isCompleted() ? "✓":"✗");
            accomplishmentList.getItems().add(display);
        }
    }

    public void showCongratulations(String message)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Congratulations!");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add("stylesheet.css");
        alert.showAndWait();
    }

    @FXML
    public void addAccomplishment()
    {
        String label = accomplishmentLabel.getText();
        String username = UserSession.getInstance().getUsername();

        if (label.isEmpty())
        {
            statusLabel.setText("Please enter a description");
            return;
        }

        Accomplishment accomplishment = new Accomplishment(0, username, label, false);
        accomplishmentDAO.addAccomplishment(accomplishment);

        loadAccomplishments();
        accomplishmentLabel.clear();

        List<Accomplishment> userAccomplishments = accomplishmentDAO.getAccomplishmentsByUsername(username);
        if (userAccomplishments.size() >= 5)
        {
            showCongratulations(" You've logged " + userAccomplishments.size() + " accomplishments!");
        }
        else
        {
            statusLabel.setText("Successfully added accomplishment");
        }
    }

    @FXML
    public void updateAccomplishment()
    {
        int selectedIndex = accomplishmentList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0)
        {
            statusLabel.setText("Please select a accomplishment to update");
            return;
        }

        String label = accomplishmentLabel.getText();
        if (label.isEmpty())
        {
            statusLabel.setText("Please enter a description");
            return;
        }
        Accomplishment selected =  accomplishmentDAO.getAccomplishmentsByUsername(UserSession.getInstance().getUsername()).get(selectedIndex);
        selected.setLabel(label);
        accomplishmentDAO.updateAccomplishment(selected);
        statusLabel.setText("Successfully updated accomplishment");
        loadAccomplishments();
    }

    @FXML
    public void deleteAccomplishment()
    {
        int selectedIndex = accomplishmentList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0)
        {
            statusLabel.setText("Please select an accomplishment to delete");
            return;
        }

        Accomplishment selected =  accomplishmentDAO.getAccomplishmentsByUsername(UserSession.getInstance().getUsername()).get(selectedIndex);
        accomplishmentDAO.deleteAccomplishment(selected);
        statusLabel.setText("Successfully deleted accomplishment");
        loadAccomplishments();
    }
}