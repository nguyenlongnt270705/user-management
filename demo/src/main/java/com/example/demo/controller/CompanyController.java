package com.example.demo.controller;

import com.example.demo.dto.CompanyDTO;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.SearchResponse;
import com.example.demo.security.Permissions;
import com.example.demo.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('" + Permissions.COMPANY_VIEW + "')")
    public ResponseEntity<ApiResponse<SearchResponse<CompanyDTO>>> search(
            @RequestBody(required = false) SearchRequest<SearchCompanyReq> request) {
        return ResponseEntity.ok(ApiResponse.success(companyService.search(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.COMPANY_VIEW + "')")
    public ResponseEntity<ApiResponse<CompanyDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(companyService.getById(id)));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('" + Permissions.COMPANY_CREATE + "')")
    public ResponseEntity<ApiResponse<CompanyDTO>> create(@Valid @RequestBody CreateCompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(companyService.create(request)));
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.COMPANY_UPDATE + "')")
    public ResponseEntity<ApiResponse<CompanyDTO>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateCompanyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(companyService.update(id, request)));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('" + Permissions.COMPANY_CHANGE_STATUS + "')")
    public ResponseEntity<ApiResponse<CompanyDTO>> changeStatus(
            @PathVariable Long id, @Valid @RequestBody ChangeCompanyStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(companyService.changeStatus(id, request)));
    }
}
