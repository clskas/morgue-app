package com.gestionmorgue.update;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class UpdateInstaller {

    private static final Path BACKUP_DIR = Path.of(
        System.getProperty("user.home"), ".gestionmorgue", "backups");

    public static void backupCurrentJar(String version) {
        try {
            Path currentJar = findCurrentJar();
            if (currentJar == null) {
                System.err.println("Impossible de trouver le JAR actuel, sauvegarde ignorée");
                return;
            }
            Files.createDirectories(BACKUP_DIR);
            Path backupFile = BACKUP_DIR.resolve("gestionmorgue-backup-" + version + ".jar");
            Files.copy(currentJar, backupFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde JAR: " + e.getMessage());
        }
    }

    private static Path findCurrentJar() {
        String javaCommand = System.getProperty("sun.java.command");
        if (javaCommand != null) {
            String[] parts = javaCommand.split("\\s+");
            for (String part : parts) {
                if (part.endsWith(".jar")) {
                    Path p = Path.of(part);
                    if (!p.isAbsolute()) {
                        p = Path.of(System.getProperty("user.dir"), part);
                    }
                    if (Files.exists(p)) return p.toAbsolutePath();
                }
            }
        }
        Path defaultJar = Path.of(System.getProperty("user.dir"), "gestionmorgue.jar");
        if (Files.exists(defaultJar)) return defaultJar.toAbsolutePath();
        return null;
    }

    public static boolean rollback() {
        try {
            if (!Files.exists(BACKUP_DIR)) return false;
            try (var files = Files.list(BACKUP_DIR)) {
                var backups = files
                    .filter(f -> f.getFileName().toString().startsWith("gestionmorgue-backup-"))
                    .sorted((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString()))
                    .collect(java.util.stream.Collectors.toList());
                if (backups.isEmpty()) return false;
                Path latestBackup = backups.get(0);
                Path currentJar = findCurrentJar();
                if (currentJar == null) return false;
                Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), "gestionmorgue-updates");
                if (Files.exists(tempDir)) {
                    try (var tempFiles = Files.list(tempDir)) {
                        tempFiles.forEach(f -> {
                            try { Files.delete(f); } catch (IOException ignored) {}
                        });
                    }
                }
                Files.copy(latestBackup, currentJar, StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Erreur restauration: " + e.getMessage());
            return false;
        }
    }

    public static boolean isRollbackAvailable() {
        try {
            if (!Files.exists(BACKUP_DIR)) return false;
            try (var files = Files.list(BACKUP_DIR)) {
                return files.anyMatch(f -> f.getFileName().toString().startsWith("gestionmorgue-backup-"));
            }
        } catch (IOException e) {
            return false;
        }
    }

    public static void applyUpdate(Path downloadedJar) {
        try {
            String appDir = System.getProperty("user.dir");
            Path currentJar = Path.of(appDir, "gestionmorgue.jar");
            Path backupJar = Path.of(appDir, "gestionmorgue.backup.jar");

            Files.copy(currentJar, backupJar, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(downloadedJar, currentJar, StandardCopyOption.REPLACE_EXISTING);

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                scheduleWindowsRestart(currentJar);
            } else {
                scheduleUnixRestart(currentJar);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur installation mise à jour", e);
        }
    }

    private static void scheduleWindowsRestart(Path appJar) throws IOException {
        Path scriptPath = Path.of(System.getProperty("user.dir"), "update.bat");
        List<String> script = new ArrayList<>();
        script.add("@echo off");
        script.add("timeout /t 2 /nobreak >nul");
        script.add("java -jar \"" + appJar.toAbsolutePath() + "\"");
        script.add("del \"%~f0\"");
        Files.write(scriptPath, script, StandardCharsets.ISO_8859_1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                new ProcessBuilder("cmd", "/c", "start", "min",
                        scriptPath.toAbsolutePath().toString()).start();
            } catch (IOException e) {
                System.err.println("Erreur lancement script mise à jour: " + e.getMessage());
            }
        }));
    }

    private static void scheduleUnixRestart(Path appJar) throws IOException {
        Path scriptPath = Path.of(System.getProperty("user.dir"), "update.sh");
        List<String> script = new ArrayList<>();
        script.add("#!/bin/sh");
        script.add("sleep 2");
        script.add("java -jar \"" + appJar.toAbsolutePath() + "\" &");
        script.add("rm -- \"$0\"");
        Files.write(scriptPath, script, StandardCharsets.UTF_8);
        scriptPath.toFile().setExecutable(true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                new ProcessBuilder("sh", scriptPath.toAbsolutePath().toString()).start();
            } catch (IOException e) {
                System.err.println("Erreur lancement script mise à jour: " + e.getMessage());
            }
        }));
    }

    public static void scheduleUpdate(Path downloadedJar) {
        applyUpdate(downloadedJar);
    }
}
