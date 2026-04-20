package com.nicasia.cyberintel.service;

import com.nicasia.cyberintel.model.Alert;
import com.nicasia.cyberintel.model.AuditLog;
import com.nicasia.cyberintel.repository.AlertRepository;
import com.nicasia.cyberintel.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class SecurityMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityMonitorService.class);

    private final AuditLogRepository auditLogRepository;
    private final AlertRepository alertRepository;

    public SecurityMonitorService(AuditLogRepository auditLogRepository,
                                  AlertRepository alertRepository) {
        this.auditLogRepository = auditLogRepository;
        this.alertRepository = alertRepository;
    }

    // ── Audit logging ────────────────────────────────────────────────────

    public void log(String userEmail, String action, String endpoint,
                    String result, String ipAddress, String details) {
        AuditLog log = new AuditLog();
        log.setUserEmail(userEmail);
        log.setAction(action);
        log.setEndpoint(endpoint);
        log.setResult(result);
        log.setIpAddress(ipAddress);
        log.setDetails(details);
        auditLogRepository.save(log);

        logger.info("[{}] {} - {} {} - IP: {} - {}",
            result, userEmail, action, endpoint, ipAddress, details);
    }

    // ── Alert creation ───────────────────────────────────────────────────

    public void createAlert(String type, String severity, String userEmail,
                            String ipAddress, String message) {
        Alert alert = new Alert();
        alert.setType(type);
        alert.setSeverity(severity);
        alert.setUserEmail(userEmail);
        alert.setIpAddress(ipAddress);
        alert.setMessage(message);
        alertRepository.save(alert);

        logger.warn("[ALERT][{}][{}] {} - IP: {} - {}",
            severity, type, userEmail, ipAddress, message);
    }

    // ── Brute-force detection ────────────────────────────────────────────

    public void checkBruteForce(String ipAddress, String userEmail) {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

        List<AuditLog> recentFailures = auditLogRepository
            .findByIpAddress(ipAddress)
            .stream()
            .filter(l -> "LOGIN".equals(l.getAction())
                && "FAILED".equals(l.getResult())
                && l.getTimestamp().isAfter(tenMinutesAgo))
            .toList();

        if (recentFailures.size() >= 5) {
            createAlert(
                "BRUTE_FORCE", "HIGH", userEmail, ipAddress,
                "5+ failed login attempts in 10 minutes from IP: " + ipAddress
            );
        }
    }

    // ── Suspicious upload detection ──────────────────────────────────────

    public void checkSuspiciousUpload(String userEmail, String ipAddress,
                                      String filename, long fileSize) {
        if (fileSize > 5 * 1024 * 1024) {
            createAlert("SUSPICIOUS_UPLOAD", "MEDIUM", userEmail, ipAddress,
                "Large file upload: " + filename + " (" + (fileSize / 1024 / 1024) + " MB)");
        }
        if (!filename.toLowerCase().endsWith(".pdf")) {
            createAlert("SUSPICIOUS_UPLOAD", "HIGH", userEmail, ipAddress,
                "Non-PDF upload attempt: " + filename);
        }
    }

    // ── Getters for dashboard ────────────────────────────────────────────

    public List<AuditLog> getAllLogs()        { return auditLogRepository.findAll(); }
    public List<Alert>    getAllAlerts()       { return alertRepository.findAll(); }
    public List<Alert>    getUnreadAlerts()   { return alertRepository.findByStatus("UNREAD"); }
    public long           getUnreadAlertCount() { return alertRepository.countByStatus("UNREAD"); }

    public void markAlertRead(@NonNull Long alertId) {
        // Objects.requireNonNull satisfies @NonNull on CrudRepository.findById
        alertRepository.findById(Objects.requireNonNull(alertId)).ifPresent(alert -> {
            alert.setStatus("READ");
            alertRepository.save(alert);
        });
    }
}
