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
| `Idempotency-Key` | `POST`, `PUT`, `PATCH`, `DELETE` | Deduplicação e segurança operacional. |
| `X-Cofre-Token` | Rotas `/api/v1/cofre/**` | Proteção adicional para administração de segredos. |

## Cofre de segredos

O cofre não deve retornar segredo em claro. As respostas devem expor apenas:

- chave;
- sistema;
- descrição;
- indicador de valor cadastrado;
- fingerprint não reversível.

## Próximos gates recomendados

1. Persistir idempotência com cache de resposta por `Idempotency-Key`.
2. Mover publicação Redmine para outbox transacional.
3. Adicionar rate limit por usuário/client application.
4. Adicionar testes de segurança para JWT, CORS, cofre e headers obrigatórios.
5. Adicionar CodeQL e OWASP Dependency-Check com baseline controlado.
