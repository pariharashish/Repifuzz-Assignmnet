package com.repifuzz.Repo;

import com.repifuzz.Entity.AuditLog;
import com.repifuzz.Entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Get audit trail for an incident (paginated)
    List<AuditLog> findByIncidentOrderByTimestampDesc(Incident incident, Pageable pageable);
    
    // Get all audit logs for an incident
    List<AuditLog> findByIncidentOrderByTimestampDesc(Incident incident);
    
    // Get all changes made by a user
    List<AuditLog> findByPerformedByUserIdOrderByTimestampDesc(Long userId);
}