# ReqSys Java Enterprise Platform

Monorepo Java/Spring Boot 3 para consolidar as aplicações ReqSys em uma base corporativa com arquitetura hexagonal, governança, rastreabilidade e gates de produção.

## Aplicações incluídas

| Módulo | Objetivo |
|---|---|
| `reqsys-enterprise-api` | Extrair, refinar e publicar requisitos com BDD, rastreabilidade e histórico |
| `forms-planner-orchestrator` | Orquestrar Forms → Power Automate → Planner → Teams com idempotência |
| `planner-teams-status-worker` | Detectar mudança de status no Planner e enviar mensagem governada no Teams |
| `cadastra-pf-siopi-api` | Recriação Java do fluxo cadastra_pf_siopi em arquitetura hexagonal |
| `report-services-api` | Expor relatórios/SSRS, logs, assinaturas e reenvio controlado |
| `redmine-copilot-sync` | Sincronizar Redmine, Wiki e contexto para Copilot Studio |

## Bibliotecas compartilhadas

| Módulo | Objetivo |
|---|---|
| `reqsys-common` | Erros padronizados, exceções, LGPD, idempotência e utilitários |
| `reqsys-observability` | `X-Correlation-Id`, MDC e autoconfiguração de observabilidade |
| `reqsys-security` | Headers de segurança, CORS restrito, OAuth2 Resource Server e validação JWT |
| `reqsys-microsoft-graph` | Ports/adapters para Graph, Planner e Teams |

## Stack

- Java 21 LTS
- Spring Boot 3.x
- Maven multi-module
- SQL Server
- Flyway
- Spring Web / Validation / Security / OAuth2 Resource Server / Data JPA / Retry / Actuator
- JUnit 5 / Mockito-ready / Testcontainers-ready
- Docker multi-stage
- GitHub Actions

## Padrões aplicados

- Arquitetura hexagonal / Ports and Adapters
- Use cases explícitos
- DTOs separados do domínio
- `X-Correlation-Id`
- `Idempotency-Key`
- Auditoria persistente
- Outbox + DLQ lógica
- Retry com backoff
- Mascaramento de PII/LGPD
- OpenAPI e ADRs
- Testes-base
- Gates de produção documentados em `docs/producao-gates.md`

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

## Variáveis de ambiente para produção

```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL='jdbc:sqlserver://<host>:1433;databaseName=reqsys;encrypt=true;trustServerCertificate=false'
SQLSERVER_USERNAME='<usuario_app_sem_privilégio_sa>'
SQLSERVER_PASSWORD='<segredo>'
JWT_ISSUER_URI='https://login.microsoftonline.com/<tenant-id>/v2.0'
JWT_AUDIENCES='<api-client-id-ou-app-id-uri>'
CORS_ALLOWED_ORIGINS='https://app.reqsys.exemplo.com'
REQSYS_COFRE_TOKEN='<segredo-forte>'
REDMINE_BASE_URL='https://redmine.exemplo.local'
REDMINE_API_KEY='<segredo>'
REDMINE_PROJECT_ID='reqsys'
```

## Convenção HTTP

```http
Authorization: Bearer <jwt>
X-Correlation-Id: <uuid-ou-id-rastreavel>
Idempotency-Key: <chave-unica-por-operacao>
```

## Gates bloqueantes

Em `prod`, a aplicação bloqueia startup se detectar:

- autenticação desligada;
- JWT sem issuer;
- JWT sem audience;
- CORS com `*`;
- cofre sem token;
- SQL Server com `trustServerCertificate=true`;
- usuário SQL `sa`;
- senha default ou vazia;
- integração Redmine sem API key.

## Observação

Este pacote é uma base técnica pronta para evolução controlada. Antes de produção real, ainda é recomendado fechar idempotência persistente com cache de resposta, mover integrações externas críticas para outbox worker e adicionar testes automatizados específicos de segurança.
