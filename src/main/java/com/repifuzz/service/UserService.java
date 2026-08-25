package com.repifuzz.service;

import com.repifuzz.Entity.User;
import com.repifuzz.EntityDTO.LoginRequest;
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

    /*public User registerUser(User user) {
        // hash password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setPassword(user.getPassword());

        return userRepository.save(user);
    }*/

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
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
}
