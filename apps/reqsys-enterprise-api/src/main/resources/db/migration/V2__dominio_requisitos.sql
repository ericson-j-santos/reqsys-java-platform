CREATE TABLE dbo.tb_solicitacao (
    id               UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWSEQUENTIALID(),
    correlation_id   VARCHAR(80)      NOT NULL,
    texto_bruto      NVARCHAR(MAX)    NOT NULL,
    origem           VARCHAR(120)     NOT NULL,
    status           VARCHAR(40)      NOT NULL DEFAULT 'NOVA',
    criado_em_utc    DATETIME2        NOT NULL DEFAULT SYSUTCDATETIME(),
    atualizado_em_utc DATETIME2       NULL
);

CREATE TABLE dbo.tb_requisito (
    id               UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWSEQUENTIALID(),
    solicitacao_id   UNIQUEIDENTIFIER NOT NULL REFERENCES dbo.tb_solicitacao(id),
    correlation_id   VARCHAR(80)      NOT NULL,
    titulo           NVARCHAR(500)    NOT NULL,
    historia_usuario NVARCHAR(MAX)    NULL,
    criterios_bdd    NVARCHAR(MAX)    NULL,
    confianca        VARCHAR(20)      NULL,
    status           VARCHAR(40)      NOT NULL DEFAULT 'VALIDADO',
    redmine_issue_id INT              NULL,
    criado_em_utc    DATETIME2        NOT NULL DEFAULT SYSUTCDATETIME(),
    atualizado_em_utc DATETIME2       NULL
);

CREATE INDEX ix_tb_solicitacao_correlation ON dbo.tb_solicitacao(correlation_id);
CREATE INDEX ix_tb_requisito_solicitacao   ON dbo.tb_requisito(solicitacao_id);
CREATE INDEX ix_tb_requisito_status        ON dbo.tb_requisito(status, criado_em_utc);