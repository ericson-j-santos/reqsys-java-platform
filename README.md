# ReqSys Java Enterprise Platform

Monorepo Java/Spring Boot 3 para consolidar as aplicações ReqSys em uma base corporativa com arquitetura hexagonal, governança, rastreabilidade, idempotência, outbox, observabilidade e gates de produção.

## Decisão de release

A branch somente deve sair de draft e ser mergeada quando:

1. `ci` estiver verde;
2. `security-scan` estiver verde;
3. os relatórios de OWASP Dependency-Check, Trivy e CodeQL forem revisados;
4. não houver secret, token, CPF, PII ou connection string em logs/artefatos.

Enquanto qualquer item acima não estiver validado, o merge em `main` fica bloqueado por decisão técnica.

## Gates canônicos

Os gates completos estão documentados em `docs/producao-gates.md`.

## Execução local

```bash
cd reqsys-java-platform
docker compose -f docker/docker-compose.yml up -d
SPRING_PROFILES_ACTIVE=local mvn -pl apps/reqsys-enterprise-api -am spring-boot:run
```

Executar suíte:

```bash
mvn clean verify
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
