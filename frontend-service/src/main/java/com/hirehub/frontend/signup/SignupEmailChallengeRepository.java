package com.hirehub.frontend.signup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SignupEmailChallengeRepository extends JpaRepository<SignupEmailChallenge, UUID> {

    Optional<SignupEmailChallenge> findTopByEmailAndRoleAndConsumedFalseOrderByExpiresAtDesc(String email, String role);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SignupEmailChallenge s where lower(s.email) = lower(:email) and s.role = :role and s.consumed = false")
    void deletePendingByEmailAndRole(@Param("email") String email, @Param("role") String role);
}
