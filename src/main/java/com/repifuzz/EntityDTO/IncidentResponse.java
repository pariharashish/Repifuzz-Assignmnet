package com.repifuzz.EntityDTO;

import com.repifuzz.Entity.*;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class IncidentResponse {
    private Long id;
    private String incidentId;
    private Long reporterUserId;
    private String reporterName;
    private String reporterEmail;
    private String reporterPhone;
    private IncidentType incidentType;
    private String description;
    private String details;

    // ========== NEW LIFECYCLE FIELDS ==========
    private IncidentStatus status;
    private IncidentSeverity severity;
    private Long assignedUserId;
    private String assignedUserName;
    private LocalDateTime assignedAt;
    private String resolutionSummary;
    private ResolutionType resolutionType;
    private LocalDateTime resolvedAt;
    private Long resolvedByUserId;
    private LocalDateTime lastStatusChangeAt;
    private Long viewCount;

    // ========== AUDIT INFO ==========
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Optional: Include audit trail summary
    private List<AuditLogDTO> recentAuditLogs;
}