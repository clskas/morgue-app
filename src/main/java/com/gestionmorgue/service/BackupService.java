package com.gestionmorgue.service;

import com.gestionmorgue.config.DatabaseConfig;
import com.gestionmorgue.util.DatabaseManager;
import org.hibernate.Session;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupService {

    private static final String BACKUP_DIR = System.getProperty("user.home") + "/.gestionmorgue/backups";

    public String exportBackup() throws IOException {
        Files.createDirectories(Paths.get(BACKUP_DIR));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String filename = BACKUP_DIR + "/gestionmorgue-backup-" + timestamp + ".sql";
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            String sqlContent = session.doReturningWork(conn -> {
                StringBuilder sb = new StringBuilder();
                try (var stmt = conn.createStatement();
                     var rs = stmt.executeQuery("SCRIPT")) {
                    while (rs.next()) {
                        sb.append(rs.getString(1)).append("\n");
                    }
                }
                return sb.toString();
            });
            Files.writeString(Paths.get(filename), sqlContent, StandardCharsets.UTF_8);
        }
        return filename;
    }

    public void importBackup(String filePath) throws IOException {
        String sql = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();
            session.doWork(conn -> {
                try (var stmt = conn.createStatement()) {
                    stmt.execute("RUNSCRIPT FROM '" + filePath.replace("\\", "\\\\") + "'");
                }
            });
            tx.commit();
        }
    }

    public static String getBackupDir() {
        return BACKUP_DIR;
    }
}
