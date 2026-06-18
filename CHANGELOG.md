# Changelog

## [1.0.2-outbox-idempotency] - 2026-06-17

### Adicionado

- Request wrapper para leitura segura do corpo HTTP em comandos idempotentes.
- Idempotência persistente em `dbo.tb_idempotencia` com hash de método, URI, query string e body.
- Replay de resposta para chamadas repetidas com mesma `Idempotency-Key` e mesmo payload.
- Rejeição `409 IDEMPOTENCY_KEY_CONFLITANTE` para reutilização da chave com payload diferente.
- Campo `http_status` na idempotência para preservar status original no replay.
- Domínio `OutboxMensagem` e porta `OutboxPort`.
- Adapter JDBC `OutboxDbAdapter`.
- Payload `RedmineOutboxPayload`.
- Worker `RedmineOutboxWorker` com lote, retry, max tentativas e DLQ.
- Testes unitários para enfileiramento Redmine e processamento do worker.

### Alterado

- `PublicarRedmineUseCase` não chama mais Redmine diretamente dentro da transação principal.
- Publicação Redmine passa a ser enfileirada em `tb_outbox`.
- Status do requisito passa para `PUBLICACAO_REDMINE_PENDENTE` até o worker concluir a publicação.
- `docs/producao-gates.md` atualizado com regras de idempotência e outbox.

### Pendências controladas

- Testes automatizados específicos de JWT, CORS, cofre e production gates.
- Métricas Micrometer para idempotência, outbox, retries e DLQ.
- Tracing OpenTelemetry ponta a ponta.

## [1.0.1-prod-readiness-gates] - 2026-06-17

### Adicionado

- Gates de produção via `ProductionReadinessValidator`.
- OAuth2 Resource Server com validação de JWT issuer e audience.
- CORS restrito por configuração, com bloqueio de wildcard em produção.
- Filtro obrigatório para `X-Correlation-Id` e `Idempotency-Key` em comandos.
- Auditoria persistente em `tb_evento_auditoria`.
- Migration de governança para auditoria, idempotência, outbox e DLQ.
- Dockerfile multi-stage com usuário não-root e healthcheck.
- Pipeline GitHub Actions com Maven verify, SBOM, Trivy filesystem e Trivy image.
- Documento `docs/producao-gates.md`.

### Alterado

- `application.yml` não possui mais usuário/senha default sensíveis.
- `application.yml` remove `trustServerCertificate=true` como default.
- `management.endpoints.web.exposure` reduzido para `health,info`.
- `reqsys-security` passa a depender de `spring-boot-starter-oauth2-resource-server`.
- Profile `local` isolado para desenvolvimento sem afetar produção.

### Corrigido

- Cofre não fica mais aberto quando `REQSYS_COFRE_TOKEN` está vazio.
- Cofre não retorna mais valor de segredo em claro.
- Auth `permitAll` deixou de ser o comportamento padrão de produção.

## [1.0.0-java-padrao-ouro] - 2026-06-12

### Adicionado

- Monorepo Maven multi-module.
- Seis aplicações Spring Boot.
- Quatro bibliotecas compartilhadas.
- Scripts SQL de governança: auditoria, idempotência, outbox e DLQ.
- Docker Compose para SQL Server.
- GitHub Actions para build, testes, security scan e release.
- ADRs iniciais e documentação de arquitetura.
- OpenAPI base.
- Testes unitários de referência.
