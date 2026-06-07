package com.gestionmorgue.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gestionmorgue.App;
import com.gestionmorgue.config.Constants;
import com.gestionmorgue.service.AuthService;
import com.gestionmorgue.config.ConfigService;
import com.gestionmorgue.util.I18nUtil;
import com.gestionmorgue.util.NotificationUtil;
import com.gestionmorgue.util.SessionManager;
import com.gestionmorgue.util.ValidationUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label versionLabel;
    @FXML private Text titleLabel;
    @FXML private Text subtitleLabel;
    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;
    @FXML private Button langButton;

    private AuthService authService;

    @FXML
    public void initialize() {
        this.authService = new AuthService();
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleLogin();
        });
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) passwordField.requestFocus();
        });
        Platform.runLater(() -> usernameField.requestFocus());
        updateUIText();
    }

    private void updateUIText() {
        titleLabel.setText("Gestion Morgue");
        subtitleLabel.setText(I18nUtil.t("login.title"));
        usernameLabel.setText(I18nUtil.t("login.username"));
        passwordLabel.setText(I18nUtil.t("login.password"));
        loginButton.setText(I18nUtil.t("login.button"));
        versionLabel.setText(Constants.APP_NAME + " " + Constants.APP_VERSION);
        String lang = ConfigService.getInstance().getLanguage();
        langButton.setText("fr".equals(lang) ? "EN" : "FR");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        usernameField.setStyle(null);
        passwordField.setStyle(null);

        if (username.isEmpty()) usernameField.setStyle("-fx-border-color: #c62828;");
        if (password.isEmpty()) passwordField.setStyle("-fx-border-color: #c62828;");
        if (username.isEmpty() || password.isEmpty()) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("login.error.required"));
            return;
        }

        try {
            authService.authenticate(username, password);
            openDashboard();
        } catch (RuntimeException e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
        }
    }

    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);
            App.applyTheme(scene);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(Constants.APP_NAME + " - " + I18nUtil.t("dashboard.title"));
            stage.centerOnScreen();
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), I18nUtil.t("login.error.dashboard") + ": " + e.getMessage());
        }
    }

    @FXML
    private void handleToggleLanguage() {
        String current = ConfigService.getInstance().getLanguage();
        if ("fr".equals(current)) {
            I18nUtil.getInstance().setLanguage("en");
            ConfigService.getInstance().setLanguage("en");
        } else {
            I18nUtil.getInstance().setLanguage("fr");
            ConfigService.getInstance().setLanguage("fr");
        }
        updateUIText();
    }
}
