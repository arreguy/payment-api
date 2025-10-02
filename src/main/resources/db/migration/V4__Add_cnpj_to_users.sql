-- V2__Add_cnpj_to_users.sql
-- Migração pra adicionar a coluna CNPJ na tabela users pra suportar usuário tipo MERCHANT

-- Adiciona a coluna cnpj (nullable pro tipo COMMON_USER)
ALTER TABLE users ADD COLUMN cnpj VARCHAR(14) NULL;

-- Adiciona constraint pra não deixar CNPJ duplicado
ALTER TABLE users ADD CONSTRAINT uk_users_cnpj UNIQUE (cnpj);

-- Cria índice parcial pra buscar CNPJ mais rápido (só indexa valores que não são null)
CREATE INDEX idx_users_cnpj ON users(cnpj) WHERE cnpj IS NOT NULL;

-- Adiciona comentário na coluna pra documentar
COMMENT ON COLUMN users.cnpj IS 'CNPJ pra usuários do tipo MERCHANT';
