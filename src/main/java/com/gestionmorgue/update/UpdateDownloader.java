package com.gestionmorgue.update;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;

public class UpdateDownloader {
    private final HttpClient httpClient;
    private final Path tempDir;
    private DownloadProgressListener progressListener;

    public interface DownloadProgressListener {
        void onProgress(int percent);
        void onComplete(Path filePath);
        void onError(String error);
    }

    public UpdateDownloader() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.tempDir = Path.of(System.getProperty("java.io.tmpdir"), "gestionmorgue-updates");
    }

    public void setProgressListener(DownloadProgressListener listener) {
        this.progressListener = listener;
    }

    public void downloadUpdate(VersionInfo versionInfo) {
        try {
            Files.createDirectories(tempDir);

            Path downloadPath = tempDir.resolve("gestionmorgue-" + versionInfo.getVersion() + ".jar");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(versionInfo.getDownloadUrl()))
                    .timeout(Duration.ofMinutes(30))
                    .GET()
                    .build();

            httpClient.send(request, responseInfo -> {
                try {
                    return HttpResponse.BodySubscribers.ofFile(downloadPath,
                            java.nio.file.StandardOpenOption.CREATE,
                            java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                            java.nio.file.StandardOpenOption.WRITE);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur ouverture fichier", e);
                }
            });

            if (versionInfo.getChecksumSha256() != null) {
                verifyChecksum(downloadPath, versionInfo.getChecksumSha256());
            }

            if (progressListener != null) {
                progressListener.onComplete(downloadPath);
            }
        } catch (Exception e) {
            if (progressListener != null) {
                progressListener.onError("Erreur téléchargement: " + e.getMessage());
            }
        }
    }

    private void verifyChecksum(Path file, String expectedChecksum) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        String actualChecksum = HexFormat.of().formatHex(digest.digest());
        if (!actualChecksum.equalsIgnoreCase(expectedChecksum)) {
            Files.delete(file);
            throw new RuntimeException("Checksum invalide. Fichier corrompu.");
        }
    }
}
