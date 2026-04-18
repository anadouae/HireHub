package com.hirehub.frontend.auth;

import com.hirehub.common.enums.RecruiterVerificationStatus;
import com.hirehub.common.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "users")
public class FrontendUserAccount {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole role;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "recruiter_approved", nullable = false)
    private boolean recruiterApproved;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 40)
    private RecruiterVerificationStatus verificationStatus;

    @Column(name = "company_name", length = 300)
    private String companyName;

    @Column(name = "blocked")
    private Boolean blocked;

    protected FrontendUserAccount() {
    }

    /** Creation locale (OAuth Google ou autre flux serveur). */
    public FrontendUserAccount(
            UUID id,
            String email,
            String passwordHash,
            UserRole role,
            String fullName,
            boolean recruiterApproved,
            RecruiterVerificationStatus verificationStatus
    ) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.recruiterApproved = recruiterApproved;
        this.verificationStatus = verificationStatus;
        this.companyName = null;
        this.blocked = false;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isRecruiterApproved() {
        return recruiterApproved;
    }

    public RecruiterVerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public String getCompanyName() {
        return companyName;
    }

    public boolean isBlocked() {
        return Boolean.TRUE.equals(blocked);
    }

    public void setRecruiterApproved(boolean recruiterApproved) {
        this.recruiterApproved = recruiterApproved;
    }

    public void setVerificationStatus(RecruiterVerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
