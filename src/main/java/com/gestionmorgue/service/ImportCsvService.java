package com.gestionmorgue.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ImportCsvService {

    private final DeceasedService deceasedService;

    public ImportCsvService() {
        this.deceasedService = new DeceasedService();
    }

    public ImportResult importDeceasedFromCsv(String filePath) {
        ImportResult result = new ImportResult();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (isFirstLine) {
                    isFirstLine = false;
                    if (line.toLowerCase().contains("firstname")) {
                        continue;
                    }
                }

                try {
                    String[] fields = line.split(",", -1);
                    if (fields.length < 6) {
                        result.errors++;
                        result.errorMessages.add("Ligne invalide (champs insuffisants): " + line);
                        continue;
                    }

                    String firstName = fields[0].trim();
                    String lastName = fields[1].trim();
                    String birthDate = fields[2].trim();
                    String deathDate = fields[3].trim();
                    String placeOfDeath = fields[4].trim();
                    String gender = fields[5].trim();

                    if (birthDate.isEmpty()) {
                        birthDate = LocalDate.now().toString();
                    }
                    if (deathDate.isEmpty()) {
                        deathDate = LocalDate.now().toString();
                    }
                    if (gender.isEmpty()) {
                        gender = "NON_PRECISE";
                    }

                    deceasedService.createDeceased(lastName, firstName, birthDate, deathDate, placeOfDeath, gender);
                    result.imported++;

                } catch (Exception e) {
                    result.errors++;
                    result.errorMessages.add("Erreur ligne: " + line + " -> " + e.getMessage());
                }
            }
        } catch (IOException e) {
            result.errors++;
            result.errorMessages.add("Erreur de lecture du fichier: " + e.getMessage());
        }

        return result;
    }

    public static class ImportResult {
        private int imported;
        private int errors;
        private List<String> errorMessages;

        public ImportResult() {
            this.imported = 0;
            this.errors = 0;
            this.errorMessages = new ArrayList<>();
        }

        public int getImported() {
            return imported;
        }

        public int getErrors() {
            return errors;
        }

        public List<String> getErrorMessages() {
            return errorMessages;
        }
    }
}
