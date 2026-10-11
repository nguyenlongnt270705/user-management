package com.example.demo.config;

import com.example.demo.constant.SystemConstants;
import com.example.demo.entity.Company;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Locale;

@RequiredArgsConstructor
@Component
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.full-name:System Administrator}")
    private String adminFullName;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initSystemAdmin() {
        String email = adminEmail.trim().toLowerCase(Locale.ROOT);
        if (email.isBlank() || adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "Thiếu SYSTEM_ADMIN_EMAIL hoặc SYSTEM_ADMIN_PASSWORD");
        }
        userRepository.findByEmail(email).ifPresentOrElse(
                this::ensureExistingSystemAdmin,
                () -> createInitialSystemAdmin(email));
    }

    private void ensureExistingSystemAdmin(User existing) {
        boolean changed = false;

        if (!hasSystemAdminRole(existing)) {
            existing.getRoles().add(resolveSystemAdminRole());
            changed = true;
        }
        if (existing.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(adminPassword));
            changed = true;
        }
        if (!SystemConstants.HQ_COMPANY_CODE.equals(existing.getCompany().getCode())) {
            log.warn("System Admin đang thuộc công ty {}, chuyển về công ty {}",
                    existing.getCompany().getCode(), SystemConstants.HQ_COMPANY_CODE);
            existing.setCompany(resolveHqCompany());
            changed = true;
        }

        if (changed) {
            userRepository.saveAndFlush(existing);
            log.info("Đã bổ sung thông tin còn thiếu cho System Admin: email={}", existing.getEmail());
        } else {
            log.info("System Admin đã tồn tại, bỏ qua tạo mới: email={}, userCode={}",
                    existing.getEmail(), existing.getUserCode());
        }
    }

    private boolean hasSystemAdminRole(User user) {
        for (Role role : user.getRoles()) {
            if (role.isSystemAdmin()) {
                return true;
            }
        }
        return false;
    }

    private void createInitialSystemAdmin(String email) {
        Company company = resolveHqCompany();
        Role systemAdminRole = resolveSystemAdminRole();

        User admin = User.builder()
                .company(company)
                .userCode(SystemConstants.SYSTEM_ADMIN_USER_CODE)
                .email(email)
                .password(passwordEncoder.encode(adminPassword))
                .fullName(adminFullName)
                .status(Status.ACTIVE)
                .roles(new HashSet<>())
                .build();

        admin.getRoles().add(systemAdminRole);
        userRepository.save(admin);
        log.info("Đã khởi tạo thành công tài khoản System Admin mới: email={}, userCode={}", email, SystemConstants.SYSTEM_ADMIN_USER_CODE);
    }

    private Company resolveHqCompany() {
        return companyRepository.findByCode(SystemConstants.HQ_COMPANY_CODE)
                .orElseGet(() -> companyRepository.save(Company.builder()
                        .code(SystemConstants.HQ_COMPANY_CODE)
                        .name(SystemConstants.HQ_COMPANY_NAME)
                        .address(SystemConstants.HQ_COMPANY_ADDRESS)
                        .build()));
    }

    private Role resolveSystemAdminRole() {
        return roleRepository.findByName(SystemConstants.SYSTEM_ADMIN_ROLE_NAME)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(SystemConstants.SYSTEM_ADMIN_ROLE_NAME)
                        .description("Quản trị viên hệ thống")
                        .systemAdmin(true)
                        .build()));
    }
}
