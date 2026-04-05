package com.hirehub.common.constants;

public class SecurityConstants {
    private SecurityConstants() {}

    // Headers injectés par la Gateway dans chaque requête
    public static final String HEADER_USER_ID   = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";
    public static final String HEADER_USER_EMAIL = "X-User-Email";

    // Préfixe JWT
    public static final String BEARER_PREFIX = "Bearer ";
}
