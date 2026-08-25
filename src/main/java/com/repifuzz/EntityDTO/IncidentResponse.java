package com.repifuzz.EntityDTO;

import com.repifuzz.Entity.IncidentType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}