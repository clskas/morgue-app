package com.gestionmorgue.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "family_contacts")
public class FamilyContact {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Deceased deceased;

    private String fullName;
    private String relationship;
    private String phone;
    private String email;
    private String address;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FamilyContact() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Deceased getDeceased() { return deceased; }
    public void setDeceased(Deceased deceased) { this.deceased = deceased; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
