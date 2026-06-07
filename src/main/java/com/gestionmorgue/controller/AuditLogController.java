package com.gestionmorgue.controller;

import com.gestionmorgue.dao.AuditLogDao;
import com.gestionmorgue.model.AuditLog;
import com.gestionmorgue.service.AuditService;
import com.gestionmorgue.util.I18nUtil;
import com.gestionmorgue.util.NotificationUtil;
import com.gestionmorgue.util.PaginatedResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLogController {
    private static final Logger log = LoggerFactory.getLogger(AuditLogController.class);

    @FXML private TableView<AuditLog> logTable;
    @FXML private TableColumn<AuditLog, String> colTimestamp;
    @FXML private TableColumn<AuditLog, String> colUser;
    @FXML private TableColumn<AuditLog, String> colAction;
    @FXML private TableColumn<AuditLog, String> colDetails;
    @FXML private TableColumn<AuditLog, String> colEntity;
    @FXML private Label logCountLabel;
    @FXML private TextField filterUserField;
    @FXML private TextField filterActionField;
    @FXML private ComboBox<String> filterEntityType;
    @FXML private Pagination pagination;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;

    private AuditService auditService;
    private AuditLogDao auditLogDao;
    private ObservableList<AuditLog> logs;
    private List<AuditLog> allLogs;
    private int currentPage = 0;
    private boolean hasMore = true;
    private static final int PAGE_SIZE = 100;

    @FXML
    public void initialize() {
        auditService = new AuditService();
        auditLogDao = new AuditLogDao();
        logs = FXCollections.observableArrayList();
        filterEntityType.setItems(FXCollections.observableArrayList("", "Deceased", "User", "Intervention", "ExitAuthorization", "StorageLocation"));
        filterEntityType.setValue("");

        dateFrom.setPromptText(I18nUtil.t("audit.filter.from"));
        dateTo.setPromptText(I18nUtil.t("audit.filter.to"));
        dateFrom.valueProperty().addListener((obs, old, val) -> loadPage(0));
        dateTo.valueProperty().addListener((obs, old, val) -> loadPage(0));

        colTimestamp.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getTimestamp().toString()));
        colUser.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getUser() != null
                        ? cell.getValue().getUser().getUsername() : ""));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colDetails.setCellValueFactory(new PropertyValueFactory<>("details"));
        colEntity.setCellValueFactory(cell -> {
            String type = cell.getValue().getEntityType();
            Long id = cell.getValue().getEntityId();
            return new SimpleStringProperty(type != null ? type + "#" + id : "");
        });

        logTable.setItems(logs);
        pagination.currentPageIndexProperty().addListener((obs, old, idx) -> applyFiltersToPage(idx.intValue()));
        setupScrollListener();

        loadPage(0);
    }

    private void setupScrollListener() {
        logTable.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar scrollBar = (ScrollBar) logTable.lookup(".scroll-bar:vertical");
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

    private void loadAllLogs() {
        allLogs = auditService.getRecentLogs(5000);
        applyFiltersToPage(0);
    }

    @FXML
    private void handleRefresh() {
        loadPage(0);
    }

    @FXML
    private void handleFilter() {
        applyFiltersToPage(0);
    }

    @FXML
    private void handleClearFilters() {
        filterUserField.clear();
        filterActionField.clear();
        filterEntityType.setValue("");
        dateFrom.setValue(null);
        dateTo.setValue(null);
        loadPage(0);
    }

    @FXML
    private void handleExportCsv() {
        try {
            File file = new File(System.getProperty("user.home") + "/Desktop/journal-audit.csv");
            try (FileWriter w = new FileWriter(file, StandardCharsets.UTF_8)) {
                w.write(I18nUtil.t("audit.csv.header") + "\n");
                for (AuditLog log : allLogs) {
                    w.write(String.join(";",
                            log.getTimestamp().toString(),
                            log.getUser() != null ? log.getUser().getUsername() : "",
                            log.getAction(),
                            log.getEntityType() != null ? log.getEntityType() : "",
                            log.getEntityId() != null ? log.getEntityId().toString() : "",
                            log.getDetails() != null ? log.getDetails().replace("\n", " ") : ""
                    ) + "\n");
                }
            }
            NotificationUtil.showInfo(I18nUtil.t("info.title"), "Journal exporté sur le Bureau (" + allLogs.size() + " entrées)");
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
        }
    }

    private void loadNextPage() {
        currentPage++;
        PaginatedResult<AuditLog> result = auditLogDao.findPaginatedWithUser(currentPage, PAGE_SIZE);
        hasMore = result.hasNext();
        logs.addAll(result.getResults());
    }

    private void loadPage(int page) {
        currentPage = page;
        LocalDate fromDate = dateFrom.getValue();
        LocalDate toDate = dateTo.getValue();
        boolean hasDateFilter = fromDate != null || toDate != null;
        boolean hasTextFilters = !filterUserField.getText().trim().isEmpty()
                || !filterActionField.getText().trim().isEmpty()
                || (filterEntityType.getValue() != null && !filterEntityType.getValue().isEmpty());

        if (hasDateFilter) {
            LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
            LocalDateTime to = toDate != null ? toDate.atTime(LocalTime.MAX) : LocalDateTime.of(2099, 12, 31, 23, 59, 59);
            PaginatedResult<AuditLog> result = auditLogDao.findPaginatedByDateRange(from, to, page, PAGE_SIZE);
            hasMore = result.hasNext();
            logs.setAll(result.getResults());
            pagination.setPageCount(result.getTotalPages());
            pagination.setCurrentPageIndex(Math.min(page, result.getTotalPages() - 1));
            logCountLabel.setText(I18nUtil.t("deceased.list.results", result.getTotalCount())
                    + " - Page " + (result.getPage() + 1) + "/" + result.getTotalPages());
            return;
        }

        if (!hasTextFilters) {
            PaginatedResult<AuditLog> result = auditLogDao.findPaginatedWithUser(page, PAGE_SIZE);
            hasMore = result.hasNext();
            logs.setAll(result.getResults());
            pagination.setPageCount(result.getTotalPages());
            pagination.setCurrentPageIndex(Math.min(page, result.getTotalPages() - 1));
            logCountLabel.setText(I18nUtil.t("deceased.list.results", result.getTotalCount())
                    + " - Page " + (result.getPage() + 1) + "/" + result.getTotalPages());
            return;
        }
        applyFiltersInMemory(page);
    }

    private void applyFiltersInMemory(int page) {
        String userFilter = filterUserField.getText().trim().toLowerCase();
        String actionFilter = filterActionField.getText().trim().toLowerCase();
        String entityFilter = filterEntityType.getValue();

        if (allLogs == null) {
            allLogs = auditService.getRecentLogs(5000);
        }

        List<AuditLog> filtered = allLogs.stream()
                .filter(l -> userFilter.isEmpty()
                        || (l.getUser() != null && l.getUser().getUsername().toLowerCase().contains(userFilter)))
                .filter(l -> actionFilter.isEmpty()
                        || l.getAction().toLowerCase().contains(actionFilter))
                .filter(l -> entityFilter == null || entityFilter.isEmpty()
                        || (l.getEntityType() != null && l.getEntityType().equals(entityFilter)))
                .collect(Collectors.toList());

        long total = filtered.size();
        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        pagination.setPageCount(Math.max(totalPages, 1));
        pagination.setCurrentPageIndex(Math.min(page, totalPages - 1));

        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filtered.size());
        logs.setAll(from < filtered.size() ? filtered.subList(from, to) : List.of());

        logCountLabel.setText(I18nUtil.t("deceased.list.results", filtered.size()) + " - Page " + (page + 1) + "/" + Math.max(totalPages, 1));
    }

    private void applyFiltersToPage(int page) {
        loadPage(page);
    }
}
