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

## Cofre de segredos

O cofre não deve retornar segredo em claro. As respostas devem expor apenas chave, sistema, descrição, indicador de valor cadastrado e fingerprint não reversível.

## Idempotência

Comandos HTTP devem registrar `Idempotency-Key` em `dbo.tb_idempotencia` com hash canônico de método, URI, query string e corpo, status de processamento, resposta JSON e HTTP status original.

| Cenário | Resultado |
|---|---|
| Primeira chamada | Reserva chave como `PENDENTE`. |
| Repetição com mesmo payload e concluída | Retorna resposta persistida. |
| Repetição com payload diferente | Retorna `409 IDEMPOTENCY_KEY_CONFLITANTE`. |
| Falha 5xx | Marca registro como `ERRO`. |

## Outbox Redmine

Publicações externas no Redmine não devem ocorrer dentro da transação principal do caso de uso.

```text
API -> transação local -> tb_outbox(PENDENTE) -> worker -> Redmine -> status local -> auditoria
```

Regras:

- O caso de uso apenas enfileira a publicação.
- O worker processa em lote configurável.
- Falhas incrementam tentativas.
- Após o limite, a mensagem vai para DLQ.
- Reprocessamento idempotente ignora requisito já publicado.

## Observabilidade mínima

| Métrica | Uso operacional |
|---|---|
| `reqsys.idempotencia.replay.total` | Detectar volume de reenvios idempotentes. |
| `reqsys.idempotencia.conflito.total` | Detectar mau uso de `Idempotency-Key`. |
| `reqsys.idempotencia.concluida.total` | Confirmar comandos processados com persistência de resposta. |
| `reqsys.idempotencia.erro.total` | Acompanhar falhas 5xx em comandos idempotentes. |
| `reqsys.outbox.processada.total` | Acompanhar publicação externa concluída. |
| `reqsys.outbox.falha.total` | Acompanhar falhas de worker/outbox. |
| `reqsys.outbox.idempotente.total` | Acompanhar reprocessamentos sem efeito colateral. |

## Testes obrigatórios

A suíte deve conter testes para startup gate em produção, auth desligada, idempotência desligada, JWT issuer/audience ausentes, CORS wildcard, cofre sem token, token inválido, resposta do cofre sem segredo em claro, headers obrigatórios e outbox Redmine com sucesso/falha.

## CI/CD obrigatório

O PR deve validar:

- `mvn clean verify`;
- OWASP Dependency-Check;
- SBOM CycloneDX;
- Trivy filesystem scan;
- Docker build;
- Trivy image scan;
- CodeQL Java.

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
