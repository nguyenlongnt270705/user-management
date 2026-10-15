package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import com.example.demo.exception.ErrorMessage;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PermissionJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final UserRepository userRepository;
    private final AuthorityService authorityService;
    private final AuthSessionStore authSessionStore;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        long userId;

        try {
            userId = Long.parseLong(Objects.requireNonNull(jwt.getSubject()));
        } catch (NumberFormatException e) {
            throw new InvalidBearerTokenException("Token không hợp lệ");
        }

        checkSession(userId, jwt.getClaimAsString("sid"));

        User user = userRepository.findWithCompanyById(userId)
                .orElseThrow(() -> new InvalidBearerTokenException("Token không hợp lệ"));

        // xoá token user inactive không đợi token hết hạn
        if (user.getStatus() == Status.INACTIVE) {
            throw new InvalidBearerTokenException("Tài khoản đã bị vô hiệu hóa");
        }
        if (user.getCompany().getStatus() == Status.INACTIVE) {
            throw new InvalidBearerTokenException("Công ty đã bị vô hiệu hoá");
        }

        Set<GrantedAuthority> authorities = authorityService.loadAuthorities(userId);

        return new JwtAuthenticationToken(jwt, authorities, user.getUserCode());
    }

    private void checkSession(long userId, String tokenSid) {
        // token không có sid = token cũ / giả
        if (tokenSid == null) {
            throw new TokenAuthenticationException(ErrorMessage.INVALID_TOKEN);
        }

        String currentSid = authSessionStore.getCurrentSid(userId);

        // không có phiên: đã logout / hết hạn / bị khoá
        if (currentSid == null) {
            throw new TokenAuthenticationException(ErrorMessage.SESSION_EXPIRED);
        }
        // có phiên nhưng khác sid: máy khác đã đăng nhập
        if (!currentSid.equals(tokenSid)) {
            throw new TokenAuthenticationException(ErrorMessage.SESSION_REPLACED);
        }
    }
}
