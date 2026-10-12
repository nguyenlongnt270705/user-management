package com.example.demo.dto.request;

import com.example.demo.enums.Status;

public record SearchUserReq(String userCode,
                            String fullName,
                            String email,
                            Status status,
                            String companyName,
                            Long roleId) {
}