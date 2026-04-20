package com.nicasia.cyberintel.controller;

import com.nicasia.cyberintel.model.Application;
import com.nicasia.cyberintel.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4201"})
public class AdminController {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final IncidentRepository incidentRepository;
    private final ThreatRepository threatRepository;
    private final AuditLogRepository auditLogRepository;
    private final AlertRepository alertRepository;

    public AdminController(UserRepository userRepository,
                           ApplicationRepository applicationRepository,
                           IncidentRepository incidentRepository,
                           ThreatRepository threatRepository,
                           AuditLogRepository auditLogRepository,
                           AlertRepository alertRepository) {
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.incidentRepository = incidentRepository;
        this.threatRepository = threatRepository;
        this.auditLogRepository = auditLogRepository;
        this.alertRepository = alertRepository;
    }

    // ── Stats ─────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalStaff      = userRepository.findAll().stream().filter(u -> "STAFF".equals(u.getRole())).count();
        long totalApplicants = userRepository.findAll().stream().filter(u -> "USER".equals(u.getRole())).count();

        return ResponseEntity.ok(Map.of(
            "totalUsers",        userRepository.count(),
            "totalStaff",        totalStaff,
            "totalApplicants",   totalApplicants,
            "totalApplications", applicationRepository.count(),
            "totalIncidents",    incidentRepository.count(),
            "totalThreats",      threatRepository.count(),
            "openIncidents",     (long) incidentRepository.findByStatus("OPEN").size(),
            "criticalIncidents", (long) incidentRepository.findBySeverity("CRITICAL").size(),
            "unreadAlerts",      alertRepository.countByStatus("UNREAD"),
            "totalLogs",         auditLogRepository.count()
        ));
    }

    // ── System health ─────────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

        long usedMB  = mem.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long maxMB   = mem.getHeapMemoryUsage().getMax()  / (1024 * 1024);
        double pct   = maxMB > 0 ? (usedMB * 100.0 / maxMB) : 0;
        long upMins  = ManagementFactory.getRuntimeMXBean().getUptime() / 60000;

        return ResponseEntity.ok(Map.ofEntries(
            Map.entry("status",              "UP"),
            Map.entry("timestamp",           LocalDateTime.now().toString()),
            Map.entry("uptimeMinutes",       upMins),
            Map.entry("memoryUsedMB",        usedMB),
            Map.entry("memoryMaxMB",         maxMB),
            Map.entry("memoryUsedPct",       Math.round(pct)),
            Map.entry("availableProcessors", os.getAvailableProcessors()),
            Map.entry("systemLoadAvg",       os.getSystemLoadAverage()),
            Map.entry("dbUsers",             userRepository.count()),
            Map.entry("dbIncidents",         incidentRepository.count()),
            Map.entry("dbThreats",           threatRepository.count())
        ));
    }

    // ── Users ─────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        List<Map<String, Object>> enriched = userRepository.findAll().stream().map(u -> {
            List<String> appliedJobs = applicationRepository.findAll().stream()
                .filter(a -> u.getEmail().equals(a.getEmail()))
                .map(Application::getJobTitle)
                .toList();

            return java.util.Map.<String, Object>of(
                "id",          u.getId(),
                "name",        u.getName(),
                "surname",     u.getSurname() != null ? u.getSurname() : "",
                "email",       u.getEmail(),
                "cell",        u.getCell() != null ? u.getCell() : "",
                "role",        u.getRole(),
                "loginCount",  u.getLoginCount() != null ? u.getLoginCount() : 0,
                "lastLogin",   u.getLastLogin()  != null ? u.getLastLogin().toString()  : "Never",
                "createdAt",   u.getCreatedAt()  != null ? u.getCreatedAt().toString()  : "Unknown",
                "appliedJobs", appliedJobs
            );
        }).toList();

        return ResponseEntity.ok(enriched);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@NonNull @PathVariable Long id) {
        // Objects.requireNonNull makes the @NonNull contract explicit for
        // existsById / deleteById, resolving the null-safety warnings.
        if (!userRepository.existsById(Objects.requireNonNull(id))) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(Objects.requireNonNull(id));
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    // ── Applications ──────────────────────────────────────────────────────

    @GetMapping("/applications")
    public ResponseEntity<?> getApplications() {
        // FIX: Map to explicit fields so all columns render correctly in the
        // admin panel, including submittedAt (was wrongly named createdAt in template).
        List<Map<String, Object>> result = applicationRepository.findAll().stream().map(a ->
            java.util.Map.<String, Object>ofEntries(
                Map.entry("id",               a.getId()),
                Map.entry("fullName",         a.getFullName()          != null ? a.getFullName()          : ""),
                Map.entry("email",            a.getEmail()             != null ? a.getEmail()             : ""),
                Map.entry("phone",            a.getPhone()             != null ? a.getPhone()             : ""),
                Map.entry("gender",           a.getGender()            != null ? a.getGender()            : ""),
                Map.entry("nationality",      a.getNationality()       != null ? a.getNationality()       : ""),
                Map.entry("idNumber",         a.getIdNumber()          != null ? a.getIdNumber()          : ""),
                Map.entry("hasDisability",    a.getHasDisability()     != null ? a.getHasDisability()     : false),
                Map.entry("jobTitle",         a.getJobTitle()          != null ? a.getJobTitle()          : ""),
                Map.entry("jobCompany",       a.getJobCompany()        != null ? a.getJobCompany()        : ""),
                Map.entry("jobLocation",      a.getJobLocation()       != null ? a.getJobLocation()       : ""),
                Map.entry("submittedAt",      a.getSubmittedAt()       != null ? a.getSubmittedAt().toString() : ""),
                Map.entry("cvPath",           a.getCvPath()            != null ? a.getCvPath()            : ""),
                Map.entry("idDocPath",        a.getIdDocPath()         != null ? a.getIdDocPath()         : "")
            )
        ).toList();
        return ResponseEntity.ok(result);
    }

    // ── Incidents ─────────────────────────────────────────────────────────

    @GetMapping("/incidents")
    public ResponseEntity<?> getIncidents() {
        return ResponseEntity.ok(incidentRepository.findAll());
    }

    // ── Logs ──────────────────────────────────────────────────────────────

    @GetMapping("/logs")
    public ResponseEntity<?> getLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll());
    }

    // ── Alerts ────────────────────────────────────────────────────────────

    @GetMapping("/alerts")
    public ResponseEntity<?> getAlerts() {
        return ResponseEntity.ok(alertRepository.findAll());
    }

    @GetMapping("/alerts/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("count", alertRepository.countByStatus("UNREAD")));
    }
}
