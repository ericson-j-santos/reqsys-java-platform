package br.com.reqsys.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockGraphAdapter implements PlannerPort, TeamsPort {
    private static final Logger log = LoggerFactory.getLogger(MockGraphAdapter.class);

    @Override
    public String criarTarefa(String titulo, String descricao, String bucketId) {
        log.info("Mock Graph: criando tarefa Planner titulo={} bucketId={}", titulo, bucketId);
        return "mock-planner-task-id";
    }

    @Override
    public PlannerTask consultarTarefa(String taskId) {
        return new PlannerTask(taskId, "Tarefa mock", "50", "usuario@exemplo.com");
    }

    @Override
    public void enviarMensagemUsuario(String emailDestinatario, String mensagemMarkdown) {
        log.info("Mock Graph: enviando Teams para {}", emailDestinatario);
    }

    @Override
    public boolean usuarioExiste(String email) {
        return email != null && email.contains("@");
    }
}
