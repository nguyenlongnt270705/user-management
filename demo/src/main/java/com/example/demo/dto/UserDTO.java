package com.example.demo.dto;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String userCode;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;
    private Status status;
    private CompanyInfo company;
    private List<RoleInfo> roles;

    public record CompanyInfo(Long id, String code, String name) {
    }

    public record RoleInfo(Long id, String name) {
    }

    public static UserDTO fromEntity(User entity) {
        if (entity == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.fill(entity);
        return dto;
    }

    protected void fill(User entity) {
        this.id = entity.getId();
        this.userCode = entity.getUserCode();
        this.fullName = entity.getFullName();
        this.email = entity.getEmail();
        this.phoneNumber = entity.getPhoneNumber();
        this.address = entity.getAddress();
        this.dateOfBirth = entity.getDateOfBirth();
        this.status = entity.getStatus();
        this.company = new CompanyInfo(entity.getCompany().getId(),
                entity.getCompany().getCode(), entity.getCompany().getName());
        this.roles = entity.getRoles().stream()
                .sorted(Comparator.comparing(Role::getName))
                .map(role -> new RoleInfo(role.getId(), role.getName()))
                .toList();
    }
}
