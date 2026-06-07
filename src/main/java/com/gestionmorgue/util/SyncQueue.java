package com.gestionmorgue.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SyncQueue {

    private static final Logger log = LoggerFactory.getLogger(SyncQueue.class);

    private static final Path QUEUE_FILE = Paths.get(
        System.getProperty("user.home"), ".gestionmorgue", ".syncqueue.json");
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static String getApiBase() {
        return "http://localhost:" + System.getProperty("api.port", "8080");
    }

    private final ConcurrentLinkedQueue<SyncItem> queue = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Object lock = new Object();
    private volatile boolean running;
    private static SyncQueue instance;

    public static synchronized SyncQueue getInstance() {
        if (instance == null) instance = new SyncQueue();
        return instance;
    }

    private SyncQueue() {
        loadPersisted();
    }

    public void start() {
        if (running) return;
        running = true;
        scheduler.scheduleWithFixedDelay(this::processQueue, 5, 30, TimeUnit.SECONDS);
    }

    public void stop() {
        running = false;
        scheduler.shutdown();
    }

    public void enqueue(String method, String path, String body, String token) {
        synchronized (lock) {
            queue.add(new SyncItem(method, path, body, token, Instant.now()));
            try {
                persist();
            } catch (IOException e) {
                log.error("Erreur persistance lors de l'ajout: " + e.getMessage());
            }
        }
    }

    public int pendingCount() {
        return queue.size();
    }

    public List<SyncItem> getPendingItems() {
        return new ArrayList<>(queue);
    }

    public void clearCompleted() {
        synchronized (lock) {
            queue.clear();
            try { Files.deleteIfExists(QUEUE_FILE); } catch (IOException ignored) {}
        }
    }

    public void processAll() {
        processQueue();
    }

    private void processQueue() {
        synchronized (lock) {
            if (!running || queue.isEmpty()) return;
            SyncItem item = queue.peek();
            if (item == null) return;
            try {
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(getApiBase() + item.path))
                        .method(item.method,
                                item.body != null
                                    ? HttpRequest.BodyPublishers.ofString(item.body)
                                    : HttpRequest.BodyPublishers.noBody())
                        .header("Content-Type", "application/json");
                if (item.token != null) {
                    builder.header("Authorization", "Bearer " + item.token);
                }
                HttpResponse<String> resp = HTTP_CLIENT.send(builder.build(),
                        HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    SyncItem completed = queue.poll();
                    try {
                        persist();
                    } catch (IOException e) {
                        log.error("Erreur persistance après retrait, opération remise en file: " + e.getMessage());
                        if (completed != null) {
                            queue.add(completed);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("[SyncQueue] Échec synchronisation: " + e.getMessage());
            }
        }
    }

    private void persist() throws IOException {
        Files.createDirectories(QUEUE_FILE.getParent());
        MAPPER.writeValue(QUEUE_FILE.toFile(), new ArrayList<>(queue));
    }

    private void loadPersisted() {
        synchronized (lock) {
            try {
                if (Files.exists(QUEUE_FILE)) {
                    List<Map<String, Object>> items = MAPPER.readValue(
                            QUEUE_FILE.toFile(), List.class);
                    for (Map<String, Object> item : items) {
                        queue.add(new SyncItem(
                            (String) item.get("method"),
                            (String) item.get("path"),
                            (String) item.get("body"),
                            (String) item.get("token"),
                            item.get("timestamp") != null
                                ? Instant.parse((String) item.get("timestamp"))
                                : Instant.now()
                        ));
                    }
                    log.info("[SyncQueue] " + queue.size() + " opérations en attente de synchronisation");
                }
            } catch (Exception e) {
                log.error("[SyncQueue] Erreur chargement file: " + e.getMessage());
            }
        }
    }

    public static class SyncItem {
        private final String method;
        private final String path;
        private final String body;
        private final String token;
        private final Instant timestamp;

        public SyncItem(String method, String path, String body, String token, Instant timestamp) {
            this.method = method;
            this.path = path;
            this.body = body;
            this.token = token;
            this.timestamp = timestamp;
        }

        public String getMethod() { return method; }
        public String getPath() { return path; }
        public String getBody() { return body; }
        public String getToken() { return token; }
        public Instant getTimestamp() { return timestamp; }
    }
}
