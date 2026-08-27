package com.repifuzz.EntityDTO;

import lombok.Data;

@Data
public class IncidentAssignmentRequest {
    private String incidentId;
    private Long assignedToUserId;
    private String notes; // Assignment context/instructions
}