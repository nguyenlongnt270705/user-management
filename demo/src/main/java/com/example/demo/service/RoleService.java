package com.example.demo.service;

import com.example.demo.dto.PermissionDTO;
import com.example.demo.dto.RoleDTO;
import com.example.demo.dto.RoleDetailDTO;
import com.example.demo.dto.request.AssignPermissionsRequest;
import com.example.demo.dto.request.CreateRoleRequest;
import com.example.demo.dto.request.SearchRequest;
import com.example.demo.dto.request.SearchRoleReq;
import com.example.demo.dto.response.SearchResponse;
import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorMessage;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.security.PrivilegeGuard;
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

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PrivilegeGuard privilegeGuard;

    public SearchResponse<RoleDTO> search(SearchRequest<SearchRoleReq> request) {
        SearchRequest<SearchRoleReq> safeRequest = request != null ? request : new SearchRequest<>();
        Specification<Role> spec = specification(safeRequest.getFilter());

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
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        }

        Page<Role> pageResult = roleRepository.findAll(spec, pageable);
        List<RoleDTO> data = pageResult.getContent().stream()
                .map(RoleDTO::fromEntity)
                .toList();
        return new SearchResponse<>(data, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public RoleDetailDTO getById(Long id) {
        Role entity = roleRepository.findById(id).orElseThrow(() -> AppException.error(ErrorMessage.ROLE_NOT_FOUND));

        if (entity.isSystemAdmin()) {
            List<PermissionDTO> all = permissionRepository.findAll().stream()
                    .sorted(Comparator.comparing(Permission::getCode))
                    .map(PermissionDTO::fromEntity)
                    .toList();
            return new RoleDetailDTO(entity.getId(), entity.getName(), entity.getDescription(), true, all);
        }
        return RoleDetailDTO.fromEntity(entity);
    }

    @Transactional
    public RoleDTO create(CreateRoleRequest request) {
        String name = normalizeName(request.name());
        if (roleRepository.findByName(name).isPresent()) {
            throw AppException.error(ErrorMessage.ROLE_NAME_EXISTS);
        }

        try {
            Role role = Role.builder()
                    .name(name)
                    .description(request.description())
                    .build();
            return RoleDTO.fromEntity(roleRepository.saveAndFlush(role));
        } catch (DataIntegrityViolationException e) {
            throw AppException.error(ErrorMessage.ROLE_NAME_EXISTS);
        }
    }

    @Transactional
    public RoleDTO update(Long id, CreateRoleRequest request) {
        Role role = roleRepository.findById(id).orElseThrow(() -> AppException.error(ErrorMessage.ROLE_NOT_FOUND));
        privilegeGuard.assertNotSystemAdminRole(role);

        String newName = normalizeName(request.name());
        boolean usedByOther = roleRepository.findByName(newName)
                .filter(other -> !other.getId().equals(id))
                .isPresent();
        if (usedByOther) {
            throw AppException.error(ErrorMessage.ROLE_NAME_EXISTS);
        }

        try {
            role.setName(newName);
            role.setDescription(request.description());
            return RoleDTO.fromEntity(roleRepository.saveAndFlush(role));
        } catch (DataIntegrityViolationException e) {
            throw AppException.error(ErrorMessage.ROLE_NAME_EXISTS);
        }
    }

    @Transactional
    public void delete(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> AppException.error(ErrorMessage.ROLE_NOT_FOUND));
        privilegeGuard.assertNotSystemAdminRole(role);

        long userCount = roleRepository.countByRoleId(id);
        if (userCount > 0) {
            throw AppException.error(ErrorMessage.ROLE_IN_USE,
                    "Role đang được " + userCount + " người dùng sử dụng.");
        }
        try {
            roleRepository.delete(role);
            roleRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw AppException.error(ErrorMessage.ROLE_IN_USE, "Role vừa được gán cho người dùng, vui lòng thử lại");
        }
    }

    @Transactional
    public RoleDetailDTO assignPermissions(Long id, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(id).orElseThrow(() -> AppException.error(ErrorMessage.ROLE_NOT_FOUND));
        privilegeGuard.assertNotSystemAdminRole(role);

        Set<Long> requestIds = new HashSet<>(request.permissionIds());
        List<Permission> requested = permissionRepository.findAllById(requestIds);
        if (requested.size() != requestIds.size()) {
            throw AppException.error(ErrorMessage.BAD_REQUEST, "Có permission không tồn tại");
        }

        // permission mới
        Set<String> currentCodes = role.getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
        List<String> addedCodes = requested.stream()
                .map(Permission::getCode)
                .filter(code -> !currentCodes.contains(code))
                .toList();
        privilegeGuard.assertCanGrantPermissions(addedCodes);

        role.getPermissions().clear();
        role.getPermissions().addAll(requested);
        return RoleDetailDTO.fromEntity(role);
    }

    private Specification<Role> specification(SearchRoleReq request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request != null && StringUtils.hasText(request.name())) {
                predicates.add(cb.equal(root.get("name"), normalizeName(request.name())));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // bỏ khoảng trắng đầu cuối, thống nhất Unicode, viết hoa toàn bộ
    private String normalizeName(String name) {
        return Normalizer.normalize(name.trim(), Normalizer.Form.NFC).toUpperCase(Locale.ROOT);
    }
}
