package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApiRuleRequest(
        @NotBlank String httpMethod,
        @NotBlank @Size(max = 255) String pathPattern,
        @NotNull Long permissionId,
        @Size(max = 255) String description
) {
}
