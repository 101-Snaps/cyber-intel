package com.nicasia.cyberintel.repository;

import com.nicasia.cyberintel.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatus(String status);
    List<Alert> findByUserEmail(String email);
    List<Alert> findBySeverity(String severity);
    long countByStatus(String status);
}