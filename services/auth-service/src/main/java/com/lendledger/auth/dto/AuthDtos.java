package com.lendledger.auth.dto;

import com.lendledger.common.security.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 6) String password,
            @NotBlank String fullName,
            String phone
    ) {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds) {}

    public record UserResponse(UUID id, String email, Role role, String fullName, String phone) {}

    public record CreateUserRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 6) String password,
            @NotBlank String fullName,
            String phone,
            Role role
    ) {}
}
