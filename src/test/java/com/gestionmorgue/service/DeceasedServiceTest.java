package com.gestionmorgue.service;

import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.util.DataInitializer;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeceasedServiceTest {
    private static DeceasedService deceasedService;

    @BeforeAll
    static void setup() {
        DataInitializer.initialize();
        deceasedService = new DeceasedService();
    }

    @Test
    @Order(1)
    void testCreateDeceased() {
        Deceased d = deceasedService.createDeceased(
                "DUPONT", "Jean", "1960-05-15", "2024-01-10",
                "Paris", "MASCULIN");
        assertNotNull(d);
        assertNotNull(d.getId());
        assertNotNull(d.getDossierNumber());
        assertTrue(d.getDossierNumber().startsWith("DOS-"));
        assertEquals("DUPONT", d.getLastName());
        assertEquals("Jean", d.getFirstName());
    }

    @Test
    @Order(2)
    void testSearchByLastName() {
        List<Deceased> results = deceasedService.search("DUPONT", null, null);
        assertFalse(results.isEmpty());
        assertEquals("DUPONT", results.get(0).getLastName());
    }

    @Test
    @Order(3)
    void testSearchByDossierNumber() {
        Deceased first = deceasedService.getRecentDeceased(1).get(0);
        List<Deceased> results = deceasedService.search(null, null, first.getDossierNumber());
        assertEquals(1, results.size());
        assertEquals(first.getId(), results.get(0).getId());
    }

    @Test
    @Order(4)
    void testGetCount() {
        assertTrue(deceasedService.getCount() >= 1);
    }

    @Test
    @Order(5)
    void testUpdateDeceased() {
        Deceased first = deceasedService.getRecentDeceased(1).get(0);
        first.setPlaceOfDeath("Lyon");
        Deceased updated = deceasedService.update(first);
        assertEquals("Lyon", updated.getPlaceOfDeath());
    }

    @Test
    @Order(6)
    void testDeleteDeceased() {
        Deceased d = deceasedService.createDeceased(
                "TEMP", "Delete", null, null, null, null);
        Long id = d.getId();
        deceasedService.delete(d);
        assertNull(deceasedService.findById(id));
    }

    @Test
    @Order(7)
    void testFindById() {
        Deceased d = deceasedService.createDeceased(
                "FINDBYID", "Test", "1970-01-01", "2024-09-01", "Marseille", "MASCULIN");
        Deceased found = deceasedService.findById(d.getId());
        assertNotNull(found);
        assertEquals("FINDBYID", found.getLastName());
        assertEquals("Marseille", found.getPlaceOfDeath());
    }

    @Test
    @Order(8)
    void testFindByIdNotFound() {
        assertNull(deceasedService.findById(-1L));
    }

    @Test
    @Order(9)
    void testCountActiveAssignments() {
        Deceased d = deceasedService.createDeceased(
                "ASSIGNCNT", "Test", null, null, null, null);
        long count = deceasedService.countActiveAssignments(d.getId());
        assertEquals(0, count);
    }

    @Test
    @Order(10)
    void testCountPendingExits() {
        Deceased d = deceasedService.createDeceased(
                "PENDCNT", "Test", null, null, null, null);
        long count = deceasedService.countPendingExits(d.getId());
        assertEquals(0, count);
    }

    @Test
    @Order(11)
    void testCountPlannedInterventions() {
        Deceased d = deceasedService.createDeceased(
                "PLANCNT", "Test", null, null, null, null);
        long count = deceasedService.countPlannedInterventions(d.getId());
        assertEquals(0, count);
    }

    @Test
    @Order(12)
    void testSearchByLastNameNotFound() {
        assertTrue(deceasedService.search("XXXXXXXXXX", null, null).isEmpty());
    }

    @Test
    @Order(13)
    void testSearchByFirstName() {
        Deceased d = deceasedService.createDeceased(
                "SEARCHFN", "Robert", null, null, null, null);
        assertFalse(deceasedService.search(null, "Robert", null).isEmpty());
    }

    @Test
    @Order(14)
    void testGetRecentDeceased() {
        assertFalse(deceasedService.getRecentDeceased(10).isEmpty());
    }
}
