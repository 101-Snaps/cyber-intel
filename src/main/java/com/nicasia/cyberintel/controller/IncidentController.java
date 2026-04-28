package com.nicasia.cyberintel.controller;

import com.nicasia.cyberintel.model.Incident;
import com.nicasia.cyberintel.repository.IncidentRepository;
import com.nicasia.cyberintel.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin(origins = "https://101-Snaps.github.io")
public class IncidentController {

    private final IncidentService incidentService;
    private final IncidentRepository incidentRepository;

    public IncidentController(IncidentService incidentService, IncidentRepository incidentRepository) {
        this.incidentService = incidentService;
        this.incidentRepository = incidentRepository;
    }

    @GetMapping
    public ResponseEntity<List<Incident>> getAll() {
        return ResponseEntity.ok(incidentService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Incident> getById(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Incident> create(@Valid @RequestBody Incident incident) {
        return ResponseEntity.ok(incidentService.create(incident));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Incident> update(@PathVariable Long id,
                                           @Valid @RequestBody Incident incident) {
        return ResponseEntity.ok(incidentService.update(id, incident));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        incidentService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Incident deleted successfully"));
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<Incident>> getBySeverity(@PathVariable String severity) {
        return ResponseEntity.ok(incidentService.getBySeverity(severity));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Incident>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(incidentService.getByStatus(status));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Incident>> search(@RequestParam String q) {
        return ResponseEntity.ok(incidentRepository.search(q));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(incidentService.getStats());
    }

    @GetMapping("/chart/severity")
    public ResponseEntity<List<Object[]>> severityChart() {
        return ResponseEntity.ok(incidentRepository.countGroupBySeverity());
    }

    @GetMapping("/chart/type")
    public ResponseEntity<List<Object[]>> typeChart() {
        return ResponseEntity.ok(incidentRepository.countGroupByType());
    }
}
