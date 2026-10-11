package com.example.demo.dto;

import com.example.demo.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    private String name;
    private String description;
    private boolean systemAdmin;

    public static RoleDTO fromEntity(Role entity) {
        if (entity == null) {
            return null;
        }
        return new RoleDTO(
                entity.getId(),
                entity.getName(), entity
                .getDescription(), entity
                .isSystemAdmin());
    }
}
