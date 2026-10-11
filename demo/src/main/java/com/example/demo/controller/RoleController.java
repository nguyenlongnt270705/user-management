package com.example.demo.controller;

import com.example.demo.dto.RoleDTO;
import com.example.demo.dto.RoleDetailDTO;
import com.example.demo.dto.request.AssignPermissionsRequest;
import com.example.demo.dto.request.CreateRoleRequest;
import com.example.demo.dto.request.SearchRequest;
import com.example.demo.dto.request.SearchRoleReq;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.SearchResponse;
import com.example.demo.security.Permissions;
import com.example.demo.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('" + Permissions.ROLE_VIEW + "')")
    public ResponseEntity<ApiResponse<SearchResponse<RoleDTO>>> search(@RequestBody(required = false) SearchRequest<SearchRoleReq> request) {
        return ResponseEntity.ok(ApiResponse.success(roleService.search(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.ROLE_VIEW + "')")
    public ResponseEntity<ApiResponse<RoleDetailDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getById(id)));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('" + Permissions.ROLE_CREATE + "')")
    public ResponseEntity<ApiResponse<RoleDTO>> create(@Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(roleService.create(request)));
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.ROLE_UPDATE + "')")
    public ResponseEntity<ApiResponse<RoleDTO>> update(@PathVariable Long id, @Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roleService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.ROLE_DELETE + "')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('" + Permissions.ROLE_ASSIGN_PERMISSION + "')")
    public ResponseEntity<ApiResponse<RoleDetailDTO>> assignPermissions(@PathVariable Long id, @Valid @RequestBody AssignPermissionsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roleService.assignPermissions(id, request)));
    }
}
