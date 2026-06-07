package com.gestionmorgue.update;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestionmorgue.config.Constants;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UpdateChecker {
    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private String updateUrl;

    public UpdateChecker() {
        this(Constants.UPDATE_URL);
    }

    public UpdateChecker(String updateUrl) {
        this.updateUrl = updateUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    public UpdateCheckResult checkForUpdate() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(updateUrl))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                VersionInfo remoteVersion = mapper.readValue(response.body(), VersionInfo.class);
                return compareVersions(remoteVersion);
            }
            return new UpdateCheckResult(false, null, "Impossible de contacter le serveur de mise à jour");
        } catch (Exception e) {
            return new UpdateCheckResult(false, null, "Erreur: " + e.getMessage());
        }
    }

    private UpdateCheckResult compareVersions(VersionInfo remote) {
        String currentVersion = Constants.APP_VERSION;
        String remoteVersion = remote.getVersion();

        if (isNewerVersion(currentVersion, remoteVersion)) {
            return UpdateCheckResult.updateAvailable(remote);
        }

        return new UpdateCheckResult(false, null, "Application à jour");
    }

    private boolean isNewerVersion(String current, String remote) {
        String[] currentParts = current.split("\\.");
        String[] remoteParts = remote.split("\\.");

        int maxLength = Math.max(currentParts.length, remoteParts.length);
        for (int i = 0; i < maxLength; i++) {
            int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            int remotePart = i < remoteParts.length ? Integer.parseInt(remoteParts[i]) : 0;
            if (remotePart > currentPart) return true;
            if (remotePart < currentPart) return false;
        }
        return false;
    }
}
