package com.gestionmorgue.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.Intervention;
import com.gestionmorgue.model.StorageLocation;
import com.gestionmorgue.service.AuthService;
import com.gestionmorgue.service.DeceasedService;
import com.gestionmorgue.service.InterventionService;
import com.gestionmorgue.service.StorageService;
import com.gestionmorgue.util.SyncQueue;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class RestApiServer {

    private final HttpServer server;
    private final ObjectMapper mapper;
    private final DeceasedService deceasedService;
    private final StorageService storageService;
    private final InterventionService interventionService;
    private final AuthService authService;
    private final MetricsCollector metrics;
    private final TokenManager tokenManager;

    public RestApiServer(int port) throws IOException {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        this.deceasedService = new DeceasedService();
        this.storageService = new StorageService();
        this.interventionService = new InterventionService();
        this.authService = new AuthService();
        this.metrics = MetricsCollector.getInstance();
        this.tokenManager = new TokenManager();

        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(4));
        registerRoutes();
    }

    private void registerRoutes() {
        server.createContext("/api/auth", new AuthHandler());
        server.createContext("/api/deceased", new DeceasedHandler());
        server.createContext("/api/storage", new StorageHandler());
        server.createContext("/api/interventions", new InterventionHandler());
        server.createContext("/api/metrics", this::handleMetrics);
        server.createContext("/api/syncqueue", this::handleSyncQueue);
        server.createContext("/api/health", this::handleHealth);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                byte[] redirect = "<!DOCTYPE html><html><head><meta http-equiv=\"refresh\" content=\"0;url=/index.html\"></head></html>".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, redirect.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(redirect); }
            } else {
                sendJson(exchange, 404, "{\"error\":\"Ressource non trouvée\"}");
            }
        });
        server.createContext("/index.html", this::handleStatic);
        server.createContext("/app.js", this::handleStatic);
        server.createContext("/style.css", this::handleStatic);
        server.createContext("/openapi.yaml", this::handleStatic);
        server.createContext("/favicon.ico", this::handleStatic);
    }

    public void start() {
        server.start();
        System.out.println("[REST API] Serveur démarré sur le port " + server.getAddress().getPort());
    }

    public void stop() {
        server.stop(0);
    }

    private void handleSyncQueue(HttpExchange exchange) throws IOException {
        long start = System.currentTimeMillis();
        int status = 200;
        try {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 204, ""); status = 204; return;
            }
            if (!requireAuth(exchange)) { status = 401; return; }
            SyncQueue queue = SyncQueue.getInstance();
            if ("GET".equals(exchange.getRequestMethod())) {
                List<SyncQueue.SyncItem> items = queue.getPendingItems();
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of(
                    "count", items.size(),
                    "items", items
                ));
                sendJson(exchange, 200, json);
            } else if ("DELETE".equals(exchange.getRequestMethod())) {
                queue.clearCompleted();
                sendJson(exchange, 200, "{\"message\":\"File vidée\"}");
            } else {
                sendError(exchange, 405, "Méthode non supportée"); status = 405;
            }
        } catch (Exception e) {
            sendError(exchange, 500, e.getMessage()); status = 500;
        } finally {
            metrics.recordRequest("/api/syncqueue", status, System.currentTimeMillis() - start);
        }
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        long start = System.currentTimeMillis();
        try {
            String json = mapper.writeValueAsString(Map.of(
                "status", "UP", "app", "Gestion Morgue",
                "version", com.gestionmorgue.config.Constants.APP_VERSION,
                "timestamp", java.time.Instant.now().toString()
            ));
            sendJson(exchange, 200, json);
        } finally {
            metrics.recordRequest("/api/health", 200, System.currentTimeMillis() - start);
        }
    }

    private void handleMetrics(HttpExchange exchange) throws IOException {
        long start = System.currentTimeMillis();
        int status = 200;
        try {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 204, ""); status = 204; return;
            }
            if (!requireAuth(exchange)) { status = 401; return; }
            String accept = exchange.getRequestHeaders().getFirst("Accept");
            if (accept != null && accept.contains("text/plain")) {
                String metricsText = metrics.getOpenMetrics();
                byte[] bytes = metricsText.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                addCorsHeaders(exchange);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
            } else {
                Map<String, Object> snapshot = metrics.getSnapshot();
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot);
                sendJson(exchange, 200, json);
            }
        } catch (Exception e) {
            status = 500;
        } finally {
            metrics.recordRequest("/api/metrics", status, System.currentTimeMillis() - start);
        }
    }

    private static final Map<String, String> MIME_TYPES = Map.of(
        "html", "text/html; charset=UTF-8",
        "js", "application/javascript",
        "css", "text/css",
        "yaml", "text/yaml",
        "json", "application/json",
        "png", "image/png",
        "ico", "image/x-icon"
    );

    private void handleStatic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) { path = "/index.html"; }
        String resourcePath = "/web" + path;
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                String json = "{\"error\":\"Ressource non trouvée\"}";
                sendJson(exchange, 404, json);
                return;
            }
            String ext = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : "";
            String mime = MIME_TYPES.getOrDefault(ext, "application/octet-stream");
            byte[] bytes = readAllBytes(is);
            exchange.getResponseHeaders().set("Content-Type", mime);
            addCorsHeaders(exchange);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        }
    }

    private byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        is.transferTo(baos);
        return baos.toByteArray();
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private boolean requireAuth(HttpExchange exchange) throws IOException {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            sendJson(exchange, 401, "{\"error\":\"Token requis\"}");
            return false;
        }
        String token = auth.substring(7);
        TokenManager.TokenValidation validation = TokenManager.validateToken(token);
        if (!validation.isValid()) {
            sendJson(exchange, 401, "{\"error\":\"" + validation.getError() + "\"}");
            return false;
        }
        exchange.setAttribute("username", validation.getUsername());
        exchange.setAttribute("role", validation.getRole());
        return true;
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        addCorsHeaders(exchange);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            is.transferTo(baos);
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> parseQuery(String query) {
        if (query == null || query.isEmpty()) return Map.of();
        return java.util.Arrays.stream(query.split("&"))
                .map(kv -> kv.split("=", 2))
                .filter(kv -> kv.length == 2)
                .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
    }

    @SuppressWarnings("unchecked")
    private void writeJson(HttpExchange exchange, Object obj) throws IOException {
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        sendJson(exchange, 200, json);
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        String json = "{\"error\":\"" + message.replace("\"", "'") + "\"}";
        sendJson(exchange, status, json);
    }

    class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long start = System.currentTimeMillis();
            try {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    sendJson(exchange, 204, "");
                    return;
                }
                if (!"POST".equals(exchange.getRequestMethod())) {
                    sendError(exchange, 405, "Méthode non supportée");
                    return;
                }
                String body = readBody(exchange);
                Map<String, String> creds = mapper.readValue(body, Map.class);
                String username = creds.getOrDefault("username", "");
                String password = creds.getOrDefault("password", "");

                if (username.isEmpty() || password.isEmpty()) {
                    sendJson(exchange, 400, "{\"error\":\"username et password requis\"}");
                    return;
                }

                var user = new com.gestionmorgue.dao.UserDao().findByUsername(username).orElse(null);
                if (user == null || !org.mindrot.jbcrypt.BCrypt.checkpw(password, user.getPasswordHash())) {
                    sendJson(exchange, 401, "{\"error\":\"Identifiants invalides\"}");
                    return;
                }

                String token = TokenManager.generateToken(user.getUsername(), user.getRole());
                String json = mapper.writeValueAsString(Map.of(
                    "token", token,
                    "username", user.getUsername(),
                    "role", user.getRole(),
                    "fullName", user.getFullName()
                ));
                sendJson(exchange, 200, json);
            } finally {
                metrics.recordRequest("/api/auth", exchange.getResponseCode(), System.currentTimeMillis() - start);
            }
        }
    }

    class DeceasedHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long start = System.currentTimeMillis();
            int status = 200;
            try {
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    sendJson(exchange, 204, "");
                    return;
                }

                if (!requireAuth(exchange)) { status = 401; return; }

                String path = exchange.getRequestURI().getPath();
                String method = exchange.getRequestMethod();
                Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());

                if ("GET".equals(method)) {
                    if (path.matches("/api/deceased/\\d+")) {
                        long id = Long.parseLong(path.replaceAll("/api/deceased/", ""));
                        Deceased d = deceasedService.findById(id);
                        if (d == null) { sendError(exchange, 404, "Défunt non trouvé"); status = 404; }
                        else writeJson(exchange, d);
                    } else {
                        String q = params.getOrDefault("q", "");
                        List<Deceased> list = q.isEmpty() ? deceasedService.getRecentDeceased(100)
                                : deceasedService.search(q, null, null);
                        writeJson(exchange, list);
                    }
                } else {
                    if ("POST".equals(method)) {
                        Deceased input = mapper.readValue(readBody(exchange), Deceased.class);
                        Deceased created = deceasedService.createDeceased(
                                input.getLastName(), input.getFirstName(),
                                input.getBirthDate() != null ? input.getBirthDate().toString() : null,
                                input.getDeathDate() != null ? input.getDeathDate().toString() : null,
                                input.getPlaceOfDeath(), input.getGender());
                        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(created);
                        sendJson(exchange, 201, json); status = 201;
                    } else if ("PUT".equals(method) && path.matches("/api/deceased/\\d+")) {
                        long id = Long.parseLong(path.replaceAll("/api/deceased/", ""));
                        Deceased existing = deceasedService.findById(id);
                        if (existing == null) { sendError(exchange, 404, "Défunt non trouvé"); status = 404; return; }
                        Deceased input = mapper.readValue(readBody(exchange), Deceased.class);
                        existing.setLastName(input.getLastName());
                        existing.setFirstName(input.getFirstName());
                        existing.setBirthDate(input.getBirthDate());
                        existing.setDeathDate(input.getDeathDate());
                        existing.setPlaceOfDeath(input.getPlaceOfDeath());
                        existing.setGender(input.getGender());
                        deceasedService.update(existing);
                        writeJson(exchange, existing);
                    } else if ("DELETE".equals(method) && path.matches("/api/deceased/\\d+")) {
                        long id = Long.parseLong(path.replaceAll("/api/deceased/", ""));
                        Deceased d = deceasedService.findById(id);
                        if (d == null) { sendError(exchange, 404, "Défunt non trouvé"); status = 404; }
                        else { deceasedService.delete(d); sendJson(exchange, 200, "{\"message\":\"Supprimé\"}"); }
                    } else {
                        sendError(exchange, 404, "Route non trouvée"); status = 404;
                    }
                }
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage()); status = 500;
            } finally {
                metrics.recordRequest(pathOf(exchange), status, System.currentTimeMillis() - start);
            }
        }
    }

    class StorageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long start = System.currentTimeMillis();
            int status = 200;
            try {
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                if ("OPTIONS".equals(exchange.getRequestMethod())) { sendJson(exchange, 204, ""); return; }
                if (!requireAuth(exchange)) return;
                if ("GET".equals(exchange.getRequestMethod())) {
                    writeJson(exchange, storageService.getAllLocations());
                } else {
                    sendError(exchange, 405, "Méthode non supportée"); status = 405;
                }
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage()); status = 500;
            } finally {
                metrics.recordRequest("/api/storage", status, System.currentTimeMillis() - start);
            }
        }
    }

    class InterventionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long start = System.currentTimeMillis();
            int status = 200;
            try {
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                if ("OPTIONS".equals(exchange.getRequestMethod())) { sendJson(exchange, 204, ""); return; }
                if (!requireAuth(exchange)) return;
                if ("GET".equals(exchange.getRequestMethod())) {
                    writeJson(exchange, interventionService.getPendingInterventions());
                } else {
                    sendError(exchange, 405, "Méthode non supportée"); status = 405;
                }
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage()); status = 500;
            } finally {
                metrics.recordRequest("/api/interventions", status, System.currentTimeMillis() - start);
            }
        }
    }

    private String pathOf(HttpExchange exchange) {
        String p = exchange.getRequestURI().getPath();
        return p.replaceAll("/\\d+", "/{id}");
    }
}
