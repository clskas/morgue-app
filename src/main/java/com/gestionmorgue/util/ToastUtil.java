package com.gestionmorgue.util;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ToastUtil {

    public enum Type { INFO, SUCCESS, WARNING, ERROR }

    public static void show(Stage owner, String message, Type type) {
        show(owner, message, type, 3000);
    }

    public static void show(Stage owner, String message, Type type, int durationMs) {
        Platform.runLater(() -> {
            String color = switch (type) {
                case INFO -> "#1a237e";
                case SUCCESS -> "#2e7d32";
                case WARNING -> "#e65100";
                case ERROR -> "#c62828";
            };
            Label label = new Label(message);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 16;");
            label.setWrapText(true);
            StackPane pane = new StackPane(label);
            pane.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6;"
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);");
            pane.setMaxWidth(400);
            VBox container = new VBox(pane);
            container.setAlignment(Pos.BOTTOM_RIGHT);
            container.setStyle("-fx-padding: 20;");
            Popup popup = new Popup();
            popup.getContent().add(container);
            popup.setAutoFix(true);
            if (owner != null) {
                popup.show(owner);
                popup.setX(owner.getX() + owner.getWidth() - 420);
                popup.setY(owner.getY() + owner.getHeight() - 80);
            }
            PauseTransition delay = new PauseTransition(Duration.millis(durationMs));
            delay.setOnFinished(e -> popup.hide());
            delay.play();
        });
    }
}
