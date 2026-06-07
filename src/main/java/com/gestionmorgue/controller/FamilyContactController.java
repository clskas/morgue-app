package com.gestionmorgue.controller;

import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.FamilyContact;
import com.gestionmorgue.service.DeceasedService;
import com.gestionmorgue.service.FamilyContactService;
import com.gestionmorgue.util.I18nUtil;
import com.gestionmorgue.util.NotificationUtil;
import com.gestionmorgue.util.SecurityUtil;
import com.gestionmorgue.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class FamilyContactController {
    private static final Logger log = LoggerFactory.getLogger(FamilyContactController.class);

    @FXML private ComboBox<Deceased> deceasedCombo;
    @FXML private TextField contactNameField;
    @FXML private TextField relationshipField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea addressArea;
    @FXML private TextArea notesArea;
    @FXML private TableView<FamilyContact> contactTable;
    @FXML private TableColumn<FamilyContact, String> colName;
    @FXML private TableColumn<FamilyContact, String> colRelationship;
    @FXML private TableColumn<FamilyContact, String> colPhone;

    private FamilyContactService contactService;
    private DeceasedService deceasedService;

    @FXML
    public void initialize() {
        contactService = new FamilyContactService();
        deceasedService = new DeceasedService();
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRelationship.setCellValueFactory(new PropertyValueFactory<>("relationship"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        refreshDeceased();
        deceasedCombo.setOnAction(e -> loadContacts());
    }

    @FXML
    private void handleAdd() {
        try { SecurityUtil.requireRole("ADMIN", "MEDECIN", "GREFFIER"); } catch (SecurityException e) { NotificationUtil.showWarning(I18nUtil.t("access.denied"), e.getMessage()); return; }
        Deceased deceased = deceasedCombo.getValue();
        String name = contactNameField.getText().trim();
        if (deceased == null || name.isEmpty()) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("family.validation.required"));
            return;
        }
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("family.validation.phone"));
            return;
        }
        if (!email.isEmpty() && !ValidationUtil.isValidEmail(email)) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("family.validation.email"));
            return;
        }
        try {
            contactService.createContact(deceased, name, relationshipField.getText().trim(),
                    phone, email,
                    addressArea.getText().trim(), notesArea.getText().trim());
            NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("family.add.success"));
            clearForm();
            loadContacts();
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        try { SecurityUtil.requireRole("ADMIN", "MEDECIN", "GREFFIER"); } catch (SecurityException e) { NotificationUtil.showWarning(I18nUtil.t("access.denied"), e.getMessage()); return; }
        FamilyContact selected = contactTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        boolean confirm = NotificationUtil.showConfirm(I18nUtil.t("confirm.title"), I18nUtil.t("family.delete.confirm"));
        if (confirm) {
            contactService.deleteContact(selected);
            loadContacts();
        }
    }

    private void loadContacts() {
        Deceased selected = deceasedCombo.getValue();
        if (selected != null && selected.getId() != null) {
            contactTable.setItems(FXCollections.observableArrayList(
                    contactService.getContactsForDeceased(selected.getId())));
        } else {
            contactTable.setItems(FXCollections.observableArrayList());
        }
    }

    private void refreshDeceased() {
        deceasedCombo.setItems(FXCollections.observableArrayList(
                deceasedService.getRecentDeceased(100)));
    }

    private void clearForm() {
        contactNameField.clear();
        relationshipField.clear();
        phoneField.clear();
        emailField.clear();
        addressArea.clear();
        notesArea.clear();
    }
}
