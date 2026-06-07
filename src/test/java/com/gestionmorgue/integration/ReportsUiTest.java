package com.gestionmorgue.integration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReportsUiTest extends TestFxBase {

    private Label totalDeceased;
    private Label occupiedStorage;
    private Label totalStorage;
    private Label pendingInterventions;
    private Label pendingExits;
    private Label interventionsToday;
    private Label entriesThisMonth;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/reports.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void findFields() {
        totalDeceased = lookup("#totalDeceased").query();
        occupiedStorage = lookup("#occupiedStorage").query();
        totalStorage = lookup("#totalStorage").query();
        pendingInterventions = lookup("#pendingInterventions").query();
        pendingExits = lookup("#pendingExits").query();
        interventionsToday = lookup("#interventionsToday").query();
        entriesThisMonth = lookup("#entriesThisMonth").query();
    }

    @Test
    void testReportsFieldsExist() {
        assertNotNull(totalDeceased);
        assertNotNull(occupiedStorage);
        assertNotNull(totalStorage);
        assertNotNull(pendingInterventions);
        assertNotNull(pendingExits);
        assertNotNull(interventionsToday);
        assertNotNull(entriesThisMonth);
    }

    @Test
    void testReportsFieldsVisible() {
        assertTrue(totalDeceased.isVisible());
        assertTrue(occupiedStorage.isVisible());
        assertTrue(totalStorage.isVisible());
        assertTrue(pendingInterventions.isVisible());
        assertTrue(pendingExits.isVisible());
        assertTrue(interventionsToday.isVisible());
        assertTrue(entriesThisMonth.isVisible());
    }

    @Test
    void testStatsLoaded() {
        assertNotNull(totalDeceased.getText());
        assertNotNull(occupiedStorage.getText());
        assertNotNull(totalStorage.getText());
        assertNotNull(pendingInterventions.getText());
        assertNotNull(pendingExits.getText());
        assertNotNull(interventionsToday.getText());
        assertNotNull(entriesThisMonth.getText());
    }

    @Test
    void testDeceasedCountIsNumber() {
        Integer.parseInt(totalDeceased.getText());
    }

    @Test
    void testStorageFormat() {
        assertTrue(occupiedStorage.getText().contains("/"));
    }
}
