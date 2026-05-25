package com.lendledger.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeyEntity {
    @Id
    private String key;
    @Column(name = "response_body", nullable = false, columnDefinition = "TEXT")
    private String responseBody;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
