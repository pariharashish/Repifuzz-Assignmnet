package com.repifuzz.service;

import com.repifuzz.Entity.Incident;
import com.repifuzz.Entity.User;
import com.repifuzz.Entity.UserRole;
import com.repifuzz.EntityDTO.IncidentRequest;
import com.repifuzz.EntityDTO.IncidentResponse;
import com.repifuzz.Repo.IncidentRepository;
import com.repifuzz.Repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IncidentService incidentService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    void generateUniqueIncidentId_returnsFormattedId() {
        when(incidentRepository.existsByIncidentId(anyString())).thenReturn(false);

        String id = incidentService.generateUniqueIncidentId();

        assertThat(id).startsWith("RMG");
        String year = String.valueOf(java.time.LocalDate.now().getYear());
        assertThat(id).endsWith(year);
        // RMG + 5 digits + year
        assertThat(id.length()).isGreaterThanOrEqualTo(3 + 5 + year.length());
    }

    @Test
    void generateUniqueIncidentId_handlesCollisions() {
        // Simulate first several attempts colliding, then success
        when(incidentRepository.existsByIncidentId(anyString()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        String id = incidentService.generateUniqueIncidentId();
        assertThat(id).startsWith("RMG");
    }

    @Test
    void createIncident_persistsAndMapsResponse() {
        // Prepare authenticated user
        User reporter = new User();
        reporter.setId(10L);
        reporter.setUsername("rep");
        reporter.setEmail("rep@example.com");
        reporter.setPhone("123");
        reporter.setRole(UserRole.REPORTER);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(reporter.getEmail(), null));
        SecurityContextHolder.setContext(context);

        when(userRepository.findByEmail(reporter.getEmail())).thenReturn(Optional.of(reporter));

        IncidentRequest req = new IncidentRequest();
        req.setIncidentType(null);
        req.setDescription("desc");
        req.setDetails("{\"k\":\"v\"}");

        Incident saved = new Incident();
        saved.setId(5L);
        saved.setIncidentId("RMG000012025");
        saved.setReporterUser(reporter);
        saved.setReporterName(reporter.getUsername());
        saved.setReporterEmail(reporter.getEmail());
        saved.setReporterPhone(reporter.getPhone());
        saved.setDescription(req.getDescription());
        saved.setDetails(req.getDetails());
        saved.setCreatedAt(LocalDateTime.now());
        saved.setUpdatedAt(LocalDateTime.now());

        when(incidentRepository.save(any(Incident.class))).thenReturn(saved);
        when(incidentRepository.existsByIncidentId(anyString())).thenReturn(false);

        IncidentResponse resp = incidentService.createIncident(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(5L);
        assertThat(resp.getReporterEmail()).isEqualTo(reporter.getEmail());
        assertThat(resp.getDetails()).isEqualTo(req.getDetails());

        verify(incidentRepository).save(any(Incident.class));
    }

    @Test
    void getIncident_notFound_throwsResourceNotFound() {
        when(incidentRepository.findByIncidentId("NOPE")).thenReturn(Optional.empty());
        assertThrows(com.repifuzz.exception.ResourceNotFoundException.class,
                () -> incidentService.getIncident("NOPE"));
    }

    @Test
    void getIncident_reporterAccessDenied() {
        User reporter = new User();
        reporter.setId(1L);
        reporter.setEmail("a@example.com");
        reporter.setRole(UserRole.REPORTER);

        User other = new User();
        other.setId(2L);
        other.setEmail("b@example.com");

        Incident inc = new Incident();
        inc.setId(99L);
        inc.setIncidentId("RMG000012025");
        inc.setReporterUser(other);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(reporter.getEmail(), null));
        SecurityContextHolder.setContext(context);

        when(userRepository.findByEmail(reporter.getEmail())).thenReturn(Optional.of(reporter));
        when(incidentRepository.findByIncidentId(inc.getIncidentId())).thenReturn(Optional.of(inc));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> incidentService.getIncident(inc.getIncidentId()));
    }

    @Test
    void getIncident_withoutAuthentication_throwsAccessDenied() {
        // prepare an incident present in repo
        Incident inc = new Incident();
        inc.setIncidentId("RMG000012025");
        when(incidentRepository.findByIncidentId(inc.getIncidentId())).thenReturn(Optional.of(inc));

        // Ensure no SecurityContext set
        SecurityContextHolder.clearContext();

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> incidentService.getIncident(inc.getIncidentId()));
    }

}
