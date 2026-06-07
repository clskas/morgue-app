package com.gestionmorgue.controller;

import com.gestionmorgue.service.BackupService;
import com.gestionmorgue.service.ReportService;
import com.gestionmorgue.util.I18nUtil;
import com.gestionmorgue.util.NotificationUtil;
import com.gestionmorgue.util.ProgressUtil;
import com.gestionmorgue.util.SecurityUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.io.File;

public class ReportController {
    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    @FXML private Label totalDeceased;
    @FXML private Label occupiedStorage;
    @FXML private Label totalStorage;
    @FXML private Label pendingInterventions;
    @FXML private Label pendingExits;
    @FXML private Label interventionsToday;
    @FXML private Label entriesThisMonth;

    private ReportService reportService;
    private BackupService backupService;

    @FXML
    public void initialize() {
        reportService = new ReportService();
        backupService = new BackupService();
        loadStats();
    }

    private void loadStats() {
        ReportService.DashboardStats stats = reportService.getDashboardStats();
        totalDeceased.setText(String.valueOf(stats.totalDeceased));
        occupiedStorage.setText(stats.occupiedLocations + " / " + stats.totalLocations);
        totalStorage.setText(String.valueOf(stats.totalLocations));
        pendingInterventions.setText(String.valueOf(stats.pendingInterventions));
        pendingExits.setText(String.valueOf(stats.pendingExits));
        interventionsToday.setText(String.valueOf(stats.interventionsToday));
        entriesThisMonth.setText(String.valueOf(stats.entriesThisMonth));
    }

    @FXML
    private void handleExportCsv() {
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return reportService.exportDeceasedCsv();
            }
        };
        ProgressUtil.runWithProgress(I18nUtil.t("action.export") + " CSV", task, csv -> {
            try {
                File file = new File(System.getProperty("user.home") + "/Desktop/export-defunts.csv");
                java.nio.file.Files.writeString(file.toPath(), csv);
                NotificationUtil.showInfo(I18nUtil.t("action.export") + " CSV", I18nUtil.t("reports.export.desktop") + " (" + file.length() + " octets)");
            } catch (Exception e) {
                NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
            }
        });
    }

    @FXML
    private void handleExportPdf() {
        Task<File> task = new Task<>() {
            @Override
            protected File call() throws Exception {
                return reportService.exportPdfReport();
            }
        };
        ProgressUtil.runWithProgress(I18nUtil.t("action.export") + " HTML", task, html -> {
            try {
                File pdf = new File(System.getProperty("user.home") + "/Desktop/rapport-morgue.html");
                java.nio.file.Files.copy(html.toPath(), pdf.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                NotificationUtil.showInfo(I18nUtil.t("action.export"), I18nUtil.t("reports.export.desktop"));
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(pdf);
            } catch (Exception e) {
                NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
            }
        });
    }

    @FXML
    private void handleExportPdfNative() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                reportService.exportNativePdf();
                return null;
            }
        };
        ProgressUtil.runWithProgress(I18nUtil.t("action.export") + " PDF", task, v -> {
            NotificationUtil.showInfo(I18nUtil.t("action.export"), I18nUtil.t("reports.export.desktop"));
        });
    }

    @FXML
    private void handleBackup() {
        try { SecurityUtil.requireAdmin(); } catch (SecurityException e) { NotificationUtil.showWarning(I18nUtil.t("access.denied"), e.getMessage()); return; }
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return backupService.exportBackup();
            }
        };
        ProgressUtil.runWithProgress(I18nUtil.t("action.backup"), task, path -> {
            NotificationUtil.showInfo(I18nUtil.t("action.backup"), "Base sauvegardée : " + path);
        });
    }

    @FXML
    private void handleRestore() {
        try { SecurityUtil.requireAdmin(); } catch (SecurityException e) { NotificationUtil.showWarning(I18nUtil.t("access.denied"), e.getMessage()); return; }
        try {
            File backupDir = new File(BackupService.getBackupDir());
            if (!backupDir.exists()) {
                NotificationUtil.showWarning(I18nUtil.t("action.restore"), I18nUtil.t("backup.notfound"));
                return;
            }
            File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".sql"));
            if (files == null || files.length == 0) {
                NotificationUtil.showWarning(I18nUtil.t("action.restore"), I18nUtil.t("backup.notfound"));
                return;
            }
            File latestFile = files[files.length - 1];
            for (File f : files) {
                if (f.lastModified() > latestFile.lastModified()) latestFile = f;
            }
            File fileToRestore = latestFile;
            boolean confirm = NotificationUtil.showConfirm(I18nUtil.t("action.restore"),
                    I18nUtil.t("backup.restore.confirm", fileToRestore.getName()));
            if (confirm) {
                String path = fileToRestore.getAbsolutePath();
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        backupService.importBackup(path);
                        return null;
                    }
                };
                ProgressUtil.runWithProgress(I18nUtil.t("action.restore"), task, v -> {
                    NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("backup.restore.success"));
                });
            }
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadStats();
        NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("dashboard.refresh"));
    }

    @FXML
    private void handleExportJson() {
        Task<String> task = new Task<>() {
            @Override
            protected String call() { return reportService.exportDeceasedJson(); }
        };
        ProgressUtil.runWithProgress(I18nUtil.t("action.export") + " JSON", task, json -> {
            try {
                String path = System.getProperty("user.home") + "/Desktop/export-defunts.json";
                java.nio.file.Files.writeString(java.nio.file.Paths.get(path), json);
                NotificationUtil.showInfo(I18nUtil.t("action.export"), "Fichier exporté : " + path);
            } catch (Exception e) {
                NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
            }
        });
    }

    @FXML
    private void handleExportXlsx() {
        ProgressUtil.runWithProgress(I18nUtil.t("action.export") + " Excel", new Task<>() {
            protected String call() { return reportService.exportDeceasedXlsx(); }
        }, path -> NotificationUtil.showInfo(I18nUtil.t("action.export"), "Fichier exporté : " + path));
    }

    @FXML
    private void handlePrint() {
        javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
        if (job == null) {
            NotificationUtil.showWarning(I18nUtil.t("action.print"), I18nUtil.t("reports.noprinter"));
            return;
        }
        if (job.showPrintDialog(totalDeceased.getScene().getWindow())) {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    var stats = reportService.getDashboardStats();
                    Platform.runLater(() -> {
                        var grid = new javafx.scene.layout.GridPane();
                        grid.setHgap(20); grid.setVgap(10);
                        grid.setStyle("-fx-padding: 30; -fx-font-family: 'Segoe UI';");
                        int row = 0;
                        for (var entry : new String[][]{
                            {I18nUtil.t("reports.print.title"), ""},
                            {I18nUtil.t("dashboard.stats.deceased"), String.valueOf(stats.totalDeceased)},
                            {I18nUtil.t("dashboard.stats.occupied"), stats.occupiedLocations + " / " + stats.totalLocations},
                            {I18nUtil.t("dashboard.stats.pending"), String.valueOf(stats.pendingInterventions)},
                            {I18nUtil.t("dashboard.stats.pendingExits"), String.valueOf(stats.pendingExits)},
                            {I18nUtil.t("dashboard.stats.interventionsToday"), String.valueOf(stats.interventionsToday)},
                            {I18nUtil.t("dashboard.stats.entriesThisMonth"), String.valueOf(stats.entriesThisMonth)},
                        }) {
                            var lbl = new javafx.scene.control.Label(entry[0]);
                            lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
                            var val = new javafx.scene.control.Label(entry[1]);
                            val.setStyle("-fx-font-size: 14;");
                            if (row == 0) { lbl.setStyle("-fx-font-size: 18; -fx-font-weight: bold;"); val.setText(""); }
                            grid.add(lbl, 0, row); grid.add(val, 1, row); row++;
                        }
                        boolean printed = job.printPage(grid);
                        if (printed) job.endJob();
                    });
                    return null;
                }
            };
            new Thread(task).start();
        }
    }
}
