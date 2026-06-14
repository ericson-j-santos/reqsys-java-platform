# ReqSys Java Enterprise Platform

Monorepo Java/Spring Boot 3 para consolidar as últimas aplicações recentes em uma base corporativa **padrão ouro**.

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
| `reqsys-security` | Headers de segurança e configuração base Spring Security |
| `reqsys-microsoft-graph` | Ports/adapters para Graph, Planner e Teams |

## Stack

- Java 21 LTS
- Spring Boot 3.x
- Maven multi-module
- SQL Server
- Flyway
- Spring Web / Validation / Security / Data JPA / Retry / Actuator
- JUnit 5 / Mockito-ready / Testcontainers-ready
- Docker Compose
- GitHub Actions

## Padrões aplicados

- Arquitetura hexagonal / Ports and Adapters
- Use cases explícitos
- DTOs separados do domínio
- `X-Correlation-Id`
- `Idempotency-Key`
- Auditoria
- Outbox + DLQ lógica
- Retry com backoff
- Mascaramento de PII/LGPD
- OpenAPI e ADRs
- Testes-base

## Execução local

```bash
cd reqsys-java-platform
docker compose -f docker/docker-compose.yml up -d
mvn clean verify
```

Executar módulo específico:

```bash
mvn -pl apps/forms-planner-orchestrator -am spring-boot:run
```

## Variáveis de ambiente

```bash
SPRING_PROFILES_ACTIVE=local
SQLSERVER_HOST=localhost
SQLSERVER_PORT=1433
SQLSERVER_DATABASE=reqsys
SQLSERVER_USERNAME=sa
SQLSERVER_PASSWORD='YourStrong!Passw0rd'
GRAPH_TENANT_ID=00000000-0000-0000-0000-000000000000
GRAPH_CLIENT_ID=00000000-0000-0000-0000-000000000000
GRAPH_CLIENT_SECRET=change-me
REDMINE_BASE_URL=https://redmine.exemplo.local
REDMINE_API_KEY=change-me
```

## Convenção HTTP

```http
X-Correlation-Id: <uuid-ou-id-rastreavel>
Idempotency-Key: <chave-unica-por-operacao>
```

## Observação

Este pacote é uma base técnica pronta para evolução. Os adapters de Microsoft Graph, Redmine e SSRS estão isolados por ports e mocks, para posterior substituição pelos clients reais do ambiente corporativo.
