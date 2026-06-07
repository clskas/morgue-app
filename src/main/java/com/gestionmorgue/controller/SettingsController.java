package com.gestionmorgue.controller;

import com.gestionmorgue.App;
import com.gestionmorgue.config.ConfigService;
import com.gestionmorgue.util.I18nUtil;
import com.gestionmorgue.util.NotificationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SettingsController {
    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);

    @FXML private TextField hospitalNameField;
    @FXML private TextField hospitalAddressField;
    @FXML private TextField hospitalPhoneField;
    @FXML private TextField logoPathField;
    @FXML private TextField dossierPrefixField;
    @FXML private ComboBox<String> langSelector;
    @FXML private Slider fontSizeSlider;
    @FXML private Label fontSizeLabel;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        ConfigService cfg = ConfigService.getInstance();
        hospitalNameField.setText(cfg.getHospitalName());
        hospitalAddressField.setText(cfg.getHospitalAddress());
        hospitalPhoneField.setText(cfg.getHospitalPhone());
        logoPathField.setText(cfg.getHospitalLogoPath());
        dossierPrefixField.setText(cfg.getDossierPrefix());
        langSelector.getItems().addAll("Français", "English");
        langSelector.setValue("fr".equals(cfg.getLanguage()) ? "Français" : "English");
        int size = cfg.getFontSize();
        fontSizeSlider.setValue(size);
        fontSizeLabel.setText(size + "px");
        fontSizeSlider.valueProperty().addListener((obs, old, val) ->
            fontSizeLabel.setText(val.intValue() + "px"));
    }

    @FXML
    private void handleChooseLogo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir le logo de l'hôpital");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fc.showOpenDialog(logoPathField.getScene().getWindow());
        if (file != null) {
            logoPathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleClearLogo() {
        logoPathField.setText("");
    }

    @FXML
    private void handleSave() {
        ConfigService cfg = ConfigService.getInstance();
        cfg.setHospitalName(hospitalNameField.getText().trim());
        cfg.setHospitalAddress(hospitalAddressField.getText().trim());
        cfg.setHospitalPhone(hospitalPhoneField.getText().trim());
        cfg.setHospitalLogoPath(logoPathField.getText().trim());
        cfg.setDossierPrefix(dossierPrefixField.getText().trim());
        cfg.setLanguage("Français".equals(langSelector.getValue()) ? "fr" : "en");
        cfg.setFontSize((int) fontSizeSlider.getValue());
        I18nUtil.getInstance().setLanguage(cfg.getLanguage());
        App.refreshScene(hospitalNameField.getScene());
        statusLabel.setText("Paramètres enregistrés.");
        NotificationUtil.showInfo("Paramètres", "Configuration sauvegardée.");
        log.info("Settings saved");
    }

    @FXML
    private void handleReset() {
        ConfigService cfg = ConfigService.getInstance();
        cfg.setHospitalName("");
        cfg.setHospitalAddress("");
        cfg.setHospitalPhone("");
        cfg.setHospitalLogoPath("");
        cfg.setDossierPrefix("DOS");
        cfg.setFontSize(13);
        hospitalNameField.setText("");
        hospitalAddressField.setText("");
        hospitalPhoneField.setText("");
        logoPathField.setText("");
        dossierPrefixField.setText("DOS");
        fontSizeSlider.setValue(13);
        statusLabel.setText("Paramètres réinitialisés.");
        NotificationUtil.showInfo("Paramètres", "Valeurs par défaut restaurées.");
    }
}
