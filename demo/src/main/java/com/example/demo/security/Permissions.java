package com.example.demo.security;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Permissions {
    public static final String COMPANY_VIEW = "COMPANY_VIEW";
    public static final String COMPANY_CREATE = "COMPANY_CREATE";
    public static final String COMPANY_UPDATE = "COMPANY_UPDATE";
    public static final String COMPANY_CHANGE_STATUS = "COMPANY_CHANGE_STATUS";

    public static final String USER_VIEW = "USER_VIEW";
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_CHANGE_STATUS = "USER_CHANGE_STATUS";
    public static final String USER_ASSIGN_ROLE = "USER_ASSIGN_ROLE";

    public static final String ROLE_VIEW = "ROLE_VIEW";
    public static final String ROLE_CREATE = "ROLE_CREATE";
    public static final String ROLE_UPDATE = "ROLE_UPDATE";
    public static final String ROLE_DELETE = "ROLE_DELETE";
    public static final String ROLE_ASSIGN_PERMISSION = "ROLE_ASSIGN_PERMISSION";

    public static final String PERMISSION_VIEW = "PERMISSION_VIEW";
}
