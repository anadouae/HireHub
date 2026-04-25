package com.hirehub.notification.email.interfaces;

import com.hirehub.common.dtos.candidatures.CandidatureDTO;

public interface EmailBusinessService {
    /**
     * Template: Confirmation de candidature
     */
    void sendCandidatureConfirmation(CandidatureDTO candidatureDTO);

    /**
     * Template: Changement de statut
     */
    void sendStatutChangedNotification(String candidatEmail, String candidatName,
                                       String offreTitle, String ancienStatut,
                                       String nouveauStatut, String commentaire);

    /**
     * Template: Planification d'entretien
     */
    void sendEntretienPlanification(String candidatEmail, String candidatName,
                                    String offreTitle, String dateEntretien,
                                    String lieux, String interviewer);

    public void sendCandidatureCreatedEmail(String email, String offerTitle);

    /**
     * Template: Annulation d'entretien
     */
    void sendEntretienAnnulation(String candidatEmail, String candidatName,
                                 String offreTitle, String raison);

    void sendRegisterOtp(String email, String userName, String otpCode, int otpValidityMinutes);
    void sendLoginAlert(String email, String userName, String loginDateTime, String ipAddress, String userAgent);
    void sendLogoutInfo(String email, String userName, String logoutDateTime);

}
