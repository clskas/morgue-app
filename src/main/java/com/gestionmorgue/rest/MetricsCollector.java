package com.gestionmorgue.rest;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsCollector {

    private final AtomicLong totalRequests = new AtomicLong();
    private final AtomicLong totalDurationMs = new AtomicLong();
    private final ConcurrentHashMap<String, AtomicLong> endpointCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, AtomicLong> statusCounts = new ConcurrentHashMap<>();
    private volatile Instant startedAt = Instant.now();

    public void recordRequest(String path, int statusCode, long durationMs) {
        totalRequests.incrementAndGet();
        totalDurationMs.addAndGet(durationMs);
        endpointCounts.computeIfAbsent(path, k -> new AtomicLong()).incrementAndGet();
        statusCounts.computeIfAbsent(statusCode, k -> new AtomicLong()).incrementAndGet();
    }

    public Map<String, Object> getSnapshot() {
        long count = totalRequests.get();
        return Map.of(
            "uptime", Instant.now().getEpochSecond() - startedAt.getEpochSecond(),
            "totalRequests", count,
            "avgDurationMs", count > 0 ? totalDurationMs.get() / (double) count : 0.0,
            "endpoints", endpointCounts.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey, e -> e.getValue().get())),
            "statusCodes", statusCounts.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            e -> String.valueOf(e.getKey()), e -> e.getValue().get()))
        );
    }

    public String getOpenMetrics() {
        long uptime = Instant.now().getEpochSecond() - startedAt.getEpochSecond();
        long count = totalRequests.get();
        double avg = count > 0 ? totalDurationMs.get() / (double) count : 0.0;
        StringBuilder sb = new StringBuilder();
        sb.append("# HELP gm_uptime_seconds Application uptime\n");
        sb.append("# TYPE gm_uptime_seconds gauge\n");
        sb.append("gm_uptime_seconds ").append(uptime).append("\n");
        sb.append("# HELP gm_requests_total Total HTTP requests\n");
        sb.append("# TYPE gm_requests_total counter\n");
        sb.append("gm_requests_total ").append(count).append("\n");
        sb.append("# HELP gm_request_duration_ms Average request duration\n");
        sb.append("# TYPE gm_request_duration_ms gauge\n");
        sb.append("gm_request_duration_ms ").append(String.format("%.2f", avg)).append("\n");
        sb.append("# HELP gm_requests_by_endpoint Requests per endpoint\n");
        sb.append("# TYPE gm_requests_by_endpoint counter\n");
        endpointCounts.forEach((path, c) ->
            sb.append("gm_requests_by_endpoint{endpoint=\"").append(path).append("\"} ").append(c.get()).append("\n"));
        sb.append("# HELP gm_requests_by_status Requests per HTTP status\n");
        sb.append("# TYPE gm_requests_by_status counter\n");
        statusCounts.forEach((status, c) ->
            sb.append("gm_requests_by_status{status=\"").append(status).append("\"} ").append(c.get()).append("\n"));
        sb.append("# EOF\n");
        return sb.toString();
    }

    public static MetricsCollector getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        static final MetricsCollector INSTANCE = new MetricsCollector();
    }
}
