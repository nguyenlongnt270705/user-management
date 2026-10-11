package com.example.demo.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final ErrorMessage errorMessage;

    public AppException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }

    public AppException(ErrorMessage errorMessage, String message) {
        super(message);
        this.errorMessage = errorMessage;
    }

    public static AppException error(ErrorMessage errorMessage) {
        return new AppException(errorMessage);
    }

    public static AppException error(ErrorMessage errorMessage, String detail) {
        if (detail != null && !detail.isBlank()) {
            return new AppException(errorMessage, detail);
        }
        return new AppException(errorMessage);
    }
}
