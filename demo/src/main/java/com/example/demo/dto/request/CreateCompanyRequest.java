package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
        @NotBlank(message = "Mã công ty không được để trống")
        @Size(max = 10, message = "Mã công ty tối đa 10 ký tự")
        String code,

        @NotBlank(message = "Tên công ty không được để trống")
        @Size(max = 150, message = "Tên công ty tối đa 150 ký tự")
        String name,

        @NotBlank(message = "Địa chỉ không được để trống")
        @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
        String address,

        @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
        String description) {
}
