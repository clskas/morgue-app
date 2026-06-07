package com.gestionmorgue.service;

import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.StorageAssignment;
import com.gestionmorgue.model.StorageLocation;
import com.gestionmorgue.util.DataInitializer;
import com.gestionmorgue.util.DatabaseManager;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StorageServiceTest {
    private static StorageService storageService;
    private static DeceasedService deceasedService;
    private static String testZone;

    @BeforeAll
    static void setup() {
        System.setProperty("test.db.url", "jdbc:h2:mem:testStorage;DB_CLOSE_DELAY=-1");
        DatabaseManager.shutdown();
        DataInitializer.initialize();
        storageService = new StorageService();
        deceasedService = new DeceasedService();
        testZone = "Z" + System.currentTimeMillis();
    }

    @AfterAll
    static void teardown() {
        DatabaseManager.shutdown();
    }

    @Test
    @Order(1)
    void testCreateLocation() {
        StorageLocation loc = storageService.createLocation(testZone + "-01", "Test Zone",
                testZone, 4);
        assertNotNull(loc);
        assertNotNull(loc.getId());
        assertFalse(loc.isOccupied());
    }

    @Test
    @Order(2)
    void testGetAvailableLocations() {
        List<StorageLocation> available = storageService.getAvailableLocations();
        assertFalse(available.isEmpty());
    }

    @Test
    @Order(3)
    void testAssignLocation() {
        Deceased deceased = deceasedService.createDeceased(
                "TEST", "Storage", null, null, null, null);
        StorageLocation loc = storageService.createLocation(testZone + "-02", "Assign Test",
                testZone, 4);
        StorageAssignment assignment = storageService.assignLocation(deceased, loc);
        assertNotNull(assignment);
        assertNotNull(assignment.getId());
        assertEquals(deceased.getId(), assignment.getDeceased().getId());
    }

    @Test
    @Order(4)
    void testAssignOccupiedLocationThrows() {
        Deceased deceased = deceasedService.createDeceased(
                "OCCUPIED", "Test", null, null, null, null);
        StorageLocation loc = storageService.createLocation(testZone + "-03", "Occupied Test",
                testZone, 4);
        storageService.assignLocation(deceased, loc);
        Deceased another = deceasedService.createDeceased(
                "OCCUPIED2", "Test", null, null, null, null);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            storageService.assignLocation(another, loc);
        });
        assertEquals("Emplacement déjà occupé", exception.getMessage());
    }

    @Test
    @Order(5)
    void testReleaseLocation() {
        Deceased deceased = deceasedService.createDeceased(
                "RELEASE", "Test", null, null, null, null);
        StorageLocation loc = storageService.createLocation(testZone + "-04", "Release Test",
                testZone, 4);
        StorageAssignment assignment = storageService.assignLocation(deceased, loc);
        assertTrue(loc.isOccupied());

        storageService.releaseLocation(assignment, "Test User");
        assertNotNull(assignment.getReleasedAt());
        assertTrue(storageService.getAvailableLocations().stream()
                .anyMatch(l -> l.getId().equals(loc.getId())));
    }

    @Test
    @Order(6)
    void testCounts() {
        assertTrue(storageService.getTotalCount() >= 3);
    }
}
