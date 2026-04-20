package com.nicasia.cyberintel.repository;

import com.nicasia.cyberintel.model.Threat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ThreatRepository extends JpaRepository<Threat, Long> {

    List<Threat> findByCategory(String category);
    List<Threat> findByRiskLevel(String riskLevel);
    List<Threat> findByStatus(String status);

    List<Threat> findAllByOrderByDetectedAtDesc();

    int countByCategory(String category);
    long countByStatus(String status);
    long countByRiskLevel(String riskLevel);

    @Query("SELECT t.category, COUNT(t) FROM Threat t GROUP BY t.category")
    List<Object[]> countGroupByCategory();

    @Query("SELECT t.riskLevel, COUNT(t) FROM Threat t GROUP BY t.riskLevel")
    List<Object[]> countGroupByRiskLevel();

    @Query("SELECT t FROM Threat t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(t.category) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Threat> search(@Param("q") String query);
}
