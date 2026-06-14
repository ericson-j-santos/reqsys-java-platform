# Planner Teams Status Worker

Detecta mudanças de status no Planner e gera notificações Teams governadas.

## Endpoint base

```http
/api/v1/planner
```

## Padrões obrigatórios

- Header `X-Correlation-Id`.
- Header `Idempotency-Key` em comandos.
- Validação com Bean Validation.
- Auditoria por evento.
- Outbox para efeitos externos.
- Logs com PII mascarada.
- Testes unitários, integração e contrato.
