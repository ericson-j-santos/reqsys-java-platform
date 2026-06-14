# Regras — cadastra_pf_siopi

- Validar CPF antes da orquestração.
- Não iniciar demanda reservada por outro operador.
- Validar duplicidade do dia.
- Não registrar PII aberta em logs.
- Não permitir bypass TLS.
- Registrar status por `correlation_id`.
- Retry limitado e rastreável.
