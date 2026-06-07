package com.gestionmorgue.service;

import com.gestionmorgue.util.DataInitializer;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LabelServiceTest {
    private static LabelService labelService;

    @BeforeAll
    static void setup() {
        DataInitializer.initialize();
        labelService = new LabelService();
    }

    @Test
    @Order(1)
    void testGenerateStorageLabels() throws IOException {
        File html = labelService.generateStorageLabels();
        assertNotNull(html);
        assertTrue(html.exists());
        assertTrue(html.getName().startsWith("etiquettes-stockage"));
        assertTrue(html.getName().endsWith(".html"));
    }

    @Test
    @Order(2)
    void testGeneratedLabelsContent() throws IOException {
        File html = labelService.generateStorageLabels();
        String content = Files.readString(html.toPath(), StandardCharsets.UTF_8);
        assertTrue(content.contains("<html>"));
        assertTrue(content.contains("</html>"));
        assertTrue(content.contains("Zone:"));
    }

    @Test
    @Order(3)
    void testGeneratedLabelsHasCodes() throws IOException {
        File html = labelService.generateStorageLabels();
        String content = Files.readString(html.toPath(), StandardCharsets.UTF_8);
        assertTrue(content.contains("<div class='code'"));
    }

    @Test
    @Order(4)
    void testGenerateLabelsMultipleCalls() throws IOException {
        File h1 = labelService.generateStorageLabels();
        File h2 = labelService.generateStorageLabels();
        assertNotNull(h1);
        assertNotNull(h2);
        assertNotSame(h1, h2);
    }

    @Test
    @Order(5)
    void testGeneratedLabelsValidHtml() throws IOException {
        File html = labelService.generateStorageLabels();
        String content = Files.readString(html.toPath(), StandardCharsets.UTF_8);
        assertTrue(content.startsWith("<html>"));
        assertTrue(content.contains("</html>"));
    }
}
