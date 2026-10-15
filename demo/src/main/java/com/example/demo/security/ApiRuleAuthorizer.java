package com.example.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@RequiredArgsConstructor
@Component
@Slf4j
public class ApiRuleAuthorizer {
    private final ApiRuleRegistry apiRuleRegistry;

    public AuthorizationDecision decide(Authentication authentication, HttpServletRequest request) {

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        String path = request.getRequestURI().substring(request.getContextPath().length());

        // không có rule thì từ chối kể cả System Admin
        var rule = apiRuleRegistry.match(request.getMethod(), path);
        if (rule.isEmpty()) {
            log.warn("Chưa có api rule cho {} {}", request.getMethod(), path);
            return new AuthorizationDecision(false);
        }

        // user phải có permission của rule
        String required = rule.get().permissionCode();
        boolean granted = authentication.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), required));
        return new AuthorizationDecision(granted);

    }
}
