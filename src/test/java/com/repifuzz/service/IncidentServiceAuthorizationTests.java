package com.repifuzz.service;

import com.repifuzz.Entity.Incident;
import com.repifuzz.Entity.User;
import com.repifuzz.Entity.UserRole;
import com.repifuzz.Repo.IncidentRepository;
import com.repifuzz.Repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;



import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class IncidentServiceAuthorizationTests {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IncidentService incidentService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }





    @Test
    void analystCanReadAnyIncident() {
        authenticateAs("analyst@example.com", UserRole.ANALYST);
        User analyst = user(3L, "analyst@example.com", UserRole.ANALYST);
        User reporter = user(1L, "reporter@example.com", UserRole.REPORTER);
        Incident incident = incident("RMG000002026", reporter);

        org.mockito.Mockito.lenient().when(userRepository.findByEmail("analyst@example.com")).thenReturn(Optional.of(analyst));
        org.mockito.Mockito.lenient().when(incidentRepository.findByIncidentId("RMG000002026")).thenReturn(Optional.of(incident));

        assertThat(incidentService.getIncident("RMG000002026").getReporterUserId()).isEqualTo(1L);
    }

    private void authenticateAs(String email, UserRole role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
    }

    private User user(Long id, String email, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setUsername(email.substring(0, email.indexOf('@')));
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    private Incident incident(String incidentId, User reporter) {
        Incident incident = new Incident();
        incident.setIncidentId(incidentId);
        incident.setReporterUser(reporter);
        return incident;
    }
}
