package com.example.demo.service;

import com.example.demo.dto.ApiRuleDTO;
import com.example.demo.dto.request.ApiRuleRequest;
import com.example.demo.dto.request.SearchApiRuleReq;
import com.example.demo.dto.request.SearchRequest;
import com.example.demo.dto.response.SearchResponse;
import com.example.demo.entity.ApiRule;
import com.example.demo.entity.Permission;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorMessage;
import com.example.demo.repository.ApiRuleRepository;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.security.ApiRuleRegistry;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ApiRuleService {

    private static final Set<String> ALLOWED_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
    private static final String RULE_PATH = "/api/api-rules";
    private static final String AUTH_PATH = "/api/auth";
    private static final PathPatternParser PARSER = new PathPatternParser();
    private final ApiRuleRepository apiRuleRepository;
    private final PermissionRepository permissionRepository;
    private final ApiRuleRegistry apiRuleRegistry;

    @Transactional(readOnly = true)
    public SearchResponse<ApiRuleDTO> search(SearchRequest<SearchApiRuleReq> request) {
        SearchRequest<SearchApiRuleReq> safeRequest = request != null ? request : new SearchRequest<>();
        Specification<ApiRule> spec = specification(safeRequest.getFilter());

        int page = safeRequest.getPagination() != null && safeRequest.getPagination().getPage() != null
                ? Math.max(safeRequest.getPagination().getPage(), 1) - 1
                : 0;
        int size = safeRequest.getPagination() != null && safeRequest.getPagination().getSize() != null
                ? Math.min(Math.max(safeRequest.getPagination().getSize(), 1), 100)
                : 10;

        Pageable pageable;
        SearchRequest.Sort sort = safeRequest.getSort();
        if (sort != null && StringUtils.hasText(sort.getField()) && StringUtils.hasText(sort.getOrder())) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sort.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
            pageable = PageRequest.of(page, size, Sort.by(direction, sort.getField()));
        } else {
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "pathPattern"));
        }

        Page<ApiRule> pageResult = apiRuleRepository.findAll(spec, pageable);
        List<ApiRuleDTO> data = pageResult.getContent().stream().map(ApiRuleDTO::fromEntity).toList();
        return new SearchResponse<>(data, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ApiRuleDTO getById(Long id) {
        return ApiRuleDTO.fromEntity(findRule(id));
    }

    public ApiRuleDTO create(ApiRuleRequest request) {
        String method = normalizeMethod(request.httpMethod());
        String path = request.pathPattern().trim();

        validatePath(path);

        if (apiRuleRepository.findByMethodAndPath(method, path).isPresent()) {
            throw AppException.error(ErrorMessage.API_RULE_EXISTS);
        }

        ApiRule rule = ApiRule.builder()
                .httpMethod(method)
                .pathPattern(path)
                .permission(findPermission(request.permissionId()))
                .description(request.description())
                .build();

        ApiRule saved = save(rule);

        // cache lại api rule
        apiRuleRegistry.reload();
        return ApiRuleDTO.fromEntity(saved);
    }

    public ApiRuleDTO update(Long id, ApiRuleRequest request) {
        ApiRule rule = findRule(id);
        if (rule.isLocked()) {
            throw AppException.error(ErrorMessage.API_RULE_LOCKED);
        }
        String method = normalizeMethod(request.httpMethod());
        String path = request.pathPattern().trim();
        validatePath(path);

        // check trùng rule
        if (apiRuleRepository.findByMethodAndPath(method, path).
                filter(other -> !other.getId().equals(id)).isPresent()) {
            throw AppException.error(ErrorMessage.API_RULE_EXISTS);
        }

        rule.setHttpMethod(method);
        rule.setPathPattern(path);
        rule.setPermission(findPermission(request.permissionId()));
        rule.setDescription(request.description());

        ApiRule saved = save(rule);
        apiRuleRegistry.reload();
        return ApiRuleDTO.fromEntity(saved);
    }

    public void delete(Long id) {
        ApiRule rule = findRule(id);
        if (rule.isLocked()) {
            throw AppException.error(ErrorMessage.API_RULE_LOCKED);
        }
        apiRuleRepository.deleteById(id);
        apiRuleRegistry.reload();
    }

    private ApiRule save(ApiRule rule) {
        try {
            return apiRuleRepository.saveAndFlush(rule);
        } catch (DataIntegrityViolationException e) {
            throw AppException.error(ErrorMessage.API_RULE_EXISTS);
        }
    }

    private Permission findPermission(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> AppException.error(ErrorMessage.API_RULE_NOT_FOUND));
    }

    // validate path, bắt đầu bằng /api, không *, không bị chặn
    private void validatePath(String path) {
        if (!path.startsWith("/api/") || path.contains("*")) {
            throw AppException.error(ErrorMessage.API_RULE_PATH_INVALID);
        }

        if (isUnder(path, RULE_PATH) || isUnder(path, AUTH_PATH)) {
            throw AppException.error(ErrorMessage.API_RULE_PATH_RESERVED);
        }
        try {
            PARSER.parse(path);
        } catch (RuntimeException e) {
            throw AppException.error(ErrorMessage.API_RULE_PATH_INVALID);
        }
    }


    private boolean isUnder(String path, String prefix) {
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }

    private String normalizeMethod(String method) {
        String normalized = method.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_METHODS.contains(normalized)) {
            throw AppException.error(ErrorMessage.BAD_REQUEST, "HTTP Method không hợp lệ");
        }
        return normalized;

    }

    private ApiRule findRule(Long id) {
        return apiRuleRepository.findById(id)
                .orElseThrow(() -> AppException.error(ErrorMessage.API_RULE_NOT_FOUND));
    }

    private Specification<ApiRule> specification(SearchApiRuleReq filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter != null) {
                if (StringUtils.hasText(filter.httpMethod())) {
                    predicates.add(cb.equal(root.get("httpMethod"), filter.httpMethod().trim().toUpperCase(Locale.ROOT)));
                }
                if (StringUtils.hasText(filter.pathPattern())) {
                    predicates.add(cb.equal(root.get("pathPattern"), filter.pathPattern().trim()));
                }
                if (StringUtils.hasText(filter.permissionCode())) {
                    predicates.add(cb.equal(root.join("permission").get("code"), filter.permissionCode().trim()));
                }
                if (filter.locked() != null) {
                    predicates.add(cb.equal(root.get("locked"), filter.locked()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
