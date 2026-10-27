CREATE TABLE users (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    department_id UUID NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_users_department_same_tenant FOREIGN KEY (tenant_id, department_id)
        REFERENCES departments (tenant_id, id),
    CONSTRAINT uq_users_tenant_id UNIQUE (tenant_id, id)
);

CREATE INDEX idx_users_tenant_id ON users (tenant_id);
CREATE INDEX idx_users_tenant_department_id ON users (tenant_id, department_id);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_roles_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT uq_roles_tenant_id UNIQUE (tenant_id, id)
);

CREATE INDEX idx_roles_tenant_id ON roles (tenant_id);

CREATE TABLE user_roles (
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    CONSTRAINT fk_user_roles_user_same_tenant FOREIGN KEY (tenant_id, user_id)
        REFERENCES users (tenant_id, id),
    CONSTRAINT fk_user_roles_role_same_tenant FOREIGN KEY (tenant_id, role_id)
        REFERENCES roles (tenant_id, id),
    CONSTRAINT uq_user_roles_tenant_user_role UNIQUE (tenant_id, user_id, role_id)
);

CREATE INDEX idx_user_roles_tenant_user_id ON user_roles (tenant_id, user_id);
CREATE INDEX idx_user_roles_tenant_role_id ON user_roles (tenant_id, role_id);
