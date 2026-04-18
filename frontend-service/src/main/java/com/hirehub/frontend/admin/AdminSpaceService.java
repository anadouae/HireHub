package com.hirehub.frontend.admin;

import com.hirehub.common.enums.UserRole;
import com.hirehub.common.events.UserAdminActionEvent;
import com.hirehub.frontend.auth.FrontendUserAccount;
import com.hirehub.frontend.auth.FrontendUserRepository;
import com.hirehub.common.constants.RabbitMQConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminSpaceService {

    private final FrontendUserRepository frontendUserRepository;
    private final RabbitTemplate rabbitTemplate;

    public AdminSpaceService(FrontendUserRepository frontendUserRepository, RabbitTemplate rabbitTemplate) {
        this.frontendUserRepository = frontendUserRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional(readOnly = true)
    public AdminDashboardStats dashboardStats() {
        long totalUsers = frontendUserRepository.count();
        long candidats = frontendUserRepository.countByRole(UserRole.CANDIDAT);
        long recruteurs = frontendUserRepository.countByRole(UserRole.RECRUTEUR);
        long admins = frontendUserRepository.countByRole(UserRole.ADMIN);
        long recruiterPendingReview = frontendUserRepository.countByRoleAndRecruiterApprovedFalse(UserRole.RECRUTEUR);
        return new AdminDashboardStats(totalUsers, candidats, recruteurs, admins, recruiterPendingReview);
    }

    @Transactional(readOnly = true)
    public List<FrontendUserAccount> allUsers() {
        return frontendUserRepository.findAllByOrderByEmailAsc();
    }

    @Transactional(readOnly = true)
    public Optional<FrontendUserAccount> findUser(UUID userId) {
        return frontendUserRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public List<FrontendUserAccount> recruiters() {
        return frontendUserRepository.findAllByRoleOrderByEmailAsc(UserRole.RECRUTEUR);
    }

    @Transactional
    public void blockUser(UUID userId) {
        FrontendUserAccount account = frontendUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Compte introuvable"));
        if (account.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("Blocage d'un admin interdit.");
        }
        account.setBlocked(true);
        frontendUserRepository.save(account);
        publishAdminAction(account, "BLOCKED", RabbitMQConstants.ROUTING_USER_BLOCKED);
    }

    @Transactional
    public void unblockUser(UUID userId) {
        FrontendUserAccount account = frontendUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Compte introuvable"));
        account.setBlocked(false);
        frontendUserRepository.save(account);
        publishAdminAction(account, "UNBLOCKED", RabbitMQConstants.ROUTING_USER_BLOCKED);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        FrontendUserAccount account = frontendUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Compte introuvable"));
        if (account.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("Suppression d'un admin interdit.");
        }
        publishAdminAction(account, "DELETED", RabbitMQConstants.ROUTING_USER_DELETED);
        frontendUserRepository.delete(account);
    }

    private void publishAdminAction(FrontendUserAccount account, String action, String routingKey) {
        UserAdminActionEvent event = new UserAdminActionEvent(
                account.getId().toString(),
                account.getEmail(),
                account.getRole().name(),
                action,
                "frontend-service-admin"
        );
        try {
            rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE, routingKey, event);
        } catch (Exception ignored) {
            // Action locale deja appliquee; la publication event est best-effort.
        }
    }
}
