package com.repifuzz.EntityDTO;

import com.repifuzz.Entity.IncidentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IncidentRequest {
    @NotNull(message = "Reporter user ID is required")
    @Positive(message = "Reporter user ID must be positive")
    private Long reporterUserId;

    @NotBlank(message = "Reporter name is required")
    @Size(max = 100, message = "Reporter name must not exceed 100 characters")
    private String reporterName;

    @NotBlank(message = "Reporter email is required")
    @Email(message = "Reporter email must be valid")
    @Size(max = 254, message = "Reporter email must not exceed 254 characters")
    private String reporterEmail;

    @NotBlank(message = "Reporter phone is required")
    @Size(max = 25, message = "Reporter phone must not exceed 25 characters")
    private String reporterPhone;

    @NotNull(message = "Incident type is required")
    private IncidentType incidentType;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 10000, message = "Details must not exceed 10000 characters")
    private String details;
}
