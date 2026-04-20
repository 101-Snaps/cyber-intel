package com.nicasia.cyberintel.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. BRUTE_FORCE, SUSPICIOUS_UPLOAD, RATE_LIMIT_EXCEEDED
    private String type;

    // LOW, MEDIUM, HIGH, CRITICAL
    private String severity;

    private String userEmail;
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String message;

    // UNREAD or READ
    private String status;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = "UNREAD";
    }
}