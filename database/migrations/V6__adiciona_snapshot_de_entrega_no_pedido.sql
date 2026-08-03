ALTER TABLE pedidos
    ADD COLUMN endereco_cep VARCHAR(9),
    ADD COLUMN endereco_logradouro VARCHAR(160),
    ADD COLUMN endereco_numero VARCHAR(20),
    ADD COLUMN endereco_complemento VARCHAR(100),
    ADD COLUMN endereco_bairro VARCHAR(100),
    ADD COLUMN endereco_cidade VARCHAR(100),
    ADD COLUMN endereco_estado CHAR(2);

ALTER TABLE pedidos DROP CONSTRAINT IF EXISTS pedidos_endereco_id_fkey;

ALTER TABLE pedidos
    ADD CONSTRAINT pedidos_endereco_id_fkey
    FOREIGN KEY (endereco_id) REFERENCES enderecos(id) ON DELETE SET NULL;

CREATE INDEX idx_pedidos_status ON pedidos(status);
