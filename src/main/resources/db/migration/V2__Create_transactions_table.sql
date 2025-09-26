-- V2__Create_transactions_table.sql
-- Transaction records table

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payer_id UUID NOT NULL REFERENCES users(id),
    payee_id UUID NOT NULL REFERENCES users(id),
    amount INTEGER NOT NULL CHECK (amount > 0),
    transaction_status VARCHAR(50) NOT NULL CHECK (transaction_status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    external_authorization_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_payer_id ON transactions(payer_id);
CREATE INDEX idx_transactions_payee_id ON transactions(payee_id);
CREATE INDEX idx_transactions_status ON transactions(transaction_status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

COMMENT ON TABLE transactions IS 'Transaction records for money transfers';
COMMENT ON COLUMN transactions.amount IS 'Transaction amount in cents';
COMMENT ON COLUMN transactions.external_authorization_id IS 'Reference to external authorization service response';