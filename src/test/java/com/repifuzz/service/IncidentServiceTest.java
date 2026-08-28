package com.repifuzz.service;

import com.repifuzz.Entity.Incident;
import com.repifuzz.Entity.User;
import com.repifuzz.Entity.UserRole;
import com.repifuzz.EntityDTO.IncidentRequest;
import com.repifuzz.EntityDTO.IncidentResponse;
import com.repifuzz.Repo.IncidentRepository;
import com.repifuzz.Repo.UserRepository;
import com.repifuzz.Repo.AuditLogRepository; // Added import
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock // FIX: Added missing AuditLogRepository mock to prevent NullPointerException
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void generateUniqueIncidentId_returnsFormattedId() {
        when(incidentRepository.existsByIncidentId(anyString())).thenReturn(false);

        String id = incidentService.generateUniqueIncidentId();

        assertThat(id).startsWith("RMG");
        String year = String.valueOf(java.time.LocalDate.now().getYear());
        assertThat(id).endsWith(year);
        assertThat(id.length()).isGreaterThanOrEqualTo(3 + 5 + year.length());
    }

    @Test
    void generateUniqueIncidentId_handlesCollisions() {
        when(incidentRepository.existsByIncidentId(anyString()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        String id = incidentService.generateUniqueIncidentId();
        assertThat(id).startsWith("RMG");
    }

    @Test
    void createIncident_persistsAndMapsResponse() {
        User reporter = new User();
        reporter.setId(10L);
        reporter.setUsername("rep");
        reporter.setEmail("rep@example.com");
        reporter.setPhone("123");
        reporter.setRole(UserRole.REPORTER);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(reporter, null, Collections.emptyList()));
        SecurityContextHolder.setContext(context);

        lenient().when(userRepository.findById(any())).thenReturn(Optional.of(reporter));
        lenient().when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(reporter));

        IncidentRequest req = new IncidentRequest();
        req.setDescription("desc");
        req.setDetails("{\"k\":\"v\"}");

        Incident saved = new Incident();
        saved.setId(5L);
        saved.setIncidentId("RMG000012025");
        saved.setReporterUser(reporter);
        saved.setReporterName(reporter.getUsername());
        saved.setReporterEmail(reporter.getEmail());
        saved.setDescription(req.getDescription());
        saved.setDetails(req.getDetails());
        saved.setCreatedAt(LocalDateTime.now());
        saved.setUpdatedAt(LocalDateTime.now());

        when(incidentRepository.save(any(Incident.class))).thenReturn(saved);
        when(incidentRepository.existsByIncidentId(anyString())).thenReturn(false);

        // Mock auditLogRepository.save to safely return any logged item or null
        // Since it's a void or tracking save transaction, it won't crash now.
        lenient().when(auditLogRepository.save(any())).thenReturn(null);

        IncidentResponse resp = incidentService.createIncident(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(5L);
        assertThat(resp.getReporterEmail()).isEqualTo(reporter.getEmail());

        verify(incidentRepository).save(any(Incident.class));
    }

    @Test
    void getIncident_notFound_throwsResourceNotFound() {
        when(incidentRepository.findByIncidentId("NOPE")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> incidentService.getIncident("NOPE"));
    }
}
