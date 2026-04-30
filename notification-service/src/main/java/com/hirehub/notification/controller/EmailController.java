package com.hirehub.notification.controller;

import com.hirehub.common.dtos.candidatures.CandidatureDTO;
import com.hirehub.common.dtos.candidatures.CandidatureStatutChangedDTO;
import com.hirehub.common.dtos.entretiens.EntretienPlanifiedDTO;
import com.hirehub.notification.EmailBusinessServiceImpl;
import com.hirehub.notification.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification-service/notifications")
public class EmailController {

    @Autowired
    EmailBusinessServiceImpl businessMailService;

    @PostMapping("/candidature-confirmation")
    public ResponseEntity<Void> sendCandidatureConfirmation(@RequestBody CandidatureDTO candidature) {
        if(candidature == null || Utils.isBlank(candidature.getCandidatEmail()) || Utils.isNegativeOrNull(candidature.getOffreId())
        || Utils.isBlank(candidature.getOffreTitle()) || Utils.isBlank(candidature.getCandidatName())){
            return ResponseEntity.badRequest().build();
        }
        businessMailService.sendCandidatureConfirmation(candidature);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/statut-changed")
    public ResponseEntity<Void> sendStatutChangedNotification(@RequestBody CandidatureStatutChangedDTO candidatureChangedDTO){
        if(candidatureChangedDTO == null || Utils.isBlank(candidatureChangedDTO.getCandidatEmail())
                || Utils.isBlank(candidatureChangedDTO.getOffreTitle()) || Utils.isBlank(candidatureChangedDTO.getCandidatName())
                || Utils.isBlank(candidatureChangedDTO.getAncienStatut()) || Utils.isBlank(candidatureChangedDTO.getNouveauStatut())){
            return ResponseEntity.badRequest().build();
        }
        businessMailService.sendCandidatureStatutChangedNotification(
                candidatureChangedDTO.getCandidatEmail(),
                candidatureChangedDTO.getCandidatName(),
                candidatureChangedDTO.getOffreTitle(),
                candidatureChangedDTO.getAncienStatut(),
                candidatureChangedDTO.getNouveauStatut(),
                candidatureChangedDTO.getCommentaire()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notifications/entretien-planification")
    public ResponseEntity<Void> sendEntretienPlanification(@RequestBody EntretienPlanifiedDTO entretienPlanifiedDTO){
        if(entretienPlanifiedDTO == null || Utils.isBlank(entretienPlanifiedDTO.getCandidatEmail()) || Utils.isBlank(entretienPlanifiedDTO.getLieuEntretien())
                || Utils.isBlank(entretienPlanifiedDTO.getOffreTitle()) || Utils.isBlank(entretienPlanifiedDTO.getCandidatName())
                || Utils.isBlank(entretienPlanifiedDTO.getDateEntretien()) || Utils.isBlank(entretienPlanifiedDTO.getHeureEntretien())){
            return ResponseEntity.badRequest().build();
        }
        businessMailService.sendEntretienPlanification(
                entretienPlanifiedDTO.getCandidatEmail(),
                entretienPlanifiedDTO.getCandidatName(),
                entretienPlanifiedDTO.getOffreTitle(),
                entretienPlanifiedDTO.getDateEntretien(),
                entretienPlanifiedDTO.getLieuEntretien(),
                entretienPlanifiedDTO.getInterviewerName()
        );
        return ResponseEntity.ok().build();
    }

}
