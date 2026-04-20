package com.nicasia.cyberintel.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;

import com.nicasia.cyberintel.model.Incident;
import com.nicasia.cyberintel.model.Threat;
import com.nicasia.cyberintel.repository.AlertRepository;
import com.nicasia.cyberintel.repository.IncidentRepository;
import com.nicasia.cyberintel.repository.ThreatRepository;
import com.nicasia.cyberintel.service.IncidentService;
import com.nicasia.cyberintel.service.SecurityMonitorService;
import com.nicasia.cyberintel.service.ThreatService;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class CyberIntelGraphqlController {

    private final IncidentService incidentService;
    private final ThreatService threatService;
    private final IncidentRepository incidentRepository;
    private final ThreatRepository threatRepository;
    private final AlertRepository alertRepository;
    private final SecurityMonitorService securityMonitorService;

    public CyberIntelGraphqlController(IncidentService incidentService,
                                       ThreatService threatService,
                                       IncidentRepository incidentRepository,
                                       ThreatRepository threatRepository,
                                       AlertRepository alertRepository,
                                       SecurityMonitorService securityMonitorService) {
        this.incidentService = incidentService;
        this.threatService = threatService;
        this.incidentRepository = incidentRepository;
        this.threatRepository = threatRepository;
        this.alertRepository = alertRepository;
        this.securityMonitorService = securityMonitorService;
    }

    // ── INCIDENT QUERIES ─────────────────────────────────────────────────

    @QueryMapping
    public List<Incident> incidents() {
        return incidentService.getAll();
    }

    @QueryMapping
    public Incident incident(@Argument Long id) {
        return incidentService.getById(id);
    }

    @QueryMapping
    public List<Incident> incidentsBySeverity(@Argument String severity) {
        return incidentService.getBySeverity(severity);
    }

    @QueryMapping
    public List<Incident> incidentsByStatus(@Argument String status) {
        return incidentService.getByStatus(status);
    }

    @QueryMapping
    public Map<String, Long> incidentStats() {
        return incidentService.getStats();
    }

    @QueryMapping
    public List<Incident> searchIncidents(@Argument String query) {
        return incidentRepository.search(query);
    }

    // ── THREAT QUERIES ───────────────────────────────────────────────────

    @QueryMapping
    public List<Threat> threats() {
        return threatService.getAll();
    }

    @QueryMapping
    public Threat threat(@Argument Long id) {
        return threatService.getById(id);
    }

    @QueryMapping
    public List<Threat> threatsByRiskLevel(@Argument String riskLevel) {
        return threatService.getByRiskLevel(riskLevel);
    }

    @QueryMapping
    public Map<String, Long> threatStats() {
        return threatService.getStats();
    }

    @QueryMapping
    public List<Threat> searchThreats(@Argument String query) {
        return threatRepository.search(query);
    }

    // ── DASHBOARD SUMMARY ────────────────────────────────────────────────

    @QueryMapping
    public Map<String, Long> dashboardSummary() {
        return Map.of(
            "totalIncidents",    incidentRepository.count(),
            "openIncidents",     incidentRepository.countByStatus("OPEN"),
            "criticalIncidents", incidentRepository.countBySeverity("CRITICAL"),
            "totalThreats",      threatRepository.count(),
            "activeThreats",     threatRepository.countByStatus("ACTIVE"),
            "criticalThreats",   threatRepository.countByRiskLevel("CRITICAL"),
            "unreadAlerts",      alertRepository.countByStatus("UNREAD")
        );
    }

    // ── INCIDENT MUTATIONS ───────────────────────────────────────────────

    @MutationMapping
    public Incident createIncident(@Argument Map<String, Object> input) {
        return incidentService.create(mapToIncident(input));
    }

    @MutationMapping
    public Incident updateIncident(@Argument Long id, @Argument Map<String, Object> input) {
        return incidentService.update(id, mapToIncident(input));
    }

    @MutationMapping
    public boolean deleteIncident(@Argument Long id) {
        incidentService.delete(id);
        return true;
    }

    // ── THREAT MUTATIONS ─────────────────────────────────────────────────

    @MutationMapping
    public Threat createThreat(@Argument Map<String, Object> input) {
        return threatService.create(mapToThreat(input));
    }

    @MutationMapping
    public Threat updateThreat(@Argument Long id, @Argument Map<String, Object> input) {
        return threatService.update(id, mapToThreat(input));
    }

    @MutationMapping
    public boolean deleteThreat(@Argument Long id) {
        threatService.delete(id);
        return true;
    }

    // ── ALERT MUTATIONS ──────────────────────────────────────────────────

    @MutationMapping
    public boolean markAlertRead(@Argument Long id) {
        securityMonitorService.markAlertRead(id);
        return true;
    }

    // ── HELPERS ──────────────────────────────────────────────────────────

    private Incident mapToIncident(Map<String, Object> input) {
        Incident i = new Incident();
        i.setTitle((String) input.get("title"));
        i.setDescription((String) input.get("description"));
        i.setSeverity((String) input.get("severity"));
        i.setType((String) input.get("type"));
        i.setStatus((String) input.getOrDefault("status", "OPEN"));
        i.setAffectedSystem((String) input.get("affectedSystem"));
        i.setReportedBy((String) input.get("reportedBy"));
        return i;
    }

    private Threat mapToThreat(Map<String, Object> input) {
        Threat t = new Threat();
        t.setName((String) input.get("name"));
        t.setCategory((String) input.get("category"));
        t.setRiskLevel((String) input.get("riskLevel"));
        t.setDescription((String) input.get("description"));
        t.setStatus((String) input.getOrDefault("status", "ACTIVE"));
        t.setSource((String) input.get("source"));
        return t;
    }
}
