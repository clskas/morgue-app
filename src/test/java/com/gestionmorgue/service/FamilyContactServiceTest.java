package com.gestionmorgue.service;

import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.FamilyContact;
import com.gestionmorgue.util.DataInitializer;
import com.gestionmorgue.util.DatabaseManager;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FamilyContactServiceTest {
    private static FamilyContactService contactService;
    private static DeceasedService deceasedService;
    private static Deceased testDeceased;
    private static String uniqueId;

    @BeforeAll
    static void setup() {
        System.setProperty("test.db.url", "jdbc:h2:mem:testFamilyContact;DB_CLOSE_DELAY=-1");
        DatabaseManager.shutdown();
        DataInitializer.initialize();
        contactService = new FamilyContactService();
        deceasedService = new DeceasedService();
        uniqueId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        testDeceased = deceasedService.createDeceased("CONTACT", uniqueId,
                "1950-05-15", "2024-08-01", "Lyon", "MASCULIN");
    }

    @AfterAll
    static void teardown() {
        DatabaseManager.shutdown();
    }

    @Test
    @Order(1)
    void testCreateContact() {
        FamilyContact c = contactService.createContact(testDeceased, "Mme Dupont",
                "Conjoint", "0123456789", "dupont@test.com",
                "1 rue de la Paix, Paris", "Contact principal");
        assertNotNull(c);
        assertNotNull(c.getId());
        assertEquals("Mme Dupont", c.getFullName());
        assertEquals("Conjoint", c.getRelationship());
        assertEquals("0123456789", c.getPhone());
        assertEquals("dupont@test.com", c.getEmail());
        assertEquals("1 rue de la Paix, Paris", c.getAddress());
        assertEquals("Contact principal", c.getNotes());
    }

    @Test
    @Order(2)
    void testGetContactsForDeceased() {
        contactService.createContact(testDeceased, "M. Martin",
                "Fils", "0987654321", null, null, null);
        List<FamilyContact> contacts = contactService.getContactsForDeceased(testDeceased.getId());
        assertFalse(contacts.isEmpty());
        assertTrue(contacts.stream().anyMatch(c -> "M. Martin".equals(c.getFullName())));
    }

    @Test
    @Order(3)
    void testGetContactsForUnknownDeceased() {
        List<FamilyContact> contacts = contactService.getContactsForDeceased(-1L);
        assertTrue(contacts.isEmpty());
    }

    @Test
    @Order(4)
    void testCreateContactMinimal() {
        FamilyContact c = contactService.createContact(testDeceased, "Minimal",
                null, null, null, null, null);
        assertNotNull(c.getId());
        assertEquals("Minimal", c.getFullName());
        assertNull(c.getRelationship());
    }

    @Test
    @Order(5)
    void testDeleteContact() {
        FamilyContact c = contactService.createContact(testDeceased, "ToDelete",
                "Test", null, null, null, null);
        Long id = c.getId();
        contactService.deleteContact(c);
        List<FamilyContact> remaining = contactService.getContactsForDeceased(testDeceased.getId());
        assertTrue(remaining.stream().noneMatch(ct -> ct.getId().equals(id)));
    }

    @Test
    @Order(6)
    void testGetContactsEmptyAfterDelete() {
        Deceased d = deceasedService.createDeceased("TEMP_FC", uniqueId,
                null, null, null, null);
        assertTrue(contactService.getContactsForDeceased(d.getId()).isEmpty());
        FamilyContact c = contactService.createContact(d, "Temp", null, null, null, null, null);
        assertEquals(1, contactService.getContactsForDeceased(d.getId()).size());
        contactService.deleteContact(c);
        assertTrue(contactService.getContactsForDeceased(d.getId()).isEmpty());
    }
}
