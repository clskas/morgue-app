package com.gestionmorgue.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gestionmorgue.config.ConfigService;
import com.gestionmorgue.model.*;
import com.gestionmorgue.util.DatabaseManager;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportService {

    public static class DashboardStats {
        public long totalDeceased;
        public long occupiedLocations;
        public long totalLocations;
        public long pendingInterventions;
        public long pendingExits;
        public long interventionsToday;
        public long entriesThisMonth;
    }

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();

            stats.totalDeceased = countAll(session, Deceased.class);
            stats.totalLocations = countAll(session, StorageLocation.class);

            var occupiedQ = cb.createQuery(Long.class);
            var occupiedR = occupiedQ.from(StorageLocation.class);
            occupiedQ.select(cb.count(occupiedR)).where(cb.isTrue(occupiedR.get("occupied")));
            stats.occupiedLocations = session.createQuery(occupiedQ).getSingleResult();

            var intQ = cb.createQuery(Long.class);
            var intR = intQ.from(Intervention.class);
            intQ.select(cb.count(intR)).where(cb.equal(intR.get("status"), "PLANIFIEE"));
            stats.pendingInterventions = session.createQuery(intQ).getSingleResult();

            var exitQ = cb.createQuery(Long.class);
            var exitR = exitQ.from(ExitAuthorization.class);
            exitQ.select(cb.count(exitR)).where(cb.equal(exitR.get("status"), "PENDING"));
            stats.pendingExits = session.createQuery(exitQ).getSingleResult();

            LocalDate today = LocalDate.now();
            var todayIntQ = cb.createQuery(Long.class);
            var todayIntR = todayIntQ.from(Intervention.class);
            todayIntQ.select(cb.count(todayIntR)).where(
                    cb.greaterThanOrEqualTo(todayIntR.get("scheduledAt"), today.atStartOfDay()),
                    cb.lessThan(todayIntR.get("scheduledAt"), today.plusDays(1).atStartOfDay())
            );
            stats.interventionsToday = session.createQuery(todayIntQ).getSingleResult();

            LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            var monthQ = cb.createQuery(Long.class);
            var monthR = monthQ.from(Deceased.class);
            monthQ.select(cb.count(monthR))
                    .where(cb.greaterThanOrEqualTo(monthR.get("createdAt"), monthStart));
            stats.entriesThisMonth = session.createQuery(monthQ).getSingleResult();
        }
        return stats;
    }

    public String exportDeceasedCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("N°Dossier;Nom;Prénom;DateNaissance;DateDécès;Lieu;Sexe\n");
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaQuery<Deceased> q = session.getCriteriaBuilder().createQuery(Deceased.class);
            q.select(q.from(Deceased.class));
            List<Deceased> list = session.createQuery(q).list();
            for (Deceased d : list) {
                sb.append(d.getDossierNumber()).append(";")
                  .append(nullToEmpty(d.getLastName())).append(";")
                  .append(nullToEmpty(d.getFirstName())).append(";")
                  .append(d.getBirthDate() != null ? d.getBirthDate() : "").append(";")
                  .append(d.getDeathDate() != null ? d.getDeathDate() : "").append(";")
                  .append(nullToEmpty(d.getPlaceOfDeath())).append(";")
                  .append(nullToEmpty(d.getGender())).append("\n");
            }
        }
        return sb.toString();
    }

    public File exportPdfReport() throws IOException {
        File tempFile = File.createTempFile("rapport-morgue-", ".html");
        try (Writer w = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            DashboardStats stats = getDashboardStats();
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            w.write(String.format("""
                <html><head><meta charset="UTF-8"><style>
                body{font-family:sans-serif;margin:40px}
                h1{color:#1a237e}
                .stat{margin:10px 0;padding:10px;background:#f5f5f5;border-radius:4px}
                </style></head><body>
                <h1>Rapport d'activité</h1>
                <p>Généré le %s</p>
                <div class="stat">Défunts enregistrés : %d</div>
                <div class="stat">Emplacements occupés : %d / %d</div>
                <div class="stat">Interventions planifiées : %d</div>
                <div class="stat">Sorties en attente : %d</div>
                <div class="stat">Interventions aujourd'hui : %d</div>
                <div class="stat">Entrées ce mois : %d</div>
                </body></html>""", date, stats.totalDeceased, stats.occupiedLocations,
                stats.totalLocations, stats.pendingInterventions, stats.pendingExits,
                stats.interventionsToday, stats.entriesThisMonth));
        }
        return tempFile;
    }

    public String exportDeceasedJson() {
        var mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaQuery<Deceased> q = session.getCriteriaBuilder().createQuery(Deceased.class);
            q.select(q.from(Deceased.class));
            List<Deceased> list = session.createQuery(q).list();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException("Erreur export JSON", e);
        }
    }

    public String exportDeceasedXlsx() {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaQuery<Deceased> q = session.getCriteriaBuilder().createQuery(Deceased.class);
            q.select(q.from(Deceased.class));
            List<Deceased> list = session.createQuery(q).list();

            org.apache.poi.ss.usermodel.Workbook wb = new XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Défunts");
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            String[] cols = {"N°Dossier", "Nom", "Prénom", "DateNaissance", "DateDécès", "Lieu", "Sexe", "NIR"};
            for (int i = 0; i < cols.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(styleHeader(wb));
                sheet.autoSizeColumn(i);
            }
            int rowIdx = 1;
            for (Deceased d : list) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(nullToEmpty(d.getDossierNumber()));
                row.createCell(1).setCellValue(nullToEmpty(d.getLastName()));
                row.createCell(2).setCellValue(nullToEmpty(d.getFirstName()));
                row.createCell(3).setCellValue(d.getBirthDate() != null ? d.getBirthDate().toString() : "");
                row.createCell(4).setCellValue(d.getDeathDate() != null ? d.getDeathDate().toString() : "");
                row.createCell(5).setCellValue(nullToEmpty(d.getPlaceOfDeath()));
                row.createCell(6).setCellValue(nullToEmpty(d.getGender()));
                row.createCell(7).setCellValue(nullToEmpty(d.getNir()));
            }
            String path = System.getProperty("user.home") + "/Desktop/export-defunts.xlsx";
            try (OutputStream os = Files.newOutputStream(Paths.get(path))) { wb.write(os); }
            return path;
        } catch (Exception e) {
            throw new RuntimeException("Erreur export XLSX", e);
        }
    }

    private CellStyle styleHeader(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private long countAll(Session session, Class<?> entityClass) {
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> q = cb.createQuery(Long.class);
        q.select(cb.count(q.from(entityClass)));
        return session.createQuery(q).getSingleResult();
    }

    public void exportNativePdf() throws DocumentException, IOException {
        DashboardStats stats = getDashboardStats();
        Document document = new Document(PageSize.A4);
        String path = System.getProperty("user.home") + "/Desktop/rapport-morgue.pdf";
        PdfWriter.getInstance(document, new FileOutputStream(path));
        document.open();

        ConfigService cfg = ConfigService.getInstance();
        String hospitalName = cfg.getHospitalName();
        String hospitalInfo = hospitalName.isEmpty() ? "" : hospitalName;
        String addr = cfg.getHospitalAddress();
        if (!addr.isEmpty()) hospitalInfo += (hospitalInfo.isEmpty() ? "" : "  |  ") + addr;
        String phone = cfg.getHospitalPhone();
        if (!phone.isEmpty()) hospitalInfo += "  |  Tél: " + phone;

        String logoPath = cfg.getHospitalLogoPath();
        if (!logoPath.isEmpty()) {
            try {
                com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(logoPath);
                img.scaleToFit(120, 60);
                document.add(img);
            } catch (Exception ignored) {}
        }

        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new java.awt.Color(26, 35, 126));
        com.lowagie.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        com.lowagie.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        String title = "Rapport d'activité";
        if (!hospitalName.isEmpty()) title += " - " + hospitalName;
        else title += " - Gestion Morgue";
        document.add(new Paragraph(title, titleFont));
        if (!hospitalInfo.isEmpty()) {
            document.add(new Paragraph(hospitalInfo,
                    FontFactory.getFont(FontFactory.HELVETICA, 9, new java.awt.Color(100, 100, 100))));
        }
        document.add(new Paragraph("Généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                FontFactory.getFont(FontFactory.HELVETICA, 10, new java.awt.Color(100, 100, 100))));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        addStatRow(table, "Défunts enregistrés", String.valueOf(stats.totalDeceased));
        addStatRow(table, "Emplacements occupés", stats.occupiedLocations + " / " + stats.totalLocations);
        addStatRow(table, "Interventions planifiées", String.valueOf(stats.pendingInterventions));
        addStatRow(table, "Sorties en attente", String.valueOf(stats.pendingExits));
        addStatRow(table, "Interventions aujourd'hui", String.valueOf(stats.interventionsToday));
        addStatRow(table, "Entrées ce mois", String.valueOf(stats.entriesThisMonth));

        document.add(table);
        document.close();
    }

    private void addStatRow(PdfPTable table, String label, String value) {
        com.lowagie.text.Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        com.lowagie.text.Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(8);
        labelCell.setBackgroundColor(new java.awt.Color(240, 240, 240));
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(8);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String nullToEmpty(String s) { return s != null ? s : ""; }
}
