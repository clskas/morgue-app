package com.gestionmorgue.controller;

import com.gestionmorgue.model.User;
import com.gestionmorgue.service.AuthService;
import com.gestionmorgue.util.DataInitializer;
import com.gestionmorgue.util.DatabaseManager;
import com.gestionmorgue.util.SecurityUtil;
import com.gestionmorgue.util.SessionManager;
import com.gestionmorgue.util.ValidationUtil;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

class UserControllerLogicTest {
    private static AuthService authService;
    private static String uniqueId;
    private static User adminUser;
    private static User medecinUser;

    @BeforeAll
    static void setup() {
        String dbUrl = "jdbc:h2:mem:test" + UserControllerLogicTest.class.getSimpleName() + ";DB_CLOSE_DELAY=-1";
        System.setProperty("test.db.url", dbUrl);
        DataInitializer.initialize();
        authService = new AuthService();
        uniqueId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        adminUser = authService.createUser("ut_adm_" + uniqueId, "pass", "Admin Test", "ADMIN", "ut_adm_" + uniqueId + "@t.com");
        medecinUser = authService.createUser("ut_med_" + uniqueId, "pass", "Medecin Test", "MEDECIN", "ut_med_" + uniqueId + "@t.com");
    }

    @AfterEach
    void clearSession() {
        SessionManager.getInstance().logout();
    }

    @AfterAll
    static void cleanup() {
        SessionManager.getInstance().logout();
        DatabaseManager.shutdown();
    }

    @Test
    @Order(1)
    void testAdminHasFullAccess() {
        SessionManager.getInstance().login(adminUser);
        assertTrue(SecurityUtil.isAdmin());
        assertDoesNotThrow(() -> SecurityUtil.requireAdmin());
        assertDoesNotThrow(() -> SecurityUtil.requireRole("ADMIN", "MEDECIN"));
    }

    @Test
    @Order(2)
    void testMedecinIsNotAdmin() {
        SessionManager.getInstance().login(medecinUser);
        assertFalse(SecurityUtil.isAdmin());
        assertThrows(SecurityException.class, () -> SecurityUtil.requireAdmin());
    }

    @Test
    @Order(3)
    void testMedecinCanAccessAllowedRoles() {
        SessionManager.getInstance().login(medecinUser);
        assertDoesNotThrow(() -> SecurityUtil.requireRole("ADMIN", "MEDECIN", "THANATOPRACTEUR"));
    }

    @Test
    @Order(4)
    void testMedecinCannotAccessAdminOnlyRole() {
        SessionManager.getInstance().login(medecinUser);
        assertThrows(SecurityException.class, () -> SecurityUtil.requireRole("ADMIN"));
    }

    @Test
    @Order(5)
    void testNoUserThrowsSecurityException() {
        SessionManager.getInstance().logout();
        assertThrows(SecurityException.class, () -> SecurityUtil.requireAdmin());
        assertThrows(SecurityException.class, () -> SecurityUtil.requireRole("ADMIN"));
        assertFalse(SecurityUtil.isAdmin());
        assertFalse(SecurityUtil.hasRole("ADMIN"));
    }

    @Test
    @Order(6)
    void testSessionManagerRoleCheck() {
        SessionManager.getInstance().login(adminUser);
        assertTrue(SessionManager.getInstance().hasRole("ADMIN"));
        assertFalse(SessionManager.getInstance().hasRole("MEDECIN"));
    }

    @Test
    @Order(7)
    void testCreateUserValidatesFields() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        User user = authService.createUser("new_user_" + ts, "secure123",
                "Jean Dupont", "MEDECIN", "jean_" + ts + "@test.com");
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals("new_user_" + ts, user.getUsername());
        assertEquals("Jean Dupont", user.getFullName());
        assertEquals("MEDECIN", user.getRole());
        assertTrue(user.isActive());
        assertTrue(user.getPasswordHash().startsWith("$2a$"));
    }

    @Test
    @Order(8)
    void testCreateUserFailsWithDuplicateUsername() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        authService.createUser("dup_user_" + ts, "pass1", "First", "GREFFIER", "dup1_" + ts + "@t.com");
        assertThrows(Exception.class, () ->
                authService.createUser("dup_user_" + ts, "pass2", "Second", "MEDECIN", "dup2_" + ts + "@t.com"));
    }

    @Test
    @Order(9)
    void testPasswordReset() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        User user = authService.createUser("reset_user_" + ts, "oldpass", "Reset User",
                "MEDECIN", "reset_" + ts + "@test.com");
        String newHash = BCrypt.hashpw("newpass123", BCrypt.gensalt());
        user.setPasswordHash(newHash);
        var dao = new com.gestionmorgue.dao.UserDao();
        dao.update(user);
        User updated = dao.findByUsername("reset_user_" + ts).orElse(null);
        assertNotNull(updated);
        assertTrue(BCrypt.checkpw("newpass123", updated.getPasswordHash()));
    }

    @Test
    @Order(10)
    void testRoleAssignmentPreserved() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        User user = authService.createUser("role_user_" + ts, "pass", "Role User",
                "THANATOPRACTEUR", "role_" + ts + "@test.com");
        assertEquals("THANATOPRACTEUR", user.getRole());
        var dao = new com.gestionmorgue.dao.UserDao();
        user.setRole("ADMIN");
        dao.update(user);
        User fetched = dao.findByUsername("role_user_" + ts).orElse(null);
        assertNotNull(fetched);
        assertEquals("ADMIN", fetched.getRole());
    }

    @Test
    @Order(11)
    void testUserValidationUtil() {
        assertTrue(ValidationUtil.isNotEmpty("valid"));
        assertFalse(ValidationUtil.isNotEmpty(""));
        assertFalse(ValidationUtil.isNotEmpty(null));
        assertFalse(ValidationUtil.isNotEmpty("   "));
    }
}
