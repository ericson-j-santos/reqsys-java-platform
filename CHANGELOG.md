# Changelog

## [1.0.3-security-tests-observability-ci] - 2026-06-17

### Adicionado

- Testes de `ProductionReadinessValidator` cobrindo auth desligada, idempotência desligada, JWT ausente, CORS wildcard, cofre sem token, TLS relaxado, usuário `sa` e senha default.
- Testes de segurança do cofre validando falha fechada, token inválido e resposta sem segredo em claro.
- Testes do `IdempotencyFilter` para `X-Correlation-Id`, `Idempotency-Key` e profile local sem idempotência persistente.
- Testes do conversor JWT para authorities de scopes e roles.
- Métricas Micrometer para idempotência e outbox.
- `ReqSysMetrics` com contadores para replay, conflito, conclusão, erro, outbox processado, falha e idempotência de worker.
- CodeQL Java no workflow `security-scan`.
- OWASP Dependency-Check no workflow `ci` e `security-scan`.
- Validação documental de CI/CD obrigatório em `docs/producao-gates.md`.

### Alterado

- `reqsys.idempotency.enabled=false` passa a ser bloqueado em produção.
- Profile `local` desabilita idempotência persistente para não exigir tabelas de governança no H2 em memória.
- Workflow `ci` remove `aquasecurity/trivy-action@0.24.0`, que falhava por tag inexistente, e instala Trivy via repositório oficial.
- Workflow `build-test` deixa de rodar em PR e fica restrito a `main`/manual para evitar gates duplicados e conflitantes.
- Workflow `security-scan` substitui `dependency-review-action` por CodeQL + OWASP Dependency-Check.

### Corrigido

- Falha inicial do CI causada por action Trivy inválida.
- Risco de testes unitários tentarem descoberta remota de issuer JWT.
- Divergência documental sobre pendências já implementadas.

### Pendências controladas

- Validar execução real do novo CI após push da branch.
- Adicionar tracing OpenTelemetry ponta a ponta.
- Adicionar dashboard operacional mínimo para métricas de idempotência, outbox e DLQ.

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
