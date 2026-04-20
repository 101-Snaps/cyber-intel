package com.nicasia.cyberintel.controller;

import com.nicasia.cyberintel.model.Alert;
import com.nicasia.cyberintel.model.AuditLog;
import com.nicasia.cyberintel.service.SecurityMonitorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "*")
public class SecurityDashboardController {

    private final SecurityMonitorService securityMonitorService;

    public SecurityDashboardController(SecurityMonitorService securityMonitorService) {
        this.securityMonitorService = securityMonitorService;
    }

    // Get all audit logs
    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getLogs() {
        return ResponseEntity.ok(securityMonitorService.getAllLogs());
    }

    // Get all alerts
    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> getAlerts() {
        return ResponseEntity.ok(securityMonitorService.getAllAlerts());
    }

    // Get only unread alerts (for notification badge)
    @GetMapping("/alerts/unread")
    public ResponseEntity<List<Alert>> getUnreadAlerts() {
        return ResponseEntity.ok(securityMonitorService.getUnreadAlerts());
    }

    // Get unread alert count (for dashboard badge)
    @GetMapping("/alerts/count")
    public ResponseEntity<Map<String, Long>> getAlertCount() {
        return ResponseEntity.ok(Map.of(
            "count", securityMonitorService.getUnreadAlertCount()
        ));
    }

    // FIX #1: Path variable was {id} but method param was alertId — names must match
    @PutMapping("/alerts/{alertId}/read")
    public ResponseEntity<?> markRead(@PathVariable Long alertId) {
        securityMonitorService.markAlertRead(alertId);
        return ResponseEntity.ok(Map.of("message", "Alert marked as read"));
    }
}
