package com.example.demo.dto.request;

import com.example.demo.enums.Status;
import jakarta.validation.constraints.NotNull;

public record ChangeUserStatusRequest(
        @NotNull(message = "Trạng thái không được null")
        Status status) {
}
