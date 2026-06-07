package com.gestionmorgue.controller;

import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.ExitAuthorization;
import com.gestionmorgue.service.DeceasedService;
import com.gestionmorgue.service.ExitService;
import com.gestionmorgue.util.I18nUtil;
import com.gestionmorgue.util.NotificationUtil;
import com.gestionmorgue.util.PaginatedResult;
import com.gestionmorgue.util.SecurityUtil;
import com.gestionmorgue.util.SessionManager;
import java.time.LocalDateTime;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;

public class ExitController {
    private static final Logger log = LoggerFactory.getLogger(ExitController.class);

    @FXML private TableView<ExitAuthorization> exitTable;
    @FXML private TableColumn<ExitAuthorization, String> colDeceasedName;
    @FXML private TableColumn<ExitAuthorization, String> colTransport;
    @FXML private TableColumn<ExitAuthorization, String> colStatus;
    @FXML private TableColumn<ExitAuthorization, String> colDate;
    @FXML private TableColumn<ExitAuthorization, String> colSignedBy;
    @FXML private TableColumn<ExitAuthorization, String> colDocuments;

    @FXML private ComboBox<Deceased> deceasedCombo;
    @FXML private TextField transportField;
    @FXML private TextField authorizedPersonField;
    @FXML private TextArea notesArea;
    @FXML private Button approveButton;
    @FXML private Button confirmExitButton;
    @FXML private Button createButton;
    @FXML private TextField signatureField;
    @FXML private CheckBox chkCertificateDeath;
    @FXML private CheckBox chkAuthorizationForm;
    @FXML private CheckBox chkIdentityVerified;
    @FXML private TextArea verificationNotes;

    private ExitService exitService;
    private DeceasedService deceasedService;
    private ObservableList<ExitAuthorization> exits;
    private ExitAuthorization selected;
    private int currentPage = 0;
    private boolean hasMore = true;
    private static final int PAGE_SIZE = 50;

    @FXML
    public void initialize() {
        boolean canApprove = SecurityUtil.isAdmin() || SecurityUtil.hasRole("MEDECIN");
        approveButton.setDisable(!canApprove);

        exitService = new ExitService();
        deceasedService = new DeceasedService();
        exits = FXCollections.observableArrayList();

        colDeceasedName.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDeceased().getFullName()));
        colTransport.setCellValueFactory(new PropertyValueFactory<>("transportCompany"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDate.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAuthorizedAt().toLocalDate().toString()));
        colSignedBy.setCellValueFactory(new PropertyValueFactory<>("signedBy"));
        colDocuments.setCellValueFactory(cell -> {
            ExitAuthorization e = cell.getValue();
            boolean allOk = e.isCertificateOfDeathVerified() && e.isAuthorizationFormVerified() && e.isIdentityVerified();
            return new SimpleStringProperty(allOk ? "Oui" : "Non");
        });

        exitTable.setItems(exits);
        exitTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> selectExit(n));
        setupScrollListener();
        refresh();
    }

    private void setupScrollListener() {
        exitTable.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar scrollBar = (ScrollBar) exitTable.lookup(".scroll-bar:vertical");
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
    private void handleCreate() {
        Deceased dec = deceasedCombo.getValue();
        String transport = transportField.getText().trim();
        if (dec == null || transport.isEmpty()) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("exit.validation.required"));
            return;
        }
        try {
            exitService.createAuthorization(dec, SessionManager.getInstance().getCurrentUser(),
                    transport, authorizedPersonField.getText().trim(), notesArea.getText());
            NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("exit.create.success"));
            clearForm();
            refresh();
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
        }
    }

    @FXML
    private void handleApprove() {
        try { SecurityUtil.requireRole("ADMIN", "MEDECIN"); } catch (SecurityException e) { NotificationUtil.showWarning(I18nUtil.t("access.denied"), e.getMessage()); return; }
        if (selected == null) return;
        String signature = signatureField.getText().trim();
        if (signature.isEmpty()) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("exit.signature.required"));
            return;
        }
        if (!chkCertificateDeath.isSelected() || !chkAuthorizationForm.isSelected() || !chkIdentityVerified.isSelected()) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("exit.documents.all.required"));
            return;
        }
        try {
            selected.setSignedBy(signature);
            selected.setSignedAt(LocalDateTime.now());
            selected.setCertificateOfDeathVerified(chkCertificateDeath.isSelected());
            selected.setAuthorizationFormVerified(chkAuthorizationForm.isSelected());
            selected.setIdentityVerified(chkIdentityVerified.isSelected());
            selected.setVerificationNotes(verificationNotes.getText());
            exitService.approve(selected);
            NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("exit.approve.success"));
            refresh();
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
        }
    }

    @FXML
    private void handleConfirmExit() {
        if (selected == null) return;
        boolean confirm = NotificationUtil.showConfirm(I18nUtil.t("confirm.title"),
                I18nUtil.t("exit.confirm.message", selected.getDeceased().getFullName()));
        if (confirm) {
            try {
                exitService.confirmExit(selected);
                NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("exit.confirm.success"));
                refresh();
            } catch (Exception e) {
                NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
            }
        }
    }

    private void selectExit(ExitAuthorization e) {
        selected = e;
        if (e != null) {
            boolean isPending = "PENDING".equals(e.getStatus());
            boolean isApproved = "APPROUVEE".equals(e.getStatus());
            approveButton.setDisable(!isPending || !(SecurityUtil.isAdmin() || SecurityUtil.hasRole("MEDECIN")));
            confirmExitButton.setDisable(!isApproved);
            signatureField.setText(e.getSignedBy() != null ? e.getSignedBy() : "");
            chkCertificateDeath.setSelected(e.isCertificateOfDeathVerified());
            chkAuthorizationForm.setSelected(e.isAuthorizationFormVerified());
            chkIdentityVerified.setSelected(e.isIdentityVerified());
            verificationNotes.setText(e.getVerificationNotes() != null ? e.getVerificationNotes() : "");
        } else {
            approveButton.setDisable(true);
            confirmExitButton.setDisable(true);
            signatureField.clear();
            chkCertificateDeath.setSelected(false);
            chkAuthorizationForm.setSelected(false);
            chkIdentityVerified.setSelected(false);
            verificationNotes.clear();
        }
    }

    private void clearForm() {
        deceasedCombo.setValue(null);
        transportField.clear();
        authorizedPersonField.clear();
        notesArea.clear();
        signatureField.clear();
        chkCertificateDeath.setSelected(false);
        chkAuthorizationForm.setSelected(false);
        chkIdentityVerified.setSelected(false);
        verificationNotes.clear();
    }

    private void loadNextPage() {
        currentPage++;
        PaginatedResult<ExitAuthorization> result = exitService.findPaginated(currentPage, PAGE_SIZE);
        hasMore = result.hasNext();
        exits.addAll(result.getResults());
    }

    private void loadPage(int page) {
        currentPage = page;
        PaginatedResult<ExitAuthorization> result = exitService.findPaginated(page, PAGE_SIZE);
        hasMore = result.hasNext();
        exits.setAll(result.getResults());
    }

    private void refresh() {
        deceasedCombo.setItems(FXCollections.observableArrayList(deceasedService.getRecentDeceased(50)));
        loadPage(0);
    }
}
