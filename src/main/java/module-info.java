module group13.demo1 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens group13.demo1 to javafx.fxml;
    exports group13.demo1;
}