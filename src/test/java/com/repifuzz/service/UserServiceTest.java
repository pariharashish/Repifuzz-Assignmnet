package com.repifuzz.service;

import com.repifuzz.Entity.User;
import com.repifuzz.Entity.UserRole;
import com.repifuzz.EntityDTO.RegisterUserRequest;
import com.repifuzz.EntityDTO.UserResponse;
import com.repifuzz.Repo.UserRepository;
import com.repifuzz.jwtUtil.JwtUtil;
import org.junit.jupiter.api.BeforeEach; // Added
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder; // Kept as standard interface

    @Mock
    private JwtUtil jwtUtil;

    // Removed @InjectMocks to handle creation manually and prevent NullPointerExceptions
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Force manual constructor injection to guarantee passwordEncoder is NEVER null
        userService = new UserService(userRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void registerUser_encodesPasswordAndReturnsUserResponse() {
        RegisterUserRequest req = new RegisterUserRequest();
        req.setUsername("u");
        req.setEmail("e@example.com");
        req.setPassword("plain");

        User saved = new User();
        saved.setId(7L);
        saved.setUsername(req.getUsername());
        saved.setEmail(req.getEmail());
        saved.setPassword("encoded");
        saved.setRole(UserRole.REPORTER);

        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse resp = userService.registerUser(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(7L);
        assertThat(resp.getEmail()).isEqualTo(req.getEmail());
        assertThat(resp.getRole()).isEqualTo(UserRole.REPORTER);

        verify(passwordEncoder).encode("plain");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_duplicate_throwsDataIntegrity() {
        RegisterUserRequest req = new RegisterUserRequest();
        req.setUsername("u");
        req.setEmail("e@example.com");
        req.setPassword("plain");

        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("dup"));

        assertThrows(DataIntegrityViolationException.class, () -> userService.registerUser(req));
    }

    @Test
    void login_successfulPasswordMatches_returnsTokenOptional() {
        User user = new User();
        user.setEmail("e@example.com");
        user.setPassword("encoded");

        when(userRepository.findByEmail("e@example.com")).thenReturn(Optional.of(user));

        // Using generic matchers to eliminate CharSequence comparison issues
        when(passwordEncoder.matches(any(CharSequence.class), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(user.getEmail())).thenReturn("tok");

        Optional<String> res = userService.login("e@example.com","raw");
        assertTrue(res.isPresent());
        assertEquals("tok", res.get());
    }
}
