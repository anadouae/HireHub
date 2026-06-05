package com.hirehub.candidature.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class CandidatureJwtConfig {

    @Bean
    public SecretKey hirehubJwtSecretKey(@Value("${hirehub.jwt.secret}") String rawSecret) {
        return Keys.hmacShaKeyFor(rawSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public JwtDecoder hirehubJwtDecoder(SecretKey hirehubJwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(hirehubJwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
