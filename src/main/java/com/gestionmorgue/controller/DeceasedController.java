package com.gestionmorgue.controller;

import com.gestionmorgue.model.Attachment;
import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.service.AttachmentService;
import com.gestionmorgue.service.DeceasedService;
import com.gestionmorgue.util.I18nUtil;
import com.gestionmorgue.util.NotificationUtil;
import com.gestionmorgue.util.SecurityUtil;
import com.gestionmorgue.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;

public class DeceasedController {
    private static final Logger log = LoggerFactory.getLogger(DeceasedController.class);

    @FXML private TextField searchField;
    @FXML private TextField searchDossierField;
    @FXML private ComboBox<String> searchGenderCombo;
    @FXML private DatePicker searchDateFrom;
    @FXML private DatePicker searchDateTo;
    @FXML private TableView<Deceased> deceasedTable;
    @FXML private TableColumn<Deceased, String> colDossier;
    @FXML private TableColumn<Deceased, String> colLastName;
    @FXML private TableColumn<Deceased, String> colFirstName;
    @FXML private TableColumn<Deceased, String> colDeathDate;
    @FXML private TableColumn<Deceased, String> colGender;
    @FXML private Pagination pagination;

    @FXML private TextField lastNameField;
    @FXML private TextField firstNameField;
    @FXML private TextField nirField;
    @FXML private DatePicker birthDatePicker;
    @FXML private DatePicker deathDatePicker;
    @FXML private TextField placeOfDeathField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextArea observationsArea;
    @FXML private Label resultCountLabel;
    @FXML private Button btnExportPdf;

    @FXML private ListView<Attachment> attachmentList;
    @FXML private Button btnAddAttachment;
    @FXML private Button btnDeleteAttachment;
    @FXML private TextField attachmentDescription;
    @FXML private TitledPane attachmentPane;

    private Popup autocompletePopup;
    private ListView<Deceased> suggestionList;

    private DeceasedService deceasedService;
    private AttachmentService attachmentService;
    private ObservableList<Deceased> deceasedList;
    private ObservableList<Attachment> attachmentData;
    private Deceased selectedDeceased;
    private static final int PAGE_SIZE = 25;

    @FXML
    public void initialize() {
        deceasedService = new DeceasedService();
        attachmentService = new AttachmentService();
        deceasedList = FXCollections.observableArrayList();
        attachmentData = FXCollections.observableArrayList();
        genderCombo.setItems(FXCollections.observableArrayList(I18nUtil.t("gender.male"), I18nUtil.t("gender.female"), I18nUtil.t("gender.unspecified")));
        searchGenderCombo.setItems(FXCollections.observableArrayList("", I18nUtil.t("gender.male"), I18nUtil.t("gender.female"), I18nUtil.t("gender.unspecified")));

        colDossier.setCellValueFactory(new PropertyValueFactory<>("dossierNumber"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colDeathDate.setCellValueFactory(new PropertyValueFactory<>("deathDate"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));

        deceasedTable.setItems(deceasedList);
        deceasedTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, val) -> {
                    if (val != null) selectDeceased(val);
                });

        pagination.currentPageIndexProperty().addListener((obs, old, idx) -> {
            if (!searchActive()) loadPage(idx.intValue());
        });

        btnExportPdf.setOnAction(e -> handleExportPdf());

        attachmentList.setItems(attachmentData);
        attachmentList.setCellFactory(lv -> new ListCell<Attachment>() {
            @Override
            protected void updateItem(Attachment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String desc = item.getDescription() != null ? " - " + item.getDescription() : "";
                    setText(item.getFileName() + desc);
                }
            }
        });
        attachmentPane.setVisible(false);

        suggestionList = new ListView<>();
        suggestionList.setPrefWidth(300);
        suggestionList.setMaxHeight(200);
        autocompletePopup = new Popup();
        autocompletePopup.getContent().add(suggestionList);
        autocompletePopup.setAutoHide(true);

        searchField.setOnKeyReleased(e -> {
            String text = searchField.getText().trim();
            if (text.length() >= 2) {
                var suggestions = deceasedService.search(text, null, null);
                if (!suggestions.isEmpty()) {
                    suggestionList.getItems().setAll(suggestions);
                    suggestionList.setCellFactory(lv -> new ListCell<>() {
                        @Override
                        protected void updateItem(Deceased d, boolean empty) {
                            super.updateItem(d, empty);
                            setText(d == null ? null : d.getFullName() + " (" + d.getDossierNumber() + ")");
                        }
                    });
                    var screenPos = searchField.localToScreen(0, searchField.getHeight());
                    autocompletePopup.show(searchField, screenPos.getX(), screenPos.getY());
                } else {
                    autocompletePopup.hide();
                }
            } else {
                autocompletePopup.hide();
            }
        });

        suggestionList.setOnMouseClicked(e -> {
            Deceased selected = suggestionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                searchField.setText(selected.getFullName());
                autocompletePopup.hide();
                handleSearch();
            }
        });

        suggestionList.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                Deceased selected = suggestionList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    searchField.setText(selected.getFullName());
                    autocompletePopup.hide();
                    handleSearch();
                }
            }
        });

        loadPage(0);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        String dossier = searchDossierField.getText().trim();
        String gender = searchGenderCombo.getValue();

        if (query.isEmpty() && dossier.isEmpty() && (gender == null || gender.isEmpty())) {
            loadPage(0);
            return;
        }

        var results = deceasedService.search(query, null, dossier.isEmpty() ? null : dossier);
        if (gender != null && !gender.isEmpty()) {
            results = results.stream().filter(d -> gender.equals(d.getGender())).toList();
        }
        if (searchDateFrom.getValue() != null) {
            results = results.stream().filter(d -> d.getDeathDate() != null
                    && !d.getDeathDate().isBefore(searchDateFrom.getValue())).toList();
        }
        if (searchDateTo.getValue() != null) {
            results = results.stream().filter(d -> d.getDeathDate() != null
                    && !d.getDeathDate().isAfter(searchDateTo.getValue())).toList();
        }
        deceasedList.setAll(results);
        resultCountLabel.setText(I18nUtil.t("deceased.list.results", results.size()));
        pagination.setVisible(false);
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        searchDossierField.clear();
        searchGenderCombo.setValue(null);
        searchDateFrom.setValue(null);
        searchDateTo.setValue(null);
        resultCountLabel.setText("");
        pagination.setVisible(true);
        loadPage(0);
    }

    @FXML
    private void handleSave() {
        try { SecurityUtil.requireRole("ADMIN", "MEDECIN", "THANATOPRACTEUR"); } catch (SecurityException e) { NotificationUtil.showWarning(I18nUtil.t("access.denied"), e.getMessage()); return; }
        lastNameField.setStyle(null);
        firstNameField.setStyle(null);
        genderCombo.setStyle(null);
        nirField.setStyle(null);

        ValidationUtil.ValidationResult validation = new ValidationUtil.ValidationResult();
        if (!ValidationUtil.isNotEmpty(lastNameField.getText())) {
            lastNameField.setStyle("-fx-border-color: #c62828;");
            validation.addError(I18nUtil.t("deceased.validation.name"));
        }
        if (!ValidationUtil.isNotEmpty(firstNameField.getText())) {
            firstNameField.setStyle("-fx-border-color: #c62828;");
            validation.addError(I18nUtil.t("deceased.validation.firstname"));
        }
        if (deathDatePicker.getValue() != null && birthDatePicker.getValue() != null
                && deathDatePicker.getValue().isBefore(birthDatePicker.getValue())) {
            validation.addError(I18nUtil.t("deceased.validation.deathdate"));
        }
        String nir = nirField.getText().trim();
        if (!nir.isEmpty() && !ValidationUtil.isValidNir(nir)) {
            nirField.setStyle("-fx-border-color: #c62828;");
            validation.addError(I18nUtil.t("deceased.validation.nir"));
        }
        if (!validation.isValid()) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), validation.getErrorMessage());
            return;
        }
        try {
            String birthDate = birthDatePicker.getValue() != null ? birthDatePicker.getValue().toString() : null;
            String deathDate = deathDatePicker.getValue() != null ? deathDatePicker.getValue().toString() : null;

            if (selectedDeceased == null) {
                Deceased d = deceasedService.createDeceased(
                        lastNameField.getText().trim(), firstNameField.getText().trim(),
                        birthDate, deathDate, placeOfDeathField.getText().trim(), genderCombo.getValue());
                d.setNir(nir.isEmpty() ? null : nir);
                deceasedService.update(d);
                NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("deceased.save.success"));
            } else {
                selectedDeceased.setLastName(lastNameField.getText().trim().toUpperCase());
                selectedDeceased.setFirstName(firstNameField.getText().trim());
                selectedDeceased.setNir(nir.isEmpty() ? null : nir);
                selectedDeceased.setBirthDate(birthDatePicker.getValue());
                selectedDeceased.setDeathDate(deathDatePicker.getValue());
                selectedDeceased.setPlaceOfDeath(placeOfDeathField.getText().trim());
                selectedDeceased.setGender(genderCombo.getValue());
                selectedDeceased.setObservations(observationsArea.getText());
                deceasedService.update(selectedDeceased);
                NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("deceased.update.success"));
            }
            clearForm();
            loadPage(0);
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), I18nUtil.t("deceased.error.save") + ": " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        try { SecurityUtil.requireRole("ADMIN", "MEDECIN", "THANATOPRACTEUR"); } catch (SecurityException e) { NotificationUtil.showWarning(I18nUtil.t("access.denied"), e.getMessage()); return; }
        if (selectedDeceased == null || selectedDeceased.getId() == null) return;
        long assignments = deceasedService.countActiveAssignments(selectedDeceased.getId());
        long exits = deceasedService.countPendingExits(selectedDeceased.getId());
        long interventions = deceasedService.countPlannedInterventions(selectedDeceased.getId());
        StringBuilder msg = new StringBuilder(I18nUtil.t("deceased.delete.confirm", selectedDeceased.getFullName()));
        msg.append("\n\n").append(I18nUtil.t("deceased.delete.warning")).append(" :");
        msg.append("\n- ").append(assignments).append(" ").append(I18nUtil.t("deceased.delete.assignments", assignments));
        msg.append("\n- ").append(exits).append(" ").append(I18nUtil.t("deceased.delete.exits", exits));
        msg.append("\n- ").append(interventions).append(" ").append(I18nUtil.t("deceased.delete.interventions", interventions));
        msg.append("\n- ").append(I18nUtil.t("deceased.delete.contacts"));
        msg.append("\n\n").append(I18nUtil.t("deceased.delete.irreversible"));
        boolean confirm = NotificationUtil.showConfirm(I18nUtil.t("confirm.title"), msg.toString());
        if (confirm) {
            deceasedService.delete(selectedDeceased);
            clearForm();
            loadPage(0);
            NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("deceased.delete.success"));
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    @FXML
    private void handleExportPdf() {
        try { SecurityUtil.requireRole("ADMIN", "MEDECIN", "THANATOPRACTEUR"); } catch (SecurityException e) { NotificationUtil.showWarning(I18nUtil.t("access.denied"), e.getMessage()); return; }
        Deceased deceased = deceasedTable.getSelectionModel().getSelectedItem();
        if (deceased == null) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("deceased.export.pdf.no.selection"));
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18nUtil.t("deceased.export.pdf"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        fileChooser.setInitialFileName(deceased.getDossierNumber() + "_" + deceased.getLastName() + ".pdf");
        java.io.File file = fileChooser.showSaveDialog(btnExportPdf.getScene().getWindow());
        if (file == null) return;
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new java.awt.Color(26, 35, 126));
            com.lowagie.text.Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            com.lowagie.text.Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph(I18nUtil.t("deceased.export.pdf"), titleFont));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph(I18nUtil.t("deceased.field.dossier") + " : " + (deceased.getDossierNumber() != null ? deceased.getDossierNumber() : ""), valueFont));
            document.add(new Paragraph(I18nUtil.t("deceased.field.lastname") + " : " + (deceased.getLastName() != null ? deceased.getLastName() : ""), valueFont));
            document.add(new Paragraph(I18nUtil.t("deceased.field.firstname") + " : " + (deceased.getFirstName() != null ? deceased.getFirstName() : ""), valueFont));
            document.add(new Paragraph(I18nUtil.t("deceased.field.nir") + " : " + (deceased.getNir() != null ? deceased.getNir() : ""), valueFont));
            document.add(new Paragraph(I18nUtil.t("deceased.field.birthdate") + " : " + (deceased.getBirthDate() != null ? deceased.getBirthDate().toString() : ""), valueFont));
            document.add(new Paragraph(I18nUtil.t("deceased.field.deathdate") + " : " + (deceased.getDeathDate() != null ? deceased.getDeathDate().toString() : ""), valueFont));
            document.add(new Paragraph(I18nUtil.t("deceased.field.gender") + " : " + (deceased.getGender() != null ? deceased.getGender() : ""), valueFont));
            document.add(new Paragraph(I18nUtil.t("deceased.field.placeofdeath") + " : " + (deceased.getPlaceOfDeath() != null ? deceased.getPlaceOfDeath() : ""), valueFont));
            document.add(new Paragraph(I18nUtil.t("deceased.field.causeofdeath") + " : " + (deceased.getCauseOfDeath() != null ? deceased.getCauseOfDeath() : ""), valueFont));
            document.add(new Paragraph(I18nUtil.t("deceased.field.observations") + " : " + (deceased.getObservations() != null ? deceased.getObservations() : ""), valueFont));

            document.close();
            NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("deceased.export.pdf.success"));
        } catch (Exception ex) {
            NotificationUtil.showError(I18nUtil.t("error.title"), I18nUtil.t("deceased.export.pdf.error") + ": " + ex.getMessage());
        }
    }

    @FXML
    private void handleAddAttachment() {
        if (selectedDeceased == null || selectedDeceased.getId() == null) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("attachment.no.selection"));
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18nUtil.t("attachment.file.chooser.title"));
        File file = fileChooser.showOpenDialog(btnAddAttachment.getScene().getWindow());
        if (file == null) return;
        try {
            String desc = attachmentDescription.getText().trim();
            attachmentService.attachFile("DECEASED", selectedDeceased.getId(), file, desc.isEmpty() ? null : desc);
            attachmentDescription.clear();
            refreshAttachments();
            NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("attachment.add"));
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
        }
    }

    @FXML
    private void handleDeleteAttachment() {
        Attachment selected = attachmentList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        boolean confirm = NotificationUtil.showConfirm(I18nUtil.t("confirm.title"),
                I18nUtil.t("action.delete") + " " + selected.getFileName() + " ?");
        if (confirm) {
            attachmentService.deleteAttachment(selected);
            refreshAttachments();
        }
    }

    private void refreshAttachments() {
        if (selectedDeceased != null && selectedDeceased.getId() != null) {
            attachmentData.setAll(attachmentService.getAttachments("DECEASED", selectedDeceased.getId()));
        } else {
            attachmentData.clear();
        }
    }

    private void selectDeceased(Deceased deceased) {
        selectedDeceased = deceased;
        lastNameField.setText(deceased.getLastName());
        firstNameField.setText(deceased.getFirstName());
        nirField.setText(deceased.getNir());
        if (deceased.getBirthDate() != null) birthDatePicker.setValue(deceased.getBirthDate());
        if (deceased.getDeathDate() != null) deathDatePicker.setValue(deceased.getDeathDate());
        placeOfDeathField.setText(deceased.getPlaceOfDeath());
        genderCombo.setValue(deceased.getGender());
        observationsArea.setText(deceased.getObservations());
        attachmentPane.setVisible(true);
        refreshAttachments();
    }

    private void clearForm() {
        selectedDeceased = null;
        lastNameField.clear(); lastNameField.setStyle(null);
        firstNameField.clear(); firstNameField.setStyle(null);
        nirField.clear(); nirField.setStyle(null);
        birthDatePicker.setValue(null);
        deathDatePicker.setValue(null);
        placeOfDeathField.clear();
        genderCombo.setValue(null); genderCombo.setStyle(null);
        observationsArea.clear();
        deceasedTable.getSelectionModel().clearSelection();
        attachmentPane.setVisible(false);
        attachmentData.clear();
        attachmentDescription.clear();
    }

    public void searchExternal(String query) {
        searchField.setText(query);
        handleSearch();
    }

    private boolean searchActive() {
        return !searchField.getText().trim().isEmpty()
                || !searchDossierField.getText().trim().isEmpty()
                || (searchGenderCombo.getValue() != null && !searchGenderCombo.getValue().isEmpty());
    }

    private void loadPage(int page) {
        var result = new com.gestionmorgue.dao.GenericDao<>(Deceased.class).findPaginated(page, PAGE_SIZE);
        deceasedList.setAll(result.getResults());
        pagination.setPageCount(Math.max(result.getTotalPages(), 1));
        pagination.setCurrentPageIndex(Math.min(page, result.getTotalPages() - 1));
        resultCountLabel.setText(I18nUtil.t("deceased.list.count", result.getTotalCount(), page + 1, result.getTotalPages()));
    }
}
