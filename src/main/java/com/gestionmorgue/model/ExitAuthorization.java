package com.gestionmorgue.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exit_authorizations", indexes = {
    @Index(name = "idx_exit_status", columnList = "status"),
    @Index(name = "idx_exit_deceased", columnList = "deceased_id"),
    @Index(name = "idx_exit_authorized", columnList = "authorizedAt")
})
public class ExitAuthorization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deceased_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Deceased deceased;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorized_by", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User authorizedBy;

    @Column(nullable = false)
    private LocalDateTime authorizedAt;

    @Column(nullable = false, length = 150)
    private String transportCompany;

    @Column(length = 100)
    private String authorizedPerson;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, length = 20)
    private String status;

    private LocalDateTime effectiveExitAt;

    private String signedBy;
    private LocalDateTime signedAt;
    private boolean certificateOfDeathVerified;
    private boolean authorizationFormVerified;
    private boolean identityVerified;

    @Column(columnDefinition = "TEXT")
    private String verificationNotes;

    @PrePersist
    protected void onCreate() {
        authorizedAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Deceased getDeceased() { return deceased; }
    public void setDeceased(Deceased deceased) { this.deceased = deceased; }
    public User getAuthorizedBy() { return authorizedBy; }
    public void setAuthorizedBy(User authorizedBy) { this.authorizedBy = authorizedBy; }
    public LocalDateTime getAuthorizedAt() { return authorizedAt; }
    public void setAuthorizedAt(LocalDateTime authorizedAt) { this.authorizedAt = authorizedAt; }
    public String getTransportCompany() { return transportCompany; }
    public void setTransportCompany(String transportCompany) { this.transportCompany = transportCompany; }
    public String getAuthorizedPerson() { return authorizedPerson; }
    public void setAuthorizedPerson(String authorizedPerson) { this.authorizedPerson = authorizedPerson; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getEffectiveExitAt() { return effectiveExitAt; }
    public void setEffectiveExitAt(LocalDateTime effectiveExitAt) { this.effectiveExitAt = effectiveExitAt; }
    public String getSignedBy() { return signedBy; }
    public void setSignedBy(String signedBy) { this.signedBy = signedBy; }
    public LocalDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }
    public boolean isCertificateOfDeathVerified() { return certificateOfDeathVerified; }
    public void setCertificateOfDeathVerified(boolean certificateOfDeathVerified) { this.certificateOfDeathVerified = certificateOfDeathVerified; }
    public boolean isAuthorizationFormVerified() { return authorizationFormVerified; }
    public void setAuthorizationFormVerified(boolean authorizationFormVerified) { this.authorizationFormVerified = authorizationFormVerified; }
    public boolean isIdentityVerified() { return identityVerified; }
    public void setIdentityVerified(boolean identityVerified) { this.identityVerified = identityVerified; }
    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }
}
