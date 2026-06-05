package com.hirehub.candidature.config;

import com.hirehub.candidature.clients.IOffreServiceClient;
import com.hirehub.candidature.entities.Candidature;
import com.hirehub.candidature.exceptions.UnauthorizedException;
import com.hirehub.candidature.security.CurrentUser;
import com.hirehub.common.enums.UserRole;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service centralisé pour les vérifications de sécurité et d'autorisation
 * Vérifie que les utilisateurs ont les droits d'accès appropriés
 */
@Service
@Slf4j
public class CandidatureSecurityService {

    private final IOffreServiceClient IOffreServiceClient;

    public CandidatureSecurityService(IOffreServiceClient IOffreServiceClient) {
        this.IOffreServiceClient = IOffreServiceClient;
    }

    /**
     * Vérifie que l'utilisateur est authentifié
     * @throws UnauthorizedException si l'utilisateur n'est pas authentifié
     */
    public UserContext.UserInfo requireAuth() {
        UserContext.UserInfo user = UserContext.getUser();
        if (user == null) {
            user = CurrentUser.toUserInfo();
            UserContext.setUser(user);
        }
        if (user == null || user.userId == null) {
            log.warn("Tentative d'accès sans authentification");
            throw new UnauthorizedException("Authentification requise");
        }
        return user;
    }

    /**
     * Vérifie que l'utilisateur est candidat
     * @throws UnauthorizedException si l'utilisateur n'est pas candidat
     */
    public void requireCandidat(UserContext.UserInfo user) {
        if (!UserRole.CANDIDAT.name().equals(user.role)) {
            log.warn("Utilisateur {} n'a pas le rôle CANDIDAT", user.userId);
            throw new UnauthorizedException("Accès réservé aux candidats");
        }
    }

    /**
     * Vérifie que l'utilisateur est recruteur
     * @throws UnauthorizedException si l'utilisateur n'est pas recruteur
     */
    public void requireRecruteur(UserContext.UserInfo user) {
        if (!UserRole.RECRUTEUR.name().equals(user.role)) {
            log.warn("Utilisateur {} n'a pas le rôle RECRUTEUR", user.userId);
            throw new UnauthorizedException("Accès réservé aux recruteurs");
        }
    }

    /**
     * Vérifie que l'utilisateur est propriétaire de la candidature
     * @param user l'utilisateur
     * @param candidature la candidature
     * @throws UnauthorizedException si l'utilisateur n'est pas propriétaire
     */
    public void requireCandidatureBelongsToCandidat(UserContext.UserInfo user, Candidature candidature) {
        if (!candidature.getCandidatId().equals(user.userId.toString())) {
            log.warn("Candidat {} tente d'accéder à une candidature qui ne lui appartient pas ({})",
                user.userId, candidature.getId());
            throw new UnauthorizedException("Cette candidature ne vous appartient pas");
        }
    }

    /**
     * Vérifie que le recruteur est propriétaire de l'offre associée à la candidature
     * @param user le recruteur
     * @param candidature la candidature
     * @throws UnauthorizedException si le recruteur n'est pas propriétaire de l'offre
     */
    public void requireRecruteurOwnsOffre(UserContext.UserInfo user, Candidature candidature) {
        try {
            // Appel Feign pour vérifier que le recruteur est propriétaire de l'offre
            boolean isOwner = IOffreServiceClient.isRecruteurOwner(candidature.getOffreId(), user.userId.toString());

            if (!isOwner) {
                log.warn("Recruteur {} n'est pas propriétaire de l'offre {}",
                    user.userId, candidature.getOffreId());
                throw new UnauthorizedException("Vous n'êtes pas propriétaire de cette offre");
            }
        } catch (FeignException.NotFound e) {
            log.error("Offre {} non trouvée", candidature.getOffreId(), e);
            throw new UnauthorizedException("Offre non trouvée");
        } catch (FeignException e) {
            log.error("Erreur lors de la vérification de propriété de l'offre: {}", e.getMessage(), e);
            throw new UnauthorizedException("Erreur de vérification d'accès à l'offre");
        }
    }

    /**
     * Vérifie que le candidat/recruteur a accès aux détails de la candidature
     * Les candidats ne peuvent voir que leurs propres candidatures
     * Les recruteurs ne peuvent voir que les candidatures pour leurs offres
     */
    public void requireAccessToCandidature(UserContext.UserInfo user, Candidature candidature) {
        if (UserRole.CANDIDAT.name().equals(user.role)) {
            requireCandidatureBelongsToCandidat(user, candidature);
        } else if (UserRole.RECRUTEUR.name().equals(user.role)) {
            requireRecruteurOwnsOffre(user, candidature);
        } else {
            throw new UnauthorizedException("Rôle utilisateur non reconnu");
        }
    }

    /**
     * Vérifie que le candidat a accès à télécharger son propre fichier
     */
    public void requireCandidatCanDownloadOwnFile(UserContext.UserInfo user, Candidature candidature) {
        requireCandidat(user);
        requireCandidatureBelongsToCandidat(user, candidature);
        log.info("Candidat {} autorisé à télécharger son fichier pour candidature {}",
            user.userId, candidature.getId());
    }

    /**
     * Vérifie que le recruteur peut accéder à un fichier d'une candidature pour son offre
     */
    public void requireRecruteurCanDownloadFile(UserContext.UserInfo user, Candidature candidature) {
        requireRecruteur(user);
        requireRecruteurOwnsOffre(user, candidature);
        log.info("Recruteur {} autorisé à télécharger le fichier pour candidature {}",
            user.userId, candidature.getId());
    }

    /**
     * Vérifie que le recruteur peut changer le statut d'une candidature (doit être propriétaire de l'offre)
     */
    public void requireRecruteurCanChangeStatus(UserContext.UserInfo user, Candidature candidature) {
        requireRecruteur(user);
        requireRecruteurOwnsOffre(user, candidature);
        log.info("Recruteur {} autorisé à changer le statut de candidature {}",
            user.userId, candidature.getId());
    }

    /**
     * Vérifie que le candidat peut voir son historique de candidature
     */
    public void requireCandidatCanViewHistory(UserContext.UserInfo user, Candidature candidature) {
        requireCandidat(user);
        requireCandidatureBelongsToCandidat(user, candidature);
        log.info("Candidat {} autorisé à voir l'historique de candidature {}",
            user.userId, candidature.getId());
    }

    /**
     * Vérifie que le recruteur peut voir l'historique d'une candidature pour son offre
     */
    public void requireRecruteurCanViewHistory(UserContext.UserInfo user, Candidature candidature) {
        requireRecruteur(user);
        requireRecruteurOwnsOffre(user, candidature);
        log.info("Recruteur {} autorisé à voir l'historique de candidature {}",
            user.userId, candidature.getId());
    }

    /**
     * Vérifie que le recruteur peut accéder au pipeline (liste des candidatures) d'une offre
     */
    public void requireAdmin(UserContext.UserInfo user) {
        if (!UserRole.ADMIN.name().equals(user.role)) {
            throw new UnauthorizedException("Accès réservé aux administrateurs");
        }
    }

    public void requireAdminOrRecruteurPipeline(UserContext.UserInfo user, String offreId) {
        if (UserRole.ADMIN.name().equals(user.role)) {
            return;
        }
        requireRecruteurCanViewPipeline(user, offreId);
    }

    public void requireRecruteurCanViewPipeline(UserContext.UserInfo user, String offreId) {
        requireRecruteur(user);
        try {
            boolean isOwner = IOffreServiceClient.isRecruteurOwner(offreId, user.userId.toString());
            if (!isOwner) {
                log.warn("Recruteur {} n'est pas propriétaire de l'offre {}", user.userId, offreId);
                throw new UnauthorizedException("Vous n'êtes pas propriétaire de cette offre");
            }
        } catch (FeignException e) {
            log.error("Erreur lors de la vérification d'accès au pipeline: {}", e.getMessage(), e);
            throw new UnauthorizedException("Erreur de vérification d'accès");
        }
        log.info("Recruteur {} autorisé à voir le pipeline de l'offre {}", user.userId, offreId);
    }

    /**
     * Vérifie que le candidat peut supprimer sa candidature
     */
    public void requireCandidatCanDeleteCandidature(UserContext.UserInfo user, Candidature candidature) {
        requireCandidat(user);
        requireCandidatureBelongsToCandidat(user, candidature);
        log.info("Candidat {} autorisé à supprimer la candidature {}", user.userId, candidature.getId());
    }

    /**
     * Vérifie que le candidat peut mettre à jour les fichiers de sa candidature
     */
    public void requireCandidatCanUpdateFiles(UserContext.UserInfo user, Candidature candidature) {
        requireCandidat(user);
        requireCandidatureBelongsToCandidat(user, candidature);
        log.info("Candidat {} autorisé à mettre à jour les fichiers de candidature {}",
            user.userId, candidature.getId());
    }
}

