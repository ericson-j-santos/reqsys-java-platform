package br.com.reqsys.enterprise.application;

import br.com.reqsys.common.domain.ValidacaoNegocioException;
import br.com.reqsys.enterprise.domain.Requisito;
import br.com.reqsys.enterprise.domain.StatusRequisito;
import br.com.reqsys.enterprise.ports.AuditPort;
import br.com.reqsys.enterprise.ports.RequisitoPort;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstruturarRequisitoUseCaseTest {

    @Mock RequisitoPort requisitoPort;
    @Mock AuditPort auditPort;
    @InjectMocks EstruturarRequisitoUseCase useCase;

    private Requisito requisitoPadrao(UUID id, StatusRequisito status) {
        return new Requisito(id, UUID.randomUUID(), "corr-1",
                "Gerar relatório mensal", null, null, null,
                status, null, Instant.now(), null);
    }

    @Test
    void deveEstruturarRequisito() {
        UUID id = UUID.randomUUID();
        Requisito r = requisitoPadrao(id, StatusRequisito.VALIDADO);
        when(requisitoPort.buscarPorId(id)).thenReturn(Optional.of(r));
        when(requisitoPort.atualizar(any())).thenAnswer(inv -> inv.getArgument(0));

        Requisito resultado = useCase.executar("corr-1", id, "gestor");

        assertEquals(StatusRequisito.ESTRUTURADO, resultado.status());
        assertTrue(resultado.historiaUsuario().contains("gestor"));
        assertNotNull(resultado.criteriosBdd());
    }

    @Test
    void deveUsarAtorPadraoQuandoNulo() {
        UUID id = UUID.randomUUID();
        when(requisitoPort.buscarPorId(id)).thenReturn(Optional.of(requisitoPadrao(id, StatusRequisito.VALIDADO)));
        when(requisitoPort.atualizar(any())).thenAnswer(inv -> inv.getArgument(0));

        Requisito resultado = useCase.executar("corr-1", id, null);
        assertTrue(resultado.historiaUsuario().contains("usuário"));
    }

    @Test
    void deveRejeitarRequisitoJaPublicado() {
        UUID id = UUID.randomUUID();
        when(requisitoPort.buscarPorId(id)).thenReturn(Optional.of(
                requisitoPadrao(id, StatusRequisito.PUBLICADO_REDMINE)));

        assertThrows(ValidacaoNegocioException.class,
                () -> useCase.executar("corr-1", id, "gestor"));
    }
}
