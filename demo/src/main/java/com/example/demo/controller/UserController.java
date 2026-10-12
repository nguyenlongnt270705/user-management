package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserDetailDTO;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.SearchResponse;
import com.example.demo.security.Permissions;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('" + Permissions.USER_VIEW + "')")
    public ResponseEntity<ApiResponse<SearchResponse<UserDTO>>> search(
            @RequestBody(required = false) SearchRequest<SearchUserReq> request) {
        return ResponseEntity.ok(ApiResponse.success(userService.search(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.USER_VIEW + "')")
    public ResponseEntity<ApiResponse<UserDetailDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getById(id)));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('" + Permissions.USER_CREATE + "')")
    public ResponseEntity<ApiResponse<UserDetailDTO>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userService.create(request)));
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.USER_UPDATE + "')")
    public ResponseEntity<ApiResponse<UserDetailDTO>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.update(id, request)));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('" + Permissions.USER_CHANGE_STATUS + "')")
    public ResponseEntity<ApiResponse<UserDTO>> changeStatus(
            @PathVariable Long id, @Valid @RequestBody ChangeUserStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.changeStatus(id, request)));
    }

    @PostMapping("/{id}/assign-role-permission")
    @PreAuthorize("hasAuthority('" + Permissions.USER_ASSIGN_ROLE + "')")
    public ResponseEntity<ApiResponse<UserDetailDTO>> assignRolePermission(
            @PathVariable Long id, @Valid @RequestBody AssignUserRolePermissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.assignRolePermission(id, request)));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('" + Permissions.USER_UPDATE + "')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
