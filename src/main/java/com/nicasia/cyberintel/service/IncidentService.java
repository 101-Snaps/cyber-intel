package com.nicasia.cyberintel.service;

import com.nicasia.cyberintel.exception.ResourceNotFoundException;
import com.nicasia.cyberintel.model.Incident;
import com.nicasia.cyberintel.repository.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class IncidentService {

    private static final Logger logger = LoggerFactory.getLogger(IncidentService.class);

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Transactional(readOnly = true)
    public List<Incident> getAll() {
        logger.debug("Fetching all incidents");
        return incidentRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Incident getById(@NonNull Long id) {
        // Objects.requireNonNull satisfies the @NonNull contract that
        // JpaRepository.findById expects, suppressing the null-safety warning.
        return incidentRepository.findById(Objects.requireNonNull(id))
            .orElseThrow(() -> new ResourceNotFoundException("Incident", id));
    }

    public Incident create(Incident incident) {
        logger.info("Creating incident: title={}, severity={}", incident.getTitle(), incident.getSeverity());
        Incident saved = incidentRepository.save(incident);
        logger.info("Incident created with id={}", saved.getId());
        return saved;
    }

    public Incident update(@NonNull Long id, Incident updated) {
        logger.info("Updating incident id={}", id);
        Incident existing = getById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setSeverity(updated.getSeverity());
        existing.setType(updated.getType());
        existing.setStatus(updated.getStatus());
        existing.setAffectedSystem(updated.getAffectedSystem());
        existing.setReportedBy(updated.getReportedBy());
        if ("RESOLVED".equals(updated.getStatus()) && existing.getResolvedAt() == null) {
            existing.setResolvedAt(LocalDateTime.now());
            logger.info("Incident id={} marked RESOLVED", id);
        }
        return incidentRepository.save(existing);
    }

    public void delete(@NonNull Long id) {
        logger.info("Deleting incident id={}", id);
        if (!incidentRepository.existsById(Objects.requireNonNull(id))) {
            throw new ResourceNotFoundException("Incident", id);
        }
        incidentRepository.deleteById(Objects.requireNonNull(id));
    }

    @Transactional(readOnly = true)
    public List<Incident> getBySeverity(String severity) {
        return incidentRepository.findBySeverity(severity.toUpperCase());
    }

    @Transactional(readOnly = true)
    public List<Incident> getByStatus(String status) {
        return incidentRepository.findByStatus(status.toUpperCase());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStats() {
        return Map.of(
            "total",      incidentRepository.count(),
            "open",       incidentRepository.countByStatus("OPEN"),
            "inProgress", incidentRepository.countByStatus("IN_PROGRESS"),
            "resolved",   incidentRepository.countByStatus("RESOLVED"),
            "critical",   incidentRepository.countBySeverity("CRITICAL"),
            "high",       incidentRepository.countBySeverity("HIGH")
        );
    }
}
