# Forms Planner Orchestrator

Recebe solicitações de Forms/Power Automate e orquestra Planner/Teams.

## Endpoint base

```http
/api/v1/solicitacoes
```

## Padrões obrigatórios

- Header `X-Correlation-Id`.
- Header `Idempotency-Key` em comandos.
- Validação com Bean Validation.
- Auditoria por evento.
- Outbox para efeitos externos.
- Logs com PII mascarada.
- Testes unitários, integração e contrato.
