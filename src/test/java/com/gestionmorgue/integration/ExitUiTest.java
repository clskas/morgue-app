package com.gestionmorgue.integration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExitUiTest extends TestFxBase {

    private ComboBox<?> deceasedCombo;
    private TextField transportField;
    private TextField authorizedPersonField;
    private TextArea notesArea;
    private Button approveButton;
    private Button confirmExitButton;
    private TableView<?> exitTable;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/exits.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void findFields() {
        deceasedCombo = lookup("#deceasedCombo").query();
        transportField = lookup("#transportField").query();
        authorizedPersonField = lookup("#authorizedPersonField").query();
        notesArea = lookup("#notesArea").query();
        approveButton = lookup("#approveButton").query();
        confirmExitButton = lookup("#confirmExitButton").query();
        exitTable = lookup("#exitTable").query();
    }

    @Test
    void testExitsFieldsExist() {
        assertNotNull(deceasedCombo);
        assertNotNull(transportField);
        assertNotNull(authorizedPersonField);
        assertNotNull(notesArea);
        assertNotNull(approveButton);
        assertNotNull(confirmExitButton);
        assertNotNull(exitTable);
    }

    @Test
    void testExitsFieldsVisible() {
        assertTrue(deceasedCombo.isVisible());
        assertTrue(transportField.isVisible());
        assertTrue(authorizedPersonField.isVisible());
        assertTrue(notesArea.isVisible());
        assertTrue(approveButton.isVisible());
        assertTrue(confirmExitButton.isVisible());
        assertTrue(exitTable.isVisible());
    }

    @Test
    void testTableColumnsExist() {
        assertNotNull(lookup("#colDeceasedName").query());
        assertNotNull(lookup("#colTransport").query());
        assertNotNull(lookup("#colStatus").query());
        assertNotNull(lookup("#colDate").query());
    }

    @Test
    void testConfirmExitButtonDisabledByDefault() {
        assertTrue(confirmExitButton.isDisable());
    }

    @Test
    void testTransportFieldPrompt() {
        assertEquals("Transporteur", transportField.getPromptText());
    }
}
