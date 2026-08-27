package com.repifuzz.EntityDTO;

import com.repifuzz.Entity.IncidentStatus;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    private String incidentId;
    private IncidentStatus newStatus;
    private String changeReason; // Why the status changed
}