package com.example.demo.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignUserRolePermissionRequest(
        @NotEmpty(message = "Phải chọn ít nhất một role")
        List<@NotNull(message = "Id role không được null") Long> roleIds,

        @NotEmpty(message = "Phải chọn ít nhất một permission")
        List<@NotNull(message = "Id permission không được null") Long> permissionIds) {
}
