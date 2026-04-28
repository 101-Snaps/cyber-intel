package com.nicasia.cyberintel.controller;

import com.nicasia.cyberintel.model.Threat;
import com.nicasia.cyberintel.repository.ThreatRepository;
import com.nicasia.cyberintel.service.ThreatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/threats")
public class ThreatController {

    private final ThreatService threatService;
    private final ThreatRepository threatRepository;

    public ThreatController(ThreatService threatService, ThreatRepository threatRepository) {
        this.threatService = threatService;
        this.threatRepository = threatRepository;
    }

    @GetMapping
    public ResponseEntity<List<Threat>> getAll() {
        return ResponseEntity.ok(threatService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Threat> getById(@PathVariable Long id) {
        return ResponseEntity.ok(threatService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Threat> create(@Valid @RequestBody Threat threat) {
        return ResponseEntity.ok(threatService.create(threat));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Threat> update(@PathVariable Long id,
                                         @Valid @RequestBody Threat threat) {
        return ResponseEntity.ok(threatService.update(id, threat));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        threatService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Threat deleted successfully"));
    }

    @GetMapping("/risk/{riskLevel}")
    public ResponseEntity<List<Threat>> getByRiskLevel(@PathVariable String riskLevel) {
        return ResponseEntity.ok(threatService.getByRiskLevel(riskLevel));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Threat>> search(@RequestParam String q) {
        return ResponseEntity.ok(threatRepository.search(q));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(threatService.getStats());
    }

    @GetMapping("/chart/category")
    public ResponseEntity<List<Object[]>> categoryChart() {
        return ResponseEntity.ok(threatRepository.countGroupByCategory());
    }

    @GetMapping("/chart/risk")
    public ResponseEntity<List<Object[]>> riskChart() {
        return ResponseEntity.ok(threatRepository.countGroupByRiskLevel());
    }
}

