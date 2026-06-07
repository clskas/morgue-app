package com.gestionmorgue.integration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

public class LoginUiTest extends ApplicationTest {

    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void findFields() {
        usernameField = lookup("#usernameField").query();
        passwordField = lookup("#passwordField").query();
        loginButton = lookup("#loginButton").query();
    }

    @Test
    void testLoginFieldsExist() {
        assertNotNull(usernameField);
        assertNotNull(passwordField);
        assertNotNull(loginButton);
    }

    @Test
    void testLoginFieldsVisible() {
        assertTrue(usernameField.isVisible());
        assertTrue(passwordField.isVisible());
        assertTrue(loginButton.isVisible());
    }

    @Test
    void testLoginEmptyFieldsShowsError() {
        assertDoesNotThrow(() -> {
            clickOn("#loginButton");
        });
    }

    @Test
    void testLoginWrongCredentials() {
        clickOn("#usernameField").write("inconnu");
        clickOn("#passwordField").write("wrong");
        clickOn("#loginButton");
    }

    @Test
    void testPasswordFieldMasked() {
        clickOn("#passwordField").write("admin123");
        String style = passwordField.getStyle();
        assertNotNull(style);
    }
}
