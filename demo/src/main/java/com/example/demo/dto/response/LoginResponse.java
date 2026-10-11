package com.example.demo.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn // số giây access token còn sống
) {
}
