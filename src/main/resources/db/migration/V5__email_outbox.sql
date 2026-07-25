CREATE TABLE email_outbox (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient VARCHAR(255) NOT NULL,
    email_type VARCHAR(255) NOT NULL,
    payload JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INT,
    last_error TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);