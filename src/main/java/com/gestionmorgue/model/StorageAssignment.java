package com.gestionmorgue.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "storage_assignments", indexes = {
    @Index(name = "idx_assign_deceased", columnList = "deceased_id"),
    @Index(name = "idx_assign_location", columnList = "location_id"),
    @Index(name = "idx_assign_active", columnList = "releasedAt")
})
public class StorageAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deceased_id", nullable = false)
    private Deceased deceased;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private StorageLocation location;

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    private LocalDateTime releasedAt;

    @Column(length = 50)
    private String releasedBy;

    @PrePersist
    protected void onCreate() { assignedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Deceased getDeceased() { return deceased; }
    public void setDeceased(Deceased deceased) { this.deceased = deceased; }
    public StorageLocation getLocation() { return location; }
    public void setLocation(StorageLocation location) { this.location = location; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public LocalDateTime getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }
    public String getReleasedBy() { return releasedBy; }
    public void setReleasedBy(String releasedBy) { this.releasedBy = releasedBy; }
}
