package com.hirehub.frontend.oauth;

/**
 * Compte Google non lié à une inscription HireHub existante.
 */
public class GoogleOAuthAccountNotRegisteredException extends RuntimeException {

    public GoogleOAuthAccountNotRegisteredException(String email) {
        super("Aucun compte HireHub pour : " + email);
    }
}
