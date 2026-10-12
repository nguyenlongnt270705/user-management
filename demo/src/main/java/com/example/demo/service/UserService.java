package com.example.demo.service;

import com.example.demo.dto.PermissionDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserDetailDTO;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.SearchResponse;
import com.example.demo.entity.Company;
import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorMessage;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthorityService;
import com.example.demo.security.PrivilegeGuard;
import com.example.demo.util.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthorityService authorityService;
    private final PermissionRepository permissionRepository;
    private final PrivilegeGuard privilegeGuard;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Transactional
    public SearchResponse<UserDTO> search(SearchRequest<SearchUserReq> request) {
        SearchRequest<SearchUserReq> safeRequest = request != null ? request : new SearchRequest<>();
        Specification<User> spec = specification(safeRequest.getFilter());

        int page = safeRequest.getPagination() != null && safeRequest.getPagination().getPage() != null
                ? Math.max(safeRequest.getPagination().getPage(), 1) - 1
                : 0;
        int size = safeRequest.getPagination() != null && safeRequest.getPagination().getSize() != null
                ? Math.min(Math.max(safeRequest.getPagination().getSize(), 1), 100)
                : 10;

        Pageable pageable;
        SearchRequest.Sort orderBy = safeRequest.getSort();
        if (orderBy != null && StringUtils.hasText(orderBy.getField()) && StringUtils.hasText(orderBy.getOrder())) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(orderBy.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
            pageable = PageRequest.of(page, size, Sort.by(direction, orderBy.getField()));
        } else {
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "userCode"));
        }

        Page<User> pageResult = userRepository.findAll(spec, pageable);
        List<UserDTO> data = pageResult.getContent().stream().map(UserDTO::fromEntity).toList();
        return new SearchResponse<>(data, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public UserDetailDTO getById(Long id) {
        return toDetail(findDetail(id));
    }

    @Transactional
    public UserDetailDTO create(CreateUserRequest request) {
        privilegeGuard.assertHasAssignPermission();

        String userCode = normalizeUserCode(request.userCode());
        String email = normalizeEmail(request.email());
        String phone = normalizePhone(request.phoneNumber());

        if (userRepository.existsByUserCode(userCode)) {
            throw AppException.error(ErrorMessage.USER_CODE_EXISTS);
        }
        if (userRepository.existsByEmail(email)) {
            throw AppException.error(ErrorMessage.EMAIL_EXISTS);
        }
        if (userRepository.existsByPhoneNumber(phone)) {
            throw AppException.error(ErrorMessage.PHONE_EXISTS);
        }

        Company company = findActiveCompany(request.companyId());

        User user = User.builder()
                .company(company)
                .userCode(userCode)
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .phoneNumber(phone)
                .address(request.address())
                .dateOfBirth(request.dateOfBirth())
                .build();

        applyRolePermission(user, request.roleIds(), request.permissionIds());

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw AppException.error(ErrorMessage.USER_CODE_EXISTS,
                    "Mã người dùng, email hoặc số điện thoại đã tồn tại");
        }
        return toDetail(user);
    }

    @Transactional
    public UserDetailDTO update(Long id, UpdateUserRequest request) {
        User user = findDetail(id);
        privilegeGuard.assertCanManageUser(user);

        String phone = normalizePhone(request.phoneNumber());
        if (phone != null && userRepository.findByPhoneNumber(phone)
                .filter(other -> !other.getId().equals(id)).isPresent()) {
            throw AppException.error(ErrorMessage.PHONE_EXISTS);
        }

        if (!user.getCompany().getId().equals(request.companyId())) {
            user.setCompany(findActiveCompany(request.companyId()));
        }

        user.setFullName(request.fullName().trim());
        user.setPhoneNumber(phone);
        user.setAddress(request.address());
        user.setDateOfBirth(request.dateOfBirth());
        userRepository.saveAndFlush(user);
        return toDetail(user);
    }

    @Transactional
    public UserDTO changeStatus(Long id, ChangeUserStatusRequest request) {
        User user = findDetail(id);
        privilegeGuard.assertCanManageUser(user);
        privilegeGuard.assertCanChangeUserStatus(user, request.status());

        user.setStatus(request.status());
        userRepository.saveAndFlush(user);
        return UserDTO.fromEntity(user);
    }

    @Transactional
    public UserDetailDTO assignRolePermission(Long id, AssignUserRolePermissionRequest request) {
        privilegeGuard.assertHasAssignPermission();

        User user = findDetail(id);
        privilegeGuard.assertNotSystemAdminUser(user);
        privilegeGuard.assertCanManageUser(user);

        applyRolePermission(user, request.roleIds(), request.permissionIds());
        userRepository.saveAndFlush(user);
        return toDetail(user);
    }

    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        User user = findDetail(id);
        privilegeGuard.assertCanManageUser(user);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserDetailDTO getProfile() {
        return toDetail(findDetail(SecurityUtils.getCurrentUserId()));
    }

    @Transactional
    public void changeMyPassword(ChangePasswordRequest request) {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> AppException.error(ErrorMessage.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw AppException.error(ErrorMessage.CURRENT_PASSWORD_INCORRECT);
        }

        if (request.currentPassword().equals(request.newPassword())) {
            throw AppException.error(ErrorMessage.BAD_REQUEST, "Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private void applyRolePermission(User user, List<Long> roleIds, List<Long> permissionIds) {
        Set<Long> roleIdSet = new HashSet<>(roleIds);
        List<Role> roles = roleRepository.findAllById(roleIdSet);

        if (roles.size() != roleIdSet.size()) {
            throw AppException.error(ErrorMessage.BAD_REQUEST, "Role không tồn tại");
        }

        privilegeGuard.assertNoSystemAdminRole(roles);

        Set<Long> permissionIdSet = new HashSet<>(permissionIds);
        List<Permission> permissions = permissionRepository.findAllById(permissionIdSet);
        if (permissions.size() != permissionIdSet.size()) {
            throw AppException.error(ErrorMessage.BAD_REQUEST, "Permission không tồn tại");
        }

        privilegeGuard.assertCanGrantPermissions(permissions.stream().map(Permission::getCode).toList());

        // permission của role đã chọn
        Map<Long, Permission> rolePermissions = new HashMap<>();
        for (Role role : roles) {
            for (Permission permission : role.getPermissions()) {
                rolePermissions.put(permission.getId(), permission);
            }
        }

        // thêm
        Set<Permission> granted = new HashSet<>();
        for (Permission permission : permissions) {
            if (!rolePermissions.containsKey(permission.getId())) {
                granted.add(permission);
            }
        }

        // bớt
        Set<Permission> revoked = new HashSet<>();
        for (Permission permission : rolePermissions.values()) {
            if (!permissionIdSet.contains(permission.getId())) {
                revoked.add(permission);
            }
        }

        user.getRoles().clear();
        user.getRoles().addAll(roles);
        user.getGrantedPermissions().clear();
        user.getGrantedPermissions().addAll(granted);
        user.getRevokedPermissions().clear();
        user.getRevokedPermissions().addAll(revoked);
    }

    private Company findActiveCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> AppException.error(ErrorMessage.COMPANY_NOT_FOUND));
        if (company.getStatus() == Status.INACTIVE) {
            throw AppException.error(ErrorMessage.BAD_REQUEST, "Công ty đã bị vô hiệu hóa");
        }
        return company;
    }

    private UserDetailDTO toDetail(User user) {
        Set<String> codes = authorityService.loadPermissionCodes(user.getId());
        List<PermissionDTO> permissions = codes.isEmpty()
                ? List.of()
                : permissionRepository.findByCodeIn(codes).stream()
                .sorted(Comparator.comparing(Permission::getCode))
                .map(PermissionDTO::fromEntity)
                .toList();
        return UserDetailDTO.of(user, permissions);
    }

    private User findDetail(Long id) {
        return userRepository.findDetailById(id)
                .orElseThrow(() -> AppException.error(ErrorMessage.USER_NOT_FOUND));
    }

    private String normalizeUserCode(String userCode) {
        return userCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        return StringUtils.hasText(phone) ? phone.trim() : null;
    }

    private Specification<User> specification(SearchUserReq filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter != null) {
                if (StringUtils.hasText(filter.userCode())) {
                    predicates.add(cb.equal(root.get("userCode"), normalizeUserCode(filter.userCode())));
                }
                if (StringUtils.hasText(filter.fullName())) {
                    predicates.add(cb.equal(root.get("fullName"), filter.fullName().trim()));
                }
                if (StringUtils.hasText(filter.email())) {
                    predicates.add(cb.equal(root.get("email"), normalizeEmail(filter.email())));
                }
                if (filter.status() != null) {
                    predicates.add(cb.equal(root.get("status"), filter.status()));
                }
                if (StringUtils.hasText(filter.companyName())) {
                    predicates.add(cb.equal(root.join("company").get("name"), filter.companyName().trim()));
                }
                if (filter.roleId() != null) {
                    predicates.add(cb.equal(root.join("roles").get("id"), filter.roleId()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
