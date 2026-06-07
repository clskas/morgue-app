package com.gestionmorgue.service;

import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.StorageAssignment;
import com.gestionmorgue.model.StorageLocation;
import com.gestionmorgue.util.DatabaseManager;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaQuery;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LabelService {

    public File generateStorageLabels() throws IOException {
        File tempFile = File.createTempFile("etiquettes-stockage-", ".html");
        try (Writer w = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            w.write("<html><head><meta charset='UTF-8'><style>");
            w.write("@page{size:90mmx55mm;margin:5mm}");
            w.write("body{font-family:sans-serif;font-size:10pt}");
            w.write(".label{width:80mm;height:45mm;border:1px solid #333;padding:3mm;margin:2mm auto;page-break-after:always}");
            w.write(".code{font-size:18pt;font-weight:bold;color:#1a237e}");
            w.write(".info{margin:2mm 0;font-size:9pt}");
            w.write(".deceased{font-size:11pt;font-weight:bold;color:#c62828}");
            w.write("</style></head><body>");

            try (Session session = DatabaseManager.getSessionFactory().openSession()) {
                CriteriaQuery<StorageLocation> q = session.getCriteriaBuilder()
                        .createQuery(StorageLocation.class);
                q.select(q.from(StorageLocation.class));
                List<StorageLocation> locations = session.createQuery(q).list();

                for (StorageLocation loc : locations) {
                    w.write("<div class='label'>");
                    w.write("<div class='code'>" + escape(loc.getCode()) + "</div>");
                    w.write("<div class='info'>" + escape(loc.getLabel()) + "</div>");
                    w.write("<div class='info'>Zone: " + escape(loc.getZone()) + "</div>");
                    w.write("<div class='info'>Température: " + loc.getTemperature() + "°C</div>");

                    StorageAssignment active = loc.getAssignments().stream()
                            .filter(a -> a.getReleasedAt() == null)
                            .findFirst().orElse(null);
                    if (active != null && active.getDeceased() != null) {
                        Deceased d = active.getDeceased();
                        w.write("<div class='deceased'>" + escape(d.getFullName()) + "</div>");
                        w.write("<div class='info'>Dossier: " + escape(d.getDossierNumber()) + "</div>");
                        if (d.getDeathDate() != null) {
                            w.write("<div class='info'>Décès: " + d.getDeathDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</div>");
                        }
                    } else {
                        w.write("<div class='info' style='color:green'>LIBRE</div>");
                    }
                    w.write("</div>");
                }
            }

            w.write("</body></html>");
        }
        return tempFile;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
