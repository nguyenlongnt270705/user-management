package com.example.demo.dto;

import com.example.demo.entity.ApiRule;

public record ApiRuleDTO(
        Long id,
        String httpMethod,
        String pathPattern,
        Long permissionId,
        String permissionCode,
        boolean locked,
        String description
) {
    public static ApiRuleDTO fromEntity(ApiRule rule) {
        return new ApiRuleDTO(
                rule.getId(),
                rule.getHttpMethod(),
                rule.getPathPattern(),
                rule.getPermission().getId(),
                rule.getPermission().getCode(),
                rule.isLocked(),
                rule.getDescription());
    }
}
