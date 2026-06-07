package com.gestionmorgue.integration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeceasedUiTest extends TestFxBase {

    private TextField searchField;
    private TextField lastNameField;
    private TextField firstNameField;
    private ComboBox<String> genderCombo;
    private Button saveButton;
    private TableView<?> deceasedTable;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/deceased.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void findFields() {
        searchField = lookup("#searchField").query();
        lastNameField = lookup("#lastNameField").query();
        firstNameField = lookup("#firstNameField").query();
        genderCombo = lookup("#genderCombo").query();
        saveButton = lookup("#saveButton").query();
        deceasedTable = lookup("#deceasedTable").query();
    }

    @Test
    void testDeceasedFieldsExist() {
        assertNotNull(searchField);
        assertNotNull(lastNameField);
        assertNotNull(firstNameField);
        assertNotNull(genderCombo);
        assertNotNull(saveButton);
        assertNotNull(deceasedTable);
    }

    @Test
    void testDeceasedFieldsVisible() {
        assertTrue(searchField.isVisible());
        assertTrue(lastNameField.isVisible());
        assertTrue(firstNameField.isVisible());
        assertTrue(genderCombo.isVisible());
        assertTrue(saveButton.isVisible());
        assertTrue(deceasedTable.isVisible());
    }

    @Test
    void testTableColumnsExist() {
        assertNotNull(lookup("#colDossier").query());
        assertNotNull(lookup("#colLastName").query());
        assertNotNull(lookup("#colFirstName").query());
        assertNotNull(lookup("#colDeathDate").query());
        assertNotNull(lookup("#colGender").query());
    }

    @Test
    void testSaveWithEmptyFields() {
        clickOn("#saveButton");
    }

    @Test
    void testGenderComboHasItems() {
        assertFalse(genderCombo.getItems().isEmpty());
        assertTrue(genderCombo.getItems().contains("MASCULIN"));
    }
}
