package com.gestionmorgue.integration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InterventionUiTest extends TestFxBase {

    private ComboBox<?> deceasedCombo;
    private ComboBox<String> typeCombo;
    private Button planButton;
    private TableView<?> interventionTable;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/intervention.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void findFields() {
        deceasedCombo = lookup("#deceasedCombo").query();
        typeCombo = lookup("#typeCombo").query();
        planButton = lookup("#planButton").query();
        interventionTable = lookup("#interventionTable").query();
    }

    @Test
    void testInterventionFieldsExist() {
        assertNotNull(deceasedCombo);
        assertNotNull(typeCombo);
        assertNotNull(planButton);
        assertNotNull(interventionTable);
    }

    @Test
    void testInterventionFieldsVisible() {
        assertTrue(deceasedCombo.isVisible());
        assertTrue(typeCombo.isVisible());
        assertTrue(planButton.isVisible());
        assertTrue(interventionTable.isVisible());
    }

    @Test
    void testTableColumnsExist() {
        assertNotNull(lookup("#colDeceased").query());
        assertNotNull(lookup("#colType").query());
        assertNotNull(lookup("#colPerformer").query());
        assertNotNull(lookup("#colScheduled").query());
        assertNotNull(lookup("#colStatus").query());
    }

    @Test
    void testTypeComboHasItems() {
        assertFalse(typeCombo.getItems().isEmpty());
    }

    @Test
    void testPlanWithoutSelection() {
        clickOn("#planButton");
    }
}
