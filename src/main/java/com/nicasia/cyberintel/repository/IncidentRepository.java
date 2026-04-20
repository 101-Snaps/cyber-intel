package com.nicasia.cyberintel.repository;

import com.nicasia.cyberintel.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findBySeverity(String severity);
    List<Incident> findByStatus(String status);
    List<Incident> findByType(String type);

    // Ordered queries for display
    List<Incident> findAllByOrderByCreatedAtDesc();
    List<Incident> findBySeverityOrderByCreatedAtDesc(String severity);
    List<Incident> findByStatusOrderByCreatedAtDesc(String status);

    // Count helpers for stats/dashboard
    long countByStatus(String status);
    long countBySeverity(String severity);

    // Full-text style search across title + description
    @Query("SELECT i FROM Incident i WHERE " +
           "LOWER(i.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(i.type) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Incident> search(@Param("q") String query);

    // Date-range queries for analytics
    @Query("SELECT i FROM Incident i WHERE i.createdAt BETWEEN :from AND :to ORDER BY i.createdAt DESC")
    List<Incident> findByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Severity distribution for charts
    @Query("SELECT i.severity, COUNT(i) FROM Incident i GROUP BY i.severity")
    List<Object[]> countGroupBySeverity();

    // Type distribution for charts
    @Query("SELECT i.type, COUNT(i) FROM Incident i GROUP BY i.type")
    List<Object[]> countGroupByType();

    // Recent open incidents
    List<Incident> findTop10ByStatusOrderByCreatedAtDesc(String status);
}
