package com.hirehub.frontend.signup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "signup_email_challenges")
public class SignupEmailChallenge {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "nom_complet", nullable = false, length = 512)
    private String nomComplet;

    @Column(nullable = false, length = 32)
    private String role;

    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean consumed;

    protected SignupEmailChallenge() {
    }

    public SignupEmailChallenge(UUID id, String email, String nomComplet, String role, String codeHash, Instant expiresAt) {
        this.id = id;
        this.email = email;
        this.nomComplet = nomComplet;
        this.role = role;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.consumed = false;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public String getRole() {
        return role;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }
}
