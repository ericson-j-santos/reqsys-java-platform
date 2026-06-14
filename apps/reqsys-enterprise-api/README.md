# ReqSys Enterprise API

Refina requisitos brutos em requisitos atômicos, BDD e rastreáveis.

## Endpoint base

```http
/api/v1/requisitos
```

## Padrões obrigatórios

- Header `X-Correlation-Id`.
- Header `Idempotency-Key` em comandos.
- Validação com Bean Validation.
- Auditoria por evento.
- Outbox para efeitos externos.
- Logs com PII mascarada.
- Testes unitários, integração e contrato.
