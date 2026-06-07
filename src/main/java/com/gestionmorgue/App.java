package com.gestionmorgue;

import com.gestionmorgue.config.ConfigService;
import com.gestionmorgue.config.Constants;
import com.gestionmorgue.config.DatabaseConfig;
import com.gestionmorgue.rest.RestApiServer;
import com.gestionmorgue.update.UpdateCheckResult;
import com.gestionmorgue.util.DataInitializer;
import com.gestionmorgue.update.UpdateChecker;
import com.gestionmorgue.update.VersionInfo;
import com.gestionmorgue.util.GlobalErrorHandler;
import com.gestionmorgue.util.I18nUtil;
import com.gestionmorgue.util.NotificationUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public class App extends Application {

    private static String currentTheme = "clair";

    @Override
    public void start(Stage primaryStage) throws Exception {
        GlobalErrorHandler.install();
        ConfigService config = ConfigService.getInstance();
        DataInitializer.initialize();

        String lang = config.getLanguage();
        I18nUtil.getInstance().setLanguage(lang);

        currentTheme = config.getTheme();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 400, 350);
        applyTheme(scene);
        primaryStage.setScene(scene);
        primaryStage.setTitle(Constants.APP_NAME + " - " + I18nUtil.t("login.title"));
        primaryStage.setResizable(false);
        primaryStage.show();

        startRestApi();
    }

    public static void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(
                App.class.getResource("/styles/styles.css").toExternalForm());
        if ("sombre".equals(currentTheme)) {
            scene.getStylesheets().add(
                    App.class.getResource("/styles/dark-theme.css").toExternalForm());
        }
        String custom = ConfigService.getInstance().getCustomTheme();
        if (!custom.isEmpty()) {
            scene.setUserAgentStylesheet(null);
            scene.getRoot().setStyle(custom);
        }
    }

    public static void setTheme(String theme) {
        currentTheme = theme;
        ConfigService.getInstance().setTheme(theme);
    }

    public static String getCurrentTheme() {
        return currentTheme;
    }

    public static void refreshScene(Scene scene) {
        applyTheme(scene);
    }

    private void checkForUpdates(Stage stage) {
        new Thread(() -> {
            try {
                UpdateChecker checker = new UpdateChecker();
                UpdateCheckResult result = checker.checkForUpdate();

                if (result.isUpdateAvailable()) {
                    Platform.runLater(() -> showUpdateNotification(stage, result));
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void showUpdateNotification(Stage stage, UpdateCheckResult result) {
        VersionInfo versionInfo = result.getVersionInfo();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18nUtil.t("action.update"));
        alert.setHeaderText("Nouvelle version disponible: " + versionInfo.getVersion());
        alert.setContentText(versionInfo.getChangelog() != null ?
                versionInfo.getChangelog() : "Cliquez pour télécharger.");

        ButtonType downloadBtn = new ButtonType(I18nUtil.t("confirm.yes"));
        ButtonType laterBtn = new ButtonType(I18nUtil.t("confirm.no"));
        ButtonType ignoreBtn = new ButtonType("Ignorer", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(downloadBtn, laterBtn, ignoreBtn);

        Optional<ButtonType> response = alert.showAndWait();
        if (response.isPresent() && response.get() == downloadBtn) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/update.fxml"));
                Parent updateRoot = loader.load();
                Stage updateStage = new Stage();
                updateStage.setTitle("Mise à jour - " + Constants.APP_NAME);
                Scene updateScene = new Scene(updateRoot, 500, 400);
                applyTheme(updateScene);
                updateStage.setScene(updateScene);
                updateStage.show();
            } catch (Exception e) {
                System.err.println("Erreur ouverture mise à jour: " + e.getMessage());
            }
        } else if (response.isPresent() && response.get() == ignoreBtn) {
            ConfigService.getInstance().setIgnoredVersion(versionInfo.getVersion());
        }
    }

    private static RestApiServer restApi;

    private void startRestApi() {
        try {
            String dbProfile = System.getProperty("db.profile", "h2");
            DatabaseConfig.setProfile(dbProfile);
            String portStr = System.getProperty("api.port", "8080");
            int port = Integer.parseInt(portStr);
            restApi = new RestApiServer(port);
            restApi.start();
        } catch (Exception e) {
            System.err.println("[REST API] Impossible de démarrer le serveur: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args != null) {
            for (String arg : args) {
                if (arg.startsWith("--db=")) {
                    DatabaseConfig.setProfile(arg.substring(5));
                }
                if (arg.startsWith("--api-port=")) {
                    System.setProperty("api.port", arg.substring(11));
                }
            }
            boolean isBatch = false;
            for (String arg : args) {
                if (arg.equals("--export-csv") || arg.equals("--export-json")
                        || arg.equals("--export-xlsx") || arg.equals("--export-pdf")
                        || arg.equals("--backup") || arg.equals("--server")
                        || arg.equals("--help")) {
                    isBatch = true;
                    break;
                }
            }
            if (isBatch) {
                int code = new com.gestionmorgue.util.BatchMode().run(args);
                System.exit(code);
            }
        }
        launch(args);
    }
}
