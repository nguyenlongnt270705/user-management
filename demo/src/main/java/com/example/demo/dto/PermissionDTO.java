package com.example.demo.dto;

import com.example.demo.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PermissionDTO {
    private Long id;
    private String code;
    private String name;
    private String description;

    public PermissionDTO(Permission entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.code = entity.getCode();
            this.name = entity.getName();
            this.description = entity.getDescription();
        }
    }

    public static PermissionDTO fromEntity(Permission entity) {
        if (entity == null) {
            return null;
        }
        PermissionDTO response = new PermissionDTO();
        BeanUtils.copyProperties(entity, response);
        return response;
    }
}