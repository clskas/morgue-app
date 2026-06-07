package com.gestionmorgue.service;

import com.gestionmorgue.model.User;
import com.gestionmorgue.util.DataInitializer;
import com.gestionmorgue.util.SessionManager;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    private static AuthService authService;
    private static String uniqueId;

    @BeforeAll
    static void setup() {
        DataInitializer.initialize();
        authService = new AuthService();
        uniqueId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    @Test
    void testCreateAndAuthenticate() {
        String email = "test-" + uniqueId + "@test.com";
        User user = authService.createUser("user_" + uniqueId, "mypass", "Test User", "GREFFIER", email);
        assertNotNull(user.getId());

        User authenticated = authService.authenticate("user_" + uniqueId, "mypass");
        assertEquals("user_" + uniqueId, authenticated.getUsername());
    }

    @Test
    void testAuthenticateWrongPassword() {
        String email = "wp-" + uniqueId + "@test.com";
        authService.createUser("wp_" + uniqueId, "correct", "WP User", "GREFFIER", email);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticate("wp_" + uniqueId, "wrong");
        });
        assertEquals("Mot de passe incorrect", exception.getMessage());
    }

    @Test
    void testAuthenticateUnknownUser() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticate("unknown_" + uniqueId, "password");
        });
        assertEquals("Utilisateur introuvable", exception.getMessage());
    }

    @Test
    void testCreateDuplicateUsername() {
        String email1 = "dup1-" + uniqueId + "@test.com";
        authService.createUser("dup_" + uniqueId, "pass1", "User 1", "GREFFIER", email1);
        String email2 = "dup2-" + uniqueId + "@test.com";
        assertThrows(Exception.class, () -> {
            authService.createUser("dup_" + uniqueId, "pass2", "User 2", "MEDECIN", email2);
        });
    }

    @Test
    void testCreateUserWithAllFields() {
        String email = "full-" + uniqueId + "@test.com";
        User user = authService.createUser("full_" + uniqueId, "secure123",
                "Jean Dupont", "ADMIN", email);
        assertAll(
            () -> assertNotNull(user.getId()),
            () -> assertEquals("full_" + uniqueId, user.getUsername()),
            () -> assertEquals("Jean Dupont", user.getFullName()),
            () -> assertEquals("ADMIN", user.getRole()),
            () -> assertTrue(user.isActive())
        );
    }

    @Test
    void testLogout() {
        User user = authService.authenticate("admin", "admin123");
        assertNotNull(user);
        authService.logout();
        assertNull(SessionManager.getInstance().getCurrentUser());
    }

    @Test
    void testChangePassword() {
        String cpId = "cp_" + uniqueId;
        authService.createUser(cpId, "oldpass", "Change Pass", "MEDECIN", cpId + "@test.com");
        User u = authService.authenticate(cpId, "oldpass");
        authService.changePassword(u, "oldpass", "newpass");
        authService.logout();
        User reAuth = authService.authenticate(cpId, "newpass");
        assertEquals(cpId, reAuth.getUsername());
    }

    @Test
    void testChangePasswordWrongOld() {
        String cpId = "cpw_" + uniqueId;
        authService.createUser(cpId, "correct", "Wrong Old", "GREFFIER", cpId + "@test.com");
        User u = authService.authenticate(cpId, "correct");
        Exception ex = assertThrows(RuntimeException.class, () -> {
            authService.changePassword(u, "wrong", "newpass");
        });
        assertEquals("Ancien mot de passe incorrect", ex.getMessage());
    }

    @Test
    void testAuthenticateDisabledAccount() {
        String disId = "dis_" + uniqueId;
        User u = authService.createUser(disId, "pass", "Disabled", "GREFFIER", disId + "@test.com");
        u.setActive(false);
        var dao = new com.gestionmorgue.dao.UserDao();
        dao.update(u);
        Exception ex = assertThrows(RuntimeException.class, () -> {
            authService.authenticate(disId, "pass");
        });
        assertEquals("Compte désactivé", ex.getMessage());
    }

    @Test
    void testCreateUserNullEmail() {
        User u = authService.createUser("noemail_" + uniqueId, "pass", "No Email", "MEDECIN", null);
        assertNotNull(u.getId());
        assertNull(u.getEmail());
    }
}
