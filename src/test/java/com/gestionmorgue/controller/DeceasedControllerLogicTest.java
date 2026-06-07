package com.gestionmorgue.controller;

import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.service.DeceasedService;
import com.gestionmorgue.util.DataInitializer;
import com.gestionmorgue.util.DatabaseManager;
import com.gestionmorgue.util.ValidationUtil;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeceasedControllerLogicTest {
    private static DeceasedService deceasedService;
    private static String uniqueId;

    @BeforeAll
    static void setup() {
        String dbUrl = "jdbc:h2:mem:test" + DeceasedControllerLogicTest.class.getSimpleName() + ";DB_CLOSE_DELAY=-1";
        System.setProperty("test.db.url", dbUrl);
        DataInitializer.initialize();
        deceasedService = new DeceasedService();
        uniqueId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    @AfterAll
    static void cleanup() {
        DatabaseManager.shutdown();
    }

    @Test
    void testSearchExternalWithValidQuery() {
        String lastName = "SEARCH_EXT_" + uniqueId;
        deceasedService.createDeceased(lastName, "TestSearch", "1960-01-01", "2024-06-15", "Paris", "MASCULIN");
        List<Deceased> results = deceasedService.search(lastName, null, null);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(d -> lastName.equals(d.getLastName())));
    }

    @Test
    void testSearchExternalWithEmptyQueryReturnsAll() {
        List<Deceased> results = deceasedService.search("", null, null);
        assertFalse(results.isEmpty());
    }

    @Test
    void testSearchExternalWithNullQuery() {
        List<Deceased> results = deceasedService.search(null, null, null);
        assertFalse(results.isEmpty());
    }

    @Test
    void testValidateNirWithValidNir() {
        assertTrue(ValidationUtil.isValidNir("1 85 05 45 123 456 75"));
        assertTrue(ValidationUtil.isValidNir("185054512345675"));
    }

    @Test
    void testValidateNirWithInvalidNir() {
        assertFalse(ValidationUtil.isValidNir("123"));
        assertFalse(ValidationUtil.isValidNir("ABCDEFGHIJKLM"));
        assertFalse(ValidationUtil.isValidNir("9999999999999"));
    }

    @Test
    void testValidateNirWithNull() {
        assertTrue(ValidationUtil.isValidNir(null));
    }

    @Test
    void testValidateNirWithEmptyString() {
        assertTrue(ValidationUtil.isValidNir(""));
    }
}
