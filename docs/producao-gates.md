# Gates canônicos de produção — ReqSys

Este documento define os bloqueios mínimos para considerar a aplicação candidata a produção.

## Gates bloqueantes

A aplicação deve bloquear startup em `SPRING_PROFILES_ACTIVE=prod` quando qualquer condição abaixo ocorrer:

| Gate | Regra |
|---|---|
| Auth desligada | `reqsys.security.enabled=false` não é permitido em produção. |
| JWT sem issuer | `JWT_ISSUER_URI` deve estar configurado. |
| JWT sem audience | `JWT_AUDIENCES` deve estar configurado. |
| CORS aberto | `CORS_ALLOWED_ORIGINS=*` é proibido. |
| Cofre sem token | `REQSYS_COFRE_TOKEN` é obrigatório. |
| TLS SQL relaxado | `trustServerCertificate=true` é proibido. |
| Usuário SQL privilegiado | `SQLSERVER_USERNAME=sa` é proibido. |
| Senha default | Senha vazia ou default é proibida. |
| Redmine sem chave | `REDMINE_API_KEY` é obrigatório quando a integração estiver ativa. |

## Headers obrigatórios

| Header | Obrigatório em | Finalidade |
|---|---|---|
| `Authorization: Bearer <jwt>` | Endpoints protegidos | Autenticação e autorização por JWT. |
| `X-Correlation-Id` | Todas as rotas de negócio | Rastreabilidade ponta a ponta. |
| `Idempotency-Key` | `POST`, `PUT`, `PATCH`, `DELETE` | Deduplicação, replay seguro e proteção contra duplo envio. |
| `X-Cofre-Token` | Rotas `/api/v1/cofre/**` | Proteção adicional para administração de segredos. |

## Cofre de segredos

O cofre não deve retornar segredo em claro. As respostas devem expor apenas:

- chave;
- sistema;
- descrição;
- indicador de valor cadastrado;
- fingerprint não reversível.

## Idempotência

Comandos HTTP devem registrar `Idempotency-Key` em `dbo.tb_idempotencia` com:

- hash canônico de método, URI, query string e corpo;
- status de processamento;
- resposta JSON;
- HTTP status original.

Regras:

| Cenário | Resultado |
|---|---|
| Primeira chamada | Reserva chave como `PENDENTE`. |
| Repetição com mesmo payload e concluída | Retorna resposta persistida. |
| Repetição com payload diferente | Retorna `409 IDEMPOTENCY_KEY_CONFLITANTE`. |
| Falha 5xx | Marca registro como `ERRO`. |

## Outbox Redmine

Publicações externas no Redmine não devem ocorrer dentro da transação principal do caso de uso.

Fluxo canônico:

```text
API -> transação local -> tb_outbox(PENDENTE) -> worker -> Redmine -> status local -> auditoria
```

Regras:

- O caso de uso apenas enfileira a publicação.
- O worker processa em lote configurável.
- Falhas incrementam tentativas.
- Após o limite, a mensagem vai para DLQ.
- Reprocessamento idempotente ignora requisito já publicado.

## Próximos gates recomendados

1. Adicionar rate limit por usuário/client application.
2. Adicionar testes de segurança para JWT, CORS, cofre e startup em produção.
3. Adicionar CodeQL e OWASP Dependency-Check com baseline controlado.
4. Adicionar métricas Micrometer para outbox, DLQ, retries e idempotência.
5. Adicionar tracing OpenTelemetry para correlação entre API, worker e Redmine.
