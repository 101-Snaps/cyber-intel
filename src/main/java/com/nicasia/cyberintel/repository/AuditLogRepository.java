package com.nicasia.cyberintel.repository;

import com.nicasia.cyberintel.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserEmail(String email);
    List<AuditLog> findByAction(String action);
    List<AuditLog> findByIpAddress(String ipAddress);
}