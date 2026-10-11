package com.example.demo.service;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorMessage;
import com.example.demo.repository.UserRepository;
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

        String accessToken = jwtService.generateAccessToken(user);
        return new LoginResponse(accessToken, "Bearer", jwtService.getExpirationInSeconds());
    }
}
