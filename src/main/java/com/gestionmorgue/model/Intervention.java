package com.gestionmorgue.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interventions", indexes = {
    @Index(name = "idx_interventions_status", columnList = "status"),
    @Index(name = "idx_interventions_scheduled", columnList = "scheduledAt"),
    @Index(name = "idx_interventions_deceased", columnList = "deceased_id")
})
public class Intervention {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deceased_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Deceased deceased;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User performer;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    private LocalDateTime completedAt;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String report;

    @Column(columnDefinition = "TEXT")
    private String productsUsed;

    private String signedBy;
    private LocalDateTime signedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Deceased getDeceased() { return deceased; }
    public void setDeceased(Deceased deceased) { this.deceased = deceased; }
    public User getPerformer() { return performer; }
    public void setPerformer(User performer) { this.performer = performer; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReport() { return report; }
    public void setReport(String report) { this.report = report; }
    public String getProductsUsed() { return productsUsed; }
    public void setProductsUsed(String productsUsed) { this.productsUsed = productsUsed; }
    public String getSignedBy() { return signedBy; }
    public void setSignedBy(String signedBy) { this.signedBy = signedBy; }
    public LocalDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
