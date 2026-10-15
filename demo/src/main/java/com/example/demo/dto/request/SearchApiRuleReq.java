package com.example.demo.dto.request;

public record SearchApiRuleReq(
        String httpMethod,
        String pathPattern,
        String permissionCode,
        Boolean locked
) {
}
