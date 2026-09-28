CREATE TABLE IF NOT EXISTS banking_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    identity_number VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(30),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_banking_users_deleted_at
    ON banking_users(deleted_at);
