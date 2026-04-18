package com.hirehub.auth.exception;

/**
 * Incohérence entre le justificatif (OCR) et les champs formulaire (raison sociale / SIRET).
 */
public class RecruiterJustificatifMismatchException extends RuntimeException {

    public enum Kind {
        /** Les deux contrôles ont échoué. */
        BOTH,
        /** La raison sociale ne correspond pas au texte extrait du document. */
        RAISON_SOCIALE,
        /** Le SIRET saisi n'apparaît pas dans le document. */
        SIRET
    }

    private final Kind kind;

    public RecruiterJustificatifMismatchException(Kind kind) {
        super("Le justificatif ne correspond pas aux donnees du formulaire.");
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    /** Valeur du query param {@code detail} pour le front. */
    public String detailQueryValue() {
        return switch (kind) {
            case BOTH -> "both";
            case RAISON_SOCIALE -> "raison";
            case SIRET -> "siret";
        };
    }
}
