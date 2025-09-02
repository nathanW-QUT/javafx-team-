package group13.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        // Define the root node and set the alignment and spacing
    // properties. Also, set the fillWidth property to false
    // so the children are not resized to fill the width of the
    // VBox.
        VBox root = new VBox();
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setSpacing(15.0);
        root.setFillWidth(false);
    // Create a TextField, a Label, and an HBox with appropriate
    // text
        TextField textField = new TextField();
        textField.setText("TextField");
        Label label = new Label("Label");
    // The HBox is used to hold the buttons
        HBox hbox = new HBox();
        hbox.setAlignment(javafx.geometry.Pos.CENTER);
        hbox.setSpacing(15.0);
        Button button1 = new Button("log in");
        Button button2 = new Button("sign up");
    // Add the buttons to the HBox
        hbox.getChildren().addAll(button1, button2);
    // Add the children to the root vbox
        root.getChildren().addAll(textField, label, hbox);
    // Define the scene, add to the stage (window) and show the stage
        Scene scene = new Scene(root, 320, 180);
        stage.setScene(scene);
        stage.setTitle("JavaFX Example Scene");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
