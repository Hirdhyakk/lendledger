package com.lendledger.auth.controller;

import com.lendledger.auth.dto.AuthDtos;
import com.lendledger.auth.service.AuthService;
import com.lendledger.common.Constants;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthDtos.TokenResponse register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthDtos.TokenResponse login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    public AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest req) {
        return authService.refresh(req);
    }

    @GetMapping("/me")
    public AuthDtos.UserResponse me(@RequestHeader(Constants.HEADER_USER_ID) UUID userId) {
        return authService.me(userId);
    }
}
