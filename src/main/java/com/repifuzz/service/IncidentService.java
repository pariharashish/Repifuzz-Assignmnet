package com.repifuzz.service;

import com.repifuzz.Entity.*;
import com.repifuzz.EntityDTO.IncidentRequest;
import com.repifuzz.EntityDTO.IncidentResponse;
import com.repifuzz.Repo.IncidentRepository;
import com.repifuzz.Repo.UserRepository;
import com.repifuzz.Repo.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public String generateUniqueIncidentId() {
        String year = String.valueOf(LocalDate.now().getYear());
        for (int i = 0; i < 20; i++) {
            int rand = ThreadLocalRandom.current().nextInt(0, 100000);
            String five = String.format("%05d", rand);
            String candidate = "RMG" + five + "-" + year;  // ✅ ADD HYPHEN HERE
            if (!incidentRepository.existsByIncidentId(candidate)) {
                return candidate;
            }
        }
        return "RMG" + UUID.randomUUID().toString().substring(0, 5) + "-" + year;  //
    }

    @Transactional
    public IncidentResponse createIncident(IncidentRequest request) {
        User reporter = userRepository.findById(request.getReporterUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getReporterUserId()));

        Incident incident = new Incident();
        incident.setIncidentId(generateUniqueIncidentId());
        incident.setReporterUser(reporter);
        incident.setReporterName(request.getReporterName());
        incident.setReporterEmail(request.getReporterEmail());
        incident.setReporterPhone(request.getReporterPhone());
        incident.setIncidentType(request.getIncidentType());
        incident.setDescription(request.getDescription());
        incident.setDetails(request.getDetails());

        // Initialize lifecycle fields
        incident.setStatus(IncidentStatus.OPEN);
        incident.setViewCount(0L);

        Incident saved = incidentRepository.save(incident);

        // Log incident creation
        AuditLog creationLog = AuditLog.builder()
                .incident(saved)
                .action(AuditAction.CREATED)
                .performedByUser(reporter)
                .fieldChanged("status")
                .oldValue(null)
                .newValue(IncidentStatus.OPEN.toString())
                .changeReason("Incident created")
                .build();
        auditLogRepository.save(creationLog);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public IncidentResponse getIncident(String incidentId) {
        Incident incident = incidentRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found"));
        return mapToResponse(incident);
    }

    private IncidentResponse mapToResponse(Incident incident) {
        return IncidentResponse.builder()
                .id(incident.getId())
                .incidentId(incident.getIncidentId())
                .reporterUserId(incident.getReporterUser() != null ? incident.getReporterUser().getId() : null)
                .reporterName(incident.getReporterName())
                .reporterEmail(incident.getReporterEmail())
                .reporterPhone(incident.getReporterPhone())
                .incidentType(incident.getIncidentType())
                .description(incident.getDescription())
                .details(incident.getDetails())
                .status(incident.getStatus())
                .severity(incident.getSeverity())
                .assignedUserId(incident.getAssignedUser() != null ? incident.getAssignedUser().getId() : null)
                .assignedUserName(incident.getAssignedUser() != null ? incident.getAssignedUser().getEmail() : null)
                .assignedAt(incident.getAssignedAt())
                .resolutionSummary(incident.getResolutionSummary())
                .resolutionType(incident.getResolutionType())
                .resolvedAt(incident.getResolvedAt())
                .resolvedByUserId(incident.getResolvedByUser() != null ? incident.getResolvedByUser().getId() : null)
                .lastStatusChangeAt(incident.getLastStatusChangeAt())
                .viewCount(incident.getViewCount())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }
}