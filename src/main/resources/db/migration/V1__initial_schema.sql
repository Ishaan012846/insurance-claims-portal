-- Flyway Migration V1: Initial Database Schema for Insurance Claims Processing Portal

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'HANDLER', 'MANAGER')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Policies table
CREATE TABLE policies (
    id BIGSERIAL PRIMARY KEY,
    policy_number VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('HEALTH', 'MOTOR', 'LIFE', 'PROPERTY')),
    coverage_amount NUMERIC(15,2) NOT NULL CHECK (coverage_amount >= 0),
    premium NUMERIC(15,2) NOT NULL CHECK (premium >= 0),
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL CHECK (valid_to >= valid_from),
    holder_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Claims table
CREATE TABLE claims (
    id BIGSERIAL PRIMARY KEY,
    claim_number VARCHAR(50) NOT NULL UNIQUE,
    incident_date DATE NOT NULL,
    description TEXT NOT NULL,
    claimed_amount NUMERIC(15,2) NOT NULL CHECK (claimed_amount >= 0),
    approved_amount NUMERIC(15,2) CHECK (approved_amount IS NULL OR approved_amount >= 0),
    status VARCHAR(30) NOT NULL CHECK (status IN ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'INFO_REQUESTED', 'APPROVED', 'REJECTED', 'SETTLED')),
    policy_id BIGINT NOT NULL REFERENCES policies(id) ON DELETE RESTRICT,
    assigned_handler_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE
);

-- Claim Documents table
CREATE TABLE claim_documents (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes > 0),
    storage_path VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    claim_id BIGINT NOT NULL REFERENCES claims(id) ON DELETE CASCADE
);

-- Claim Audit Logs table
CREATE TABLE claim_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    actor_id BIGINT NOT NULL REFERENCES users(id),
    remarks TEXT,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Performance Indexes
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_policies_holder ON policies(holder_id);
CREATE INDEX idx_claims_policy ON claims(policy_id);
CREATE INDEX idx_claims_assigned_handler ON claims(assigned_handler_id);
CREATE INDEX idx_claims_status ON claims(status);
CREATE INDEX idx_claims_status_submitted ON claims(status, submitted_at);
CREATE INDEX idx_claim_documents_claim ON claim_documents(claim_id);
CREATE INDEX idx_claim_audit_logs_claim ON claim_audit_logs(claim_id, timestamp DESC);
