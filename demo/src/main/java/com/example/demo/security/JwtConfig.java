package com.example.demo.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Base64;

@Configuration
public class JwtConfig {
    private static final int MIN_SECRET_BYTES = 64; // SHA-512

    // Ký token
    @Bean
    public JwtEncoder jwtEncoder(JwtProperties jwtProperties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(jwtProperties)));
    }

    // Check token per request
    @Bean
    public JwtDecoder jwtDecoder(JwtProperties jwtProperties) {
        // HS512
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey(jwtProperties)).macAlgorithm(MacAlgorithm.HS512).build();

        // JWT hến hạn đúng lúc không dư 60s
        decoder.setJwtValidator(new JwtTimestampValidator(Duration.ZERO));
        return decoder;
    }

    private SecretKey secretKey(JwtProperties jwtProperties) {
        byte[] keyBytes;
        try {
            // decode JWT SECRET Base 64 -> SHA512
            keyBytes = Base64.getDecoder().decode(jwtProperties.secret().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("JWT_SECRET không phải Base64 hợp lệ", e);
        }

        // Key không đủ 64 bytes
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException("JWT_SECRET quá ngắn, bytes hiện có" + keyBytes.length);
        }
        return new SecretKeySpec(keyBytes, "HmacSHA512");
    }
}
