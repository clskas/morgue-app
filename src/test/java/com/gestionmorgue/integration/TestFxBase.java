package com.gestionmorgue.integration;

import com.gestionmorgue.util.DatabaseManager;
import com.gestionmorgue.util.DataInitializer;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

public abstract class TestFxBase extends ApplicationTest {

    private static boolean initialized;

    @BeforeAll
    static void initToolkit() {
        if (initialized) return;
        initialized = true;
        DataInitializer.initialize();
    }

    @AfterAll
    static void cleanupToolkit() {
        try {
            FxToolkit.cleanupStages();
        } catch (Exception ignored) {
        }
        DatabaseManager.shutdown();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Test");
        stage.show();
    }

    protected javafx.scene.Node findById(String id) {
        return lookup("#" + id).query();
    }

    protected void clickButton(String id) {
        clickOn("#" + id);
    }

    protected void writeText(String id, String text) {
        clickOn("#" + id);
        write(text);
    }

    protected void pressEnter() {
        type(KeyCode.ENTER);
    }

    protected void pressTab() {
        type(KeyCode.TAB);
    }

    protected void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    protected void runOnFxThread(Runnable action) {
        Platform.runLater(action);
        sleepMillis(200);
    }
}
