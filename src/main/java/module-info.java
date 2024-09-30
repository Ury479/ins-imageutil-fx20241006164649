module com.image {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    opens com.image to javafx.fxml;
    exports com.image;
}