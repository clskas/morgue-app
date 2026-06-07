package com.gestionmorgue.controller;

import com.gestionmorgue.dao.InterventionDao;
import com.gestionmorgue.model.Attachment;
import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.Intervention;
import com.gestionmorgue.model.User;
import com.gestionmorgue.service.AttachmentService;
import com.gestionmorgue.service.DeceasedService;
import com.gestionmorgue.service.InterventionService;
import com.gestionmorgue.util.I18nUtil;
import com.gestionmorgue.util.NotificationUtil;
import com.gestionmorgue.util.PaginatedResult;
import com.gestionmorgue.util.SecurityUtil;
import com.gestionmorgue.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class InterventionController {
    private static final Logger log = LoggerFactory.getLogger(InterventionController.class);

    @FXML private TableView<Intervention> interventionTable;
    @FXML private TableColumn<Intervention, String> colDeceased;
    @FXML private TableColumn<Intervention, String> colType;
    @FXML private TableColumn<Intervention, String> colPerformer;
    @FXML private TableColumn<Intervention, String> colScheduled;
    @FXML private TableColumn<Intervention, String> colStatus;

    @FXML private ComboBox<Deceased> deceasedCombo;
    @FXML private ComboBox<String> typeCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextArea reportArea;
    @FXML private TextArea productsArea;
    @FXML private Button completeButton;
    @FXML private TextField signatureField;
    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private ComboBox<String> filterStatusCombo;

    @FXML private ListView<Attachment> attachmentList;
    @FXML private Button btnAddAttachment;
    @FXML private Button btnDeleteAttachment;
    @FXML private TextField attachmentDescription;
    @FXML private TitledPane attachmentPane;

    private InterventionService interventionService;
    private DeceasedService deceasedService;
    private AttachmentService attachmentService;
    private InterventionDao interventionDao;
    private ObservableList<Intervention> interventions;
    private ObservableList<Attachment> attachmentData;
    private List<Intervention> allInterventions;
    private Intervention selected;
    private int currentPage = 0;
    private boolean hasMore = true;
    private static final int PAGE_SIZE = 50;

    @FXML
    public void initialize() {
        interventionService = new InterventionService();
        deceasedService = new DeceasedService();
        attachmentService = new AttachmentService();
        interventionDao = new InterventionDao();
        interventions = FXCollections.observableArrayList();
        attachmentData = FXCollections.observableArrayList();
        typeCombo.setItems(FXCollections.observableArrayList(
                "CONSERVATION", "SOINS_PRESENTATION", "AUTOPSIE", "PRELEVEMENT", "OTHER"));

        colDeceased.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDeceased().getFullName()));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPerformer.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPerformer().getFullName()));
        colScheduled.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getScheduledAt().toLocalDate().toString()));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        interventionTable.setItems(interventions);
        interventionTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> selectIntervention(n));
        setupScrollListener();

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

        refresh();
    }

    private void setupScrollListener() {
        interventionTable.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar scrollBar = (ScrollBar) interventionTable.lookup(".scroll-bar:vertical");
                if (scrollBar != null) {
                    scrollBar.valueProperty().addListener((obs2, oldVal, newVal) -> {
                        if ((double) newVal >= 0.95 && hasMore) {
                            loadNextPage();
                        }
                    });
                }
            }
        });
    }

    @FXML
    private void handleSchedule() {
        try { SecurityUtil.requireRole("ADMIN", "MEDECIN", "THANATOPRACTEUR"); } catch (SecurityException e) { NotificationUtil.showWarning(I18nUtil.t("access.denied"), e.getMessage()); return; }
        Deceased dec = deceasedCombo.getValue();
        String type = typeCombo.getValue();
        if (dec == null || type == null || datePicker.getValue() == null) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("intervention.validation.required"));
            return;
        }
        try {
            User performer = SessionManager.getInstance().getCurrentUser();
            interventionService.schedule(dec, performer, type,
                    datePicker.getValue().atTime(LocalDateTime.now().toLocalTime()));
            NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("intervention.save.success"));
            clearForm();
            refresh();
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
        }
    }

    @FXML
    private void handleComplete() {
        try { SecurityUtil.requireRole("ADMIN", "MEDECIN", "THANATOPRACTEUR"); } catch (SecurityException e) { NotificationUtil.showWarning(I18nUtil.t("access.denied"), e.getMessage()); return; }
        if (selected == null || !"PLANIFIEE".equals(selected.getStatus())) return;
        String signature = signatureField.getText().trim();
        if (signature.isEmpty()) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("intervention.signature.required"));
            return;
        }
        try {
            selected.setSignedBy(signature);
            selected.setSignedAt(LocalDateTime.now());
            interventionService.complete(selected, reportArea.getText(), productsArea.getText());
            NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("intervention.complete.success"));
            clearForm();
            refresh();
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
        }
    }

    @FXML
    private void handleFilter() {
        applyFilter();
    }

    @FXML
    private void handleClearFilter() {
        filterTypeCombo.setValue(null);
        filterStatusCombo.setValue(null);
        applyFilter();
    }

    private void applyFilter() {
        String type = filterTypeCombo.getValue();
        String status = filterStatusCombo.getValue();
        if (type == null && status == null) {
            loadPage(0);
            return;
        }
        interventions.setAll(allInterventions.stream()
                .filter(i -> type == null || type.isEmpty() || type.equals(i.getType()))
                .filter(i -> status == null || status.isEmpty() || status.equals(i.getStatus()))
                .collect(Collectors.toList()));
    }

    private void selectIntervention(Intervention i) {
        selected = i;
        if (i != null) {
            completeButton.setDisable(!"PLANIFIEE".equals(i.getStatus()));
            reportArea.setText(i.getReport() != null ? i.getReport() : "");
            productsArea.setText(i.getProductsUsed() != null ? i.getProductsUsed() : "");
            attachmentPane.setVisible(true);
            refreshAttachments();
        } else {
            attachmentPane.setVisible(false);
            attachmentData.clear();
        }
    }

    private void clearForm() {
        selected = null;
        deceasedCombo.setValue(null);
        typeCombo.setValue(null);
        datePicker.setValue(null);
        reportArea.clear();
        productsArea.clear();
        completeButton.setDisable(true);
        attachmentPane.setVisible(false);
        attachmentData.clear();
        attachmentDescription.clear();
    }

    @FXML
    private void handleAddAttachment() {
        if (selected == null || selected.getDeceased() == null || selected.getDeceased().getId() == null) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("attachment.no.selection"));
            return;
        }
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle(I18nUtil.t("attachment.file.chooser.title"));
        File file = fileChooser.showOpenDialog(btnAddAttachment.getScene().getWindow());
        if (file == null) return;
        try {
            String desc = attachmentDescription.getText().trim();
            attachmentService.attachFile("INTERVENTION", selected.getId(), file, desc.isEmpty() ? null : desc);
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
        if (selected != null && selected.getId() != null) {
            attachmentData.setAll(attachmentService.getAttachments("INTERVENTION", selected.getId()));
        } else {
            attachmentData.clear();
        }
    }

    private void loadNextPage() {
        currentPage++;
        PaginatedResult<Intervention> result = interventionDao.findPendingPaginated(currentPage, PAGE_SIZE);
        hasMore = result.hasNext();
        interventions.addAll(result.getResults());
    }

    private void loadPage(int page) {
        currentPage = page;
        PaginatedResult<Intervention> result = interventionDao.findPendingPaginated(page, PAGE_SIZE);
        hasMore = result.hasNext();
        interventions.setAll(result.getResults());
    }

    private void refresh() {
        deceasedCombo.setItems(FXCollections.observableArrayList(deceasedService.getRecentDeceased(50)));
        allInterventions = interventionService.getPendingInterventions();
        loadPage(0);
        filterTypeCombo.setItems(FXCollections.observableArrayList(
                allInterventions.stream().map(Intervention::getType).distinct().sorted().collect(Collectors.toList())));
        filterStatusCombo.setItems(FXCollections.observableArrayList(
                allInterventions.stream().map(Intervention::getStatus).distinct().sorted().collect(Collectors.toList())));
    }
}
