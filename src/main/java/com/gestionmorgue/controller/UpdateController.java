package com.gestionmorgue.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gestionmorgue.config.ConfigService;
import com.gestionmorgue.config.Constants;
import com.gestionmorgue.update.UpdateCheckResult;
import com.gestionmorgue.update.UpdateChecker;
import com.gestionmorgue.update.UpdateDownloader;
import com.gestionmorgue.update.VersionInfo;
import com.gestionmorgue.update.UpdateInstaller;
import com.gestionmorgue.util.I18nUtil;
import com.gestionmorgue.util.NotificationUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class UpdateController {
    private static final Logger log = LoggerFactory.getLogger(UpdateController.class);

    @FXML private Label currentVersionLabel;
    @FXML private Label remoteVersionLabel;
    @FXML private TextArea changelogArea;
    @FXML private Button downloadButton;
    @FXML private Button closeButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private CheckBox disableCheckCheckbox;
    @FXML private Button btnRollback;

    private UpdateChecker updateChecker;
    private UpdateCheckResult checkResult;

    @FXML
    public void initialize() {
        currentVersionLabel.setText(I18nUtil.t("update.current") + ": " + Constants.APP_VERSION);
        updateChecker = new UpdateChecker();
        checkForUpdate();
    }

    private void checkForUpdate() {
        statusLabel.setText(I18nUtil.t("update.checking"));
        downloadButton.setDisable(true);

        new Thread(() -> {
            try {
                UpdateCheckResult result = updateChecker.checkForUpdate();
                Platform.runLater(() -> onCheckResult(result));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText(I18nUtil.t("update.error", e.getMessage()));
                    downloadButton.setDisable(true);
                });
            }
        }).start();
    }

    private void onCheckResult(UpdateCheckResult result) {
        this.checkResult = result;
        if (result.isUpdateAvailable()) {
            VersionInfo versionInfo = result.getVersionInfo();
            remoteVersionLabel.setText(I18nUtil.t("update.remote") + ": " + versionInfo.getVersion());
            changelogArea.setText(versionInfo.getChangelog() != null ?
                    versionInfo.getChangelog() : I18nUtil.t("update.noDetails"));
            statusLabel.setText(result.getMessage());
            downloadButton.setDisable(false);
        } else {
            remoteVersionLabel.setText(I18nUtil.t("update.none"));
            changelogArea.setText(I18nUtil.t("update.latest", Constants.APP_NAME));
            statusLabel.setText(result.getMessage());
        }
    }

    @FXML
    private void handleDownload() {
        if (checkResult == null || !checkResult.isUpdateAvailable()) return;

        btnRollback.setVisible(false);
        UpdateInstaller.backupCurrentJar(Constants.APP_VERSION);

        statusLabel.setText(I18nUtil.t("update.downloading"));
        downloadButton.setDisable(true);
        progressBar.setVisible(true);

        UpdateDownloader downloader = new UpdateDownloader();
        downloader.setProgressListener(new UpdateDownloader.DownloadProgressListener() {
            @Override
            public void onProgress(int percent) {
                Platform.runLater(() -> {
                    progressBar.setProgress(percent / 100.0);
                    statusLabel.setText(I18nUtil.t("update.downloading") + ": " + percent + "%");
                });
            }

            @Override
            public void onComplete(java.nio.file.Path filePath) {
                Platform.runLater(() -> {
                    progressBar.setProgress(1.0);
                    statusLabel.setText(I18nUtil.t("update.completed"));
                    com.gestionmorgue.update.UpdateInstaller.scheduleUpdate(filePath);
                    NotificationUtil.showInfo(I18nUtil.t("info.title"),
                            I18nUtil.t("update.completed.message"));
                    close();
                });
            }

            @Override
            public void onError(String error) {
                Platform.runLater(() -> {
                    statusLabel.setText(I18nUtil.t("update.error", error));
                    downloadButton.setDisable(false);
                    progressBar.setVisible(false);
                    if (UpdateInstaller.isRollbackAvailable()) {
                        btnRollback.setVisible(true);
                    }
                });
            }
        });

        new Thread(() -> downloader.downloadUpdate(checkResult.getVersionInfo())).start();
    }

    @FXML
    private void handleRollback() {
        boolean confirm = NotificationUtil.showConfirm(I18nUtil.t("warning.title"),
                I18nUtil.t("update.rollback.confirm"));
        if (!confirm) return;
        boolean success = UpdateInstaller.rollback();
        if (success) {
            NotificationUtil.showInfo(I18nUtil.t("info.title"),
                    I18nUtil.t("update.rollback.success"));
            btnRollback.setVisible(false);
        } else {
            NotificationUtil.showError(I18nUtil.t("error.title"),
                    I18nUtil.t("update.rollback.error", ""));
        }
    }

    @FXML
    private void handleToggleCheck() {
        ConfigService.getInstance().setUpdateCheckEnabled(!disableCheckCheckbox.isSelected());
    }

    @FXML
    private void close() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
