package com.gestionmorgue.util;

import javafx.animation.PauseTransition;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class ToastUtil {

    public static void show(String message) {
        show(message, 8);
    }

    public static void show(String message, int durationSeconds) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setAlwaysOnTop(true);

        Label label = new Label(message);
        label.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-padding: 12 24; -fx-font-size: 14px; -fx-background-radius: 6; -fx-font-family: 'Segoe UI';");

        Scene scene = new Scene(new StackPane(label));
        scene.setFill(null);
        stage.setScene(scene);
        stage.sizeToScene();

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMaxX() - stage.getWidth() - 20);
        stage.setY(bounds.getMaxY() - stage.getHeight() - 50);
        stage.show();

        PauseTransition delay = new PauseTransition(Duration.seconds(durationSeconds));
        delay.setOnFinished(e -> stage.close());
        delay.play();
    }
}
