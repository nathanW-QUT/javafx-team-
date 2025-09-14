package group13.demo1.controller;

import group13.demo1.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

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
    public void initialise()
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
            String display = accomplishment.getLabel() + (accomplishment.isCompleted() ? "✓":"✗");
            accomplishmentList.getItems().add(display);
        }
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
        statusLabel.setText("Successfully added accomplishment");
    }
}
