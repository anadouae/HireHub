package com.hirehub.frontend.auth;

import com.hirehub.common.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FrontendUserRepository extends JpaRepository<FrontendUserAccount, UUID> {

    Optional<FrontendUserAccount> findByEmailIgnoreCase(String email);

    long countByRole(UserRole role);

    long countByRoleAndRecruiterApprovedFalse(UserRole role);

    List<FrontendUserAccount> findAllByOrderByEmailAsc();

    List<FrontendUserAccount> findAllByRoleOrderByEmailAsc(UserRole role);
}
