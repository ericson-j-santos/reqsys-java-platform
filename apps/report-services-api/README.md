# Report Services API

Expõe relatórios, logs SSRS, assinaturas e reenvio controlado.

## Endpoint base

```http
/api/v1/relatorios
```

## Padrões obrigatórios

- Header `X-Correlation-Id`.
- Header `Idempotency-Key` em comandos.
- Validação com Bean Validation.
- Auditoria por evento.
- Outbox para efeitos externos.
- Logs com PII mascarada.
- Testes unitários, integração e contrato.
