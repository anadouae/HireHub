package com.hirehub.common.enums;

public enum InterviewStatus implements  BaseEnum{
    PLANIFIE("PLANIFIE"),
    ANNULE("ANNULE"),
    TERMINE("TERMINE");

    private final String label;

    InterviewStatus(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return "";
    }

}
