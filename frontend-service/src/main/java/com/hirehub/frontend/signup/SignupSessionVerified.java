package com.hirehub.frontend.signup;

import java.io.Serializable;

/** Session apres validation du code email (inscription candidat ou recruteur). */
public final class SignupSessionVerified implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String email;
    private final String nomComplet;
    private final String role;

    public SignupSessionVerified(String email, String nomComplet, String role) {
        this.email = email;
        this.nomComplet = nomComplet;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public String getRole() {
        return role;
    }
}
