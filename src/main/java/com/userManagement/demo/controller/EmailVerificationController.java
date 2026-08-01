package com.userManagement.demo.controller;

import com.userManagement.demo.model.Token;
import com.userManagement.demo.service.TokenService;
import com.userManagement.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class EmailVerificationController {

    private final TokenService tokenService;
    private final UserService userService;

    public EmailVerificationController(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {

        Optional<Token> tokenOptional = tokenService.validateToken(token);

        if (tokenOptional.isEmpty()) {
            Map<String, String> errorResponse = Map.of(
                    "message", "Invalid or expired verification link."
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Token verificationToken = tokenOptional.get();

        userService.markEmailAsVerified(verificationToken.getUser());

        tokenService.markTokenAsUsed(verificationToken);


        Map<String, String> response = Map.of(
                "message", "Email verified successfully!"
        );

        return ResponseEntity.ok(response);
    }
}
