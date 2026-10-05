package com.chudzik.weather_sensors.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "auth_tokens")
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public AuthToken() {
    }

    public AuthToken(Admin admin, String tokenHash, Instant expiresAt) {
        this.admin = admin;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AuthToken authToken)) return false;
        return Objects.equals(id, authToken.id) && Objects.equals(tokenHash, authToken.tokenHash) && Objects.equals(expiresAt, authToken.expiresAt) && Objects.equals(revokedAt, authToken.revokedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tokenHash, expiresAt, revokedAt);
    }

    @Override
    public String toString() {
        return "AuthToken{" +
                "id=" + id +
                ", expiresAt=" + expiresAt +
                ", revokedAt=" + revokedAt +
                '}';
    }
}