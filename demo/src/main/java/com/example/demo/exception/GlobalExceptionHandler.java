package com.example.demo.exception;

import com.example.demo.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
        ErrorMessage errorMessage = e.getErrorMessage();
        return ResponseEntity.status(errorMessage.getCode()).
                body(ApiResponse.error(errorMessage, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) throws Exception {
        // skip 401 403
        if (e instanceof AccessDeniedException || e instanceof AuthenticationException) {
            throw e;
        }
        log.error("Unexpected error", e);
        return ResponseEntity.status(ErrorMessage.INTERNAL_SERVER_ERROR.getCode()).
                body(ApiResponse.error(ErrorMessage.INTERNAL_SERVER_ERROR));
    }

    // Lỗi valid request body
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        List<String> errors = new ArrayList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(ErrorMessage.BAD_REQUEST.getCode())
                .body(ApiResponse.error(ErrorMessage.BAD_REQUEST, String.join(", ", errors)));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        HttpStatus httpStatus = HttpStatus.resolve(statusCode.value());
        String errorCode = httpStatus != null ? httpStatus.name() : "ERROR";
        String errorMessage = httpStatus != null ? httpStatus.getReasonPhrase() : "Error";
        ApiResponse<Void> response = new ApiResponse<>(statusCode.value(), errorCode, errorMessage, null);

        return ResponseEntity.status(statusCode).headers(headers).body(response);
    }
}
