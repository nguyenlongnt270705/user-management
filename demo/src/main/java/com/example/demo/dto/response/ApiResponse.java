package com.example.demo.dto.response;

import com.example.demo.exception.ErrorMessage;

public record ApiResponse<T>(int code, String errorCode, String message, T data) {
    public static <T> ApiResponse<T> success(T data) {
        ErrorMessage success = ErrorMessage.SUCCESS;
        return new ApiResponse<>(success.getCode(), success.getErrorCode(), success.getMessage(), data);
    }

    // default msg
    public static <T> ApiResponse<T> error(ErrorMessage errorMessage) {
        return new ApiResponse<>(errorMessage.getCode(), errorMessage.getErrorCode(), errorMessage.getMessage(), null);
    }

    // detail msg
    public static <T> ApiResponse<T> error(ErrorMessage errorMessage, String message) {
        return new ApiResponse<>(errorMessage.getCode(), errorMessage.getErrorCode(), message, null);
    }
}
