package com.gestionmorgue.integration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ThemeUiTest extends TestFxBase {

    private ColorPicker primaryColor;
    private ColorPicker bgColor;
    private ColorPicker surfaceColor;
    private ColorPicker textColor;
    private ColorPicker accentColor;
    private VBox previewBox;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/theme.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void findFields() {
        primaryColor = lookup("#primaryColor").query();
        bgColor = lookup("#bgColor").query();
        surfaceColor = lookup("#surfaceColor").query();
        textColor = lookup("#textColor").query();
        accentColor = lookup("#accentColor").query();
        previewBox = lookup("#previewBox").query();
    }

    @Test
    void testThemeFieldsExist() {
        assertNotNull(primaryColor);
        assertNotNull(bgColor);
        assertNotNull(surfaceColor);
        assertNotNull(textColor);
        assertNotNull(accentColor);
        assertNotNull(previewBox);
    }

    @Test
    void testThemeFieldsVisible() {
        assertTrue(primaryColor.isVisible());
        assertTrue(bgColor.isVisible());
        assertTrue(surfaceColor.isVisible());
        assertTrue(textColor.isVisible());
        assertTrue(accentColor.isVisible());
        assertTrue(previewBox.isVisible());
    }

    @Test
    void testDefaultColorsSet() {
        assertNotNull(primaryColor.getValue());
        assertNotNull(bgColor.getValue());
        assertNotNull(surfaceColor.getValue());
        assertNotNull(textColor.getValue());
        assertNotNull(accentColor.getValue());
    }

    @Test
    void testPreviewBoxHasStyle() {
        assertNotNull(previewBox.getStyle());
    }
}
