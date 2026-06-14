CREATE TABLE dbo.tb_evento_auditoria (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    correlation_id VARCHAR(80) NOT NULL,
    entidade VARCHAR(120) NOT NULL,
    entidade_id VARCHAR(120) NULL,
    evento VARCHAR(120) NOT NULL,
    payload_json NVARCHAR(MAX) NULL,
    criado_em_utc DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

CREATE TABLE dbo.tb_idempotencia (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    idempotency_key VARCHAR(200) NOT NULL UNIQUE,
    hash_conteudo VARCHAR(128) NOT NULL,
    status VARCHAR(40) NOT NULL,
    resposta_json NVARCHAR(MAX) NULL,
    criado_em_utc DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

CREATE TABLE dbo.tb_outbox (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    correlation_id VARCHAR(80) NOT NULL,
    destino VARCHAR(300) NOT NULL,
    tipo_mensagem VARCHAR(80) NOT NULL,
    payload_json NVARCHAR(MAX) NOT NULL,
    status VARCHAR(40) NOT NULL,
    tentativas INT NOT NULL DEFAULT 0,
    erro NVARCHAR(MAX) NULL,
    criado_em_utc DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    atualizado_em_utc DATETIME2 NULL
);

CREATE TABLE dbo.tb_dead_letter (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    correlation_id VARCHAR(80) NOT NULL,
    origem VARCHAR(120) NOT NULL,
    payload_json NVARCHAR(MAX) NOT NULL,
    erro NVARCHAR(MAX) NOT NULL,
    criado_em_utc DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

CREATE INDEX ix_tb_evento_auditoria_correlation_id ON dbo.tb_evento_auditoria(correlation_id);
CREATE INDEX ix_tb_outbox_status ON dbo.tb_outbox(status, criado_em_utc);
