package br.com.reqsys.enterprise.application;

import br.com.reqsys.common.domain.ValidacaoNegocioException;
import br.com.reqsys.enterprise.domain.Requisito;
import br.com.reqsys.enterprise.domain.Solicitacao;
import br.com.reqsys.enterprise.domain.StatusRequisito;
import br.com.reqsys.enterprise.domain.StatusSolicitacao;
import br.com.reqsys.enterprise.ports.AuditPort;
import br.com.reqsys.enterprise.ports.RequisitoPort;
import br.com.reqsys.enterprise.ports.SolicitacaoPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidarRequisitoUseCaseTest {

    @Mock SolicitacaoPort solicitacaoPort;
    @Mock RequisitoPort requisitoPort;
    @Mock AuditPort auditPort;
    @InjectMocks ValidarRequisitoUseCase useCase;

    private Solicitacao solicitacaoComTexto(UUID id, String texto) {
        return new Solicitacao(id, "corr-1", texto, "API", StatusSolicitacao.NOVA, Instant.now(), null);
    }

    private Requisito requisitoValidado(UUID solicitacaoId) {
        return new Requisito(UUID.randomUUID(), solicitacaoId, "corr-1",
                "Primeira linha do texto", null, null, null,
                StatusRequisito.VALIDADO, null, Instant.now(), null);
    }

    @Test
    void deveValidarSolicitacaoECriarRequisito() {
        UUID solId = UUID.randomUUID();
        Solicitacao sol = solicitacaoComTexto(solId, "Primeira linha do texto\nDetalhes adicionais.");
        Requisito esperado = requisitoValidado(solId);

        when(solicitacaoPort.buscarPorId(solId)).thenReturn(Optional.of(sol));
        when(requisitoPort.salvar(any())).thenReturn(esperado);

        Requisito resultado = useCase.executar("corr-1", solId);

        assertEquals(StatusRequisito.VALIDADO, resultado.status());
        assertEquals("Primeira linha do texto", resultado.titulo());
        verify(solicitacaoPort).atualizarStatus(eq(solId), eq(StatusSolicitacao.PROCESSANDO));
        verify(auditPort).registrar(eq("corr-1"), eq("REQUISITO_VALIDADO"), any());
    }

    @Test
    void deveExtrairTituloComoPrimeiraLinhaNaoVazia() {
        UUID solId = UUID.randomUUID();
        Solicitacao sol = solicitacaoComTexto(solId, "Título do requisito\nLinha 2\nLinha 3");
        when(solicitacaoPort.buscarPorId(solId)).thenReturn(Optional.of(sol));
        // salvar devolve argumento com UUID preenchido para que auditPort.registrar não falhe em id.toString()
        when(requisitoPort.salvar(any())).thenAnswer(inv -> {
            Requisito r = inv.getArgument(0);
            return new Requisito(UUID.randomUUID(), r.solicitacaoId(), r.correlationId(),
                    r.titulo(), r.historiaUsuario(), r.criteriosBdd(), r.confianca(),
                    r.status(), r.redmineIssueId(), r.criadoEmUtc(), null);
        });

        Requisito resultado = useCase.executar("corr-1", solId);

        assertEquals("Título do requisito", resultado.titulo());
    }

    @Test
    void deveRejeitarSolicitacaoNaoEncontrada() {
        UUID solId = UUID.randomUUID();
        when(solicitacaoPort.buscarPorId(solId)).thenReturn(Optional.empty());

        ValidacaoNegocioException ex = assertThrows(ValidacaoNegocioException.class,
                () -> useCase.executar("corr-1", solId));

        assertEquals("SOLICITACAO_NAO_ENCONTRADA", ex.getCodigo());
        verifyNoInteractions(requisitoPort);
    }

    @Test
    void deveRejeitarTextoBrutoVazio() {
        UUID solId = UUID.randomUUID();
        Solicitacao sol = solicitacaoComTexto(solId, "   ");
        when(solicitacaoPort.buscarPorId(solId)).thenReturn(Optional.of(sol));

        ValidacaoNegocioException ex = assertThrows(ValidacaoNegocioException.class,
                () -> useCase.executar("corr-1", solId));

        assertEquals("TEXTO_BRUTO_VAZIO", ex.getCodigo());
        verifyNoInteractions(requisitoPort);
    }
}
