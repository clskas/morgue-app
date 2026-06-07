package com.gestionmorgue.util;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;

import java.util.function.Consumer;

public class ProgressUtil {

    public static <T> void runWithProgress(String title, Task<T> task, Consumer<T> onSuccess) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText("Traitement en cours...");
        alert.initStyle(StageStyle.UTILITY);

        ProgressBar bar = new ProgressBar();
        bar.setPrefWidth(300);
        bar.progressProperty().bind(task.progressProperty());
        alert.getDialogPane().setExpandableContent(new VBox(bar));
        alert.getButtonTypes().clear();

        task.setOnSucceeded(ev -> {
            alert.close();
            if (onSuccess != null) onSuccess.accept(task.getValue());
        });
        task.setOnFailed(ev -> {
            alert.close();
            NotificationUtil.showError("Erreur", task.getException().getMessage());
        });

        new Thread(task).start();
        alert.show();
    }

    public static <T> void runTask(String title, Task<T> task) {
        runWithProgress(title, task, null);
    }
}
