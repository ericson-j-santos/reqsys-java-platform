# ADR-001 — Arquitetura Hexagonal

## Status

Aceito.

## Decisão

Todas as aplicações devem separar regra de negócio, casos de uso e integrações externas por meio de ports/adapters.

## Consequências

- Testabilidade maior.
- Integrações substituíveis.
- Menor acoplamento com frameworks e vendors.
