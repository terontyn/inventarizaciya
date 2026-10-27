CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    asset_id UUID NOT NULL,
    ticket_state VARCHAR(32) NOT NULL,
    opened_at TIMESTAMP WITH TIME ZONE NOT NULL,
    closed_at TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT uq_tickets_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT ck_tickets_state CHECK (ticket_state IN ('OPEN', 'CLOSED')),
    CONSTRAINT ck_tickets_completion_consistency CHECK (
        (ticket_state = 'OPEN' AND closed_at IS NULL)
        OR
        (ticket_state = 'CLOSED' AND closed_at IS NOT NULL)
    )
);

CREATE INDEX idx_tickets_tenant_asset_id ON tickets (tenant_id, asset_id);
CREATE INDEX idx_tickets_tenant_state_opened_at ON tickets (tenant_id, ticket_state, opened_at);

CREATE TABLE local_audit_records (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    actor VARCHAR(255) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id UUID NOT NULL,
    action_type VARCHAR(64) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    correlation_id VARCHAR(255) NOT NULL,
    change_payload TEXT NOT NULL
);

CREATE INDEX idx_ticket_local_audit_tenant_entity ON local_audit_records (tenant_id, entity_type, entity_id);
