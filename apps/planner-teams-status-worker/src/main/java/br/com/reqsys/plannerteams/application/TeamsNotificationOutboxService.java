package br.com.reqsys.plannerteams.application;

import br.com.reqsys.plannerteams.domain.OutboxStatus;
import br.com.reqsys.plannerteams.domain.TeamsNotificationOutboxEvent;
import br.com.reqsys.plannerteams.infra.repository.TeamsNotificationOutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class TeamsNotificationOutboxService {

    private final TeamsNotificationOutboxRepository repository;

    public TeamsNotificationOutboxService(TeamsNotificationOutboxRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public boolean registrarEventoSeNaoExistir(
            String correlationId,
            String idempotencyKey,
            String taskId,
            String destinatario,
            String mensagem
    ) {
        if (repository.existsByIdempotencyKey(idempotencyKey)) {
            return false;
        }

        var evento = new TeamsNotificationOutboxEvent(
                correlationId,
                idempotencyKey,
                taskId,
                destinatario,
                OutboxStatus.PENDING.name(),
                gerarSha256(mensagem)
        );

        repository.save(evento);
        return true;
    }

    private String gerarSha256(String conteudo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(conteudo.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Falha ao gerar SHA-256", ex);
        }
    }
}
