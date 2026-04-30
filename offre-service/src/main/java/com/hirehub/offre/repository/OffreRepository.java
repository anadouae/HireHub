package com.hirehub.offre.repository;

import com.hirehub.offre.entity.Offre;
import com.hirehub.offre.enums.StatutOffre;
import com.hirehub.offre.enums.TypeContrat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OffreRepository extends JpaRepository<Offre, Long>, JpaSpecificationExecutor<Offre> {

    Page<Offre> findByStatut(StatutOffre statut, Pageable pageable);

    Page<Offre> findByRecruteurId(Long recruteurId, Pageable pageable);

    boolean existsByIdAndStatut(Long id, StatutOffre statut);

    @Query("""
    SELECT o FROM Offre o
    WHERE o.statut = :statut
    AND (:ville IS NULL OR LOWER(o.ville) LIKE LOWER(CONCAT('%', :ville, '%')))
    AND (:typeContrat IS NULL OR o.typeContrat = :typeContrat)
    AND (
        :motCle IS NULL
        OR LOWER(o.titre) LIKE LOWER(CONCAT('%', :motCle, '%'))
        OR LOWER(o.description) LIKE LOWER(CONCAT('%', :motCle, '%'))
    )
""")
    Page<Offre> findWithFilters(
            @Param("statut") StatutOffre statut,
            @Param("ville") String ville,
            @Param("typeContrat") TypeContrat typeContrat,
            @Param("motCle") String motCle,
            Pageable pageable
    );
}
