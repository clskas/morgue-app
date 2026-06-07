package com.gestionmorgue.controller;

import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.ExitAuthorization;
import com.gestionmorgue.model.User;
import com.gestionmorgue.service.AuthService;
import com.gestionmorgue.service.DeceasedService;
import com.gestionmorgue.service.ExitService;
import com.gestionmorgue.util.DataInitializer;
import com.gestionmorgue.util.DatabaseManager;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExitControllerLogicTest {
    private static ExitService exitService;
    private static AuthService authService;
    private static DeceasedService deceasedService;
    private static User testUser;
    private static Deceased testDeceased;
    private static String uniqueId;

    @BeforeAll
    static void setup() {
        String dbUrl = "jdbc:h2:mem:test" + ExitControllerLogicTest.class.getSimpleName() + ";DB_CLOSE_DELAY=-1";
        System.setProperty("test.db.url", dbUrl);
        DataInitializer.initialize();
        exitService = new ExitService();
        authService = new AuthService();
        deceasedService = new DeceasedService();
        uniqueId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        testUser = authService.createUser("exit_ctrl_" + uniqueId, "pass", "Exit Ctrl Test", "MEDECIN",
                "exit_ctrl_" + uniqueId + "@test.com");
        testDeceased = deceasedService.createDeceased("EXIT_CTRL", uniqueId, "1950-01-01", "2024-06-01",
                "Paris", "MASCULIN");
    }

    @AfterAll
    static void cleanup() {
        DatabaseManager.shutdown();
    }

    @Test
    void testCreateAuthorizationIsPending() {
        ExitAuthorization auth = exitService.createAuthorization(testDeceased, testUser,
                "Transport Test", "M. Dupont", "Notes test");
        assertNotNull(auth);
        assertNotNull(auth.getId());
        assertEquals("PENDING", auth.getStatus());
        assertEquals(testDeceased.getId(), auth.getDeceased().getId());
        assertEquals(testUser.getId(), auth.getAuthorizedBy().getId());
        assertEquals("Transport Test", auth.getTransportCompany());
    }

    @Test
    void testApproveAuthorization() {
        ExitAuthorization auth = exitService.createAuthorization(testDeceased, testUser,
                "Transport SA", null, null);
        assertEquals("PENDING", auth.getStatus());
        exitService.approve(auth);
        List<ExitAuthorization> all = exitService.findAll();
        ExitAuthorization updated = all.stream()
                .filter(a -> a.getId().equals(auth.getId())).findFirst().orElse(null);
        assertNotNull(updated);
        assertEquals("APPROUVEE", updated.getStatus());
    }

    @Test
    void testCannotApproveAlreadyApprovedExit() {
        ExitAuthorization auth = exitService.createAuthorization(testDeceased, testUser,
                "Transport Bis", null, null);
        exitService.approve(auth);
        assertDoesNotThrow(() -> exitService.approve(auth));
        List<ExitAuthorization> all = exitService.findAll();
        ExitAuthorization updated = all.stream()
                .filter(a -> a.getId().equals(auth.getId())).findFirst().orElse(null);
        assertNotNull(updated);
        assertEquals("APPROUVEE", updated.getStatus());
    }

    @Test
    void testConfirmExitAfterApproval() {
        ExitAuthorization auth = exitService.createAuthorization(testDeceased, testUser,
                "Transport Final", null, null);
        exitService.approve(auth);
        exitService.confirmExit(auth);
        List<ExitAuthorization> all = exitService.findAll();
        ExitAuthorization updated = all.stream()
                .filter(a -> a.getId().equals(auth.getId())).findFirst().orElse(null);
        assertNotNull(updated);
        assertEquals("SORTIE_EFFECTUEE", updated.getStatus());
        assertNotNull(updated.getEffectiveExitAt());
    }

    @Test
    void testConfirmExitOnPendingSetsStatus() {
        ExitAuthorization auth = exitService.createAuthorization(testDeceased, testUser,
                "Transport Direct", null, null);
        exitService.confirmExit(auth);
        List<ExitAuthorization> all = exitService.findAll();
        ExitAuthorization updated = all.stream()
                .filter(a -> a.getId().equals(auth.getId())).findFirst().orElse(null);
        assertNotNull(updated);
        assertEquals("SORTIE_EFFECTUEE", updated.getStatus());
    }

    @Test
    void testCountPending() {
        long pendingBefore = exitService.countPending();
        exitService.createAuthorization(testDeceased, testUser, "Count Pending", null, null);
        assertEquals(pendingBefore + 1, exitService.countPending());
    }
}
