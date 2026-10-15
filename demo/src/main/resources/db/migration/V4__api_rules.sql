-- V4: phân quyền theo path. 4 permission mới + bảng api_rules + seed rule của các API hiện có

INSERT INTO permissions (code, name, description, created_by, created_date)
VALUES ('API_RULE_VIEW', 'Xem cấu hình API', 'Xem danh sách và chi tiết rule phân quyền theo path', 'system', UTC_TIMESTAMP(6)),
       ('API_RULE_CREATE', 'Tạo cấu hình API', 'Thêm rule phân quyền theo path', 'system', UTC_TIMESTAMP(6)),
       ('API_RULE_UPDATE', 'Sửa cấu hình API', 'Sửa rule phân quyền theo path', 'system', UTC_TIMESTAMP(6)),
       ('API_RULE_DELETE', 'Xóa cấu hình API', 'Xóa rule phân quyền theo path', 'system', UTC_TIMESTAMP(6));

CREATE TABLE api_rules
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    http_method   VARCHAR(10)  NOT NULL,
    path_pattern  VARCHAR(255) NOT NULL,
    permission_id BIGINT       NOT NULL,
    locked        BIT(1)       NOT NULL DEFAULT b'0',
    description   VARCHAR(255) NULL,
    created_by    VARCHAR(100) NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    modified_by   VARCHAR(100) NULL,
    modified_date DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_api_rules_method_path UNIQUE (http_method, path_pattern),
    KEY idx_api_rules_permission_id (permission_id),
    CONSTRAINT fk_api_rules_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Seed 24 rule: 19 rule tương đương @PreAuthorize hiện tại + 5 rule quản lý rule (locked, không sửa được qua API)
INSERT INTO api_rules (http_method, path_pattern, permission_id, locked, description, created_by, created_date)
VALUES
    -- user
    ('POST', '/api/users/search', (SELECT id FROM permissions WHERE code = 'USER_VIEW'), b'0', 'Tìm kiếm người dùng', 'system', UTC_TIMESTAMP(6)),
    ('GET', '/api/users/{id}', (SELECT id FROM permissions WHERE code = 'USER_VIEW'), b'0', 'Chi tiết người dùng', 'system', UTC_TIMESTAMP(6)),
    ('POST', '/api/users/create', (SELECT id FROM permissions WHERE code = 'USER_CREATE'), b'0', 'Tạo người dùng', 'system', UTC_TIMESTAMP(6)),
    ('POST', '/api/users/{id}', (SELECT id FROM permissions WHERE code = 'USER_UPDATE'), b'0', 'Sửa người dùng', 'system', UTC_TIMESTAMP(6)),
    ('POST', '/api/users/{id}/status', (SELECT id FROM permissions WHERE code = 'USER_CHANGE_STATUS'), b'0', 'Đổi trạng thái người dùng', 'system', UTC_TIMESTAMP(6)),
    ('POST', '/api/users/{id}/assign-role-permission', (SELECT id FROM permissions WHERE code = 'USER_ASSIGN_ROLE'), b'0', 'Gán role và permission cho người dùng', 'system', UTC_TIMESTAMP(6)),
    ('POST', '/api/users/{id}/reset-password', (SELECT id FROM permissions WHERE code = 'USER_UPDATE'), b'0', 'Reset mật khẩu người dùng', 'system', UTC_TIMESTAMP(6)),
    -- company
    ('POST', '/api/companies/search', (SELECT id FROM permissions WHERE code = 'COMPANY_VIEW'), b'0', 'Tìm kiếm công ty', 'system', UTC_TIMESTAMP(6)),
    ('GET', '/api/companies/{id}', (SELECT id FROM permissions WHERE code = 'COMPANY_VIEW'), b'0', 'Chi tiết công ty', 'system', UTC_TIMESTAMP(6)),
    ('POST', '/api/companies/create', (SELECT id FROM permissions WHERE code = 'COMPANY_CREATE'), b'0', 'Tạo công ty', 'system', UTC_TIMESTAMP(6)),
    ('POST', '/api/companies/{id}', (SELECT id FROM permissions WHERE code = 'COMPANY_UPDATE'), b'0', 'Sửa công ty', 'system', UTC_TIMESTAMP(6)),
    ('POST', '/api/companies/{id}/status', (SELECT id FROM permissions WHERE code = 'COMPANY_CHANGE_STATUS'), b'0', 'Đổi trạng thái công ty', 'system', UTC_TIMESTAMP(6)),
    -- role
    ('POST', '/api/roles/search', (SELECT id FROM permissions WHERE code = 'ROLE_VIEW'), b'0', 'Tìm kiếm role', 'system', UTC_TIMESTAMP(6)),
    ('GET', '/api/roles/{id}', (SELECT id FROM permissions WHERE code = 'ROLE_VIEW'), b'0', 'Chi tiết role', 'system', UTC_TIMESTAMP(6)),
    ('POST', '/api/roles/create', (SELECT id FROM permissions WHERE code = 'ROLE_CREATE'), b'0', 'Tạo role', 'system', UTC_TIMESTAMP(6)),
    ('POST', '/api/roles/{id}', (SELECT id FROM permissions WHERE code = 'ROLE_UPDATE'), b'0', 'Sửa role', 'system', UTC_TIMESTAMP(6)),
    ('DELETE', '/api/roles/{id}', (SELECT id FROM permissions WHERE code = 'ROLE_DELETE'), b'0', 'Xóa role', 'system', UTC_TIMESTAMP(6)),
    ('POST', '/api/roles/{id}/permissions', (SELECT id FROM permissions WHERE code = 'ROLE_ASSIGN_PERMISSION'), b'0', 'Gán permission cho role', 'system', UTC_TIMESTAMP(6)),
    -- permission
    ('POST', '/api/permissions/search', (SELECT id FROM permissions WHERE code = 'PERMISSION_VIEW'), b'0', 'Tìm kiếm permission', 'system', UTC_TIMESTAMP(6)),
    -- quản lý rule (locked: tránh tự khoá chính mình)
    ('POST', '/api/api-rules/search', (SELECT id FROM permissions WHERE code = 'API_RULE_VIEW'), b'1', 'Tìm kiếm rule', 'system', UTC_TIMESTAMP(6)),
    ('GET', '/api/api-rules/{id}', (SELECT id FROM permissions WHERE code = 'API_RULE_VIEW'), b'1', 'Chi tiết rule', 'system', UTC_TIMESTAMP(6)),
    ('POST', '/api/api-rules/create', (SELECT id FROM permissions WHERE code = 'API_RULE_CREATE'), b'1', 'Tạo rule', 'system', UTC_TIMESTAMP(6)),
    ('POST', '/api/api-rules/{id}', (SELECT id FROM permissions WHERE code = 'API_RULE_UPDATE'), b'1', 'Sửa rule', 'system', UTC_TIMESTAMP(6)),
    ('DELETE', '/api/api-rules/{id}', (SELECT id FROM permissions WHERE code = 'API_RULE_DELETE'), b'1', 'Xóa rule', 'system', UTC_TIMESTAMP(6));