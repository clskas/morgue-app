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

public class DashboardUiTest extends TestFxBase {

    private Label welcomeLabel;
    private Label userRoleLabel;
    private Label totalDeceasedLabel;
    private Label occupiedStorageLabel;
    private Label totalStorageLabel;
    private Label pendingInterventionsLabel;
    private Label pendingExitsLabel;
    private Label interventionsTodayLabel;
    private Label entriesThisMonthLabel;
    private Label storagePercentLabel;
    private ProgressBar storageBar;
    private TextField searchField;
    private VBox recentActivity;
    private Button themeButton;
    private Button langButton;
    private Button logoutButton;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void findFields() {
        welcomeLabel = lookup("#welcomeLabel").query();
        userRoleLabel = lookup("#userRoleLabel").query();
        totalDeceasedLabel = lookup("#totalDeceasedLabel").query();
        occupiedStorageLabel = lookup("#occupiedStorageLabel").query();
        totalStorageLabel = lookup("#totalStorageLabel").query();
        pendingInterventionsLabel = lookup("#pendingInterventionsLabel").query();
        pendingExitsLabel = lookup("#pendingExitsLabel").query();
        interventionsTodayLabel = lookup("#interventionsTodayLabel").query();
        entriesThisMonthLabel = lookup("#entriesThisMonthLabel").query();
        storagePercentLabel = lookup("#storagePercentLabel").query();
        storageBar = lookup("#storageBar").query();
        searchField = lookup("#searchField").query();
        recentActivity = lookup("#recentActivity").query();
        themeButton = lookup("#themeButton").query();
        langButton = lookup("#langButton").query();
        logoutButton = lookup("#logoutButton").query();
    }

    @Test
    void testDashboardFieldsExist() {
        assertNotNull(welcomeLabel);
        assertNotNull(userRoleLabel);
        assertNotNull(totalDeceasedLabel);
        assertNotNull(occupiedStorageLabel);
        assertNotNull(totalStorageLabel);
        assertNotNull(pendingInterventionsLabel);
        assertNotNull(pendingExitsLabel);
        assertNotNull(interventionsTodayLabel);
        assertNotNull(entriesThisMonthLabel);
        assertNotNull(storagePercentLabel);
        assertNotNull(storageBar);
        assertNotNull(searchField);
        assertNotNull(recentActivity);
        assertNotNull(themeButton);
        assertNotNull(langButton);
        assertNotNull(logoutButton);
    }

    @Test
    void testDashboardFieldsVisible() {
        assertTrue(welcomeLabel.isVisible());
        assertTrue(userRoleLabel.isVisible());
        assertTrue(totalDeceasedLabel.isVisible());
        assertTrue(occupiedStorageLabel.isVisible());
        assertTrue(totalStorageLabel.isVisible());
        assertTrue(pendingInterventionsLabel.isVisible());
        assertTrue(pendingExitsLabel.isVisible());
        assertTrue(interventionsTodayLabel.isVisible());
        assertTrue(entriesThisMonthLabel.isVisible());
        assertTrue(storagePercentLabel.isVisible());
        assertTrue(storageBar.isVisible());
        assertTrue(searchField.isVisible());
        assertTrue(recentActivity.isVisible());
        assertTrue(themeButton.isVisible());
        assertTrue(langButton.isVisible());
        assertTrue(logoutButton.isVisible());
    }

    @Test
    void testNavButtonsExist() {
        assertNotNull(lookup("#navDeceased").query());
        assertNotNull(lookup("#navStorage").query());
        assertNotNull(lookup("#navInterventions").query());
        assertNotNull(lookup("#navExits").query());
        assertNotNull(lookup("#navReports").query());
        assertNotNull(lookup("#navAudit").query());
        assertNotNull(lookup("#navUsers").query());
        assertNotNull(lookup("#navFamily").query());
        assertNotNull(lookup("#navTheme").query());
    }

    @Test
    void testStatsLabelsHaveText() {
        assertNotNull(totalDeceasedLabel.getText());
        assertNotNull(occupiedStorageLabel.getText());
        assertNotNull(totalStorageLabel.getText());
        assertNotNull(pendingInterventionsLabel.getText());
        assertNotNull(pendingExitsLabel.getText());
        assertNotNull(interventionsTodayLabel.getText());
        assertNotNull(entriesThisMonthLabel.getText());
        assertNotNull(storagePercentLabel.getText());
    }

    @Test
    void testSearchFieldPrompt() {
        assertTrue(searchField.getPromptText().toLowerCase().contains("recherche"));
    }
}
