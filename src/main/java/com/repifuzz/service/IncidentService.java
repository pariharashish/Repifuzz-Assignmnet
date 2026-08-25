package com.repifuzz.service;

import com.repifuzz.Entity.Incident;
import com.repifuzz.Entity.User;
import com.repifuzz.EntityDTO.IncidentRequest;
import com.repifuzz.EntityDTO.IncidentResponse;
import com.repifuzz.Repo.IncidentRepository;
import com.repifuzz.Repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

        Incident saved = incidentRepository.save(incident);

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
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }
}