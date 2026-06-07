package com.gestionmorgue.service;

import com.gestionmorgue.util.DataInitializer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BackupServiceTest {
    private static BackupService backupService;

    @BeforeAll
    static void setup() {
        DataInitializer.initialize();
        backupService = new BackupService();
    }

    @Test
    @Order(1)
    void testExportBackup() throws IOException {
        String path = backupService.exportBackup();
        assertNotNull(path);
        assertTrue(path.endsWith(".sql"));
        assertTrue(Files.exists(Paths.get(path)));
        assertTrue(Files.size(Paths.get(path)) > 0);
    }

    @Test
    @Order(2)
    void testExportBackupContent() throws IOException {
        String path = backupService.exportBackup();
        String content = Files.readString(Paths.get(path));
        assertTrue(content.contains("CREATE") || content.contains("INSERT") || content.contains("DROP"));
    }

    @Test
    @Order(3)
    void testGetBackupDir() {
        String dir = BackupService.getBackupDir();
        assertNotNull(dir);
        assertTrue(dir.contains(".gestionmorgue"));
        assertTrue(dir.contains("backups"));
    }

    @Test
    @Order(4)
    void testExportMultipleBackups() throws IOException, InterruptedException {
        String p1 = backupService.exportBackup();
        Thread.sleep(1100);
        String p2 = backupService.exportBackup();
        assertNotEquals(p1, p2);
        assertTrue(Files.exists(Paths.get(p1)));
        assertTrue(Files.exists(Paths.get(p2)));
    }
}
