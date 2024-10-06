package com.image;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class AlertUtils {

    public static void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Image Processing");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
