-- ReqSys Enterprise API — complemento de idempotencia
-- Permite replay de resposta preservando HTTP status.

IF COL_LENGTH('dbo.tb_idempotencia', 'http_status') IS NULL
BEGIN
    ALTER TABLE dbo.tb_idempotencia
        ADD http_status INT NULL;
END
GO
