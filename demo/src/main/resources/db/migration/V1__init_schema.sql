-- V1: schema ban đầu (6 bảng). Thứ tự: bảng cha trước, bảng trung gian sau cùng.

CREATE TABLE companies
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)  NOT NULL,
    name          VARCHAR(150) NOT NULL,
    address       VARCHAR(255) NOT NULL,
    description   VARCHAR(500) NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_by    VARCHAR(100) NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    modified_by   VARCHAR(100) NULL,
    modified_date DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_companies_code UNIQUE (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE permissions
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(100) NOT NULL,
    name          VARCHAR(150) NOT NULL,
    description   VARCHAR(255) NULL,
    created_by    VARCHAR(100) NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    modified_by   VARCHAR(100) NULL,
    modified_date DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_permissions_code UNIQUE (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE roles
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    name            VARCHAR(50)  NOT NULL,
    description     VARCHAR(255) NULL,
    is_system_admin BIT(1)       NOT NULL DEFAULT b'0',
    created_by      VARCHAR(100) NOT NULL,
    created_date    DATETIME(6)  NOT NULL,
    modified_by     VARCHAR(100) NULL,
    modified_date   DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE users
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    company_id    BIGINT       NOT NULL,
    user_code     VARCHAR(20)  NOT NULL,
    password      VARCHAR(100) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    phone_number  VARCHAR(20)  NULL,
    address       VARCHAR(255) NULL,
    date_of_birth DATE         NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_by    VARCHAR(100) NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    modified_by   VARCHAR(100) NULL,
    modified_date DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_user_code UNIQUE (user_code),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone_number UNIQUE (phone_number),
    KEY idx_users_company_id (company_id),
    CONSTRAINT fk_users_company FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    KEY idx_user_roles_role_id (role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE role_permissions
(
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    KEY idx_role_permissions_permission_id (permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;