package com.gestionmorgue.util;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class SyncQueueTest {

    @Test
    void testSingleton() {
        assertNotNull(SyncQueue.getInstance());
        assertSame(SyncQueue.getInstance(), SyncQueue.getInstance());
    }

    @Test
    void testEnqueueDequeue() {
        SyncQueue queue = SyncQueue.getInstance();
        int before = queue.pendingCount();
        queue.enqueue("POST", "/api/test", "{\"key\":\"value\"}", "test-token");
        assertEquals(before + 1, queue.pendingCount());
        assertFalse(queue.getPendingItems().isEmpty());
        queue.clearCompleted();
        assertEquals(0, queue.pendingCount());
    }

    @Test
    void testPendingItemsList() {
        SyncQueue queue = SyncQueue.getInstance();
        queue.clearCompleted();
        queue.enqueue("GET", "/api/test2", null, null);
        var items = queue.getPendingItems();
        assertFalse(items.isEmpty());
        SyncQueue.SyncItem item = items.get(0);
        assertEquals("GET", item.getMethod());
        assertEquals("/api/test2", item.getPath());
        assertNull(item.getBody());
        queue.clearCompleted();
    }
}
