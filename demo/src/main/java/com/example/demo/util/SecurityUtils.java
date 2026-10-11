package com.example.demo.util;

import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Set;
import java.util.stream.Collectors;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getCurrentUserId() {
        JwtAuthenticationToken auth = currentAuth();

        String subject = auth.getToken().getSubject();
        if (subject == null) {
            throw AppException.error(ErrorMessage.INVALID_TOKEN);
        }

        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw AppException.error(ErrorMessage.INVALID_TOKEN);
        }
    }

    public static String getCurrentUserCode() {
        return currentAuth().getName();
    }

    public static Set<String> getCurrentPermissions() {
        return currentAuth().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    // Lấy thông tin đăng nhập hiện tại, không phải đăng nhập bằng JWT (null, anonymous) thì báo lỗi
    private static JwtAuthenticationToken currentAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth;
        }
        throw AppException.error(ErrorMessage.INVALID_TOKEN);
    }
}