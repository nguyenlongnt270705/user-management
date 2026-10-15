package com.example.demo.controller;

import com.example.demo.dto.ApiRuleDTO;
import com.example.demo.dto.request.ApiRuleRequest;
import com.example.demo.dto.request.SearchApiRuleReq;
import com.example.demo.dto.request.SearchRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.SearchResponse;
import com.example.demo.service.ApiRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/api-rules")
public class ApiRuleController {

    private final ApiRuleService apiRuleService;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<SearchResponse<ApiRuleDTO>>> search(
            @RequestBody(required = false) SearchRequest<SearchApiRuleReq> request) {
        return ResponseEntity.ok(ApiResponse.success(apiRuleService.search(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApiRuleDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(apiRuleService.getById(id)));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ApiRuleDTO>> create(@Valid @RequestBody ApiRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(apiRuleService.create(request)));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<ApiRuleDTO>> update(
            @PathVariable Long id, @Valid @RequestBody ApiRuleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(apiRuleService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        apiRuleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
