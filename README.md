# ReqSys Java Enterprise Platform

Monorepo Java/Spring Boot 3 para consolidar as aplicações ReqSys em uma base corporativa com arquitetura hexagonal, governança, rastreabilidade, idempotência, outbox, observabilidade e gates de produção.

## Padrões aplicados

- Arquitetura hexagonal / Ports and Adapters.
- Use cases explícitos.
- DTOs separados do domínio.
- `X-Correlation-Id` obrigatório.
- `Idempotency-Key` com persistência, hash e replay de resposta.
- Auditoria persistente.
- Outbox + DLQ lógica.
- Redmine assíncrono via worker de outbox.
- OAuth2 Resource Server com validação JWT.
- CORS restrito por ambiente.
- Cofre fechado por padrão e sem exposição de segredo em claro.
- Métricas Micrometer para idempotência e outbox.
- CI/CD com Maven verify, OWASP Dependency-Check, SBOM, Trivy e CodeQL.

## Decisão de release

A branch somente deve sair de draft e ser mergeada quando:

1. `ci` estiver verde;
2. `security-scan` estiver verde;
3. os relatórios de OWASP Dependency-Check, Trivy e CodeQL forem revisados;
4. não houver secret, token, CPF, PII ou connection string em logs/artefatos.

Enquanto qualquer item acima não estiver validado, o merge em `main` fica bloqueado por decisão técnica.

## Gates canônicos

Os gates completos estão documentados em `docs/producao-gates.md`.

## CI/CD deste PR

Os workflows deste PR validam `apps/reqsys-enterprise-api` e suas dependências Maven com:

```bash
mvn -B -pl apps/reqsys-enterprise-api -am clean verify
```

Esse escopo evita que módulos não alterados do monorepo bloqueiem a entrega desta fatia de produção. A validação completa de todos os módulos deve ser tratada em incremento próprio de saneamento global do monorepo.

## Execução local

```bash
cd reqsys-java-platform
docker compose -f docker/docker-compose.yml up -d
SPRING_PROFILES_ACTIVE=local mvn -pl apps/reqsys-enterprise-api -am spring-boot:run
```

Executar suíte afetada:

```bash
mvn -B -pl apps/reqsys-enterprise-api -am clean verify
```

## Produção

Configuração mínima:

```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL='jdbc:sqlserver://<host>:1433;databaseName=reqsys;encrypt=true;trustServerCertificate=false'
SQLSERVER_USERNAME='<usuario_app_sem_privilégio_sa>'
SQLSERVER_PASSWORD='<segredo>'
JWT_ISSUER_URI='https://login.microsoftonline.com/<tenant-id>/v2.0'
JWT_AUDIENCES='<api-client-id-ou-app-id-uri>'
CORS_ALLOWED_ORIGINS='https://app.reqsys.exemplo.com'
REQSYS_COFRE_TOKEN='<segredo-forte>'
REQSYS_IDEMPOTENCY_ENABLED=true
REDMINE_BASE_URL='https://redmine.exemplo.local'
REDMINE_API_KEY='<segredo>'
REDMINE_PROJECT_ID='reqsys'
REQSYS_OUTBOX_REDMINE_INTERVALO_MS=30000
REQSYS_OUTBOX_REDMINE_LIMITE_LOTE=10
REQSYS_OUTBOX_REDMINE_MAX_TENTATIVAS=5
```

## Fluxo Redmine governado

```text
POST /api/v1/backlog/publicar-redmine/{id}
  -> valida requisito
  -> status PUBLICACAO_REDMINE_PENDENTE
  -> grava tb_outbox
  -> commit local
  -> RedmineOutboxWorker
  -> cria issue Redmine
  -> atualiza status PUBLICADO_REDMINE
  -> registra auditoria
  -> em falha: retry ou DLQ
```

## Métricas principais

| Métrica | Finalidade |
|---|---|
| `reqsys.idempotencia.replay.total` | Reenvios idempotentes. |
| `reqsys.idempotencia.conflito.total` | Uso divergente de `Idempotency-Key`. |
| `reqsys.idempotencia.concluida.total` | Comandos concluídos. |
| `reqsys.idempotencia.erro.total` | Falhas 5xx em comandos idempotentes. |
| `reqsys.outbox.processada.total` | Mensagens concluídas. |
| `reqsys.outbox.falha.total` | Falhas de outbox. |
| `reqsys.outbox.idempotente.total` | Reprocessamentos sem efeito colateral. |
