package com.repifuzz.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String incidentId; // RMGxxxxxYYYY

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id")
    private User reporterUser;

    private String reporterName;
    private String reporterEmail;
    private String reporterPhone;

    @Enumerated(EnumType.STRING)
    private IncidentType incidentType;

    @Lob
    private String description;

    @Column(columnDefinition = "json")
    private String details;

    // ========== NEW LIFECYCLE FIELDS ==========

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status = IncidentStatus.OPEN;

    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser; // Analyst/Admin assigned to this incident

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    private String resolutionSummary; // Why/how it was resolved

    @Enumerated(EnumType.STRING)
    private ResolutionType resolutionType;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_user_id")
    private User resolvedByUser; // Who resolved it

    @Column(name = "last_status_change_at")
    private LocalDateTime lastStatusChangeAt;

    // ========== AUDIT FIELDS ==========

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long viewCount = 0L; // Track views for analytics

    // ========== LIFECYCLE CALLBACKS ==========

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastStatusChangeAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = IncidentStatus.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}