package com.repifuzz.EntityDTO;

import com.repifuzz.Entity.IncidentSeverity;
import lombok.Data;

@Data
public class SeverityAssignmentRequest {
    private String incidentId;
    private IncidentSeverity severity;
    private String reason; // Why this severity
}