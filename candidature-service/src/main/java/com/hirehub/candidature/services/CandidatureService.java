package com.hirehub.candidature.services;

import com.hirehub.candidature.entities.Candidature;

import java.util.List;

public interface CandidatureService {
    void createCandidatureByCandidat(Candidature candidature);
    List<Candidature> getMyCandidaturesByCandidat();
    List<Candidature> getCandidaturesByOfferIdByRecruiter(String offerId);
    Candidature getCandidatureById(String id);
    void updateCandidatureStatusByRecruiter(String id, String status);
    void updateCandidatureDetailsByCandidat(String id, String CV_Path, String lettreMotivationPath);
    void uploadCVAndCoverLetter(String id, String CV_Path, String lettreMotivationPath);
    void deleteCandidatureByCandidat(String id);
}
