package com.repifuzz.Repo;

import com.repifuzz.Entity.Incident;
import com.repifuzz.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findByIncidentId(String incidentId);

    boolean existsByIncidentId(String incidentId);

    List<Incident> findAllByReporterUser(User user);
}