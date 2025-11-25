CREATE TABLE IF NOT EXISTS audit (
    id BIGSERIAL PRIMARY KEY,
    event_date TIMESTAMP NOT NULL,
    event_code VARCHAR(100) NOT NULL,
    behavior VARCHAR(255) NOT NULL,
    username VARCHAR(150),
    email VARCHAR(200),
    organization_id BIGINT,
    organization_name VARCHAR(200),
    role VARCHAR(50),
    success BOOLEAN NOT NULL,
    error_message TEXT,
    method_signature VARCHAR(255),
    duration_ms BIGINT,
    ip_address VARCHAR(64),
    user_agent VARCHAR(255),
    request_id VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_audit_date ON audit(event_date);
CREATE INDEX IF NOT EXISTS idx_audit_org ON audit(organization_id);
CREATE INDEX IF NOT EXISTS idx_audit_event ON audit(event_code);
