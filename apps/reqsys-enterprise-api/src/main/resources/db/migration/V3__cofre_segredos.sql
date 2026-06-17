-- ReqSys Enterprise — Cofre de Segredos Centralizado
-- Armazena credenciais e chaves consumidas pelos sistemas da plataforma.
-- Acesso via endpoint REST protegido por X-Cofre-Token.

IF OBJECT_ID('dbo.tb_cofre_segredo', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_cofre_segredo (
        id              UNIQUEIDENTIFIER  NOT NULL PRIMARY KEY DEFAULT NEWSEQUENTIALID(),
        chave           VARCHAR(200)      NOT NULL UNIQUE,
        valor           NVARCHAR(2000)    NOT NULL,
        sistema         VARCHAR(100)      NOT NULL,
        descricao       NVARCHAR(500)     NULL,
        ativo           BIT               NOT NULL DEFAULT 1,
        criado_em_utc   DATETIME2         NOT NULL DEFAULT SYSUTCDATETIME(),
        atualizado_em_utc DATETIME2       NULL
    );

    CREATE INDEX ix_cofre_chave ON dbo.tb_cofre_segredo(chave) WHERE ativo = 1;
END
GO

-- Seed inicial — chaves cadastradas via POST /api/v1/cofre/segredo ou seeder local
-- Valores reais devem ser injetados via variável de ambiente ou API após o deploy.
IF NOT EXISTS (SELECT 1 FROM dbo.tb_cofre_segredo WHERE chave = 'GOVBI_GEMINI_API_KEY')
    INSERT INTO dbo.tb_cofre_segredo (chave, valor, sistema, descricao)
    VALUES (
        'GOVBI_GEMINI_API_KEY',
        '',
        'govbi-ia',
        'Google Gemini API Key — obter em https://aistudio.google.com'
    );
GO
