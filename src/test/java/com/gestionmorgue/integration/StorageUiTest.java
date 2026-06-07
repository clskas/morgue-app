package com.gestionmorgue.integration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StorageUiTest extends TestFxBase {

    private ComboBox<?> deceasedCombo;
    private ComboBox<?> locationCombo;
    private Button assignButton;
    private Button releaseButton;
    private ListView<?> unassignedList;
    private ComboBox<String> filterZoneCombo;
    private CheckBox filterOccupiedCheck;
    private TableView<?> locationTable;
    private TableView<?> assignmentTable;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/storage.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void findFields() {
        deceasedCombo = lookup("#deceasedCombo").query();
        locationCombo = lookup("#locationCombo").query();
        assignButton = lookup("#assignButton").query();
        releaseButton = lookup("#releaseButton").query();
        unassignedList = lookup("#unassignedList").query();
        filterZoneCombo = lookup("#filterZoneCombo").query();
        filterOccupiedCheck = lookup("#filterOccupiedCheck").query();
        locationTable = lookup("#locationTable").query();
        assignmentTable = lookup("#assignmentTable").query();
    }

    @Test
    void testStorageFieldsExist() {
        assertNotNull(deceasedCombo);
        assertNotNull(locationCombo);
        assertNotNull(assignButton);
        assertNotNull(releaseButton);
        assertNotNull(unassignedList);
        assertNotNull(filterZoneCombo);
        assertNotNull(filterOccupiedCheck);
        assertNotNull(locationTable);
        assertNotNull(assignmentTable);
    }

    @Test
    void testStorageFieldsVisible() {
        assertTrue(deceasedCombo.isVisible());
        assertTrue(locationCombo.isVisible());
        assertTrue(assignButton.isVisible());
        assertTrue(releaseButton.isVisible());
        assertTrue(unassignedList.isVisible());
        assertTrue(filterZoneCombo.isVisible());
        assertTrue(filterOccupiedCheck.isVisible());
        assertTrue(locationTable.isVisible());
        assertTrue(assignmentTable.isVisible());
    }

    @Test
    void testTableColumnsExist() {
        assertNotNull(lookup("#colCode").query());
        assertNotNull(lookup("#colLabel").query());
        assertNotNull(lookup("#colZone").query());
        assertNotNull(lookup("#colStatus").query());
        assertNotNull(lookup("#colAssignedDeceased").query());
        assertNotNull(lookup("#colAssignedLocation").query());
    }

    @Test
    void testFilterOccupiedDefaultUnselected() {
        assertFalse(filterOccupiedCheck.isSelected());
    }

    @Test
    void testLocationTableHasItems() {
        assertNotNull(locationTable.getItems());
    }
}
