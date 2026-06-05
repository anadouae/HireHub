package com.hirehub.candidature.services;

import com.hirehub.candidature.clients.ICandidatureAPI;
import com.hirehub.candidature.entities.Candidature;
import com.hirehub.common.dtos.ApiResponse;

import java.util.List;

public interface ICandidatureService  {
    Candidature createCandidatureByCandidat(Candidature candidature);
    List<Candidature> getMyCandidaturesByCandidat();
    List<Candidature> getCandidaturesByOfferIdByRecruiter(String offerId);
    Candidature getCandidatureById(String id);
    void updateCandidatureStatusByRecruiter(String id, String status);
    void updateCandidatureDetailsByCandidat(String id, String CV_Path, String lettreMotivationPath);
    void uploadCVAndCoverLetter(String id, String CV_Path, String lettreMotivationPath);
    void deleteCandidatureByCandidat(String id);

    /**
     * Lors de la fermeture d'une offre : refuse les candidatures encore SOUMISE et notifie les concernés.
     *
     * @return nombre de candidatures passées en REFUSEE
     */
    int rejectPendingWhenOfferClosed(String offreId);
}
