package com.repifuzz.EntityDTO;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentHistoryDTO {
    private Long assignmentId;
    private String assignedToUser;
    private String assignedByUser;
    private String notes;
    private LocalDateTime assignedAt;
    private LocalDateTime unassignedAt;
    private Boolean isActive;
}