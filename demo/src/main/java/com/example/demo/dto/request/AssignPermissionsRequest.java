package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignPermissionsRequest(
        @NotNull(message = "Danh sách permission không được null")
        List<@NotNull(message = "Id permission không được null") Long> permissionIds) {
}
