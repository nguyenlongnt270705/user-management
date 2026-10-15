package com.example.demo.service;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RefreshRequest;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorMessage;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthSessionStore;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthSessionStore authSessionStore;

    public LoginResponse login(LoginRequest request) {

        // check identifier + pass = CustomUserDetailsService + BCrypt
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.identifier(), request.password()));
        } catch (BadCredentialsException e) {
            throw AppException.error(ErrorMessage.INVALID_CREDENTIALS);
        }

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        assert principal != null;
        User user = principal.getUser();

        if (user.getStatus() == Status.INACTIVE) {
            throw AppException.error(ErrorMessage.ACCOUNT_INACTIVE);
        }
        if (user.getCompany().getStatus() == Status.INACTIVE) {
            throw AppException.error(ErrorMessage.COMPANY_INACTIVE);
        }

        // start session
        String sid = authSessionStore.startSession(user.getId());

        String accessToken = jwtService.generateAccessToken(user, sid);
        String refreshToken = authSessionStore.createRefreshToken(user.getId(), sid);
        return new LoginResponse(accessToken, refreshToken, "Bearer",
                jwtService.getExpirationInSeconds(), jwtService.getRefreshExpirationInSeconds());
    }

    // refresh token, giữ nguyên sid và gia hạn session
    public LoginResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();

        // check token trên redis
        AuthSessionStore.RefreshEntry entry = authSessionStore.findRefreshToken(token)
                .orElseThrow(() -> AppException.error(ErrorMessage.INVALID_REFRESH_TOKEN));

        // validate sid
        if (!entry.sid().equals(authSessionStore.getCurrentSid(entry.userId()))) {
            authSessionStore.deleteRefreshToken(token);
            throw AppException.error(ErrorMessage.INVALID_REFRESH_TOKEN);

        }

        User user = userRepository.findWithCompanyById(entry.userId())
                .orElseThrow(() -> AppException.error(ErrorMessage.INVALID_REFRESH_TOKEN));

        try {
            if (user.getStatus() == Status.INACTIVE) {
                throw AppException.error(ErrorMessage.ACCOUNT_INACTIVE);
            }
            if (user.getCompany().getStatus() == Status.INACTIVE) {
                throw AppException.error(ErrorMessage.COMPANY_INACTIVE);
            }
        } catch (AppException e) {
            authSessionStore.deleteSession(user.getId());
            throw e;
        }

        authSessionStore.deleteRefreshToken(token);
        authSessionStore.saveSession(user.getId(), entry.sid());

        String newAccessToken = jwtService.generateAccessToken(user, entry.sid());
        String newRefreshToken = authSessionStore.createRefreshToken(user.getId(), entry.sid());

        return new LoginResponse(newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtService.getExpirationInSeconds(),
                jwtService.getRefreshExpirationInSeconds());
    }

    public void logout(Long userId) {
        authSessionStore.deleteSession(userId);
    }
}
