# Planner Teams Status Worker — Microsoft Graph Teams

## Objetivo

Habilitar disparo real de mensagem no Microsoft Teams quando uma tarefa do Planner vinculada ao ReqSys sofrer alteração de status.

## Modo seguro por padrão

O worker inicia em modo `mock`/`default`, sem envio real para Teams.

Para habilitar envio real, execute com profile explícito:

```bash
SPRING_PROFILES_ACTIVE=graph java -jar planner-teams-status-worker.jar
```

## Variáveis obrigatórias

Nunca versionar secrets no repositório.

```env
REQSYS_GRAPH_TENANT_ID=<tenant-id>
REQSYS_GRAPH_CLIENT_ID=<client-id>
REQSYS_GRAPH_CLIENT_SECRET=<secret-ou-secret-manager>
REQSYS_GRAPH_BASE_URL=https://graph.microsoft.com/v1.0
REQSYS_GRAPH_SCOPE=https://graph.microsoft.com/.default
```

## Permissões Microsoft Graph recomendadas

Validar com o time Microsoft 365/Entra ID antes de produção.

Permissões candidatas para App Registration:

- `User.Read.All`
- `Chat.ReadWrite.All`
- `ChatMessage.Send`

A aplicação deve usar consentimento administrativo formal, com evidência anexada ao change request.

## Endpoint de disparo

```http
POST /api/v1/planner/sync
X-Correlation-Id: reqsys-20260618-001
Idempotency-Key: planner-task-123-PENDENTE-CONCLUIDO
Content-Type: application/json

{
  "taskId": "planner-task-123",
  "statusAnterior": "PENDENTE",
  "statusAtual": "CONCLUIDO",
  "destinatario": "usuario@empresa.com.br"
}
```

## Comportamento esperado

| Condição | Resultado |
|---|---|
| `statusAnterior == statusAtual` | Não envia Teams; retorna `SEM_ALTERACAO` |
| `statusAnterior != statusAtual` | Envia Teams; retorna `NOTIFICACAO_TEAMS_DISPARADA` |
| `X-Correlation-Id` ausente | Bloqueia requisição |
| destinatário inválido/inexistente | Bloqueia requisição |
| profile diferente de `graph` | Usa mock; não envia real |

## Gates para produção

A aplicação não deve ir para produção se qualquer condição abaixo ocorrer:

- `SPRING_PROFILES_ACTIVE=graph` sem secrets em cofre ou variável segura.
- Secret do Graph versionado em arquivo.
- Logs contendo token, client secret, CPF, PII ou payload sensível.
- Envio sem `X-Correlation-Id`.
- Envio sem chave de idempotência definida pelo orquestrador.
- Falta de consentimento administrativo Microsoft Graph.
- Falta de plano de rollback.
- Falta de teste integrado em tenant controlado.

## Próximo incremento recomendado

Implementar outbox persistente para idempotência real:

1. gravar evento `PLANNER_STATUS_CHANGED`;
2. deduplicar por `Idempotency-Key`;
3. processar envio em worker assíncrono;
4. registrar tentativas, erro e status;
5. expor painel operacional por `correlation_id`.
