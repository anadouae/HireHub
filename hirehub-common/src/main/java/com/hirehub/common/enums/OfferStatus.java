package com.hirehub.common.enums;

public enum OfferStatus implements BaseEnum {

    OUVERTE("OUVERTE"),   // visible, on peut postuler
    FERMEE("FERMEE"),    // plus de candidatures acceptées
    ARCHIVEE("ARCHIVEE");   // masquée, historique uniquement

    private final String label;

    OfferStatus(String label) {
        this.label = label;
    }



    @Override
    public String getLabel() {
        return label;
    }
}
