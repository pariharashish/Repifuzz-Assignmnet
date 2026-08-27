package com.repifuzz.Repo;

import com.repifuzz.Entity.IncidentResolution;
import com.repifuzz.Entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface IncidentResolutionRepository extends JpaRepository<IncidentResolution, Long> {
    
    Optional<IncidentResolution> findByIncident(Incident incident);
    
    List<IncidentResolution> findByResolvedByUserIdOrderByResolvedAtDesc(Long userId);
}