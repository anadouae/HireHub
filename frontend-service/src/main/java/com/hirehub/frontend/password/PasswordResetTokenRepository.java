package com.hirehub.frontend.password;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHashAndConsumedFalse(String tokenHash);

    @Query("select t from PasswordResetToken t where t.userId = :userId and t.consumed = false and t.expiresAt > :now")
    Optional<PasswordResetToken> findActiveForUser(@Param("userId") UUID userId, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PasswordResetToken t where t.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}
