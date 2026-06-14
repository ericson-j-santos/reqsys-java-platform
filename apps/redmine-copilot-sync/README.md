# Redmine Copilot Sync

Sincroniza requisitos, issues, wiki e contexto para Copilot Studio.

## Endpoint base

```http
/api/v1/redmine
```

## Padrões obrigatórios

- Header `X-Correlation-Id`.
- Header `Idempotency-Key` em comandos.
- Validação com Bean Validation.
- Auditoria por evento.
- Outbox para efeitos externos.
- Logs com PII mascarada.
- Testes unitários, integração e contrato.
