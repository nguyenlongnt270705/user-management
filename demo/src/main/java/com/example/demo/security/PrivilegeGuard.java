package com.example.demo.security;

import com.example.demo.constant.SystemConstants;
import com.example.demo.entity.Company;
import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorMessage;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PrivilegeGuard {

    private final UserRepository userRepository;
    private final AuthorityService authorityService;

    // permission được gán phải nằm trong quyền của người gọi
    public void assertCanGrantPermissions(Collection<String> permissionCodes) {
        Set<String> mine = SecurityUtils.getCurrentPermissions();

        for (String code : permissionCodes) {
            if (!mine.contains(code)) {
                throw AppException.error(ErrorMessage.PERMISSION_NOT_HELD);
            }
        }
    }

    // không ai được sửa, xóa hoặc đổi permission của role SA
    public void assertNotSystemAdminRole(Role role) {
        if (role.isSystemAdmin()) {
            throw AppException.error(ErrorMessage.SYSTEM_ROLE_PROTECTED);
        }
    }

    // khi gán role cho user, cấm role System Admin và role có quyền vượt quyền người gọi
    public void assertCanAssignRoles(Collection<Role> roles) {
        Set<String> mine = SecurityUtils.getCurrentPermissions();

        for (Role role : roles) {
            if (role.isSystemAdmin()) {
                throw AppException.error(ErrorMessage.SYSTEM_ROLE_ASSIGN_FORBIDDEN);
            }
            for (Permission permission : role.getPermissions()) {
                if (!mine.contains(permission.getCode())) {
                    throw AppException.error(ErrorMessage.PERMISSION_NOT_HELD);
                }
            }
        }
    }

    // cấm vô hiệu hóa công ty cha
    public void assertCanChangeCompanyStatus(Company company, Status newStatus) {
        if (newStatus == Status.INACTIVE && SystemConstants.HQ_COMPANY_CODE.equals(company.getCode())) {
            throw AppException.error(ErrorMessage.HQ_COMPANY_PROTECTED);
        }
    }

    // cấm vô hiệu hóa SA và chính mình
    public void assertCanChangeUserStatus(User target, Status newStatus) {
        if (newStatus != Status.INACTIVE) {
            return;
        }
        if (target.getId().equals(SecurityUtils.getCurrentUserId())) {
            throw AppException.error(ErrorMessage.CANNOT_DEACTIVATE_SELF);
        }
        if (userRepository.isSystemAdmin(target.getId())) {
            throw AppException.error(ErrorMessage.SYSTEM_ADMIN_PROTECTED);
        }
    }

    // người gọi chỉ được thao tác user có quyền nằm trong quyền của mình
    public void assertCanManageUser(User target) {
        Set<String> targetPermissions = authorityService.loadAuthorities(target.getId()).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (!SecurityUtils.getCurrentPermissions().containsAll(targetPermissions)) {
            throw AppException.error(ErrorMessage.USER_OUT_OF_SCOPE);
        }
    }
}
