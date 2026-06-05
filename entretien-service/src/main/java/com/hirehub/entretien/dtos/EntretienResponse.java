package com.hirehub.entretien.dtos;

import com.hirehub.common.enums.InterviewStatus;
import com.hirehub.entretien.entities.Entretien;
import com.hirehub.entretien.entities.EntretienType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
public class EntretienResponse {
    private String id;
    private String candidatureId;
    private String candidatId;
    private String recruteurId;
    private LocalDateTime dateHeure;
    private String lieu;
    private String lienVisio;
    private EntretienType type;
    private String notesInternes;
    private InterviewStatus status;

    public static EntretienResponse from(Entretien e) {
        EntretienResponse r = new EntretienResponse();
        r.id            = e.getId() != null ? e.getId().toString() : null;
        r.candidatureId = e.getCandidatureId();
        r.candidatId    = e.getCandidatId();
        r.recruteurId   = e.getRecruteurId();
        r.dateHeure     = e.getDateHeure();
        r.lieu          = e.getLieu();
        r.lienVisio     = e.getLienVisio();
        r.type          = e.getType();
        r.notesInternes = e.getNotesInternes();
        r.status        = e.getStatus();
        return r;
    }
}