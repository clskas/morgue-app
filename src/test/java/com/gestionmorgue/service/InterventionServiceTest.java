package com.gestionmorgue.service;

import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.Intervention;
import com.gestionmorgue.model.User;
import com.gestionmorgue.util.DataInitializer;
import com.gestionmorgue.util.DatabaseManager;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InterventionServiceTest {
    private static InterventionService interventionService;
    private static AuthService authService;
    private static DeceasedService deceasedService;
    private static User testUser;
    private static Deceased testDeceased;
    private static String uniqueId;

    @BeforeAll
    static void setup() {
        System.setProperty("test.db.url", "jdbc:h2:mem:testIntervention;DB_CLOSE_DELAY=-1");
        DatabaseManager.shutdown();
        DataInitializer.initialize();
        interventionService = new InterventionService();
        authService = new AuthService();
        deceasedService = new DeceasedService();
        uniqueId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        testUser = authService.createUser("int_test_" + uniqueId, "pass", "Int Test", "MEDECIN",
                "int_" + uniqueId + "@test.com");
        testDeceased = deceasedService.createDeceased("INT", uniqueId, "1960-01-01", "2024-07-01",
                "Paris", "MASCULIN");
    }

    @AfterAll
    static void teardown() {
        DatabaseManager.shutdown();
    }

    @Test
    @Order(1)
    void testSchedule() {
        Intervention i = interventionService.schedule(testDeceased, testUser, "AUTOPSIE",
                LocalDateTime.now().plusDays(1));
        assertNotNull(i);
        assertNotNull(i.getId());
        assertEquals("PLANIFIEE", i.getStatus());
        assertEquals("AUTOPSIE", i.getType());
        assertEquals(testDeceased.getId(), i.getDeceased().getId());
        assertEquals(testUser.getId(), i.getPerformer().getId());
    }

    @Test
    @Order(2)
    void testGetPendingInterventions() {
        List<Intervention> pending = interventionService.getPendingInterventions();
        assertNotNull(pending);
        assertTrue(pending.stream().anyMatch(i -> "PLANIFIEE".equals(i.getStatus())));
    }

    @Test
    @Order(3)
    void testComplete() {
        Intervention i = interventionService.schedule(testDeceased, testUser, "SOINS_PRESENTATION",
                LocalDateTime.now().plusDays(2));
        Intervention completed = interventionService.complete(i, "Soins effectués", "Produit X");
        assertEquals("TERMINEE", completed.getStatus());
        assertNotNull(completed.getCompletedAt());
        assertEquals("Soins effectués", completed.getReport());
        assertEquals("Produit X", completed.getProductsUsed());
    }

    @Test
    @Order(4)
    void testCompleteSansReport() {
        Intervention i = interventionService.schedule(testDeceased, testUser, "PRELEVEMENT",
                LocalDateTime.now().plusDays(3));
        Intervention completed = interventionService.complete(i, null, null);
        assertEquals("TERMINEE", completed.getStatus());
        assertNull(completed.getReport());
        assertNull(completed.getProductsUsed());
    }

    @Test
    @Order(5)
    void testGetByDateRange() {
        List<Intervention> results = interventionService.getByDateRange(
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(10));
        assertNotNull(results);
    }

    @Test
    @Order(6)
    void testCountByStatus() {
        long planned = interventionService.countByStatus("PLANIFIEE");
        long completed = interventionService.countByStatus("TERMINEE");
        assertTrue(planned >= 0);
        assertTrue(completed >= 0);
    }

    @Test
    @Order(7)
    void testScheduleWithNullDate() {
        assertThrows(Exception.class, () -> {
            interventionService.schedule(testDeceased, testUser, "AUTOPSIE", null);
        });
    }

    @Test
    @Order(8)
    void testCompleteAlreadyCompleted() {
        Intervention i = interventionService.schedule(testDeceased, testUser, "CONSERVATION",
                LocalDateTime.now().plusDays(4));
        interventionService.complete(i, "Fait", null);
        Intervention dup = interventionService.complete(i, "Refait", null);
        assertEquals("TERMINEE", dup.getStatus());
        assertEquals("Refait", dup.getReport());
    }
}
