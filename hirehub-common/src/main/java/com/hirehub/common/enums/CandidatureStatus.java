package com.hirehub.common.enums;

public enum CandidatureStatus implements BaseEnum {

    SOUMISE("SOUMISE"),      // candidature déposée, non lue
    EN_COURS("EN_COURS"),     // recruteur l'a vue, en traitement
    ENTRETIEN("ENTRETIEN"),    // entretien planifié
    ACCEPTEE("ACCEPTEE"),     // candidat retenu
    REFUSEE("REFUSEE");      // candidat refusé


    private final String label;

    CandidatureStatus(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    // Transitions autorisées — la règle métier est ici, pas dans chaque service
    public boolean canTransitionTo(CandidatureStatus next) {
        return switch (this) {
            case SOUMISE    -> next == EN_COURS || next == REFUSEE;
            case EN_COURS   -> next == ENTRETIEN || next == REFUSEE;
            case ENTRETIEN  -> next == ACCEPTEE || next == REFUSEE;
            case ACCEPTEE, REFUSEE -> false; // états finaux
        };
    }

}
