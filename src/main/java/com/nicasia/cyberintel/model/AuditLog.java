package com.nicasia.cyberintel.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // WHO did it
    private String userEmail;

    // WHAT they did e.g. LOGIN, UPLOAD, API_CALL
    private String action;

    // WHICH endpoint e.g. /api/incidents
    private String endpoint;

    // SUCCESS or FAILED
    private String result;

    // Their IP address
    private String ipAddress;

    // Extra details e.g. filename uploaded, error message
    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}