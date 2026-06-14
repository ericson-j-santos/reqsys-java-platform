# ADR-002 — Idempotência, Outbox e Auditoria

## Status

Aceito.

## Decisão

Todo comando com efeito externo deve registrar idempotência, evento de auditoria e, quando aplicável, outbox antes de acionar serviços externos.

## Justificativa

Power Automate, Graph, Planner, Teams, Redmine e SSRS podem apresentar retries, timeouts e duplicidade. A consistência transacional fica no SQL Server.
