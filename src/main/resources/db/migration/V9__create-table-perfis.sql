CREATE TABLE perfis(
                       id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       nome VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE usuarios_perfis(
                                usuario_id BIGINT NOT NULL,
                                perfil_id BIGINT NOT NULL,

                                PRIMARY KEY(usuario_id, perfil_id),
                                CONSTRAINT USUARIOS_PERFIS_FK_USUARIO FOREIGN KEY(usuario_id) REFERENCES usuarios(id),
                                CONSTRAINT USUARIOS_PERFIS_FK_PERFIL FOREIGN KEY(perfil_id) REFERENCES perfis(id)

);

INSERT INTO perfis(nome) VALUES('ESTUDANTE');
INSERT INTO perfis(nome) VALUES('INSTRUTOR');
INSERT INTO perfis(nome) VALUES('MODERADOR');
INSERT INTO perfis(nome) VALUES('ADMIN');