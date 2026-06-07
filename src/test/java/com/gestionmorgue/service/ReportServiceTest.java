package com.gestionmorgue.service;

import com.gestionmorgue.util.DataInitializer;
import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReportServiceTest {
    private static ReportService reportService;

    @BeforeAll
    static void setup() {
        DataInitializer.initialize();
        reportService = new ReportService();
    }

    @Test
    @Order(1)
    void testGetDashboardStats() {
        ReportService.DashboardStats stats = reportService.getDashboardStats();
        assertNotNull(stats);
        assertTrue(stats.totalLocations >= 0);
    }

    @Test
    @Order(2)
    void testExportCsv() {
        String csv = reportService.exportDeceasedCsv();
        assertNotNull(csv);
        assertTrue(csv.startsWith("N°Dossier;Nom;Prénom;DateNaissance;DateDécès;Lieu;Sexe"));
        assertTrue(csv.contains("DOS-"));
    }

    @Test
    @Order(3)
    void testExportHtmlReport() throws Exception {
        File html = reportService.exportPdfReport();
        assertNotNull(html);
        assertTrue(html.exists());
        assertTrue(html.getName().endsWith(".html"));
        html.deleteOnExit();
    }

    @Test
    @Order(4)
    void testExportNativePdf() throws Exception {
        reportService.exportNativePdf();
        File pdf = new File(System.getProperty("user.home") + "/Desktop/rapport-morgue.pdf");
        assertTrue(pdf.exists());
        assertTrue(pdf.length() > 0);
        pdf.delete();
    }

    @Test
    @Order(5)
    void testExportJson() {
        String json = reportService.exportDeceasedJson();
        assertNotNull(json);
        assertTrue(json.startsWith("["));
        assertTrue(json.contains("dossierNumber"));
    }

    @Test
    @Order(6)
    void testExportXlsx() {
        String path = reportService.exportDeceasedXlsx();
        assertNotNull(path);
        assertTrue(path.endsWith(".xlsx"));
        assertTrue(new java.io.File(path).exists());
        assertTrue(new java.io.File(path).length() > 0);
        new java.io.File(path).delete();
    }

    @Test
    @Order(7)
    void testGetDashboardStatsNotNull() {
        ReportService.DashboardStats stats = reportService.getDashboardStats();
        assertAll(
            () -> assertTrue(stats.totalDeceased >= 0),
            () -> assertTrue(stats.totalLocations >= 0),
            () -> assertTrue(stats.occupiedLocations >= 0),
            () -> assertTrue(stats.pendingInterventions >= 0),
            () -> assertTrue(stats.pendingExits >= 0),
            () -> assertTrue(stats.interventionsToday >= 0),
            () -> assertTrue(stats.entriesThisMonth >= 0)
        );
    }
}
