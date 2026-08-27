// src/main/java/com/repifuzz/controller/IncidentLifecycleController.java
package com.repifuzz.controller;

import com.repifuzz.Entity.User;
import com.repifuzz.EntityDTO.*;
import com.repifuzz.Repo.UserRepository;
import com.repifuzz.service.IncidentLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ims/incidents/lifecycle")
@RequiredArgsConstructor
public class IncidentLifecycleController {

    private final IncidentLifecycleService lifecycleService;
    private final UserRepository userRepository;

    /**
     * Helper method to get current user from authentication
     */
    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName(); // Get email/username
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Update incident status
     * POST /api/ims/incidents/lifecycle/status
     */
    @PostMapping("/status")
    public ResponseEntity<IncidentResponse> updateStatus(
            @RequestBody StatusUpdateRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication); // ✅ FIX: Get from database
        return ResponseEntity.ok(lifecycleService.updateStatus(request, currentUser));
    }

    /**
     * Assign incident to analyst
     * POST /api/ims/incidents/lifecycle/assign
     */
    @PostMapping("/assign")
    public ResponseEntity<IncidentResponse> assignIncident(
            @RequestBody IncidentAssignmentRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication); // ✅ FIX: Get from database
        return ResponseEntity.ok(lifecycleService.assignIncident(request, currentUser));
    }

    /**
     * Unassign incident
     * DELETE /api/ims/incidents/lifecycle/assign/{incidentId}
     */
    @DeleteMapping("/assign/{incidentId}")
    public ResponseEntity<IncidentResponse> unassignIncident(
            @PathVariable String incidentId,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication); // ✅ FIX: Get from database
        return ResponseEntity.ok(lifecycleService.unassignIncident(incidentId, currentUser));
    }

    /**
     * Assign severity to incident
     * POST /api/ims/incidents/lifecycle/severity
     */
    @PostMapping("/severity")
    public ResponseEntity<IncidentResponse> assignSeverity(
            @RequestBody SeverityAssignmentRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication); // ✅ FIX: Get from database
        return ResponseEntity.ok(lifecycleService.assignSeverity(request, currentUser));
    }

    /**
     * Resolve incident
     * POST /api/ims/incidents/lifecycle/resolve
     */
    @PostMapping("/resolve")
    public ResponseEntity<IncidentResponse> resolveIncident(
            @RequestBody IncidentResolutionRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication); // ✅ FIX: Get from database
        return ResponseEntity.ok(lifecycleService.resolveIncident(request, currentUser));
    }

    /**
     * Reopen incident
     * POST /api/ims/incidents/lifecycle/reopen/{incidentId}
     */
    @PostMapping("/reopen/{incidentId}")
    public ResponseEntity<IncidentResponse> reopenIncident(
            @PathVariable String incidentId,
            @RequestParam String reason,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication); // ✅ FIX: Get from database
        return ResponseEntity.ok(lifecycleService.reopenIncident(incidentId, reason, currentUser));
    }

    /**
     * Get incident with audit history
     * GET /api/ims/incidents/lifecycle/{incidentId}/full
     */
    @GetMapping("/{incidentId}/full")
    public ResponseEntity<IncidentResponse> getIncidentWithAudit(
            @PathVariable String incidentId) {
        return ResponseEntity.ok(lifecycleService.getIncidentWithAudit(incidentId));
    }

    /**
     * Get full audit trail
     * GET /api/ims/incidents/lifecycle/{incidentId}/audit
     */
    @GetMapping("/{incidentId}/audit")
    public ResponseEntity<List<AuditLogDTO>> getAuditTrail(
            @PathVariable String incidentId) {
        return ResponseEntity.ok(lifecycleService.getFullAuditTrail(incidentId));
    }
}