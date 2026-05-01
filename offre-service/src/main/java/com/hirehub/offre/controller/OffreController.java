package com.hirehub.offre.controller;

import com.hirehub.offre.dto.OffreRequest;
import com.hirehub.offre.dto.OffreResponse;
import com.hirehub.offre.enums.TypeContrat;
import com.hirehub.offre.service.OffreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/offres")
@RequiredArgsConstructor
public class OffreController {

    private final OffreService offreService;

    // POST /api/offres — créer une offre (recruteur)
    @PostMapping
    public ResponseEntity<OffreResponse> creerOffre(
            @Valid @RequestBody OffreRequest request,
            @RequestHeader(name = "X-User-Id") String recruteurId,
            @RequestHeader(name = "X-User-Email") String recruteurEmail) {
        return ResponseEntity.ok(offreService.creerOffre(request, recruteurId, recruteurEmail));
    }

    // GET /api/offres/{id} — détail d'une offre
    @GetMapping("/{id}")
    public ResponseEntity<OffreResponse> getOffre(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(offreService.getOffre(id));
    }

    // GET /api/offres — liste publique paginée + filtres
    @GetMapping
    public ResponseEntity<Page<OffreResponse>> listerOffres(
            @RequestParam(name = "ville", required = false) String ville,
            @RequestParam(name = "typeContrat", required = false) TypeContrat typeContrat,
            @RequestParam(name = "motCle", required = false) String motCle,
            Pageable pageable) {
        return ResponseEntity.ok(offreService.listerOffresPubliees(ville, typeContrat, motCle, pageable));
    }

    // GET /api/offres/mes-offres — offres du recruteur connecté
    @GetMapping("/mes-offres")
    public ResponseEntity<Page<OffreResponse>> mesOffres(
            @RequestHeader(name = "X-User-Id") String recruteurId,
            Pageable pageable) {
        return ResponseEntity.ok(offreService.listerOffresRecruteur(recruteurId, pageable));
    }

    // PUT /api/offres/{id} — modifier une offre
    @PutMapping("/{id}")
    public ResponseEntity<OffreResponse> modifierOffre(
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody OffreRequest request,
            @RequestHeader(name = "X-User-Id") String recruteurId) {
        return ResponseEntity.ok(offreService.modifierOffre(id, request, recruteurId));
    }

    // PATCH /api/offres/{id}/publier — publier une offre
    @PatchMapping("/{id}/publier")
    public ResponseEntity<OffreResponse> publierOffre(
            @PathVariable(name = "id") Long id,
            @RequestHeader(name = "X-User-Id") String recruteurId) {
        return ResponseEntity.ok(offreService.publierOffre(id, recruteurId));
    }

    // PATCH /api/offres/{id}/fermer — fermer une offre
    @PatchMapping("/{id}/fermer")
    public ResponseEntity<OffreResponse> fermerOffre(
            @PathVariable(name = "id") Long id,
            @RequestHeader(name = "X-User-Id") String recruteurId) {
        return ResponseEntity.ok(offreService.fermerOffre(id, recruteurId));
    }

    // GET /api/offres/{id}/valide — pour Lydivine (candidature-service)
    @GetMapping("/{id}/valide")
    public ResponseEntity<Boolean> isOffreValide(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(offreService.isOffreValide(id));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleNotFound(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleForbidden(SecurityException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }
}
