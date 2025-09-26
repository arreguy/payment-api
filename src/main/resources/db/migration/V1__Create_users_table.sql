-- V1__Create_users_table.sql
-- Initial users table creation

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome_completo VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    user_type VARCHAR(50) NOT NULL CHECK (user_type IN ('COMMON', 'MERCHANT')),
    wallet_balance INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_cpf ON users(cpf);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_user_type ON users(user_type);

COMMENT ON TABLE users IS 'User accounts for the payment system';
COMMENT ON COLUMN users.wallet_balance IS 'Balance in cents';
COMMENT ON COLUMN users.user_type IS 'Type of user: COMMON (individual) or MERCHANT (business)';