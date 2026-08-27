package com.repifuzz.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "incident_resolutions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResolutionType resolutionType;

    private String resolutionSummary;

    @Lob
    private String resolutionDetails; // Full resolution description

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_user_id", nullable = false)
    private User resolvedByUser;

    @Column(updatable = false)
    private LocalDateTime resolvedAt;

    private String referenceId; // Link to ticket/PR/commit (optional)

    @PrePersist
    protected void onCreate() {
        this.resolvedAt = LocalDateTime.now();
    }
}