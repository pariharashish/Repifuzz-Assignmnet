package com.repifuzz.Repo;

import com.repifuzz.Entity.IncidentAssignment;
import com.repifuzz.Entity.Incident;
import com.repifuzz.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface IncidentAssignmentRepository extends JpaRepository<IncidentAssignment, Long> {
    
    // Find current active assignment for an incident
    Optional<IncidentAssignment> findByIncidentAndIsActiveTrue(Incident incident);
    
    // Find all assignments for a user
    List<IncidentAssignment> findByAssignedToUserAndIsActiveTrue(User user);
    
    // Find all assignments for an incident (history)
    List<IncidentAssignment> findByIncidentOrderByAssignedAtDesc(Incident incident);
    
    // Find active assignments for a user
    @Query("SELECT a FROM IncidentAssignment a WHERE a.assignedToUser = :user AND a.isActive = true")
    List<IncidentAssignment> findActiveAssignmentsByUser(User user);
}