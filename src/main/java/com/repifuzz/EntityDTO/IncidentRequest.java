package com.repifuzz.EntityDTO;

import com.repifuzz.Entity.IncidentType;
import lombok.Data;

@Data
public class IncidentRequest {
    private Long reporterUserId;
    private String reporterName;
    private String reporterEmail;
    private String reporterPhone;
    private IncidentType incidentType;
    private String description;
    private String details;
}