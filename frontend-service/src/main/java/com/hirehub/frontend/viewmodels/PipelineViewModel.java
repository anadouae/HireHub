package com.hirehub.frontend.viewmodels;

import com.hirehub.frontend.clients.CandidatureDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ViewModel pour afficher les candidatures dans le pipeline recruteur
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PipelineViewModel {
    private String candidatureId;
    private String candidatId;
    private String candidatDisplayName;
    private String candidatEmail;
    private String offreId;
    private String status;
    private LocalDateTime dateSoumission;
    private String cvPath;
    private String statusLabel;
    private String statusBadgeClass;

    public static PipelineViewModel fromDTO(CandidatureDTO dto) {
        PipelineViewModel vm = new PipelineViewModel();
        vm.setCandidatureId(dto.getId());
        vm.setCandidatId(dto.getCandidatId());
        vm.setOffreId(dto.getOffreId());
        vm.setStatus(dto.getStatus());
        vm.setDateSoumission(dto.getDateSoumission());
        vm.setCvPath(dto.getCvPath());

        if (dto.getStatus() != null) {
            switch (dto.getStatus()) {
                case "SOUMISE":
                    vm.setStatusLabel("Soumise");
                    vm.setStatusBadgeClass("badge bg-secondary");
                    break;
                case "EN_COURS":
                    vm.setStatusLabel("En cours");
                    vm.setStatusBadgeClass("badge bg-warning text-dark");
                    break;
                case "ENTRETIEN":
                    vm.setStatusLabel("Entretien");
                    vm.setStatusBadgeClass("badge bg-info");
                    break;
                case "ACCEPTEE":
                    vm.setStatusLabel("Acceptee");
                    vm.setStatusBadgeClass("badge bg-success");
                    break;
                case "REFUSEE":
                    vm.setStatusLabel("Refusee");
                    vm.setStatusBadgeClass("badge bg-danger");
                    break;
                default:
                    vm.setStatusLabel(dto.getStatus());
                    vm.setStatusBadgeClass("badge bg-secondary");
            }
        }

        return vm;
    }

    public String getDateSoumissionFormatted() {
        if (dateSoumission == null) return "-";
        return dateSoumission.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public boolean hasCv() {
        return cvPath != null && !cvPath.isEmpty();
    }
}

