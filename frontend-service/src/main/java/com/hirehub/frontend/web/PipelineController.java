package com.hirehub.frontend.web;



import com.hirehub.frontend.candidature.ApplicationUploadService;

import com.hirehub.frontend.candidature.CandidatureFrontendClient;

import com.hirehub.frontend.candidature.CandidatureServiceException;

import com.hirehub.frontend.candidature.CandidateDisplayService;

import com.hirehub.frontend.clients.CandidatureDTO;

import com.hirehub.frontend.entretien.CreateEntretienRequest;

import com.hirehub.frontend.entretien.EntretienFrontendClient;

import com.hirehub.frontend.entretien.EntretienServiceException;

import com.hirehub.frontend.entretien.EntretienView;

import com.hirehub.frontend.viewmodels.CandidatureViewModel;

import com.hirehub.frontend.viewmodels.PipelineViewModel;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;

import org.springframework.core.io.UrlResource;

import org.springframework.http.HttpHeaders;

import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;



import java.nio.file.Path;

import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.List;

import java.util.UUID;



/**

 * Contrôleur pour les routes recruteur liées au pipeline des candidatures.

 */

@Controller

@RequestMapping("/recruteur")

@Slf4j

public class PipelineController {



    private final CandidatureFrontendClient candidatureFrontendClient;

    private final CandidateDisplayService candidateDisplayService;

    private final ApplicationUploadService applicationUploadService;

    private final EntretienFrontendClient entretienFrontendClient;



    public PipelineController(

            CandidatureFrontendClient candidatureFrontendClient,

            CandidateDisplayService candidateDisplayService,

            ApplicationUploadService applicationUploadService,

            EntretienFrontendClient entretienFrontendClient

    ) {

        this.candidatureFrontendClient = candidatureFrontendClient;

        this.candidateDisplayService = candidateDisplayService;

        this.applicationUploadService = applicationUploadService;

        this.entretienFrontendClient = entretienFrontendClient;

    }



    @GetMapping("/pipeline")

    public String pipelineIndex() {

        return "redirect:/recruteur/offres";

    }



    @GetMapping("/pipeline/{offreId}")

    public String pipeline(@PathVariable String offreId, Model model) {

        try {

            log.info("Récupération du pipeline pour l'offre {}", offreId);



            List<CandidatureDTO> candidaturesDTO = candidatureFrontendClient.getCandidaturesByOffre(offreId);



            List<PipelineViewModel> candidatures = new ArrayList<>();

            for (CandidatureDTO dto : candidaturesDTO) {

                candidatures.add(enrichPipeline(PipelineViewModel.fromDTO(dto)));

            }



            model.addAttribute("candidatures", candidatures);

            model.addAttribute("offreId", offreId);

            return "pages/recruteur/pipeline";

        } catch (CandidatureServiceException e) {

            log.error("Erreur pipeline offre {}", offreId, e);

            model.addAttribute("error", e.getMessage());

            model.addAttribute("candidatures", List.of());

            model.addAttribute("offreId", offreId);

            return "pages/recruteur/pipeline";

        } catch (Exception e) {

            log.error("Erreur pipeline offre {}", offreId, e);

            model.addAttribute("error", "Erreur lors de la récupération du pipeline.");

            model.addAttribute("candidatures", List.of());

            model.addAttribute("offreId", offreId);

            return "pages/recruteur/pipeline";

        }

    }



    @GetMapping("/candidature/{id}")

    public String candidatureDetail(@PathVariable String id, Model model) {

        try {

            CandidatureDTO candidatureDTO = candidatureFrontendClient.getCandidature(id).orElse(null);

            if (candidatureDTO == null) {

                model.addAttribute("loadError", "Candidature non trouvée");

                return "pages/recruteur/candidature-detail";

            }



            CandidatureViewModel candidature = enrichDetail(CandidatureViewModel.fromDTO(candidatureDTO));

            model.addAttribute("candidature", candidature);

            List<EntretienView> entretiens = entretienFrontendClient.listByCandidature(id);

            model.addAttribute("entretiens", entretiens);

            model.addAttribute("canPlanEntretien", canPlanEntretien(candidatureDTO.getStatus(), entretiens));

            model.addAttribute("allowedNextStatuses", allowedNextStatuses(candidatureDTO.getStatus()));

            applicationUploadService.readLettreText(candidatureDTO.getLettreMotivationPath())
                    .ifPresent(text -> model.addAttribute("lettreMotivationText", text));

            return "pages/recruteur/candidature-detail";

        } catch (Exception e) {

            log.error("Erreur détail candidature {}", id, e);

            model.addAttribute("loadError", "Erreur lors de la récupération de la candidature");

            return "pages/recruteur/candidature-detail";

        }

    }



    @PostMapping("/candidature/{id}/statut")

    public String changeStatus(

            @PathVariable String id,

            @RequestParam String status,

            RedirectAttributes redirectAttributes) {

        String offreId = null;

        try {

            candidatureFrontendClient.updateStatus(id, status);

            redirectAttributes.addFlashAttribute("success", "Statut mis à jour.");

            CandidatureDTO candidature = candidatureFrontendClient.getCandidature(id).orElse(null);

            if (candidature != null) {

                offreId = candidature.getOffreId();

            }

        } catch (CandidatureServiceException e) {

            log.warn("Changement statut {} : {}", id, e.getMessage());

            redirectAttributes.addFlashAttribute("error", e.getMessage());

            CandidatureDTO candidature = candidatureFrontendClient.getCandidature(id).orElse(null);

            if (candidature != null) {

                offreId = candidature.getOffreId();

            }

            return "redirect:/recruteur/candidature/" + id;

        } catch (Exception e) {

            log.error("Changement statut {}", id, e);

            redirectAttributes.addFlashAttribute("error",

                    "Changement de statut refusé. Depuis « Entretien », seules « Acceptée » et « Refusée » sont possibles.");


            return "redirect:/recruteur/candidature/" + id;

        }

        if (offreId != null) {

            return "redirect:/recruteur/pipeline/" + offreId;

        }

        return "redirect:/recruteur/offres";

    }



    @PostMapping("/candidature/{id}/entretien")

    public String planEntretien(

            @PathVariable String id,

            @RequestParam String dateHeure,

            @RequestParam String type,

            @RequestParam(required = false) String lieu,

            @RequestParam(required = false) String lienVisio,

            @RequestParam(required = false) String notesInternes,

            RedirectAttributes redirectAttributes) {

        try {

            CreateEntretienRequest request = new CreateEntretienRequest();

            request.setCandidatureId(id);

            request.setDateHeure(parseDateHeure(dateHeure));

            request.setType(type != null ? type.trim().toUpperCase() : null);

            request.setLieu(trimToNull(lieu));

            request.setLienVisio(trimToNull(lienVisio));

            request.setNotesInternes(trimToNull(notesInternes));

            if ("PRESENTIEL".equals(request.getType()) && request.getLieu() == null) {

                redirectAttributes.addFlashAttribute("error",

                        "Le lieu est obligatoire pour un entretien en présentiel.");

                return "redirect:/recruteur/candidature/" + id;

            }

            if ("VISIO".equals(request.getType()) && request.getLienVisio() == null) {

                redirectAttributes.addFlashAttribute("error",

                        "Le lien visio est obligatoire pour un entretien en visioconférence.");

                return "redirect:/recruteur/candidature/" + id;

            }

            entretienFrontendClient.create(request);

            redirectAttributes.addFlashAttribute("success",

                    "Entretien planifié. Le candidat recevra un e-mail et le statut passe à « Entretien ».");

        } catch (EntretienServiceException e) {

            log.warn("Planification entretien {} : {}", id, e.getMessage());

            redirectAttributes.addFlashAttribute("error", e.getMessage());

        } catch (java.time.format.DateTimeParseException e) {

            redirectAttributes.addFlashAttribute("error",

                    "Date ou heure invalide — utilisez le sélecteur date/heure du formulaire.");

        } catch (Exception e) {

            log.error("Planification entretien {}", id, e);

            String msg = e.getMessage() != null && !e.getMessage().isBlank()
                    ? e.getMessage()
                    : "Impossible de planifier l'entretien.";
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la planification : " + msg);

        }

        return "redirect:/recruteur/candidature/" + id;

    }



    @GetMapping("/candidature/{id}/download")

    public ResponseEntity<Resource> downloadFile(

            @PathVariable String id,

            @RequestParam(value = "type", defaultValue = "cv") String fileType) {

        try {

            CandidatureDTO candidature = candidatureFrontendClient.getCandidature(id)

                    .orElseThrow(() -> new CandidatureServiceException("Candidature introuvable"));



            Path filePath = resolveFilePath(candidature, fileType)

                    .orElseThrow(() -> new CandidatureServiceException("Fichier non disponible"));



            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {

                return ResponseEntity.notFound().build();

            }



            String fileName = filePath.getFileName().toString();

            MediaType mediaType = fileName.toLowerCase().endsWith(".pdf")

                    ? MediaType.APPLICATION_PDF

                    : MediaType.TEXT_PLAIN;



            return ResponseEntity.ok()

                    .contentType(mediaType)

                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")

                    .body(resource);

        } catch (Exception e) {

            log.error("Téléchargement candidature {}", id, e);

            return ResponseEntity.notFound().build();

        }

    }



    private java.util.Optional<Path> resolveFilePath(CandidatureDTO candidature, String fileType) throws java.io.IOException {

        if (candidature.getCvPath() != null && !candidature.getCvPath().isBlank() && "cv".equalsIgnoreCase(fileType)) {

            java.util.Optional<Path> fromStored = applicationUploadService.resolveStoredFile(candidature.getCvPath());

            if (fromStored.isPresent()) {

                return fromStored;

            }

        }

        if ("lettre".equalsIgnoreCase(fileType) && candidature.getLettreMotivationPath() != null) {

            java.util.Optional<Path> fromStored = applicationUploadService.resolveStoredFile(candidature.getLettreMotivationPath());

            if (fromStored.isPresent()) {

                return fromStored;

            }

        }

        return applicationUploadService.resolveDownloadPath(

                UUID.fromString(candidature.getCandidatId()),

                candidature.getOffreId(),

                fileType

        );

    }



    private PipelineViewModel enrichPipeline(PipelineViewModel vm) {

        vm.setCandidatDisplayName(candidateDisplayService.displayName(vm.getCandidatId()));

        vm.setCandidatEmail(candidateDisplayService.email(vm.getCandidatId()));

        return vm;

    }



    private CandidatureViewModel enrichDetail(CandidatureViewModel vm) {

        vm.setCandidatDisplayName(candidateDisplayService.displayName(vm.getCandidatId()));

        vm.setCandidatEmail(candidateDisplayService.email(vm.getCandidatId()));

        return vm;

    }



    private boolean canPlanEntretien(String status, List<EntretienView> entretiens) {

        if (status == null) {

            return false;

        }

        boolean hasActive = entretiens.stream()

                .anyMatch(e -> "PLANIFIE".equalsIgnoreCase(e.getStatus()));

        if (hasActive) {

            return false;

        }

        return "EN_COURS".equalsIgnoreCase(status) || "ENTRETIEN".equalsIgnoreCase(status);

    }



    /** Statuts sélectionnables manuellement (aligné sur CandidatureStateMachine). */

    private static LocalDateTime parseDateHeure(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new java.time.format.DateTimeParseException("empty", raw, 0);
        }
        String normalized = raw.trim();
        if (normalized.length() == 16) {
            return LocalDateTime.parse(normalized);
        }
        return LocalDateTime.parse(normalized, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private List<String> allowedNextStatuses(String currentStatus) {

        if (currentStatus == null) {

            return List.of();

        }

        return switch (currentStatus.toUpperCase()) {

            case "SOUMISE" -> List.of("EN_COURS", "REFUSEE");

            case "EN_COURS" -> List.of("REFUSEE");

            case "ENTRETIEN" -> List.of("ACCEPTEE", "REFUSEE");

            default -> List.of();

        };

    }

}

