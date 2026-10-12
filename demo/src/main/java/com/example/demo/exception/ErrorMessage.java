package com.example.demo.exception;

import lombok.Getter;

@Getter
public enum ErrorMessage {
    SUCCESS(200, "SUCCESS", "Success"),
    FORBIDDEN(403, "FORBIDDEN", "Forbidden"),
    UNAUTHORIZED(401, "UNAUTHORIZED", "Unauthorized"),
    NOT_FOUND(404, "NOT_FOUND", "Not found"),
    INVALID_CREDENTIALS(401, "INVALID_CREDENTIALS", "Email/số điện thoại hoặc mật khẩu không đúng"),
    ACCOUNT_INACTIVE(403, "ACCOUNT_INACTIVE", "Tài khoản đã bị vô hiệu hóa"),
    INVALID_TOKEN(401, "INVALID_TOKEN", "Token không hợp lệ hoặc đã hết hạn"),
    BAD_REQUEST(400, "BAD_REQUEST", "Dữ liệu không hợp lệ"),
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "Lỗi hệ thống"),
    COMPANY_INACTIVE(403, "COMPANY_INACTIVE", "Công ty của tài khoản đã bị vô hiệu hóa"),
    PERMISSION_NOT_HELD(403, "PERMISSION_NOT_HELD", "Bạn không thể gán quyền mà chính bạn không có"),
    SYSTEM_ROLE_PROTECTED(403, "SYSTEM_ROLE_PROTECTED", "Không thể thay đổi role System Admin"),
    SYSTEM_ROLE_ASSIGN_FORBIDDEN(403, "SYSTEM_ROLE_ASSIGN_FORBIDDEN", "Không thể gán role System Admin cho người dùng"),
    HQ_COMPANY_PROTECTED(403, "HQ_COMPANY_PROTECTED", "Không thể vô hiệu hóa công ty hội sở"),
    SYSTEM_ADMIN_PROTECTED(403, "SYSTEM_ADMIN_PROTECTED", "Không thể vô hiệu hóa tài khoản System Admin"),
    CANNOT_DEACTIVATE_SELF(403, "CANNOT_DEACTIVATE_SELF", "Không thể tự vô hiệu hóa tài khoản của chính mình"),
    USER_OUT_OF_SCOPE(403, "USER_OUT_OF_SCOPE", "Bạn không đủ quyền để thao tác với người dùng này"),
    ROLE_NOT_FOUND(404, "ROLE_NOT_FOUND", "Không tìm thấy role"),
    ROLE_NAME_EXISTS(409, "ROLE_NAME_EXISTS", "Tên role đã tồn tại"),
    ROLE_IN_USE(409, "ROLE_IN_USE", "Role đang được người dùng sử dụng"),
    COMPANY_NOT_FOUND(404, "COMPANY_NOT_FOUND", "Không tìm thấy công ty"),
    COMPANY_CODE_EXISTS(409, "COMPANY_CODE_EXISTS", "Mã công ty đã tồn tại"),
    USER_NOT_FOUND(404, "USER_NOT_FOUND", "Không tìm thấy người dùng"),
    USER_CODE_EXISTS(409, "USER_CODE_EXISTS", "Mã người dùng đã tồn tại"),
    EMAIL_EXISTS(409, "EMAIL_EXISTS", "Email đã được sử dụng"),
    PHONE_EXISTS(409, "PHONE_EXISTS", "Số điện thoại đã được sử dụng"),
    ASSIGN_ROLE_FORBIDDEN(403, "ASSIGN_ROLE_FORBIDDEN", "Bạn không có quyền gán role và permission cho người dùng"),
    SYSTEM_ADMIN_ACCESS_PROTECTED(403, "SYSTEM_ADMIN_ACCESS_PROTECTED", "Không thể thay đổi role và permission của tài khoản System Admin"),
    OWN_COMPANY_PROTECTED(403, "OWN_COMPANY_PROTECTED", "Không thể vô hiệu hóa công ty mà bạn đang thuộc về"),
    CURRENT_PASSWORD_INCORRECT(400, "CURRENT_PASSWORD_INCORRECT", "Mật khẩu hiện tại không đúng"),

    ;

    ErrorMessage(final int code, final String errorCode, final String message) {
        this.code = code;
        this.errorCode = errorCode;
        this.message = message;
    }

    private final int code;
    private final String errorCode;
    private final String message;
}
