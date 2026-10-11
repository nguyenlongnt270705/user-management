package com.example.demo.controller;

import com.example.demo.dto.request.SearchPermissionReq;
import com.example.demo.dto.request.SearchRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.PermissionDTO;
import com.example.demo.dto.response.SearchResponse;
import com.example.demo.security.Permissions;
import com.example.demo.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('" + Permissions.PERMISSION_VIEW + "')")
    public ResponseEntity<ApiResponse<SearchResponse<PermissionDTO>>> search(
            @RequestBody(required = false) SearchRequest<SearchPermissionReq> request) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.search(request)));
    }

}
