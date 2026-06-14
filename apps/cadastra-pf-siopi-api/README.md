# Cadastra PF SIOPI API

Valida e orquestra cadastro de Pessoa Física para SIOPI.

## Endpoint base

```http
/api/v1/cadastros-pf
```

## Padrões obrigatórios

- Header `X-Correlation-Id`.
- Header `Idempotency-Key` em comandos.
- Validação com Bean Validation.
- Auditoria por evento.
- Outbox para efeitos externos.
- Logs com PII mascarada.
- Testes unitários, integração e contrato.
