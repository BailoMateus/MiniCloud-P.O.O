-------------------------------------------------
-- Tabela de planos
-------------------------------------------------
CREATE TABLE planos (
                        id_plano         SERIAL PRIMARY KEY,
                        nome             VARCHAR(50)  NOT NULL UNIQUE,
                        limite_credito   NUMERIC(10,2) NOT NULL,
                        limite_recursos  INTEGER      NOT NULL CHECK (limite_recursos >= 0)
);

-------------------------------------------------
-- Tabela de usuários
-------------------------------------------------
CREATE TABLE usuarios (
                          id_usuario   SERIAL PRIMARY KEY,
                          nome         VARCHAR(100) NOT NULL,
                          email        VARCHAR(100) NOT NULL UNIQUE,
                          plano_id     INTEGER      NOT NULL,
                          data_criacao TIMESTAMP    NOT NULL DEFAULT NOW(),

                          CONSTRAINT fk_usuarios_plano
                              FOREIGN KEY (plano_id)
                                  REFERENCES planos (id_plano)
                                  ON UPDATE CASCADE
                                  ON DELETE RESTRICT
);

-------------------------------------------------
-- Tabela genérica de recursos
-------------------------------------------------
CREATE TABLE recursos (
                          id_recurso       SERIAL PRIMARY KEY,
                          usuario_id       INTEGER      NOT NULL,
                          nome             VARCHAR(100) NOT NULL,
                          tipo_recurso     VARCHAR(50)  NOT NULL,
                          ativo            BOOLEAN      NOT NULL DEFAULT FALSE,
                          custo_base_hora  NUMERIC(10,2) NOT NULL DEFAULT 0,
                          data_criacao     TIMESTAMP    NOT NULL DEFAULT NOW(),

                          CONSTRAINT fk_recursos_usuario
                              FOREIGN KEY (usuario_id)
                                  REFERENCES usuarios (id_usuario)
                                  ON UPDATE CASCADE
                                  ON DELETE CASCADE,

                          CONSTRAINT chk_tipo_recurso
                              CHECK (tipo_recurso IN ('COMPUTACAO', 'BANCO_DADOS', 'STORAGE'))
);

-------------------------------------------------
-- Tabela de instâncias de computação
-------------------------------------------------
CREATE TABLE instancias_computacao (
                                       id_instancia  SERIAL PRIMARY KEY,
                                       recurso_id    INTEGER NOT NULL UNIQUE,
                                       vcpus         INTEGER NOT NULL CHECK (vcpus > 0),
                                       memoria_gb    INTEGER NOT NULL CHECK (memoria_gb > 0),

                                       CONSTRAINT fk_instancias_recurso
                                           FOREIGN KEY (recurso_id)
                                               REFERENCES recursos (id_recurso)
                                               ON UPDATE CASCADE
                                               ON DELETE CASCADE
);

-------------------------------------------------
-- Tabela de bancos de dados gerenciados
-------------------------------------------------
CREATE TABLE bancos_dados_gerenciados (
                                          id_banco          SERIAL PRIMARY KEY,
                                          recurso_id        INTEGER NOT NULL UNIQUE,
                                          armazenamento_gb  INTEGER NOT NULL CHECK (armazenamento_gb >= 0),
                                          replicacao_ativa  BOOLEAN NOT NULL DEFAULT FALSE,

                                          CONSTRAINT fk_bancos_recurso
                                              FOREIGN KEY (recurso_id)
                                                  REFERENCES recursos (id_recurso)
                                                  ON UPDATE CASCADE
                                                  ON DELETE CASCADE
);

-------------------------------------------------
-- Tabela de buckets de storage
-------------------------------------------------
CREATE TABLE buckets_storage (
                                 id_bucket        SERIAL PRIMARY KEY,
                                 recurso_id       INTEGER NOT NULL UNIQUE,
                                 armazenamento_gb INTEGER NOT NULL CHECK (armazenamento_gb >= 0),
                                 requisicoes_mes  INTEGER NOT NULL CHECK (requisicoes_mes >= 0),

                                 CONSTRAINT fk_buckets_recurso
                                     FOREIGN KEY (recurso_id)
                                         REFERENCES recursos (id_recurso)
                                         ON UPDATE CASCADE
                                         ON DELETE CASCADE
);
