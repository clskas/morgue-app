package com.gestionmorgue.service;

import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.ExitAuthorization;
import com.gestionmorgue.model.User;
import com.gestionmorgue.util.DataInitializer;
import com.gestionmorgue.util.DatabaseManager;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExitServiceTest {
    private static ExitService exitService;
    private static AuthService authService;
    private static DeceasedService deceasedService;
    private static User testUser;
    private static Deceased testDeceased;
    private static String uniqueId;

    @BeforeAll
    static void setup() {
        System.setProperty("test.db.url", "jdbc:h2:mem:testExitService;DB_CLOSE_DELAY=-1");
        DatabaseManager.shutdown();
        DataInitializer.initialize();
        exitService = new ExitService();
        authService = new AuthService();
        deceasedService = new DeceasedService();
        uniqueId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        testUser = authService.createUser("exit_test_" + uniqueId, "pass", "Exit Test", "MEDECIN",
                "exit_" + uniqueId + "@test.com");
        testDeceased = deceasedService.createDeceased("EXIT", uniqueId, "1950-01-01", "2024-06-01",
                "Paris", "MASCULIN");
    }

    @AfterAll
    static void teardown() {
        DatabaseManager.shutdown();
    }

    @Test
    @Order(1)
    void testCreateAuthorization() {
        ExitAuthorization auth = exitService.createAuthorization(testDeceased, testUser,
                "Pompes Funèbres Dupont", "Mme Martin", "Sortie vers crématorium");
        assertNotNull(auth);
        assertNotNull(auth.getId());
        assertEquals("PENDING", auth.getStatus());
        assertEquals(testDeceased.getId(), auth.getDeceased().getId());
    }

    @Test
    @Order(2)
    void testFindAll() {
        List<ExitAuthorization> all = exitService.findAll();
        assertFalse(all.isEmpty());
    }

    @Test
    @Order(3)
    void testApprove() {
        ExitAuthorization auth = exitService.createAuthorization(testDeceased, testUser,
                "Transport SA", null, null);
        Long id = auth.getId();
        exitService.approve(auth);
        ExitAuthorization updated = exitService.findAll().stream()
                .filter(a -> a.getId().equals(id)).findFirst().orElse(null);
        assertNotNull(updated);
        assertEquals("APPROUVEE", updated.getStatus());
    }

    @Test
    @Order(4)
    void testConfirmExit() {
        ExitAuthorization auth = exitService.createAuthorization(testDeceased, testUser,
                "Transport SA", null, null);
        Long id = auth.getId();
        exitService.approve(auth);
        exitService.confirmExit(auth);
        ExitAuthorization updated = exitService.findAll().stream()
                .filter(a -> a.getId().equals(id)).findFirst().orElse(null);
        assertNotNull(updated);
        assertEquals("SORTIE_EFFECTUEE", updated.getStatus());
        assertNotNull(updated.getEffectiveExitAt());
    }

    @Test
    @Order(5)
    void testCountPending() {
        long pending = exitService.countPending();
        assertTrue(pending >= 0);
    }
}
