package com.gestionmorgue.service;

import com.gestionmorgue.dao.AttachmentDao;
import com.gestionmorgue.model.Attachment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AttachmentService {

    private final AttachmentDao dao;

    public AttachmentService() {
        this.dao = new AttachmentDao();
    }

    public Attachment attachFile(String entityType, Long entityId, File sourceFile, String description) {
        try {
            String userHome = System.getProperty("user.home");
            Path baseDir = Paths.get(userHome, ".gestionmorgue", "attachments", entityType, String.valueOf(entityId));
            Files.createDirectories(baseDir);

            String uniqueName = UUID.randomUUID().toString() + "_" + sourceFile.getName();
            Path targetPath = baseDir.resolve(uniqueName);
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            Attachment attachment = new Attachment();
            attachment.setEntityType(entityType);
            attachment.setEntityId(entityId);
            attachment.setFileName(sourceFile.getName());
            attachment.setFilePath(targetPath.toString());
            attachment.setFileSize(sourceFile.length());
            attachment.setUploadedAt(LocalDateTime.now());
            attachment.setDescription(description);

            String mimeType = probeMimeType(sourceFile.toPath());
            attachment.setMimeType(mimeType);

            return dao.save(attachment);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la pièce jointe", e);
        }
    }

    public List<Attachment> getAttachments(String entityType, Long entityId) {
        return dao.findByEntity(entityType, entityId);
    }

    public void deleteAttachment(Attachment attachment) {
        File file = new File(attachment.getFilePath());
        if (file.exists()) {
            file.delete();
        }
        dao.delete(attachment);
    }

    public File getFile(Attachment attachment) {
        File file = new File(attachment.getFilePath());
        return file.exists() ? file : null;
    }

    private String probeMimeType(Path path) {
        try {
            String mime = Files.probeContentType(path);
            return mime != null ? mime : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}
