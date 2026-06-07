package com.gestionmorgue.controller;

import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.FamilyContact;
import com.gestionmorgue.service.DeceasedService;
import com.gestionmorgue.service.FamilyContactService;
import com.gestionmorgue.util.DataInitializer;
import com.gestionmorgue.util.DatabaseManager;
import com.gestionmorgue.util.ValidationUtil;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FamilyContactControllerLogicTest {
    private static FamilyContactService contactService;
    private static DeceasedService deceasedService;
    private static Deceased testDeceased;
    private static String uniqueId;

    @BeforeAll
    static void setup() {
        String dbUrl = "jdbc:h2:mem:test" + FamilyContactControllerLogicTest.class.getSimpleName() + ";DB_CLOSE_DELAY=-1";
        System.setProperty("test.db.url", dbUrl);
        DataInitializer.initialize();
        contactService = new FamilyContactService();
        deceasedService = new DeceasedService();
        uniqueId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        testDeceased = deceasedService.createDeceased("FAM_CTRL", uniqueId,
                "1950-05-15", "2024-08-01", "Lyon", "MASCULIN");
    }

    @AfterAll
    static void cleanup() {
        DatabaseManager.shutdown();
    }

    @Test
    void testCreateFamilyContact() {
        FamilyContact contact = contactService.createContact(testDeceased, "Mme Test_" + uniqueId,
                "Conjoint", "0123456789", "test_" + uniqueId + "@example.com",
                "1 rue de la Paix, Paris", "Contact principal");
        assertNotNull(contact);
        assertNotNull(contact.getId());
        assertEquals("Mme Test_" + uniqueId, contact.getFullName());
        assertEquals("Conjoint", contact.getRelationship());
        assertEquals("0123456789", contact.getPhone());
        assertEquals("test_" + uniqueId + "@example.com", contact.getEmail());
        assertEquals("1 rue de la Paix, Paris", contact.getAddress());
    }

    @Test
    void testLinkContactToDeceased() {
        FamilyContact contact = contactService.createContact(testDeceased, "Lien Test_" + uniqueId,
                "Fils", "0987654321", null, null, null);
        List<FamilyContact> contacts = contactService.getContactsForDeceased(testDeceased.getId());
        assertFalse(contacts.isEmpty());
        assertTrue(contacts.stream().anyMatch(c -> c.getId().equals(contact.getId())));
    }

    @Test
    void testSaveWithInvalidEmail() {
        assertFalse(ValidationUtil.isValidEmail("invalid-email"));
        assertFalse(ValidationUtil.isValidEmail("notanemail"));
        assertFalse(ValidationUtil.isValidEmail("@missing.com"));
    }

    @Test
    void testSaveWithValidEmail() {
        assertTrue(ValidationUtil.isValidEmail("valid@example.com"));
        assertTrue(ValidationUtil.isValidEmail("user.name@domain.co"));
        assertTrue(ValidationUtil.isValidEmail(null));
        assertTrue(ValidationUtil.isValidEmail(""));
    }

    @Test
    void testCreateContactMinimal() {
        FamilyContact contact = contactService.createContact(testDeceased, "Minimal_" + uniqueId,
                null, null, null, null, null);
        assertNotNull(contact.getId());
        assertEquals("Minimal_" + uniqueId, contact.getFullName());
        assertNull(contact.getRelationship());
    }

    @Test
    void testDeleteContact() {
        FamilyContact contact = contactService.createContact(testDeceased, "ToDelete_" + uniqueId,
                "Test", null, null, null, null);
        Long id = contact.getId();
        contactService.deleteContact(contact);
        List<FamilyContact> remaining = contactService.getContactsForDeceased(testDeceased.getId());
        assertTrue(remaining.stream().noneMatch(c -> c.getId().equals(id)));
    }

    @Test
    void testGetContactsForUnknownDeceased() {
        List<FamilyContact> contacts = contactService.getContactsForDeceased(-1L);
        assertTrue(contacts.isEmpty());
    }
}
