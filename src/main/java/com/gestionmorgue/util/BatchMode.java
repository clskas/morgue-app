package com.gestionmorgue.util;

import com.gestionmorgue.config.DatabaseConfig;
import com.gestionmorgue.rest.RestApiServer;
import com.gestionmorgue.service.BackupService;
import com.gestionmorgue.service.ImportCsvService;
import com.gestionmorgue.service.ReportService;
import com.gestionmorgue.util.DatabaseManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BatchMode {

    private final ReportService reportService;
    private final BackupService backupService;

    public BatchMode() {
        this.reportService = new ReportService();
        this.backupService = new BackupService();
    }

    public static void printHelp() {
        System.out.println("Gestion Morgue - Mode batch");
        System.out.println("Usage: java -jar gestionmorgue.jar [options]");
        System.out.println("Options (sans interface graphique) :");
        System.out.println("  --export-csv      Exporte les défunts au format CSV sur le Bureau");
        System.out.println("  --export-json     Exporte les défunts au format JSON sur le Bureau");
        System.out.println("  --export-xlsx     Exporte les défunts au format Excel sur le Bureau");
        System.out.println("  --export-pdf      Génère le rapport PDF natif sur le Bureau");
        System.out.println("  --backup          Sauvegarde la base de données");
        System.out.println("  --server          Démarre le serveur API REST seul (sans GUI)");
        System.out.println("  --import-csv <f>  Importe les défunts depuis un fichier CSV");
        System.out.println("  --help            Affiche cette aide");
        System.out.println("Options avec interface graphique :");
        System.out.println("  --db=<profile>    Profil base de données (h2, postgresql)");
        System.out.println("  --api-port=<port> Port du serveur API REST");
        System.out.println("  (sans argument)   Lance l'interface graphique");
    }

    public int run(String[] args) {
        if (args.length == 0) return -1;

        try {
            DataInitializer.initialize();

            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--export-csv" -> exportCsv();
                    case "--export-json" -> exportJson();
                    case "--export-xlsx" -> exportXlsx();
                    case "--export-pdf" -> exportPdf();
                    case "--backup" -> backup();
                    case "--server" -> runServer();
                    case "--import-csv" -> { i++; if (i >= args.length) throw new IllegalArgumentException("--import-csv nécessite un chemin de fichier"); importCsv(args[i]); }
                    case "--help" -> { printHelp(); return 0; }
                }
            }
            return 0;
        } catch (Exception e) {
            System.err.println("[BatchMode] Erreur: " + e.getMessage());
            return 1;
        } finally {
            if (!hasArg(args, "--server")) {
                DatabaseManager.shutdown();
            }
        }
    }

    private static boolean hasArg(String[] args, String target) {
        for (String a : args) if (a.equals(target)) return true;
        return false;
    }

    private void runServer() throws Exception {
        String portStr = System.getProperty("api.port", "8080");
        int port = Integer.parseInt(portStr);
        RestApiServer api = new RestApiServer(port);
        api.start();
        System.out.println("[BatchMode] Serveur API REST démarré sur le port " + port + " (Ctrl+C pour arrêter)");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[BatchMode] Arrêt du serveur...");
            api.stop();
            DatabaseManager.shutdown();
        }));
        Thread.currentThread().join();
    }

    private void exportCsv() throws IOException {
        String csv = reportService.exportDeceasedCsv();
        String path = System.getProperty("user.home") + "/Desktop/export-defunts.csv";
        Files.writeString(Paths.get(path), csv);
        System.out.println("[BatchMode] CSV exporté : " + path);
    }

    private void exportJson() throws IOException {
        String json = reportService.exportDeceasedJson();
        String path = System.getProperty("user.home") + "/Desktop/export-defunts.json";
        Files.writeString(Paths.get(path), json);
        System.out.println("[BatchMode] JSON exporté : " + path);
    }

    private void exportXlsx() {
        String path = reportService.exportDeceasedXlsx();
        System.out.println("[BatchMode] Excel exporté : " + path);
    }

    private void exportPdf() throws Exception {
        reportService.exportNativePdf();
        String path = System.getProperty("user.home") + "/Desktop/rapport-morgue.pdf";
        System.out.println("[BatchMode] PDF exporté : " + path);
    }

    private void backup() throws IOException {
        String path = backupService.exportBackup();
        System.out.println("[BatchMode] Sauvegarde : " + path);
    }

    private void importCsv(String filePath) {
        ImportCsvService importService = new ImportCsvService();
        ImportCsvService.ImportResult result = importService.importDeceasedFromCsv(filePath);
        System.out.println("[BatchMode] " + result.getImported() + " lignes importées, " + result.getErrors() + " erreurs");
        for (String err : result.getErrorMessages()) {
            System.err.println("[BatchMode] Erreur: " + err);
        }
    }
}
