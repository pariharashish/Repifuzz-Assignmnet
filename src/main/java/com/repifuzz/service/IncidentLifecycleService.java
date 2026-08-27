package com.repifuzz.service;

import com.repifuzz.Entity.*;
import com.repifuzz.EntityDTO.*;
import com.repifuzz.Repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentLifecycleService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final IncidentAssignmentRepository assignmentRepository;
    private final IncidentResolutionRepository resolutionRepository;
    private final AuditLogRepository auditLogRepository;

    /**
     * Update incident status with audit logging
     */
    @Transactional
    public IncidentResponse updateStatus(StatusUpdateRequest request, User currentUser) {
        Incident incident = incidentRepository.findByIncidentId(request.getIncidentId())
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        IncidentStatus oldStatus = incident.getStatus();
        incident.setStatus(request.getNewStatus());
        incident.setLastStatusChangeAt(LocalDateTime.now());

        Incident updated = incidentRepository.save(incident);

        // Log the status change
        createAuditLog(
            incident,
            AuditAction.STATUS_CHANGED,
            currentUser,
            "status",
            oldStatus.toString(),
            request.getNewStatus().toString(),
            request.getChangeReason()
        );

        log.info("Incident {} status changed from {} to {} by user {}",
            incident.getIncidentId(), oldStatus, request.getNewStatus(), currentUser.getEmail());

        return mapToResponse(updated);
    }

    /**
     * Assign incident to an analyst
     */
    @Transactional
    public IncidentResponse assignIncident(IncidentAssignmentRequest request, User currentUser) {
        Incident incident = incidentRepository.findByIncidentId(request.getIncidentId())
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        User assignedUser = userRepository.findById(request.getAssignedToUserId())
                .orElseThrow(() -> new RuntimeException("Assigned user not found"));

        // Validate role - only ANALYST or ADMIN can be assigned
        if (assignedUser.getRole() == UserRole.REPORTER) {
            throw new RuntimeException("Can only assign to ANALYST or ADMIN users");
        }

        // Unassign previous assignee if exists
        if (incident.getAssignedUser() != null) {
            IncidentAssignment previousAssignment =
                assignmentRepository.findByIncidentAndIsActiveTrue(incident).orElse(null);
            if (previousAssignment != null) {
                previousAssignment.setIsActive(false);
                previousAssignment.setUnassignedAt(LocalDateTime.now());
                assignmentRepository.save(previousAssignment);
            }
        }

        // Create new assignment
        IncidentAssignment assignment = IncidentAssignment.builder()
                .incident(incident)
                .assignedToUser(assignedUser)
                .assignedByUser(currentUser)
                .notes(request.getNotes())
                .isActive(true)
                .build();

        assignmentRepository.save(assignment);

        // Update incident
        incident.setAssignedUser(assignedUser);
        incident.setAssignedAt(LocalDateTime.now());
        incident.setStatus(IncidentStatus.ASSIGNED);
        incident.setLastStatusChangeAt(LocalDateTime.now());

        Incident updated = incidentRepository.save(incident);

        // Audit log
        createAuditLog(
            incident,
            AuditAction.ASSIGNED,
            currentUser,
            "assignedUser",
            "unassigned",
            assignedUser.getEmail(),
            request.getNotes()
        );

        log.info("Incident {} assigned to {} by {}",
            incident.getIncidentId(), assignedUser.getEmail(), currentUser.getEmail());

        return mapToResponse(updated);
    }

    /**
     * Unassign incident
     */
    @Transactional
    public IncidentResponse unassignIncident(String incidentId, User currentUser) {
        Incident incident = incidentRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        if (incident.getAssignedUser() == null) {
            throw new RuntimeException("Incident is not currently assigned");
        }

        User previousAssignee = incident.getAssignedUser();

        // Deactivate current assignment
        IncidentAssignment currentAssignment =
            assignmentRepository.findByIncidentAndIsActiveTrue(incident)
                .orElse(null);

        if (currentAssignment != null) {
            currentAssignment.setIsActive(false);
            currentAssignment.setUnassignedAt(LocalDateTime.now());
            assignmentRepository.save(currentAssignment);
        }

        // Update incident
        incident.setAssignedUser(null);
        incident.setAssignedAt(null);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setLastStatusChangeAt(LocalDateTime.now());

        Incident updated = incidentRepository.save(incident);

        // Audit log
        createAuditLog(
            incident,
            AuditAction.UNASSIGNED,
            currentUser,
            "assignedUser",
            previousAssignee.getEmail(),
            "unassigned",
            null
        );

        return mapToResponse(updated);
    }

    /**
     * Assign severity to incident
     */
    @Transactional
    public IncidentResponse assignSeverity(SeverityAssignmentRequest request, User currentUser) {
        Incident incident = incidentRepository.findByIncidentId(request.getIncidentId())
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        IncidentSeverity oldSeverity = incident.getSeverity();
        incident.setSeverity(request.getSeverity());

        Incident updated = incidentRepository.save(incident);

        // Audit log
        createAuditLog(
            incident,
            AuditAction.SEVERITY_ASSIGNED,
            currentUser,
            "severity",
            oldSeverity != null ? oldSeverity.toString() : "unset",
            request.getSeverity().toString(),
            request.getReason()
        );

        log.info("Incident {} severity set to {} by {}",
            incident.getIncidentId(), request.getSeverity(), currentUser.getEmail());

        return mapToResponse(updated);
    }

    /**
     * Resolve an incident
     */
    @Transactional
    public IncidentResponse resolveIncident(IncidentResolutionRequest request, User currentUser) {
        Incident incident = incidentRepository.findByIncidentId(request.getIncidentId())
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        // Create resolution record
        IncidentResolution resolution = IncidentResolution.builder()
                .incident(incident)
                .resolutionType(request.getResolutionType())
                .resolutionSummary(request.getResolutionSummary())
                .resolutionDetails(request.getResolutionDetails())
                .resolvedByUser(currentUser)
                .referenceId(request.getReferenceId())
                .build();

        resolutionRepository.save(resolution);

        // Update incident
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolutionType(request.getResolutionType());
        incident.setResolutionSummary(request.getResolutionSummary());
        incident.setResolvedByUser(currentUser);
        incident.setResolvedAt(LocalDateTime.now());
        incident.setLastStatusChangeAt(LocalDateTime.now());

        Incident updated = incidentRepository.save(incident);

        // Audit log
        createAuditLog(
            incident,
            AuditAction.RESOLVED,
            currentUser,
            "status",
            incident.getStatus().toString(),
            IncidentStatus.RESOLVED.toString(),
            request.getResolutionSummary()
        );

        log.info("Incident {} resolved by {} with type {}",
            incident.getIncidentId(), currentUser.getEmail(), request.getResolutionType());

        return mapToResponse(updated);
    }

    /**
     * Reopen a resolved incident
     */
    @Transactional
    public IncidentResponse reopenIncident(String incidentId, String reason, User currentUser) {
        Incident incident = incidentRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        if (incident.getStatus() != IncidentStatus.RESOLVED && incident.getStatus() != IncidentStatus.CLOSED) {
            throw new RuntimeException("Only resolved or closed incidents can be reopened");
        }

        IncidentStatus oldStatus = incident.getStatus();
        incident.setStatus(IncidentStatus.OPEN);
        incident.setResolvedAt(null);
        incident.setResolutionType(null);
        incident.setResolvedByUser(null);
        incident.setLastStatusChangeAt(LocalDateTime.now());

        Incident updated = incidentRepository.save(incident);

        // Audit log
        createAuditLog(
            incident,
            AuditAction.REOPENED,
            currentUser,
            "status",
            oldStatus.toString(),
            IncidentStatus.OPEN.toString(),
            reason
        );

        log.info("Incident {} reopened by {} - Reason: {}",
            incident.getIncidentId(), currentUser.getEmail(), reason);

        return mapToResponse(updated);
    }

    /**
     * Record a view for an incident (for analytics)
     */
    @Transactional
    public void recordIncidentView(String incidentId) {
        Incident incident = incidentRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        incident.setViewCount(incident.getViewCount() + 1);
        incidentRepository.save(incident);
    }

    /**
     * Get incident with full audit history
     */
    @Transactional(readOnly = true)
    public IncidentResponse getIncidentWithAudit(String incidentId) {
        Incident incident = incidentRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        // Get recent audit logs (last 10)
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> auditLogs = auditLogRepository.findByIncidentOrderByTimestampDesc(incident, pageable);

        IncidentResponse response = mapToResponse(incident);
        response.setRecentAuditLogs(
            auditLogs.stream()
                .map(this::mapAuditLogToDTO)
                .collect(Collectors.toList())
        );

        return response;
    }

    /**
     * Get assignment history for an incident
     */
    @Transactional(readOnly = true)
    public List<AssignmentHistoryDTO> getAssignmentHistory(String incidentId) {
        Incident incident = incidentRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        return assignmentRepository.findByIncidentOrderByAssignedAtDesc(incident)
                .stream()
                .map(assignment -> AssignmentHistoryDTO.builder()
                    .assignmentId(assignment.getId())
                    .assignedToUser(assignment.getAssignedToUser().getEmail())
                    .assignedByUser(assignment.getAssignedByUser().getEmail())
                    .notes(assignment.getNotes())
                    .assignedAt(assignment.getAssignedAt())
                    .unassignedAt(assignment.getUnassignedAt())
                    .isActive(assignment.getIsActive())
                    .build())
                .collect(Collectors.toList());
    }

    /**
     * Get full audit trail for an incident
     */
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getFullAuditTrail(String incidentId) {
        Incident incident = incidentRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        return auditLogRepository.findByIncidentOrderByTimestampDesc(incident)
                .stream()
                .map(this::mapAuditLogToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Internal: Create audit log entry
     */
    @Transactional
    private void createAuditLog(Incident incident, AuditAction action, User performedBy,
                               String fieldChanged, String oldValue, String newValue, String reason) {
        AuditLog log = AuditLog.builder()
                .incident(incident)
                .action(action)
                .performedByUser(performedBy)
                .fieldChanged(fieldChanged)
                .oldValue(oldValue)
                .newValue(newValue)
                .changeReason(reason)
                .build();

        auditLogRepository.save(log);
    }

    /**
     * Map entity to DTO
     */
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

    /**
     * Map audit log to DTO
     */
    private AuditLogDTO mapAuditLogToDTO(AuditLog log) {
        return AuditLogDTO.builder()
                .id(log.getId())
                .incidentId(log.getIncident().getIncidentId())
                .action(log.getAction())
                .performedByUser(log.getPerformedByUser().getEmail())
                .fieldChanged(log.getFieldChanged())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .changeReason(log.getChangeReason())
                .timestamp(log.getTimestamp())
                .build();
    }
}