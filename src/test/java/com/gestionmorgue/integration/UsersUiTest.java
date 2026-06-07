package com.gestionmorgue.integration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UsersUiTest extends TestFxBase {

    private TextField usernameField;
    private PasswordField passwordField;
    private TextField fullNameField;
    private TextField emailField;
    private ComboBox<String> roleCombo;
    private CheckBox activeCheck;
    private Button saveButton;
    private Button updateButton;
    private Button resetPasswordButton;
    private Button deleteButton;
    private TableView<?> userTable;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/users.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void findFields() {
        usernameField = lookup("#usernameField").query();
        passwordField = lookup("#passwordField").query();
        fullNameField = lookup("#fullNameField").query();
        emailField = lookup("#emailField").query();
        roleCombo = lookup("#roleCombo").query();
        activeCheck = lookup("#activeCheck").query();
        saveButton = lookup("#saveButton").query();
        updateButton = lookup("#updateButton").query();
        resetPasswordButton = lookup("#resetPasswordButton").query();
        deleteButton = lookup("#deleteButton").query();
        userTable = lookup("#userTable").query();
    }

    @Test
    void testUsersFieldsExist() {
        assertNotNull(usernameField);
        assertNotNull(passwordField);
        assertNotNull(fullNameField);
        assertNotNull(emailField);
        assertNotNull(roleCombo);
        assertNotNull(activeCheck);
        assertNotNull(saveButton);
        assertNotNull(updateButton);
        assertNotNull(resetPasswordButton);
        assertNotNull(deleteButton);
        assertNotNull(userTable);
    }

    @Test
    void testUsersFieldsVisible() {
        assertTrue(usernameField.isVisible());
        assertTrue(passwordField.isVisible());
        assertTrue(fullNameField.isVisible());
        assertTrue(emailField.isVisible());
        assertTrue(roleCombo.isVisible());
        assertTrue(activeCheck.isVisible());
        assertTrue(saveButton.isVisible());
        assertTrue(updateButton.isVisible());
        assertTrue(resetPasswordButton.isVisible());
        assertTrue(deleteButton.isVisible());
        assertTrue(userTable.isVisible());
    }

    @Test
    void testTableColumnsExist() {
        assertNotNull(lookup("#colUsername").query());
        assertNotNull(lookup("#colFullName").query());
        assertNotNull(lookup("#colRole").query());
        assertNotNull(lookup("#colEmail").query());
        assertNotNull(lookup("#colActive").query());
    }

    @Test
    void testRoleComboHasItems() {
        assertFalse(roleCombo.getItems().isEmpty());
        assertTrue(roleCombo.getItems().contains("ADMIN"));
    }

    @Test
    void testActiveCheckDefaultSelected() {
        assertTrue(activeCheck.isSelected());
    }
}
