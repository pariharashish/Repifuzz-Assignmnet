package com.repifuzz.controller;
import com.repifuzz.EntityDTO.LoginRequest;
import com.repifuzz.EntityDTO.RegisterUserRequest;
import com.repifuzz.EntityDTO.UserResponse;
import com.repifuzz.exception.InvalidCredentialsException;
import com.repifuzz.jwtUtil.JwtUtil;
import jakarta.validation.Valid;
import com.repifuzz.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/api/ims/user")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException();
        }
        String token = jwtUtil.generateToken(req.getEmail());
        return Map.of("token", token);
    }

}
