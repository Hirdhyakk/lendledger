package com.lendledger.loan.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "borrowers")
public class BorrowerEntity {
    @Id
    private UUID id;
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;
    private String address;
    @Column(name = "pan_masked")
    private String panMasked;
    @Column(nullable = false)
    private String status = "ACTIVE";
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    void prePersist() { if (id == null) id = UUID.randomUUID(); }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPanMasked() { return panMasked; }
    public void setPanMasked(String panMasked) { this.panMasked = panMasked; }
    public String getStatus() { return status; }
}
