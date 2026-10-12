package com.example.demo.security;

import com.example.demo.constant.SystemConstants;
import com.example.demo.entity.Company;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorMessage;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

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

    // cấm gán SA cho user
    public void assertNoSystemAdminRole(Collection<Role> roles) {
        for (Role role : roles) {
            if (role.isSystemAdmin()) {
                throw AppException.error(ErrorMessage.SYSTEM_ROLE_ASSIGN_FORBIDDEN);
            }
        }
    }

    // ng tạo phải có quyền gán role và permission cho user
    public void assertHasAssignPermission() {
        if (!SecurityUtils.getCurrentPermissions().contains(Permissions.USER_ASSIGN_ROLE)) {
            throw AppException.error(ErrorMessage.ASSIGN_ROLE_FORBIDDEN);
        }
    }

    // Cấm đổi role và permission của SA
    public void assertNotSystemAdminUser(User target) {
        if (userRepository.isSystemAdmin(target.getId())) {
            throw AppException.error(ErrorMessage.SYSTEM_ADMIN_ACCESS_PROTECTED);
        }
    }

    // cấm inactive công ty cha và công ty ng tạo
    public void assertCanChangeCompanyStatus(Company company, Status newStatus) {
        if (newStatus != Status.INACTIVE) {
            return;
        }
        if (SystemConstants.HQ_COMPANY_CODE.equals(company.getCode())) {
            throw AppException.error(ErrorMessage.HQ_COMPANY_PROTECTED);
        }
        Long callerCompanyId = userRepository.findWithCompanyById(SecurityUtils.getCurrentUserId())
                .map(user -> user.getCompany().getId())
                .orElse(null);
        if (company.getId().equals(callerCompanyId)) {
            throw AppException.error(ErrorMessage.OWN_COMPANY_PROTECTED);
        }
    }

    // cấm inactive SA và chính mình
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
        Set<String> targetPermissions = authorityService.loadPermissionCodes(target.getId());

        if (!SecurityUtils.getCurrentPermissions().containsAll(targetPermissions)) {
            throw AppException.error(ErrorMessage.USER_OUT_OF_SCOPE);
        }
    }
}
