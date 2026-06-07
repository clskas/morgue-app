package com.gestionmorgue.service;

import com.gestionmorgue.model.AuditLog;
import com.gestionmorgue.util.DataInitializer;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuditServiceTest {
    private static AuditService auditService;

    @BeforeAll
    static void setup() {
        DataInitializer.initialize();
        auditService = new AuditService();
    }

    @Test
    @Order(1)
    void testGetRecentLogs() {
        List<AuditLog> logs = auditService.getRecentLogs(100);
        assertNotNull(logs);
    }

    @Test
    @Order(2)
    void testGetLogCount() {
        long count = auditService.getLogCount();
        assertTrue(count >= 0);
    }

    @Test
    @Order(3)
    void testGetRecentLogsLimit() {
        List<AuditLog> logs5 = auditService.getRecentLogs(5);
        assertNotNull(logs5);
        assertTrue(logs5.size() <= 5);
    }

    @Test
    @Order(4)
    void testGetRecentLogsOrder() {
        List<AuditLog> logs = auditService.getRecentLogs(50);
        if (logs.size() >= 2) {
            assertTrue(
                logs.get(0).getTimestamp().compareTo(logs.get(1).getTimestamp()) >= 0
            );
        }
    }

    @Test
    @Order(5)
    void testGetLogsWithZeroLimit() {
        List<AuditLog> logs = auditService.getRecentLogs(0);
        assertNotNull(logs);
        assertTrue(logs.isEmpty());
    }
}
