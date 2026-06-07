package com.gestionmorgue.controller;

import com.gestionmorgue.model.User;
import com.gestionmorgue.service.AuthService;
import com.gestionmorgue.util.DataInitializer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import com.gestionmorgue.util.SessionManager;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginControllerLogicTest {
    private static AuthService authService;
    private static User createdUser;
    private static String uniqueId;
    private static final Logger log = LoggerFactory.getLogger(LoginControllerLogicTest.class);

    @BeforeAll
    static void setup() {
        DataInitializer.initialize();
        authService = new AuthService();
        uniqueId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        createdUser = authService.createUser("ctrl_login_" + uniqueId, "testpass123", "Ctrl Login", "ADMIN",
                "ctrl_" + uniqueId + "@test.com");
        log.info("Created test user: {}", createdUser.getUsername());
    }

    @Test
    @Order(1)
    void testCreatedUserAuthenticates() {
        assertDoesNotThrow(() -> {
            User user = authService.authenticate("ctrl_login_" + uniqueId, "testpass123");
            assertNotNull(user);
            assertTrue(user.isAdmin());
        });
    }

    @Test
    @Order(2)
    void testWrongPasswordFails() {
        Exception ex = assertThrows(RuntimeException.class, () -> {
            authService.authenticate("ctrl_login_" + uniqueId, "wrongpass");
        });
        assertEquals("Mot de passe incorrect", ex.getMessage());
    }

    @Test
    @Order(3)
    void testUnknownUserFails() {
        Exception ex = assertThrows(RuntimeException.class, () -> {
            authService.authenticate("nonexistent_user_" + uniqueId, "password");
        });
        assertEquals("Utilisateur introuvable", ex.getMessage());
    }

    @Test
    @Order(4)
    void testEmptyPasswordFails() {
        Exception ex = assertThrows(RuntimeException.class, () -> {
            authService.authenticate("ctrl_login_" + uniqueId, "");
        });
        assertEquals("Mot de passe incorrect", ex.getMessage());
    }

    @Test
    @Order(5)
    void testLoginSetsSessionRole() {
        User user = authService.authenticate("ctrl_login_" + uniqueId, "testpass123");
        assertNotNull(user);
        assertEquals("ADMIN", user.getRole());
        assertTrue(SessionManager.getInstance().isLoggedIn());
        assertEquals(user.getUsername(), SessionManager.getInstance().getCurrentUser().getUsername());
    }

    @Test
    @Order(6)
    void testLogoutClearsSession() {
        authService.authenticate("ctrl_login_" + uniqueId, "testpass123");
        assertTrue(SessionManager.getInstance().isLoggedIn());
        authService.logout();
        assertFalse(SessionManager.getInstance().isLoggedIn());
        assertNull(SessionManager.getInstance().getCurrentUser());
    }

    @Test
    @Order(7)
    void testMedecinLoginHasCorrectRole() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        authService.createUser("med_login_" + ts, "pass123", "Med Login", "MEDECIN", "med_" + ts + "@t.com");
        User user = authService.authenticate("med_login_" + ts, "pass123");
        assertNotNull(user);
        assertEquals("MEDECIN", user.getRole());
        assertFalse(user.isAdmin());
    }
}
