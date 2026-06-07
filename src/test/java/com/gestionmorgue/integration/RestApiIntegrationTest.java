package com.gestionmorgue.integration;

import com.gestionmorgue.rest.RestApiServer;
import com.gestionmorgue.util.DataInitializer;
import com.gestionmorgue.util.SyncQueue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestApiIntegrationTest {

    private static RestApiServer server;
    private static HttpClient client;
    private static final int PORT = 18080;
    private static String authToken;
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void setUp() throws Exception {
        DataInitializer.initialize();
        server = new RestApiServer(PORT);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void tearDown() {
        server.stop();
    }

    @Test
    @Order(1)
    void testHealthEndpoint() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/health"))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("UP"));
    }

    @Test
    @Order(2)
    void testAuthSuccess() throws Exception {
        String body = mapper.writeValueAsString(Map.of("username", "admin", "password", "admin123"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/auth"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("token"));

        Map<String, Object> parsed = mapper.readValue(resp.body(), Map.class);
        authToken = (String) parsed.get("token");
        assertNotNull(authToken);
        assertEquals("admin", parsed.get("username"));
    }

    @Test
    @Order(3)
    void testAuthFailure() throws Exception {
        String body = mapper.writeValueAsString(Map.of("username", "admin", "password", "wrong"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/auth"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, resp.statusCode());
    }

    @Test
    @Order(4)
    void testGetDeceasedWithAuth() throws Exception {
        if (authToken == null) { testAuthSuccess(); }
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/deceased"))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }

    @Test
    @Order(5)
    void testCreateDeceasedWithAuth() throws Exception {
        if (authToken == null) { testAuthSuccess(); }
        String body = mapper.writeValueAsString(Map.of(
                "lastName", "Test", "firstName", "API",
                "gender", "MASCULIN"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/deceased"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, resp.statusCode());
        assertTrue(resp.body().contains("TEST"));
    }

    @Test
    @Order(6)
    void testCreateDeceasedWithoutAuth() throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "lastName", "NoAuth", "firstName", "Test"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/deceased"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, resp.statusCode());
    }

    @Test
    @Order(7)
    void testMetricsRequiresAuth() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/metrics"))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, resp.statusCode());
    }

    @Test
    @Order(8)
    void testMetricsWithAuth() throws Exception {
        if (authToken == null) { testAuthSuccess(); }
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/metrics"))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("totalRequests"));
    }

    @Test
    @Order(9)
    void testCorsPreflight() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/deceased"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", "http://example.com")
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, resp.statusCode());
    }

    @Test
    @Order(10)
    void testStorageEndpoint() throws Exception {
        if (authToken == null) { testAuthSuccess(); }
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/storage"))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }

    @Test
    @Order(11)
    void testNotFound() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/nonexistent"))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, resp.statusCode());
    }

    @Test
    @Order(12)
    void testSyncQueueWithoutAuth() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/syncqueue"))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, resp.statusCode());
    }

    @Test
    @Order(13)
    void testSyncQueueWithAuth() throws Exception {
        if (authToken == null) { testAuthSuccess(); }
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/syncqueue"))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("count"));
    }

    @Test
    @Order(14)
    void testSyncQueueProcessQueue() throws Exception {
        if (authToken == null) { testAuthSuccess(); }
        System.setProperty("api.port", String.valueOf(PORT));
        SyncQueue queue = SyncQueue.getInstance();
        queue.clearCompleted();
        assertEquals(0, queue.pendingCount());

        String body = mapper.writeValueAsString(Map.of(
                "lastName", "QueueTest", "firstName", "Sync",
                "gender", "MASCULIN"));
        queue.enqueue("POST", "/api/deceased", body, authToken);
        assertEquals(1, queue.pendingCount());

        queue.start();
        queue.processAll();
        Thread.sleep(1000);
        assertEquals(0, queue.pendingCount());

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/deceased"))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("QUEUETEST"));
        queue.stop();
    }
}
