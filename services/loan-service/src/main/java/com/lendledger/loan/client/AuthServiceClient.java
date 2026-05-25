package com.lendledger.loan.client;

import com.lendledger.common.Constants;
import com.lendledger.common.security.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
public class AuthServiceClient {
    private final RestClient restClient;
    private final String internalApiKey;

    public AuthServiceClient(@Value("${lendledger.auth-service-url}") String baseUrl,
                             @Value("${lendledger.internal-api-key}") String internalApiKey) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.internalApiKey = internalApiKey;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> createUser(String email, String password, String fullName, String phone, Role role) {
        return restClient.post()
                .uri("/internal/users")
                .header(Constants.HEADER_INTERNAL_KEY, internalApiKey)
                .body(Map.of("email", email, "password", password, "fullName", fullName,
                        "phone", phone != null ? phone : "", "role", role.name()))
                .retrieve()
                .body(Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getUser(UUID userId) {
        return restClient.get()
                .uri("/internal/users/{id}", userId)
                .header(Constants.HEADER_INTERNAL_KEY, internalApiKey)
                .retrieve()
                .body(Map.class);
    }
}
