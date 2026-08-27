package com.repifuzz.EntityDTO;

import com.repifuzz.Entity.AuditAction;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogDTO {
    private Long id;
    private String incidentId;
    private AuditAction action;
    private String performedByUser;
    private String fieldChanged;
    private String oldValue;
    private String newValue;
    private String changeReason;
    private LocalDateTime timestamp;
}