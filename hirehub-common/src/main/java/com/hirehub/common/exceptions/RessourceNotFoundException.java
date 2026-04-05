package com.hirehub.common.exceptions;

import org.springframework.http.HttpStatus;

public class RessourceNotFoundException extends BusinessException {

    public RessourceNotFoundException(String resource, String id) {
        super(resource + " introuvable : " + id, HttpStatus.NOT_FOUND);
    }
}
