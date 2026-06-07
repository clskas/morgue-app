package com.gestionmorgue.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deceased", indexes = {
    @Index(name = "idx_deceased_dossier", columnList = "dossierNumber"),
    @Index(name = "idx_deceased_lastName", columnList = "lastName"),
    @Index(name = "idx_deceased_fullName", columnList = "lastName, firstName"),
    @Index(name = "idx_deceased_deathDate", columnList = "deathDate"),
    @Index(name = "idx_deceased_createdAt", columnList = "createdAt")
})
public class Deceased {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String dossierNumber;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 100)
    private String firstName;

    private LocalDate birthDate;
    private LocalDate deathDate;
    private String placeOfDeath;
    private String causeOfDeath;
    private String gender;

    @Column(length = 20)
    private String nir;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "deceased", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<StorageAssignment> storageAssignments = new ArrayList<>();

    @OneToMany(mappedBy = "deceased", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Intervention> interventions = new ArrayList<>();

    @OneToMany(mappedBy = "deceased", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<ExitAuthorization> exitAuthorizations = new ArrayList<>();

    @OneToMany(mappedBy = "deceased", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<FamilyContact> familyContacts = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDossierNumber() { return dossierNumber; }
    public void setDossierNumber(String dossierNumber) { this.dossierNumber = dossierNumber; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public LocalDate getDeathDate() { return deathDate; }
    public void setDeathDate(LocalDate deathDate) { this.deathDate = deathDate; }
    public String getPlaceOfDeath() { return placeOfDeath; }
    public void setPlaceOfDeath(String placeOfDeath) { this.placeOfDeath = placeOfDeath; }
    public String getCauseOfDeath() { return causeOfDeath; }
    public void setCauseOfDeath(String causeOfDeath) { this.causeOfDeath = causeOfDeath; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getNir() { return nir; }
    public void setNir(String nir) { this.nir = nir; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<StorageAssignment> getStorageAssignments() { return storageAssignments; }
    public List<Intervention> getInterventions() { return interventions; }
    public List<ExitAuthorization> getExitAuthorizations() { return exitAuthorizations; }

    public String getFullName() {
        return lastName + " " + firstName;
    }
}
