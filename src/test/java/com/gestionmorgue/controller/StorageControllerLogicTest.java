package com.gestionmorgue.controller;

import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.StorageAssignment;
import com.gestionmorgue.model.StorageLocation;
import com.gestionmorgue.service.DeceasedService;
import com.gestionmorgue.service.StorageService;
import com.gestionmorgue.util.DataInitializer;
import com.gestionmorgue.util.DatabaseManager;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class StorageControllerLogicTest {
    private static StorageService storageService;
    private static DeceasedService deceasedService;
    private static String uniqueId;

    @BeforeAll
    static void setup() {
        String dbUrl = "jdbc:h2:mem:test" + StorageControllerLogicTest.class.getSimpleName() + ";DB_CLOSE_DELAY=-1";
        System.setProperty("test.db.url", dbUrl);
        DataInitializer.initialize();
        storageService = new StorageService();
        deceasedService = new DeceasedService();
        uniqueId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    @AfterAll
    static void cleanup() {
        DatabaseManager.shutdown();
    }

    @Test
    void testCreateLocationAndAssignDeceased() {
        Deceased deceased = deceasedService.createDeceased(
                "STORE_" + uniqueId, "CreateAssign", null, null, null, null);
        StorageLocation loc = storageService.createLocation(
                "CTRL-" + uniqueId, "Controller Test", "Z", 4);
        StorageAssignment assignment = storageService.assignLocation(deceased, loc);
        assertNotNull(assignment);
        assertNotNull(assignment.getId());
        assertEquals(deceased.getId(), assignment.getDeceased().getId());
        assertEquals(loc.getId(), assignment.getLocation().getId());
        assertTrue(loc.isOccupied());
    }

    @Test
    void testAssignOccupiedLocationThrows() {
        Deceased deceased = deceasedService.createDeceased(
                "STORE2_" + uniqueId, "OccupiedTest1", null, null, null, null);
        StorageLocation loc = storageService.createLocation(
                "CTRL-OCC-" + uniqueId, "Occupied Test", "Z", 4);
        storageService.assignLocation(deceased, loc);
        Deceased another = deceasedService.createDeceased(
                "STORE3_" + uniqueId, "OccupiedTest2", null, null, null, null);
        Exception ex = assertThrows(RuntimeException.class, () ->
                storageService.assignLocation(another, loc));
        assertEquals("Emplacement déjà occupé", ex.getMessage());
    }

    @Test
    void testReleaseLocation() {
        Deceased deceased = deceasedService.createDeceased(
                "STORE4_" + uniqueId, "ReleaseTest", null, null, null, null);
        StorageLocation loc = storageService.createLocation(
                "CTRL-REL-" + uniqueId, "Release Test", "Z", 4);
        StorageAssignment assignment = storageService.assignLocation(deceased, loc);
        assertTrue(loc.isOccupied());
        assertNull(assignment.getReleasedAt());
        storageService.releaseLocation(assignment, "Test User");
        assertNotNull(assignment.getReleasedAt());
        assertEquals("Test User", assignment.getReleasedBy());
    }

    @Test
    void testReleaseMakesLocationAvailable() {
        Deceased deceased = deceasedService.createDeceased(
                "STORE5_" + uniqueId, "AvailTest", null, null, null, null);
        StorageLocation loc = storageService.createLocation(
                "CTRL-AVL-" + uniqueId, "Avail Test", "Z", 4);
        StorageAssignment assignment = storageService.assignLocation(deceased, loc);
        assertTrue(loc.isOccupied());
        storageService.releaseLocation(assignment, "Test User");
        boolean isAvailable = storageService.getAvailableLocations().stream()
                .anyMatch(l -> l.getId().equals(loc.getId()));
        assertTrue(isAvailable);
    }

    @Test
    void testCountOccupiedAndFree() {
        long initialOccupied = storageService.getOccupiedCount();
        long initialTotal = storageService.getTotalCount();
        Deceased deceased = deceasedService.createDeceased(
                "STORE6_" + uniqueId, "CountTest", null, null, null, null);
        StorageLocation loc = storageService.createLocation(
                "CTRL-CNT-" + uniqueId, "Count Test", "Z", 4);
        storageService.assignLocation(deceased, loc);
        assertEquals(initialOccupied + 1, storageService.getOccupiedCount());
        assertEquals(initialTotal + 1, storageService.getTotalCount());
        long free = storageService.getTotalCount() - storageService.getOccupiedCount();
        assertTrue(free >= 0);
    }
}
