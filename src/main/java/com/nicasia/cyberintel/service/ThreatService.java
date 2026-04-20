package com.nicasia.cyberintel.service;

import com.nicasia.cyberintel.exception.ResourceNotFoundException;
import com.nicasia.cyberintel.model.Threat;
import com.nicasia.cyberintel.repository.ThreatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class ThreatService {

    private static final Logger logger = LoggerFactory.getLogger(ThreatService.class);

    private final ThreatRepository threatRepository;
    private final AiService aiService;

    public ThreatService(ThreatRepository threatRepository, AiService aiService) {
        this.threatRepository = threatRepository;
        this.aiService = aiService;
    }

    @Transactional(readOnly = true)
    public List<Threat> getAll() {
        return threatRepository.findAllByOrderByDetectedAtDesc();
    }

    @Transactional(readOnly = true)
    public Threat getById(@NonNull Long id) {
        return threatRepository.findById(Objects.requireNonNull(id))
            .orElseThrow(() -> new ResourceNotFoundException("Threat", id));
    }

    public Threat create(Threat threat) {
        int frequency = threatRepository.countByCategory(threat.getCategory());
        logger.info("Creating threat: category={}, source={}, frequency={}",
            threat.getCategory(), threat.getSource(), frequency);

        AiService.AiPrediction prediction = aiService.predict(
            threat.getCategory(), threat.getSource(), frequency);

        if (threat.getRiskLevel() == null || threat.getRiskLevel().isBlank()) {
            threat.setRiskLevel(prediction.riskLevel());
        }
        threat.setAiConfidence(prediction.confidence());

        Threat saved = threatRepository.save(threat);
        logger.info("Threat id={} created — AI risk={} confidence={}",
            saved.getId(), saved.getRiskLevel(), saved.getAiConfidence());
        return saved;
    }

    public Threat update(@NonNull Long id, Threat updated) {
        logger.info("Updating threat id={}", id);
        Threat existing = getById(id);
        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setDescription(updated.getDescription());
        existing.setStatus(updated.getStatus());
        existing.setSource(updated.getSource());

        int frequency = threatRepository.countByCategory(updated.getCategory());
        AiService.AiPrediction prediction = aiService.predict(
            updated.getCategory(), updated.getSource(), frequency);
        existing.setRiskLevel(prediction.riskLevel());
        existing.setAiConfidence(prediction.confidence());

        return threatRepository.save(existing);
    }

    public void delete(@NonNull Long id) {
        logger.info("Deleting threat id={}", id);
        if (!threatRepository.existsById(Objects.requireNonNull(id))) {
            throw new ResourceNotFoundException("Threat", id);
        }
        threatRepository.deleteById(Objects.requireNonNull(id));
    }

    @Transactional(readOnly = true)
    public List<Threat> getByRiskLevel(String riskLevel) {
        return threatRepository.findByRiskLevel(riskLevel.toUpperCase());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStats() {
        return Map.of(
            "total",     threatRepository.count(),
            "active",    threatRepository.countByStatus("ACTIVE"),
            "contained", threatRepository.countByStatus("CONTAINED"),
            "critical",  threatRepository.countByRiskLevel("CRITICAL"),
            "high",      threatRepository.countByRiskLevel("HIGH")
        );
    }
}
