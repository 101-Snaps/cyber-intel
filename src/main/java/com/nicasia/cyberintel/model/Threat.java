package com.nicasia.cyberintel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "threats", indexes = {
    @Index(name = "idx_threat_risk_level", columnList = "risk_level"),
    @Index(name = "idx_threat_category",   columnList = "category"),
    @Index(name = "idx_threat_status",     columnList = "status"),
    @Index(name = "idx_threat_detected",   columnList = "detected_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Threat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category;   // Virus, Worm, Trojan, Spyware, Ransomware

    @Column(name = "risk_level")
    private String riskLevel;  // LOW, MEDIUM, HIGH, CRITICAL — set by AI

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String status;     // ACTIVE, CONTAINED, ELIMINATED

    private String source;     // External, Internal, Unknown, Vendor

    // AI confidence score (0.0 – 1.0)
    private Double aiConfidence;

    @Column(updatable = false, name = "detected_at")
    private LocalDateTime detectedAt;

    @PrePersist
    public void prePersist() {
        this.detectedAt = LocalDateTime.now();
        if (this.status == null) this.status = "ACTIVE";
    }
}
