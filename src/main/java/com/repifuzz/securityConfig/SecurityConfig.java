// src/main/java/com/repifuzz/securityConfig/SecurityConfig.java
package com.repifuzz.securityConfig;

import com.repifuzz.Repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF for REST APIs
                .csrf(csrf -> csrf.disable())

                // 2. Set Session Management to STATELESS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Define Endpoint Authorization Rules
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC endpoints - only registration and login
                        .requestMatchers("/api/ims/user/register").permitAll()
                        .requestMatchers("/api/ims/user/login").permitAll()

                        // PROTECTED endpoints - REQUIRES AUTHENTICATION
                        // All incident endpoints require authenticated user
                        .requestMatchers("/api/ims/incidents/**").authenticated()

                        // All lifecycle operations require authentication
                        .requestMatchers("/api/ims/incidents/lifecycle/**").authenticated()

                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )

                // 4. Disable Form Login
                .formLogin(login -> login.disable())

                // 5. Add Custom JWT Filter before Spring's UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}