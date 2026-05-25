package com.lendledger.auth.service;

import com.lendledger.auth.domain.RefreshTokenEntity;
import com.lendledger.auth.domain.UserEntity;
import com.lendledger.auth.dto.AuthDtos;
import com.lendledger.auth.repository.RefreshTokenRepository;
import com.lendledger.auth.repository.UserRepository;
import com.lendledger.common.error.ApiException;
import com.lendledger.common.security.JwtProperties;
import com.lendledger.common.security.JwtTokenProvider;
import com.lendledger.common.security.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider,
                       JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthDtos.TokenResponse register(AuthDtos.RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ApiException("EMAIL_EXISTS", "Email already registered", HttpStatus.CONFLICT);
        }
        UserEntity user = new UserEntity();
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(Role.BORROWER);
        user.setFullName(req.fullName());
        user.setPhone(req.phone());
        userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional
    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest req) {
        UserEntity user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ApiException("INVALID_CREDENTIALS", "Invalid email or password", HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ApiException("INVALID_CREDENTIALS", "Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthDtos.TokenResponse refresh(AuthDtos.RefreshRequest req) {
        String hash = hashToken(req.refreshToken());
        RefreshTokenEntity rt = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new ApiException("INVALID_REFRESH", "Invalid refresh token", HttpStatus.UNAUTHORIZED));
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException("INVALID_REFRESH", "Refresh token expired", HttpStatus.UNAUTHORIZED);
        }
        UserEntity user = userRepository.findById(rt.getUserId())
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        rt.setRevoked(true);
        return issueTokens(user);
    }

    public AuthDtos.UserResponse me(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        return toUserResponse(user);
    }

    @Transactional
    public AuthDtos.UserResponse createUser(AuthDtos.CreateUserRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ApiException("EMAIL_EXISTS", "Email already exists", HttpStatus.CONFLICT);
        }
        UserEntity user = new UserEntity();
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(req.role());
        user.setFullName(req.fullName());
        user.setPhone(req.phone());
        userRepository.save(user);
        return toUserResponse(user);
    }

    public AuthDtos.UserResponse getUser(UUID id) {
        return userRepository.findById(id).map(this::toUserResponse)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
    }

    private AuthDtos.TokenResponse issueTokens(UserEntity user) {
        String access = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refresh = UUID.randomUUID().toString();
        RefreshTokenEntity rt = new RefreshTokenEntity();
        rt.setUserId(user.getId());
        rt.setTokenHash(hashToken(refresh));
        rt.setExpiresAt(Instant.now().plusSeconds(jwtProperties.getRefreshExpirationDays() * 86400));
        refreshTokenRepository.save(rt);
        return new AuthDtos.TokenResponse(access, refresh, jwtProperties.getAccessExpirationMinutes() * 60);
    }

    private AuthDtos.UserResponse toUserResponse(UserEntity user) {
        return new AuthDtos.UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getFullName(), user.getPhone());
    }

    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
