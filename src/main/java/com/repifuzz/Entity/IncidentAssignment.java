package com.repifuzz.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "incident_assignments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id", nullable = false)
    private User assignedToUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id", nullable = false)
    private User assignedByUser; // Who made the assignment

    private String notes; // Assignment notes/context

    @Column(updatable = false)
    private LocalDateTime assignedAt;

    // When unassigned (if applicable)
    private LocalDateTime unassignedAt;

    private Boolean isActive = true; // Current active assignment

    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
    }
}