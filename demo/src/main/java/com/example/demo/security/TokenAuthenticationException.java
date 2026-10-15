package com.example.demo.security;

import com.example.demo.exception.ErrorMessage;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;


@Getter
public class TokenAuthenticationException extends AuthenticationException {
    private final ErrorMessage errorMessage;

    public TokenAuthenticationException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }
}
