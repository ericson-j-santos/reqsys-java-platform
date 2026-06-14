# Arquitetura

```mermaid
flowchart LR
    Forms[Microsoft Forms] --> PA[Power Automate]
    PA --> Orchestrator[forms-planner-orchestrator]
    Orchestrator --> DB[(SQL Server)]
    Orchestrator --> Planner[Microsoft Planner]
    Planner --> Worker[planner-teams-status-worker]
    Worker --> Teams[Microsoft Teams]
    ReqSys[reqsys-enterprise-api] --> Redmine[Redmine]
    ReqSys --> Copilot[Copilot Studio]
    Reports[report-services-api] --> SSRS[SSRS]
```

## Camadas

```text
web -> application -> domain -> ports -> infrastructure
```

- `web`: controllers, DTOs e exception handlers.
- `application`: casos de uso transacionais.
- `domain`: regras puras, entidades e value objects.
- `ports`: contratos de integração.
- `infrastructure`: JPA, HTTP clients, Graph, Redmine, SSRS e SQL Server.

## Governança

- `X-Correlation-Id` por request.
- `Idempotency-Key` em comandos.
- Outbox para efeitos externos.
- Auditoria de eventos.
- DLQ lógica para falhas não recuperáveis.
- LGPD: mascaramento de CPF, e-mail e telefone.
