ALTER TABLE enderecos
    ADD COLUMN atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX idx_enderecos_usuario ON enderecos(usuario_id);
