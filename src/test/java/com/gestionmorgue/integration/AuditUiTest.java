package com.gestionmorgue.integration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuditUiTest extends TestFxBase {

    private Label logCountLabel;
    private TextField filterUserField;
    private TextField filterActionField;
    private ComboBox<String> filterEntityType;
    private Pagination pagination;
    private TableView<?> logTable;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/audit.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void findFields() {
        logCountLabel = lookup("#logCountLabel").query();
        filterUserField = lookup("#filterUserField").query();
        filterActionField = lookup("#filterActionField").query();
        filterEntityType = lookup("#filterEntityType").query();
        pagination = lookup("#pagination").query();
        logTable = lookup("#logTable").query();
    }

    @Test
    void testAuditFieldsExist() {
        assertNotNull(logCountLabel);
        assertNotNull(filterUserField);
        assertNotNull(filterActionField);
        assertNotNull(filterEntityType);
        assertNotNull(pagination);
        assertNotNull(logTable);
    }

    @Test
    void testAuditFieldsVisible() {
        assertTrue(logCountLabel.isVisible());
        assertTrue(filterUserField.isVisible());
        assertTrue(filterActionField.isVisible());
        assertTrue(filterEntityType.isVisible());
        assertTrue(pagination.isVisible());
        assertTrue(logTable.isVisible());
    }

    @Test
    void testTableColumnsExist() {
        assertNotNull(lookup("#colTimestamp").query());
        assertNotNull(lookup("#colUser").query());
        assertNotNull(lookup("#colAction").query());
        assertNotNull(lookup("#colDetails").query());
        assertNotNull(lookup("#colEntity").query());
    }

    @Test
    void testFilterEntityTypeHasItems() {
        assertFalse(filterEntityType.getItems().isEmpty());
    }

    @Test
    void testLogCountLabelExists() {
        assertNotNull(logCountLabel.getText());
    }
}
