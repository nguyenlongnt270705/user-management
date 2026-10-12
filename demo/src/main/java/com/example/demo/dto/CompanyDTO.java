package com.example.demo.dto;

import com.example.demo.entity.Company;
import com.example.demo.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDTO {
    private Long id;
    private String code;
    private String name;
    private String address;
    private String description;
    private Status status;

    public static CompanyDTO fromEntity(Company entity) {
        if (entity == null) {
            return null;
        }
        return new CompanyDTO(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getAddress(),
                entity.getDescription(),
                entity.getStatus());
    }
}
