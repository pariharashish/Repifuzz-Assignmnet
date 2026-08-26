package com.repifuzz.controller;
import com.repifuzz.EntityDTO.LoginRequest;
import com.repifuzz.EntityDTO.RegisterUserRequest;
import com.repifuzz.EntityDTO.UserResponse;
import com.repifuzz.jwtUtil.JwtUtil;
import com.repifuzz.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    public ResponseEntity<UserResponse> register(@RequestBody RegisterUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
    }

    /*@PostMapping("/login")
    public ResponseEntity<Optional<String>> login(@RequestBody LoginRequest loginRequest) {
        Optional<String> token = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(token);
    }*/

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (Exception e) {
            throw new RuntimeException("Invalid credentials", e);
        }
        String token = jwtUtil.generateToken(req.getEmail());
        return Map.of("token", token);
    }

}
