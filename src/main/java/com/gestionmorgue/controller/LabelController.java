package com.gestionmorgue.controller;

import com.gestionmorgue.service.LabelService;
import com.gestionmorgue.util.I18nUtil;
import com.gestionmorgue.util.NotificationUtil;
import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;

public class LabelController {
    private static final Logger log = LoggerFactory.getLogger(LabelController.class);

    @FXML
    private void handleGenerateLabels() {
        try {
            LabelService labelService = new LabelService();
            File html = labelService.generateStorageLabels();
            File out = new File(System.getProperty("user.home") + "/Desktop/etiquettes-stockage.html");
            java.nio.file.Files.copy(html.toPath(), out.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            NotificationUtil.showInfo(I18nUtil.t("info.title"), "Étiquettes exportées sur le Bureau");
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(out);
            }
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
        }
    }
}
