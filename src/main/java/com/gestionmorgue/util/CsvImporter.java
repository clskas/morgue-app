package com.gestionmorgue.util;

import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.service.DeceasedService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CsvImporter {

    private final DeceasedService deceasedService;

    public CsvImporter() {
        this.deceasedService = new DeceasedService();
    }

    public ImportResult importFile(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        ImportResult result = new ImportResult();
        if (lines.isEmpty()) return result;
        String header = lines.get(0);
        if (!header.toLowerCase().contains("nom") && !header.toLowerCase().contains("nom")) {
            result.errors.add("En-tête CSV non reconnue: " + header);
            return result;
        }
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            try {
                String[] parts = parseCsvLine(line);
                if (parts.length < 2) { result.errors.add("Ligne " + (i+1) + ": format invalide"); continue; }
                String lastName = parts[0].trim();
                String firstName = parts.length > 1 ? parts[1].trim() : "";
                String birthDate = parts.length > 2 ? parts[2].trim() : "";
                String deathDate = parts.length > 3 ? parts[3].trim() : "";
                String placeOfDeath = parts.length > 4 ? parts[4].trim() : "";
                String gender = parts.length > 5 ? parts[5].trim() : "NON_SPECIFIE";
                if (!gender.equals("MASCULIN") && !gender.equals("FEMININ")) gender = "NON_SPECIFIE";

                deceasedService.createDeceased(lastName, firstName,
                        birthDate.isEmpty() ? null : birthDate,
                        deathDate.isEmpty() ? null : deathDate,
                        placeOfDeath.isEmpty() ? null : placeOfDeath, gender);
                result.imported++;
            } catch (Exception e) {
                result.errors.add("Ligne " + (i+1) + ": " + e.getMessage());
            }
        }
        return result;
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"') { inQuotes = !inQuotes; continue; }
            if (c == ';' && !inQuotes) { fields.add(current.toString()); current = new StringBuilder(); continue; }
            current.append(c);
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    public static class ImportResult {
        public int imported;
        public List<String> errors = new ArrayList<>();
    }
}
