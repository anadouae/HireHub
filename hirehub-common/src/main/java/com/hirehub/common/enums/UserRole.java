package com.hirehub.common.enums;

public enum UserRole implements  BaseEnum{

    CANDIDAT("Candidat"),
    RECRUTEUR("Recruteur"),
    ADMIN("Admin");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
