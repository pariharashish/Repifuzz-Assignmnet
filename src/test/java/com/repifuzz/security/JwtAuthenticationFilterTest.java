package com.repifuzz.security;

import com.repifuzz.jwtUtil.JwtUtil;
import com.repifuzz.securityConfig.JwtAuthenticationFilter;
import com.repifuzz.service.CustomUserDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    private AutoCloseable closeable;

    public JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_withValidToken_setsSecurityContext() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        req.addHeader("Authorization", "Bearer tok");

        when(jwtUtil.validateToken("tok")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("tok")).thenReturn("alice@example.com");

        UserDetails ud = User.withUsername("alice@example.com").password("pwd").authorities(Collections.emptyList()).build();
        when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(ud);

        filter.doFilterInternal(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("alice@example.com");
    }

    @Test
    void doFilterInternal_withInvalidToken_doesNotSetSecurityContext() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        req.addHeader("Authorization", "Bearer bad");
        when(jwtUtil.validateToken("bad")).thenReturn(false);

        filter.doFilterInternal(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
