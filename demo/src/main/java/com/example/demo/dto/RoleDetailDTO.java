package com.example.demo.dto;

import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDetailDTO {
    private Long id;
    private String name;
    private String description;
    private boolean systemAdmin;
    private List<PermissionDTO> permissions;

    public static RoleDetailDTO fromEntity(Role entity) {
        if (entity == null) {
            return null;
        }
        List<PermissionDTO> permissions = entity.getPermissions().stream()
                .sorted(Comparator.comparing(Permission::getCode))
                .map(PermissionDTO::fromEntity)
                .toList();
        return new RoleDetailDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isSystemAdmin(),
                permissions);
    }
}