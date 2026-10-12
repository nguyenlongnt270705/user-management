package com.example.demo.dto.request;

import com.example.demo.util.ValidationPatterns;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UpdateUserRequest(
        @NotNull(message = "Công ty không được để trống")
        Long companyId,

        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
        String fullName,

        @Pattern(regexp = ValidationPatterns.PHONE, message = "Số điện thoại gồm 10 chữ số và bắt đầu bằng 0")
        String phoneNumber,

        @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
        String address,

        @Past(message = "Ngày sinh phải ở quá khứ")
        LocalDate dateOfBirth) {
}
