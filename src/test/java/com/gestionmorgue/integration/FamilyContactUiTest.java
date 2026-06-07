package com.gestionmorgue.integration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FamilyContactUiTest extends TestFxBase {

    private ComboBox<?> deceasedCombo;
    private TextField contactNameField;
    private TextField relationshipField;
    private TextField phoneField;
    private TextField emailField;
    private TextArea addressArea;
    private TextArea notesArea;
    private TableView<?> contactTable;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/family.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void findFields() {
        deceasedCombo = lookup("#deceasedCombo").query();
        contactNameField = lookup("#contactNameField").query();
        relationshipField = lookup("#relationshipField").query();
        phoneField = lookup("#phoneField").query();
        emailField = lookup("#emailField").query();
        addressArea = lookup("#addressArea").query();
        notesArea = lookup("#notesArea").query();
        contactTable = lookup("#contactTable").query();
    }

    @Test
    void testFamilyFieldsExist() {
        assertNotNull(deceasedCombo);
        assertNotNull(contactNameField);
        assertNotNull(relationshipField);
        assertNotNull(phoneField);
        assertNotNull(emailField);
        assertNotNull(addressArea);
        assertNotNull(notesArea);
        assertNotNull(contactTable);
    }

    @Test
    void testFamilyFieldsVisible() {
        assertTrue(deceasedCombo.isVisible());
        assertTrue(contactNameField.isVisible());
        assertTrue(relationshipField.isVisible());
        assertTrue(phoneField.isVisible());
        assertTrue(emailField.isVisible());
        assertTrue(addressArea.isVisible());
        assertTrue(notesArea.isVisible());
        assertTrue(contactTable.isVisible());
    }

    @Test
    void testTableColumnsExist() {
        assertNotNull(lookup("#colName").query());
        assertNotNull(lookup("#colRelationship").query());
        assertNotNull(lookup("#colPhone").query());
    }

    @Test
    void testContactNamePrompt() {
        assertTrue(contactNameField.getPromptText().toLowerCase().contains("nom"));
    }
}
