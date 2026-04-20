package com.nicasia.cyberintel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents", indexes = {
    @Index(name = "idx_incident_severity", columnList = "severity"),
    @Index(name = "idx_incident_status",   columnList = "status"),
    @Index(name = "idx_incident_type",     columnList = "type"),
    @Index(name = "idx_incident_created",  columnList = "created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Severity is required")
    @Column(nullable = false)
    private String severity;   // LOW, MEDIUM, HIGH, CRITICAL

    @NotBlank(message = "Type is required")
    @Column(nullable = false)
    private String type;       // Phishing, Ransomware, DDoS, Data Breach

    @Column(nullable = false)
    private String status;     // OPEN, IN_PROGRESS, RESOLVED

    private String affectedSystem;

    // FK to User — who reported this incident
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_user_id")
    private User reportedByUser;

    // Keep a plain string fallback for display (denormalised)
    private String reportedBy;

    // AI-predicted risk level from PyTorch service
    private String aiRiskLevel;

    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "OPEN";
    }
}
