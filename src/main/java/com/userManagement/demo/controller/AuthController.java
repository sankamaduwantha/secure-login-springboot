package com.userManagement.demo.controller;

import com.userManagement.demo.dto.RegisterRequest;
import com.userManagement.demo.model.User;
import com.userManagement.demo.service.EmailService;
import com.userManagement.demo.service.TokenService;
import com.userManagement.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;
    private final EmailService emailService;

    public AuthController(UserService userService, TokenService tokenService, EmailService emailService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(
                request.getFullName(),
                request.getEmail(),
                request.getPassword()
        );

        String rawToken = tokenService.createToken(user);

       
        emailService.sendVerificationEmail(user.getEmail(), rawToken);

        Map<String, String> response = Map.of(
                "message", "Registration successful. Please check your email to verify your account."
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
