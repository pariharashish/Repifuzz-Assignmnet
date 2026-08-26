package com.repifuzz.service;

import com.repifuzz.Entity.Incident;
import com.repifuzz.Entity.User;
import com.repifuzz.Entity.UserRole;
import com.repifuzz.EntityDTO.IncidentRequest;
import com.repifuzz.EntityDTO.IncidentResponse;
import com.repifuzz.Repo.IncidentRepository;
import com.repifuzz.Repo.UserRepository;
import com.repifuzz.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    public String generateUniqueIncidentId() {
        String year = String.valueOf(LocalDate.now().getYear());
        for (int i = 0; i < 20; i++) {
            int rand = ThreadLocalRandom.current().nextInt(0, 100000);
            String five = String.format("%05d", rand);
            String candidate = "RMG" + five + year;
            if (!incidentRepository.existsByIncidentId(candidate)) {
                return candidate;
            }
        }
        return "RMG" + UUID.randomUUID().toString().substring(0, 5) + year;
    }

    @Transactional
    public IncidentResponse createIncident(IncidentRequest request) {
        User reporter = getAuthenticatedUser();

        Incident incident = new Incident();
        incident.setIncidentId(generateUniqueIncidentId());
        incident.setReporterUser(reporter);
        incident.setReporterName(reporter.getUsername());
        incident.setReporterEmail(reporter.getEmail());
        incident.setReporterPhone(reporter.getPhone());
        incident.setIncidentType(request.getIncidentType());
        incident.setDescription(request.getDescription());
        incident.setDetails(request.getDetails());

        Incident saved = incidentRepository.save(incident);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public IncidentResponse getIncident(String incidentId) {
        Incident incident = incidentRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));

        User requester = getAuthenticatedUser();
        if (isReporter(requester) && !requester.getId().equals(incident.getReporterUser().getId())) {
            throw new AccessDeniedException("You are not permitted to access this incident");
        }

        return mapToResponse(incident);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private boolean isReporter(User user) {
        return user.getRole() == null || user.getRole() == UserRole.REPORTER;
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
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }
}
