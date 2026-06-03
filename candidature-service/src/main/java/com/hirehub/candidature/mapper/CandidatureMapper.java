package com.hirehub.candidature.mapper;

import com.hirehub.candidature.dtos.CandidatureCreatedDTO;
import com.hirehub.candidature.dtos.CandidatureResponseDTO;
import com.hirehub.candidature.entities.Candidature;
import com.hirehub.common.dtos.candidatures.CandidatureDTO;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public final class CandidatureMapper {

    public static CandidatureCreatedDTO toCreateDto(Candidature entity) {
        if (entity == null) {
            return null;
        }

        CandidatureCreatedDTO dto = new CandidatureCreatedDTO();
        dto.setId(entity.getId());
        dto.setOffreId(entity.getOffreId());
        dto.setCandidatId(entity.getCandidatId());
        dto.setStatus(entity.getStatus());
        dto.setCvPath(entity.getCvPath());
        dto.setLettreMotivationPath(entity.getLettreMotivationPath());
        return dto;
    }

    public CandidatureResponseDTO toResponseDTO(Candidature candidature) {

        CandidatureResponseDTO candidatureResponseDTO =new CandidatureResponseDTO();

        candidatureResponseDTO.setId(candidature.getId());
        candidatureResponseDTO.setOffreId(candidature.getOffreId());
        candidatureResponseDTO.setCandidatId(candidature.getCandidatId());
        candidatureResponseDTO.setMessage("Candidature crée");

        DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        if (candidature.getDateSoumission() != null) {
            candidatureResponseDTO.setDateSoumission(candidature.getDateSoumission().format(iso));
            candidatureResponseDTO.setCreatedAt(candidature.getDateSoumission().format(iso));
        }
        if (candidature.getDateModification() != null) {
            candidatureResponseDTO.setDateModification(candidature.getDateModification().format(iso));
        }

        candidatureResponseDTO.setStatus(candidature.getStatus().name());
        candidatureResponseDTO.setCvPath(candidature.getCvPath());
        candidatureResponseDTO.setLettreMotivationPath(candidature.getLettreMotivationPath());

        return candidatureResponseDTO;
    }

    public List<CandidatureResponseDTO> toResponseDtos(List<Candidature> candidatures) {
        return candidatures.stream().map(this::toResponseDTO).toList();
    }
    public List<CandidatureResponseDTO> toResponseDTOs(List<Candidature> data) {
        return this.toResponseDtos(data);
    }

    public static CandidatureDTO toDto(Candidature entity) {
        if (entity == null) {
            return null;
        }
        CandidatureDTO dto = new CandidatureDTO();
        dto.setId(entity.getId());
        dto.setOffreId(entity.getOffreId());
        dto.setCandidatId(entity.getCandidatId());
        dto.setStatus(entity.getStatus().getLabel());
        dto.setCvPath(entity.getCvPath());
        dto.setLettreMotivationPath(entity.getLettreMotivationPath());
        return dto;
    }

    public static Candidature toEntity(CandidatureCreatedDTO dto) {
        if (dto == null) {
            return null;
        }

        Candidature entity = new Candidature();
        entity.setId(dto.getId());
        entity.setOffreId(dto.getOffreId());
        entity.setCandidatId(dto.getCandidatId());
        entity.setStatus(dto.getStatus());
        entity.setCvPath(dto.getCvPath());
        entity.setLettreMotivationPath(dto.getLettreMotivationPath());
        return entity;
    }



}