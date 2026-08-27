package com.repifuzz.EntityDTO;

import com.repifuzz.Entity.ResolutionType;
import lombok.Data;

@Data
public class IncidentResolutionRequest {
    private String incidentId;
    private ResolutionType resolutionType;
    private String resolutionSummary;
    private String resolutionDetails;
    private String referenceId; // Optional link to ticket/PR/commit
}