package com.gestionmorgue.controller;

import com.gestionmorgue.App;
import com.gestionmorgue.config.ConfigService;
import com.gestionmorgue.config.Constants;
import com.gestionmorgue.model.User;
import com.gestionmorgue.service.*;
import com.gestionmorgue.update.UpdateCheckResult;
import com.gestionmorgue.update.UpdateChecker;
import com.gestionmorgue.util.I18nUtil;
import com.gestionmorgue.util.NotificationUtil;
import com.gestionmorgue.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.animation.FadeTransition;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DashboardController {
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @FXML private Label welcomeLabel;
    @FXML private Label totalDeceasedLabel;
    @FXML private Label occupiedStorageLabel;
    @FXML private Label totalStorageLabel;
    @FXML private Label pendingInterventionsLabel;
    @FXML private Label pendingExitsLabel;
    @FXML private Label interventionsTodayLabel;
    @FXML private Label lblInterventionsPlanned;
    @FXML private Label lblInterventionsCompleted;
    @FXML private Label lblExitsPending;
    @FXML private Label entriesThisMonthLabel;
    @FXML private Label storagePercentLabel;
    @FXML private Label userRoleLabel;
    @FXML private ProgressBar storageBar;
    @FXML private VBox mainContent;
    @FXML private VBox recentActivity;
    @FXML private VBox chartContainer;
    @FXML private VBox pieChartContainer;
    @FXML private ProgressIndicator contentProgress;
    @FXML private TextField searchField;
    @FXML private Button themeButton;
    @FXML private Button themeCreatorButton;
    @FXML private Button navDeceased;
    @FXML private Button navStorage;
    @FXML private Button navInterventions;
    @FXML private Button navExits;
    @FXML private Button navReports;
    @FXML private Button navAudit;
    @FXML private Button navUsers;
    @FXML private Button navFamily;
    @FXML private Button navTheme;
    @FXML private Button navManual;
    @FXML private Button navSettings;

    private ReportService reportService;
    private AuditService auditService;
    private InterventionService interventionService;
    private ExitService exitService;
    private ScheduledExecutorService scheduler;

    @FXML
    public void initialize() {
        reportService = new ReportService();
        auditService = new AuditService();
        interventionService = new InterventionService();
        exitService = new ExitService();
        loadDashboardData();
        buildChart();
        buildPieChart();
        Platform.runLater(() -> {
            FadeTransition ft = new FadeTransition(Duration.millis(400), mainContent);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        });
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleQuickSearch();
        });
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> Platform.runLater(this::loadDashboardData), 30, 30, TimeUnit.SECONDS);
        applyRbac();
        checkForUpdates();
    }

    private void checkForUpdates() {
        if (!ConfigService.getInstance().isUpdateCheckEnabled()) return;
        new Thread(() -> {
            try {
                UpdateChecker checker = new UpdateChecker();
                UpdateCheckResult result = checker.checkForUpdate();
                if (result.isUpdateAvailable()) {
                    Platform.runLater(() -> {
                        boolean download = NotificationUtil.showConfirm(I18nUtil.t("dashboard.update.available"),
                                I18nUtil.t("update.available.message", result.getVersionInfo().getVersion(),
                                    result.getVersionInfo().getChangelog() != null ? result.getVersionInfo().getChangelog() : ""));
                        if (download) {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/update.fxml"));
                            try {
                                Parent root = loader.load();
                                Stage stage = new Stage();
                                stage.setTitle(I18nUtil.t("action.update") + " - " + Constants.APP_NAME);
                Scene updateScene = new Scene(root, 540, 480);
                App.applyTheme(updateScene);
                App.setStageIcon(stage);
                stage.setScene(updateScene);
                stage.show();
                            } catch (Exception e) {
                                NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
                            }
                        }
                    });
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void applyRbac() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;
        String role = user.getRole();
        navDeceased.setVisible(true);
        navDeceased.setManaged(true);
        navStorage.setVisible("ADMIN".equals(role) || "MEDECIN".equals(role) || "THANATOPRACTEUR".equals(role));
        navStorage.setManaged(navStorage.isVisible());
        navInterventions.setVisible("ADMIN".equals(role) || "MEDECIN".equals(role) || "THANATOPRACTEUR".equals(role));
        navInterventions.setManaged(navInterventions.isVisible());
        navExits.setVisible("ADMIN".equals(role) || "MEDECIN".equals(role) || "THANATOPRACTEUR".equals(role));
        navExits.setManaged(navExits.isVisible());
        navReports.setVisible("ADMIN".equals(role) || "MEDECIN".equals(role) || "GREFFIER".equals(role));
        navReports.setManaged(navReports.isVisible());
        navAudit.setVisible("ADMIN".equals(role) || "GREFFIER".equals(role));
        navAudit.setManaged(navAudit.isVisible());
        navUsers.setVisible("ADMIN".equals(role));
        navUsers.setManaged(navUsers.isVisible());
        navFamily.setVisible("ADMIN".equals(role) || "MEDECIN".equals(role) || "GREFFIER".equals(role));
        navFamily.setManaged(navFamily.isVisible());
        navTheme.setVisible("ADMIN".equals(role));
        navTheme.setManaged(navTheme.isVisible());
        navSettings.setVisible("ADMIN".equals(role));
        navSettings.setManaged(navSettings.isVisible());
        navManual.setVisible(true);
        navManual.setManaged(true);
        themeCreatorButton.setVisible("ADMIN".equals(role));
        themeCreatorButton.setManaged(themeCreatorButton.isVisible());
    }

    @FXML
    private void handleQuickSearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/deceased.fxml"));
            Parent view = loader.load();
            com.gestionmorgue.controller.DeceasedController ctrl = loader.getController();
            ctrl.searchExternal(q);
            mainContent.getChildren().setAll(view);
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), I18nUtil.t("search.placeholder") + ": " + e.getMessage());
        }
    }

    @FXML
    private void handleThemeCreator() {
        loadView("/views/theme.fxml");
    }

    @FXML
    private void handleManual() {
        loadView("/views/manual.fxml");
    }

    @FXML
    private void handleSettings() {
        loadView("/views/settings.fxml");
    }

    @FXML
    private void handleFamilyContacts() {
        loadView("/views/family.fxml");
    }

    @FXML
    private void handleRefresh() {
        loadDashboardData();
        NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("dashboard.refresh"));
    }

    private void buildChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(I18nUtil.t("dashboard.stats.entriesThisMonth"));
        chart.setAnimated(true);
        chart.setLegendVisible(false);
        chart.setPrefHeight(200);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        java.time.YearMonth now = java.time.YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            java.time.YearMonth ym = now.minusMonths(i);
            String label = ym.getMonth().toString().substring(0, 3) + " " + (ym.getYear() % 100);
            long count = countEntriesForMonth(ym);
            series.getData().add(new XYChart.Data<>(label, count));
        }
        chart.getData().add(series);
        chartContainer.getChildren().add(chart);
    }

    private void buildPieChart() {
        try (var session = com.gestionmorgue.util.DatabaseManager.getSessionFactory().openSession()) {
            var cb = session.getCriteriaBuilder();
            var totalQ = cb.createQuery(Long.class);
            totalQ.select(cb.count(totalQ.from(com.gestionmorgue.model.StorageLocation.class)));
            long total = session.createQuery(totalQ).getSingleResult();
            var occQ = cb.createQuery(Long.class);
            var occR = occQ.from(com.gestionmorgue.model.StorageLocation.class);
            occQ.select(cb.count(occR)).where(cb.isTrue(occR.get("occupied")));
            long occupied = session.createQuery(occQ).getSingleResult();
            long free = total - occupied;
            PieChart chart = new PieChart();
            chart.setTitle(I18nUtil.t("dashboard.storage"));
            chart.setAnimated(true);
            chart.setPrefHeight(200);
            chart.setLabelsVisible(true);
            chart.getData().add(new PieChart.Data(I18nUtil.t("storage.status.occupied") + " (" + occupied + ")", occupied));
            chart.getData().add(new PieChart.Data(I18nUtil.t("storage.status.free") + " (" + free + ")", free));
            pieChartContainer.getChildren().add(chart);
        }
    }

    private long countEntriesForMonth(java.time.YearMonth ym) {
        try (var session = com.gestionmorgue.util.DatabaseManager.getSessionFactory().openSession()) {
            var cb = session.getCriteriaBuilder();
            var q = cb.createQuery(Long.class);
            var r = q.from(com.gestionmorgue.model.Deceased.class);
            q.select(cb.count(r)).where(
                cb.between(r.get("createdAt"),
                    ym.atDay(1).atStartOfDay(),
                    ym.atEndOfMonth().atTime(23, 59, 59)));
            return session.createQuery(q).getSingleResult();
        }
    }

    private void loadDashboardData() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText(I18nUtil.t("dashboard.welcome") + ", " + currentUser.getFullName());
            userRoleLabel.setText(I18nUtil.t("dashboard.role") + ": " + currentUser.getRole());
        }
        ReportService.DashboardStats stats = reportService.getDashboardStats();
        totalDeceasedLabel.setText(String.valueOf(stats.totalDeceased));
        occupiedStorageLabel.setText(String.valueOf(stats.occupiedLocations));
        totalStorageLabel.setText(String.valueOf(stats.totalLocations));
        pendingInterventionsLabel.setText(String.valueOf(stats.pendingInterventions));
        pendingExitsLabel.setText(String.valueOf(stats.pendingExits));
        interventionsTodayLabel.setText(String.valueOf(stats.interventionsToday));
        entriesThisMonthLabel.setText(String.valueOf(stats.entriesThisMonth));
        lblInterventionsPlanned.setText(String.valueOf(interventionService.countByStatus("PLANIFIEE")));
        lblInterventionsCompleted.setText(String.valueOf(interventionService.countByStatus("TERMINEE")));
        lblExitsPending.setText(String.valueOf(exitService.countPending()));
        double pct = stats.totalLocations > 0 ? (double) stats.occupiedLocations / stats.totalLocations : 0;
        storagePercentLabel.setText(String.format("%.0f%%", pct * 100));
        storageBar.setProgress(pct);
        loadRecentActivity();
    }

    private void loadRecentActivity() {
        try {
            recentActivity.getChildren().clear();
            var logs = auditService.getRecentLogs(5);
            for (var log : logs) {
                String user = log.getUser() != null ? log.getUser().getUsername() : "?";
                Hyperlink link = new Hyperlink(user + " — " + log.getAction());
                link.getStyleClass().add("activity-link");
                recentActivity.getChildren().add(link);
            }
            if (logs.isEmpty()) {
                recentActivity.getChildren().add(new Label(I18nUtil.t("dashboard.noActivity")));
            }
        } catch (Exception e) {
            recentActivity.getChildren().add(new Label(I18nUtil.t("error.title") + " chargement activité"));
        }
    }

    @FXML
    private void handleDeceasedManagement() {
        loadView("/views/deceased.fxml");
    }

    @FXML
    private void handleStorageManagement() {
        loadView("/views/storage.fxml");
    }

    @FXML
    private void handleInterventions() {
        loadView("/views/intervention.fxml");
    }

    @FXML
    private void handleExitAuthorizations() {
        loadView("/views/exits.fxml");
    }

    @FXML
    private void handleReports() {
        loadView("/views/reports.fxml");
    }

    @FXML
    private void handleUsers() {
        loadView("/views/users.fxml");
    }

    @FXML
    private void handleAuditLog() {
        loadView("/views/audit.fxml");
    }

    @FXML
    private void handleToggleTheme() {
        String current = App.getCurrentTheme();
        if ("clair".equals(current)) {
            App.setTheme("sombre");
        } else {
            App.setTheme("clair");
        }
        App.refreshScene(welcomeLabel.getScene());
    }

    @FXML
    private void handleToggleLanguage() {
        String current = com.gestionmorgue.config.ConfigService.getInstance().getLanguage();
        if ("fr".equals(current)) {
            I18nUtil.getInstance().setLanguage("en");
            com.gestionmorgue.config.ConfigService.getInstance().setLanguage("en");
        } else {
            I18nUtil.getInstance().setLanguage("fr");
            com.gestionmorgue.config.ConfigService.getInstance().setLanguage("fr");
        }
        loadDashboardData();
    }

    @FXML
    private void handleCheckUpdate() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/update.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(I18nUtil.t("action.update") + " - " + Constants.APP_NAME);
            Scene updateScene = new Scene(root, 500, 400);
            App.applyTheme(updateScene);
            stage.setScene(updateScene);
            stage.show();
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        boolean confirm = NotificationUtil.showConfirm(I18nUtil.t("action.logout"),
                I18nUtil.t("confirm.logout.message"));
        if (confirm && SessionManager.getInstance().isLoggedIn()) {
            new AuthService().logout();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
                Parent root = loader.load();
                Scene loginScene = new Scene(root, 400, 300);
                App.applyTheme(loginScene);
                Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                stage.setScene(loginScene);
                stage.setTitle(Constants.APP_NAME + " - Connexion");
                stage.centerOnScreen();
            } catch (Exception e) {
                NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
            }
        }
    }

    private void loadView(String fxmlPath) {
        contentProgress.setVisible(true);
        new Thread(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent view = loader.load();
                Platform.runLater(() -> {
                    mainContent.getChildren().setAll(view);
                    contentProgress.setVisible(false);
                    FadeTransition ft = new FadeTransition(Duration.millis(250), mainContent);
                    ft.setFromValue(0.6);
                    ft.setToValue(1.0);
                    ft.play();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    contentProgress.setVisible(false);
                    NotificationUtil.showError(I18nUtil.t("error.title"), I18nUtil.t("dashboard.error.loadView") + ": " + e.getMessage());
                });
            }
        }).start();
    }
}
