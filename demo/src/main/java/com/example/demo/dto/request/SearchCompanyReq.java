package com.example.demo.dto.request;

import com.example.demo.enums.Status;

public record SearchCompanyReq(String code, String name, Status status) {
}