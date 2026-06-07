package com.gestionmorgue.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gestionmorgue.App;
import com.gestionmorgue.config.ConfigService;
import com.gestionmorgue.util.I18nUtil;
import com.gestionmorgue.util.NotificationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ThemeController {
    private static final Logger log = LoggerFactory.getLogger(ThemeController.class);

    @FXML private ColorPicker primaryColor;
    @FXML private ColorPicker bgColor;
    @FXML private ColorPicker surfaceColor;
    @FXML private ColorPicker textColor;
    @FXML private ColorPicker accentColor;
    @FXML private VBox previewBox;

    @FXML
    public void initialize() {
        primaryColor.setValue(Color.web("#1a237e"));
        bgColor.setValue(Color.web("#f5f5f5"));
        surfaceColor.setValue(Color.web("#ffffff"));
        textColor.setValue(Color.web("#37474f"));
        accentColor.setValue(Color.web("#cba6f7"));
    }

    @FXML
    private void handlePreview() {
        String css = previewCss();
        previewBox.setStyle(css);
    }

    @FXML
    private void handleApply() {
        String css = previewCss();
        ConfigService.getInstance().setCustomTheme(css);
        Stage stage = (Stage) primaryColor.getScene().getWindow();
        if (stage != null) {
            App.refreshScene(stage.getScene());
            NotificationUtil.showInfo(I18nUtil.t("info.title"), "Thème personnalisé appliqué");
        }
    }

    private String previewCss() {
        return "-fx-background-color: " + toRgba(bgColor.getValue()) + ";"
                + "-fx-text-fill: " + toRgba(textColor.getValue()) + ";";
    }

    private String toRgba(Color c) {
        return String.format("rgba(%d,%d,%d,%.2f)",
                (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255), c.getOpacity());
    }
}
