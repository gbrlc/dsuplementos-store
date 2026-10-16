-- Outbox transacional: o pedido e sua notificacao sao gravados juntos.
-- O payload e um snapshot imutavel para que retries usem a mesma chave de idempotencia.
CREATE TABLE notificacoes_pedido (
    pedido_id BIGINT PRIMARY KEY REFERENCES pedidos(id),
    payload TEXT NOT NULL,
    tentativas INTEGER NOT NULL DEFAULT 0,
    proxima_tentativa TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    publicado_em TIMESTAMP WITH TIME ZONE,
    enviado_em TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_notificacoes_pendentes ON notificacoes_pedido(proxima_tentativa)
    WHERE enviado_em IS NULL AND tentativas < 8;
