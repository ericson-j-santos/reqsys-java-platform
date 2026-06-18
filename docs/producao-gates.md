# Gates canônicos de produção — ReqSys

Este documento define os bloqueios mínimos para considerar a aplicação candidata a produção.

## Gates bloqueantes

A aplicação deve bloquear startup em `SPRING_PROFILES_ACTIVE=prod` quando qualquer condição abaixo ocorrer:

| Gate | Regra |
|---|---|
| Auth desligada | `reqsys.security.enabled=false` não é permitido em produção. |
| Idempotência desligada | `reqsys.idempotency.enabled=false` não é permitido em produção. |
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

## CI/CD obrigatório

Neste PR, os gates de CI executam sobre `apps/reqsys-enterprise-api` e dependências Maven via `-pl apps/reqsys-enterprise-api -am`, porque a fatia alterada é a API enterprise e suas bibliotecas base.

O PR deve validar:

- `mvn -B -pl apps/reqsys-enterprise-api -am clean verify`;
- OWASP Dependency-Check nos módulos afetados;
- SBOM CycloneDX nos módulos afetados;
- Trivy filesystem scan;
- Docker build;
- Trivy image scan;
- CodeQL Java com build manual dos módulos afetados.

Em caso de falha no Maven, o workflow publica o artifact `maven-verify-diagnostics` contendo `maven-verify.log`, Surefire e Failsafe reports.

## Decisão canônica de merge

O PR deve permanecer como draft até todos os workflows do último commit ficarem verdes. Se qualquer gate falhar, a correção deve ocorrer no próprio PR antes de revisão final.

Checklist de saída de draft:

```text
[ ] ci verde
[ ] security-scan verde
[ ] relatórios de segurança revisados
[ ] sem secrets/PII em logs ou artefatos
[ ] sem CORS wildcard
[ ] auth/JWT validados
[ ] cofre fechado
```

## Próximos incrementos recomendados

1. Adicionar rate limit por usuário/client application.
2. Adicionar tracing OpenTelemetry para correlação entre API, worker e Redmine.
3. Adicionar dashboard operacional mínimo para métricas de idempotência, outbox e DLQ.
4. Adicionar baseline formal para vulnerabilidades aceitas, se o OWASP Dependency-Check apontar falsos positivos.
