package com.repifuzz.EntityDTO;

import com.repifuzz.Entity.IncidentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IncidentRequest {
    @NotNull(message = "Incident type is required")
    private IncidentType incidentType;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 10000, message = "Details must not exceed 10000 characters")
    private String details;
}
