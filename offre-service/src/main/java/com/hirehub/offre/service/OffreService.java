package com.hirehub.offre.service;

import com.hirehub.offre.dto.OffreRequest;
import com.hirehub.offre.dto.OffreResponse;
import com.hirehub.offre.entity.Offre;
import com.hirehub.offre.enums.StatutOffre;
import com.hirehub.offre.enums.TypeContrat;
import com.hirehub.offre.repository.OffreRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OffreService {

    private final OffreRepository offreRepository;

    public OffreResponse creerOffre(OffreRequest request, Long recruteurId, String recruteurEmail) {
        Offre offre = Offre.builder()
                .titre(request.getTitre())
                .description(request.getDescription())
                .typeContrat(request.getTypeContrat())
                .ville(request.getVille())
                .salaire(request.getSalaire())
                .dateExpiration(request.getDateExpiration())
                .statut(StatutOffre.BROUILLON)
                .recruteurId(recruteurId)
                .recruteurEmail(recruteurEmail)
                .build();
        return OffreResponse.from(offreRepository.save(offre));
    }

    public OffreResponse getOffre(Long id) {
        return OffreResponse.from(findOffre(id));
    }

    public Page<OffreResponse> listerOffresPubliees(String ville, TypeContrat typeContrat,
                                                    String motCle, Pageable pageable) {
        String villeParam = (ville != null && !ville.isBlank()) ? ville : null;
        String motCleParam = (motCle != null && !motCle.isBlank()) ? motCle : null;
        return offreRepository.findAll(publishedOffersSpecification(villeParam, typeContrat, motCleParam), pageable)
                .map(OffreResponse::from);
    }

    public Page<OffreResponse> listerOffresRecruteur(Long recruteurId, Pageable pageable) {
        return offreRepository.findByRecruteurId(recruteurId, pageable)
                .map(OffreResponse::from);
    }

    public OffreResponse modifierOffre(Long id, OffreRequest request, Long recruteurId) {
        Offre offre = findOffre(id);
        verifierProprietaire(offre, recruteurId);

        offre.setTitre(request.getTitre());
        offre.setDescription(request.getDescription());
        offre.setTypeContrat(request.getTypeContrat());
        offre.setVille(request.getVille());
        offre.setSalaire(request.getSalaire());
        offre.setDateExpiration(request.getDateExpiration());

        return OffreResponse.from(offreRepository.save(offre));
    }

    public OffreResponse publierOffre(Long id, Long recruteurId) {
        Offre offre = findOffre(id);
        verifierProprietaire(offre, recruteurId);

        offre.setStatut(StatutOffre.PUBLIEE);
        return OffreResponse.from(offreRepository.save(offre));
    }

    public OffreResponse fermerOffre(Long id, Long recruteurId) {
        Offre offre = findOffre(id);
        verifierProprietaire(offre, recruteurId);

        offre.setStatut(StatutOffre.FERMEE);
        return OffreResponse.from(offreRepository.save(offre));
    }

    public boolean isOffreValide(Long id) {
        return offreRepository.existsByIdAndStatut(id, StatutOffre.PUBLIEE);
    }

    private Offre findOffre(Long id) {
        return offreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offre non trouvee : " + id));
    }

    private void verifierProprietaire(Offre offre, Long recruteurId) {
        if (!offre.getRecruteurId().equals(recruteurId)) {
            throw new SecurityException("Acces refuse : vous n'etes pas proprietaire de cette offre");
        }
    }

    private Specification<Offre> publishedOffersSpecification(String ville, TypeContrat typeContrat, String motCle) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("statut"), StatutOffre.PUBLIEE));

            if (ville != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("ville")),
                        "%" + ville.toLowerCase() + "%"
                ));
            }

            if (typeContrat != null) {
                predicates.add(criteriaBuilder.equal(root.get("typeContrat"), typeContrat));
            }

            if (motCle != null) {
                String pattern = "%" + motCle.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("titre")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
