package com.gestionmorgue.controller;

import com.gestionmorgue.dao.StorageLocationDao;
import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.StorageAssignment;
import com.gestionmorgue.model.StorageLocation;
import com.gestionmorgue.service.DeceasedService;
import com.gestionmorgue.service.LabelService;
import com.gestionmorgue.service.StorageService;
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
import javafx.scene.input.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javafx.print.PrinterJob;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class StorageController {
    private static final Logger log = LoggerFactory.getLogger(StorageController.class);

    @FXML private TableView<StorageLocation> locationTable;
    @FXML private TableColumn<StorageLocation, String> colCode;
    @FXML private TableColumn<StorageLocation, String> colLabel;
    @FXML private TableColumn<StorageLocation, String> colZone;
    @FXML private TableColumn<StorageLocation, String> colStatus;

    @FXML private TableView<StorageAssignment> assignmentTable;
    @FXML private TableColumn<StorageAssignment, String> colAssignedDeceased;
    @FXML private TableColumn<StorageAssignment, String> colAssignedLocation;
    @FXML private TableColumn<StorageAssignment, String> colAssignedAt;
    @FXML private TableColumn<StorageAssignment, String> colReleasedAt;

    @FXML private TableView<StorageAssignment> historyTable;
    @FXML private TableColumn<StorageAssignment, String> colHistoryDeceased;
    @FXML private TableColumn<StorageAssignment, String> colHistoryLocation;
    @FXML private TableColumn<StorageAssignment, String> colHistoryAssignedAt;
    @FXML private TableColumn<StorageAssignment, String> colHistoryReleasedAt;
    @FXML private TableColumn<StorageAssignment, String> colHistoryReleasedBy;
    @FXML private Button btnRefreshHistory;
    @FXML private Label historyTitle;

    @FXML private ComboBox<StorageLocation> locationCombo;
    @FXML private ComboBox<Deceased> deceasedCombo;
    @FXML private ListView<Deceased> unassignedList;
    @FXML private ComboBox<String> filterZoneCombo;
    @FXML private CheckBox filterOccupiedCheck;

    private StorageService storageService;
    private DeceasedService deceasedService;
    private StorageLocationDao locationDao;
    private ObservableList<StorageLocation> locations;
    private List<StorageLocation> allLocations;
    private int currentPage = 0;
    private boolean hasMore = true;
    private static final int PAGE_SIZE = 50;

    @FXML
    public void initialize() {
        storageService = new StorageService();
        deceasedService = new DeceasedService();
        locationDao = new StorageLocationDao();
        locations = FXCollections.observableArrayList();

        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colLabel.setCellValueFactory(new PropertyValueFactory<>("label"));
        colZone.setCellValueFactory(new PropertyValueFactory<>("zone"));
        colStatus.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().isOccupied() ? I18nUtil.t("storage.status.occupied") : I18nUtil.t("storage.status.free")));

        colAssignedDeceased.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDeceased().getFullName()));
        colAssignedLocation.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getLocation().getCode()));
        colAssignedAt.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAssignedAt().toString()));
        colReleasedAt.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getReleasedAt() != null
                        ? cell.getValue().getReleasedAt().toString() : ""));

        colHistoryDeceased.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDeceased().getFullName()));
        colHistoryLocation.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getLocation().getCode()));
        colHistoryAssignedAt.setCellValueFactory(cell ->
                new SimpleStringProperty(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(cell.getValue().getAssignedAt())));
        colHistoryReleasedAt.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getReleasedAt() != null
                        ? DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(cell.getValue().getReleasedAt()) : ""));
        colHistoryReleasedBy.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getReleasedBy() != null ? cell.getValue().getReleasedBy() : ""));
        historyTitle.setText(I18nUtil.t("storage.history.title"));
        colHistoryDeceased.setText(I18nUtil.t("storage.history.col.deceased"));
        colHistoryLocation.setText(I18nUtil.t("storage.history.col.location"));
        colHistoryAssignedAt.setText(I18nUtil.t("storage.history.col.assignedAt"));
        colHistoryReleasedAt.setText(I18nUtil.t("storage.history.col.releasedAt"));
        colHistoryReleasedBy.setText(I18nUtil.t("storage.history.col.releasedBy"));
        btnRefreshHistory.setText(I18nUtil.t("storage.history.refresh"));
        btnRefreshHistory.setOnAction(e -> loadHistory());

        locationTable.setItems(locations);
        setupDragDrop();
        setupScrollListener();
        refresh();
        loadHistory();
    }

    private void setupScrollListener() {
        locationTable.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar scrollBar = (ScrollBar) locationTable.lookup(".scroll-bar:vertical");
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

    private void setupDragDrop() {
        unassignedList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Deceased d, boolean empty) {
                super.updateItem(d, empty);
                setText(d == null ? null : d.getFullName() + " (" + d.getDossierNumber() + ")");
            }
        });
        unassignedList.setOnDragDetected(e -> {
            Deceased sel = unassignedList.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            Dragboard db = unassignedList.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.putString(sel.getId().toString());
            db.setContent(cc);
            e.consume();
        });
        locationTable.setOnDragOver(e -> {
            if (e.getGestureSource() != locationTable && e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });
        locationTable.setOnDragDropped(e -> {
            try { SecurityUtil.requireRole("ADMIN", "MEDECIN", "THANATOPRACTEUR"); } catch (SecurityException ex) { NotificationUtil.showWarning(I18nUtil.t("access.denied"), ex.getMessage()); e.setDropCompleted(false); e.consume(); return; }
            Dragboard db = e.getDragboard();
            if (!db.hasString()) return;
            StorageLocation target = locationTable.getSelectionModel().getSelectedItem();
            if (target == null) return;
            long deceasedId = Long.parseLong(db.getString());
            Deceased dec = deceasedService.findById(deceasedId);
            if (dec == null || target.isOccupied()) return;
            try {
                storageService.assignLocation(dec, target);
                NotificationUtil.showInfo(I18nUtil.t("info.title"), dec.getFullName() + " -> " + target.getCode());
                refresh();
            } catch (Exception ex) {
                NotificationUtil.showError(I18nUtil.t("error.title"), ex.getMessage());
            }
            e.setDropCompleted(true);
            e.consume();
        });
    }

    @FXML
    private void handleAssign() {
        try { SecurityUtil.requireRole("ADMIN", "MEDECIN", "THANATOPRACTEUR"); } catch (SecurityException e) { NotificationUtil.showWarning(I18nUtil.t("access.denied"), e.getMessage()); return; }
        StorageLocation loc = locationCombo.getValue();
        Deceased dec = deceasedCombo.getValue();
        if (loc == null || dec == null) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("storage.assign.required"));
            return;
        }
        try {
            storageService.assignLocation(dec, loc);
            NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("storage.assign.success"));
            refresh();
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
        }
    }

    @FXML
    private void handleRelease() {
        try { SecurityUtil.requireRole("ADMIN", "MEDECIN", "THANATOPRACTEUR"); } catch (SecurityException e) { NotificationUtil.showWarning(I18nUtil.t("access.denied"), e.getMessage()); return; }
        StorageAssignment sel = assignmentTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("storage.release.required"));
            return;
        }
        boolean confirm = NotificationUtil.showConfirm(I18nUtil.t("confirm.title"),
                I18nUtil.t("storage.release.confirm", sel.getLocation().getCode()));
        if (confirm) {
            try {
                storageService.releaseLocation(sel, SessionManager.getInstance().getCurrentUser().getFullName());
                NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("storage.release.success"));
                refresh();
            } catch (Exception e) {
                NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
            }
        }
    }

    @FXML
    private void handlePrintLabels() {
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

    @FXML
    private void handlePrint() {
        try { SecurityUtil.requireRole("ADMIN", "MEDECIN", "THANATOPRACTEUR"); } catch (SecurityException e) { NotificationUtil.showWarning(I18nUtil.t("access.denied"), e.getMessage()); return; }
        var items = locationTable.getItems();
        if (items.isEmpty()) { NotificationUtil.showWarning(I18nUtil.t("warning.title"), "Aucun emplacement à imprimer"); return; }
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) { NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("reports.noprinter")); return; }
        if (!job.showPrintDialog(locationTable.getScene().getWindow())) return;
        VBox printNode = new VBox(6);
        printNode.setStyle("-fx-padding: 20; -fx-font-family: 'Segoe UI'; -fx-font-size: 11;");
        Label title = new Label("Emplacements de stockage");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        printNode.getChildren().add(title);
        for (var loc : items) {
            String status = loc.isOccupied() ? "Occupé" : "Libre";
            Label l = new Label(loc.getCode() + " - " + loc.getLabel() + " (" + loc.getZone() + ") [" + status + "]");
            l.setStyle("-fx-padding: 2 0;");
            printNode.getChildren().add(l);
        }
        if (job.printPage(printNode)) job.endJob();
    }

    @FXML
    private void handleFilter() {
        applyFilter();
    }

    @FXML
    private void handleClearFilter() {
        filterZoneCombo.setValue(null);
        filterOccupiedCheck.setSelected(false);
        applyFilter();
    }

    private void applyFilter() {
        String zone = filterZoneCombo.getValue();
        boolean onlyOccupied = filterOccupiedCheck.isSelected();
        if (zone == null && !onlyOccupied) {
            loadPage(0);
            return;
        }
        locations.setAll(allLocations.stream()
                .filter(l -> zone == null || zone.isEmpty() || zone.equals(l.getZone()))
                .filter(l -> !onlyOccupied || l.isOccupied())
                .collect(Collectors.toList()));
    }

    private void loadNextPage() {
        currentPage++;
        PaginatedResult<StorageLocation> result = locationDao.findPaginatedWithAssignments(currentPage, PAGE_SIZE);
        hasMore = result.hasNext();
        locations.addAll(result.getResults());
    }

    private void loadPage(int page) {
        currentPage = page;
        PaginatedResult<StorageLocation> result = locationDao.findPaginatedWithAssignments(page, PAGE_SIZE);
        hasMore = result.hasNext();
        locations.setAll(result.getResults());
    }

    private void refresh() {
        loadPage(0);
        try (var session = com.gestionmorgue.util.DatabaseManager.getSessionFactory().openSession()) {
            List<StorageLocation> allLocs = session.createQuery(
                    "select l from StorageLocation l left join fetch l.assignments a where a.releasedAt is null",
                    StorageLocation.class).list();
            List<StorageLocation> availableLocs = allLocs.stream()
                    .filter(l -> !l.isOccupied()).collect(Collectors.toList());
            locationCombo.setItems(FXCollections.observableArrayList(availableLocs));

            List<Long> deceasedWithActiveExit = session.createQuery(
                    "select distinct e.deceased.id from ExitAuthorization e where e.status = 'SORTIE_EFFECTUEE'",
                    Long.class).list();
            List<Long> assignedDeceasedIds = allLocs.stream()
                    .filter(StorageLocation::isOccupied)
                    .flatMap(l -> l.getAssignments().stream())
                    .filter(a -> a.getReleasedAt() == null)
                    .map(a -> a.getDeceased().getId())
                    .collect(Collectors.toList());

            List<Deceased> allDeceased = session.createQuery(
                    "from Deceased", Deceased.class).list();
            deceasedCombo.getItems().clear();
            allDeceased.stream()
                    .filter(d -> !deceasedWithActiveExit.contains(d.getId()))
                    .limit(50)
                    .forEach(d -> deceasedCombo.getItems().add(d));

            List<Deceased> unassigned = allDeceased.stream()
                    .filter(d -> !assignedDeceasedIds.contains(d.getId()))
                    .filter(d -> !deceasedWithActiveExit.contains(d.getId()))
                    .collect(Collectors.toList());
            unassignedList.setItems(FXCollections.observableArrayList(unassigned));

            allLocations = allLocs;
            filterZoneCombo.setItems(FXCollections.observableArrayList(
                    allLocs.stream().map(StorageLocation::getZone).distinct().sorted().collect(Collectors.toList())));

            assignmentTable.getItems().clear();
            allLocs.stream()
                    .filter(StorageLocation::isOccupied)
                    .flatMap(l -> l.getAssignments().stream())
                    .filter(a -> a.getReleasedAt() == null)
                    .forEach(a -> assignmentTable.getItems().add(a));
        }
    }

    private void loadHistory() {
        historyTable.setItems(FXCollections.observableArrayList(storageService.getAllAssignments()));
    }
}
