package com.example.demo.dto;

import com.example.demo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDTO extends UserDTO {
    private List<PermissionDTO> permissions;

    public static UserDetailDTO of(User entity, List<PermissionDTO> permissions) {
        UserDetailDTO dto = new UserDetailDTO();
        dto.fill(entity);
        dto.setPermissions(permissions);
        return dto;
    }
}
