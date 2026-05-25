package com.lendledger.auth.controller;

import com.lendledger.auth.dto.AuthDtos;
import com.lendledger.auth.service.AuthService;
import com.lendledger.common.Constants;
import com.lendledger.common.error.ApiException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal")
public class InternalAuthController {
    private final AuthService authService;
    private final String internalApiKey;

    public InternalAuthController(AuthService authService,
                                  @Value("${lendledger.internal-api-key}") String internalApiKey) {
        this.authService = authService;
        this.internalApiKey = internalApiKey;
    }

    @PostMapping("/users")
    public AuthDtos.UserResponse createUser(
            @RequestHeader(Constants.HEADER_INTERNAL_KEY) String key,
            @Valid @RequestBody AuthDtos.CreateUserRequest req) {
        verifyKey(key);
        return authService.createUser(req);
    }

    @GetMapping("/users/{id}")
    public AuthDtos.UserResponse getUser(
            @RequestHeader(Constants.HEADER_INTERNAL_KEY) String key,
            @PathVariable UUID id) {
        verifyKey(key);
        return authService.getUser(id);
    }

    private void verifyKey(String key) {
        if (!internalApiKey.equals(key)) {
            throw new ApiException("FORBIDDEN", "Invalid internal API key", HttpStatus.FORBIDDEN);
        }
    }
}
