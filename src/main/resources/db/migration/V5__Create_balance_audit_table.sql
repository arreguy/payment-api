-- Criar tabela balance_audit para trilha de auditoria de alterações de saldo da carteira
-- Esta tabela fornece uma trilha de auditoria completa para todas as alterações de saldo com rastreamento de timestamp e tipo de operação

CREATE TABLE balance_audit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    transaction_id UUID,
    previous_balance INTEGER NOT NULL,
    new_balance INTEGER NOT NULL,
    balance_change INTEGER NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',

    CONSTRAINT fk_balance_audit_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Criar índices para consultas de auditoria eficientes
CREATE INDEX idx_balance_audit_user_id ON balance_audit(user_id);
CREATE INDEX idx_balance_audit_transaction_id ON balance_audit(transaction_id);
CREATE INDEX idx_balance_audit_created_at ON balance_audit(created_at);

-- Adicionar comentários à tabela
COMMENT ON TABLE balance_audit IS 'Trilha de auditoria para todas as alterações de saldo da carteira';
COMMENT ON COLUMN balance_audit.user_id IS 'UUID do usuário cujo saldo foi alterado';
COMMENT ON COLUMN balance_audit.transaction_id IS 'UUID da transação associada (nulo para ajustes)';
COMMENT ON COLUMN balance_audit.previous_balance IS 'Saldo antes da alteração em centavos';
COMMENT ON COLUMN balance_audit.new_balance IS 'Saldo após a alteração em centavos';
COMMENT ON COLUMN balance_audit.balance_change IS 'Delta da alteração de saldo em centavos (positivo para créditos, negativo para débitos)';
COMMENT ON COLUMN balance_audit.operation_type IS 'Tipo de operação (TRANSFER_DEBIT, TRANSFER_CREDIT, ADJUSTMENT)';
COMMENT ON COLUMN balance_audit.created_at IS 'Timestamp de quando a alteração de saldo ocorreu';
COMMENT ON COLUMN balance_audit.created_by IS 'Identificador de quem criou o registro (padrão: system)';
