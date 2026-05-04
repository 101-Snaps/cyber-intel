package com.nicasia.cyberintel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);
    private static final String AI_URL = System.getenv().getOrDefault(
    "AI_SERVICE_URL", "https://ai-services-nkbg.onrender.com") + "/predict-threat";

    private final RestTemplate restTemplate = new RestTemplate();

    /** Immutable result record returned to callers */
    public record AiPrediction(String riskLevel, double confidence) {}

    /**
     * Call the Python/PyTorch service to predict threat risk.
     * Falls back to rule-based logic if the AI service is offline.
     */
    public AiPrediction predict(String category, String source, int frequency) {
        try {
            Map<String, Object> payload = Map.of(
                "category",  category  != null ? category  : "",
                "source",    source    != null ? source    : "Unknown",
                "frequency", frequency
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(AI_URL, payload, Map.class);

            if (response != null && response.containsKey("riskLevel")) {
                String risk       = (String) response.get("riskLevel");
                double confidence = response.containsKey("confidence")
                    ? ((Number) response.get("confidence")).doubleValue() : 0.5;
                logger.info("AI prediction: category={} → risk={} confidence={}", category, risk, confidence);
                return new AiPrediction(risk, confidence);
            }

        } catch (Exception e) {
            logger.warn("AI service unavailable — falling back to rule-based. Reason: {}", e.getMessage());
        }

        return ruleBasedFallback(category, frequency);
    }

    /** Convenience method used by legacy code that only needs the risk level string */
    public String predictRiskLevel(String category, String source, int frequency) {
        return predict(category, source, frequency).riskLevel();
    }

    private AiPrediction ruleBasedFallback(String category, int frequency) {
        if (category == null) return new AiPrediction("MEDIUM", 0.5);
        String risk = switch (category.toUpperCase()) {
            case "RANSOMWARE", "DATA BREACH" -> frequency > 2 ? "CRITICAL" : "HIGH";
            case "DDOS"                      -> frequency > 3 ? "HIGH"     : "MEDIUM";
            case "TROJAN", "WORM"            -> frequency > 2 ? "HIGH"     : "MEDIUM";
            case "PHISHING"                  -> frequency > 5 ? "MEDIUM"   : "LOW";
            default                          -> "MEDIUM";
        };
        logger.info("Rule-based fallback: category={} → {}", category, risk);
        return new AiPrediction(risk, 0.0); // 0.0 confidence signals fallback was used
    }
}
