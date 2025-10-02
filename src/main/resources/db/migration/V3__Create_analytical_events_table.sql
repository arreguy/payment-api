-- V3__Create_analytical_events_table.sql
-- Tabela de eventos analíticos

CREATE TABLE analytical_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_type VARCHAR(100) NOT NULL,
    user_id UUID REFERENCES users(id),
    transaction_id UUID REFERENCES transactions(id),
    event_data JSONB,
    correlation_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_analytical_events_type ON analytical_events(event_type);
CREATE INDEX idx_analytical_events_user_id ON analytical_events(user_id);
CREATE INDEX idx_analytical_events_transaction_id ON analytical_events(transaction_id);
CREATE INDEX idx_analytical_events_correlation_id ON analytical_events(correlation_id);
CREATE INDEX idx_analytical_events_created_at ON analytical_events(created_at);

CREATE INDEX idx_analytical_events_data ON analytical_events USING GIN (event_data);

COMMENT ON TABLE analytical_events IS 'Eventos pra análise e monitoramento';
COMMENT ON COLUMN analytical_events.event_data IS 'Dados em JSON com informações específicas do evento';