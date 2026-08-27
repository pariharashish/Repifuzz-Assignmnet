package com.repifuzz.EntityDTO;

import com.repifuzz.Entity.IncidentType;

import lombok.Data;

@Data
public class IncidentRequest {
    private Long reporterUserId;           // Reference to User entity
    private String reporterName;
    private String reporterEmail;
    private String reporterPhone;
    private IncidentType incidentType;     // Enum: enum for incident types
    private String description;
    private String details;
}
