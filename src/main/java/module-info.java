module group13.demo1 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens group13.demo1 to javafx.fxml;
    opens group13.demo1.controller to javafx.fxml;
    opens group13.demo1.model to javafx.fxml;

    exports group13.demo1;
    exports group13.demo1.controller;
    exports group13.demo1.model;
}