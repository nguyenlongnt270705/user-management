package com.example.demo.service;

import com.example.demo.dto.PermissionDTO;
import com.example.demo.dto.request.SearchPermissionReq;
import com.example.demo.dto.request.SearchRequest;
import com.example.demo.dto.response.SearchResponse;
import com.example.demo.entity.Permission;
import com.example.demo.repository.PermissionRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public SearchResponse<PermissionDTO> search(SearchRequest<SearchPermissionReq> request) {
        SearchRequest<SearchPermissionReq> safeRequest = request != null ? request : new SearchRequest<>();
        Specification<Permission> spec = specification(safeRequest.getFilter());

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
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        }
        Page<Permission> pageResult = permissionRepository.findAll(spec, pageable);
        List<PermissionDTO> data = pageResult.getContent()
                .stream()
                .map(PermissionDTO::fromEntity)
                .toList();
        return new SearchResponse<>(data, pageResult.getTotalElements());
    }

    private Specification<Permission> specification(SearchPermissionReq request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!ObjectUtils.isEmpty(request)) {
                if (StringUtils.hasText(request.code())) {
                    predicates.add(cb.equal(root.get("code"), request.code()));
                }
                if (StringUtils.hasText(request.name())) {
                    predicates.add(cb.equal(root.get("name"), request.name()));
                }
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
