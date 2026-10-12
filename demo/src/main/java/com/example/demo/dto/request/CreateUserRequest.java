package com.example.demo.dto.request;

import com.example.demo.util.ValidationPatterns;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record CreateUserRequest(
        @NotNull(message = "Công ty không được để trống")
        Long companyId,

        @NotBlank(message = "Mã người dùng không được để trống")
        @Size(max = 20, message = "Mã người dùng tối đa 20 ký tự")
        String userCode,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 50, message = "Email tối đa 50 ký tự")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, max = 72, message = "Mật khẩu từ 8 đến 72 ký tự")
        String password,

        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
        String fullName,

        @Pattern(regexp = ValidationPatterns.PHONE, message = "Số điện thoại gồm 10 chữ số và bắt đầu bằng 0")
        String phoneNumber,

        @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
        String address,

        @Past(message = "Ngày sinh phải ở quá khứ")
        LocalDate dateOfBirth,

        @NotEmpty(message = "Phải chọn ít nhất một role")
        List<@NotNull(message = "Id role không được null") Long> roleIds,

        @NotEmpty(message = "Phải chọn ít nhất một permission")
        List<@NotNull(message = "Id permission không được null") Long> permissionIds) {
}
