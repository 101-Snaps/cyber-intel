package com.nicasia.cyberintel.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "applications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Personal Info
    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private String nationality;
    private String idNumber;

    // Disability
    private Boolean hasDisability = false;
    private String disabilityDetails;

    // File paths (stored on server)
    private String cvPath;
    private String idDocPath;

    // Job info
    private String jobTitle;
    private String jobCompany;
    private String jobLocation;

    @Column(updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    public void prePersist() {
        this.submittedAt = LocalDateTime.now();
    }

    // Relationships
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educationList;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experienceList;
}