ALTER TABLE respostas
    DROP COLUMN autor,
    ADD COLUMN autor_id BIGINT NOT NULL DEFAULT 1,
    ADD CONSTRAINT fk_autor_resposta FOREIGN KEY (autor_id) REFERENCES usuarios(id) ON DELETE CASCADE;