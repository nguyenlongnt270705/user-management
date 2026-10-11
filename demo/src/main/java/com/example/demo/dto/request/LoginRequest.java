package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String identifier, // email / sdt
        @NotBlank String password
) {
}
