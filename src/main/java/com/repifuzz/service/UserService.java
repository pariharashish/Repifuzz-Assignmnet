package com.repifuzz.service;

import com.repifuzz.Entity.User;
import com.repifuzz.Entity.UserRole;
import com.repifuzz.EntityDTO.LoginRequest;
import com.repifuzz.EntityDTO.RegisterUserRequest;
import com.repifuzz.EntityDTO.UserResponse;
import com.repifuzz.Repo.UserRepository;
import com.repifuzz.jwtUtil.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtil jwtUtil;

    public UserResponse registerUser(RegisterUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setPinCode(request.getPinCode());
        user.setCity(request.getCity());
        user.setCountry(request.getCountry());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.REPORTER);

        return mapToResponse(userRepository.save(user));
    }

    public Optional<String> login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            return Optional.of (jwtUtil.generateToken(user.getEmail()));
        }
       if (rawPassword.equals(user.getPassword()) ) {
            return Optional.of(user.getEmail());
        }
        throw new RuntimeException("Invalid credentials");
    }

    public Optional<User> findByEmail(LoginRequest loginRequest) {
        return userRepository.findByEmail(String.valueOf(loginRequest));
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .pinCode(user.getPinCode())
                .city(user.getCity())
                .country(user.getCountry())
                .role(user.getRole())
                .build();
    }
}
