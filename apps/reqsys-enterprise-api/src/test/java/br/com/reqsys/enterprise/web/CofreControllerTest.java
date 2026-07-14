package br.com.reqsys.enterprise.web;

import br.com.reqsys.enterprise.domain.CofreSegredo;
import br.com.reqsys.enterprise.ports.CofrePort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CofreControllerTest {

    @Test
    void deveFalharFechadoQuandoTokenNaoConfigurado() {
        CofrePort cofrePort = mock(CofrePort.class);
        CofreController controller = new CofreController(cofrePort, "");

        SecurityException ex = assertThrows(SecurityException.class,
                () -> controller.buscar("GOVBI_GEMINI_API_KEY", "qualquer-token"));

        assertTrue(ex.getMessage().contains("REQSYS_COFRE_TOKEN nao configurado"));
    }

    @Test
    void deveRejeitarTokenInvalido() {
        CofrePort cofrePort = mock(CofrePort.class);
        CofreController controller = new CofreController(cofrePort, "token-correto");

        SecurityException ex = assertThrows(SecurityException.class,
                () -> controller.buscar("GOVBI_GEMINI_API_KEY", "token-errado"));

        assertTrue(ex.getMessage().contains("X-Cofre-Token invalido"));
    }

    @Test
    void deveRetornarMetadadosSemExporValorDoSegredo() {
        CofrePort cofrePort = mock(CofrePort.class);
        CofreController controller = new CofreController(cofrePort, "token-correto");
        CofreSegredo segredo = new CofreSegredo(
                UUID.randomUUID(),
                "GOVBI_GEMINI_API_KEY",
                "segredo-real-nao-deve-sair",
                "govbi-ia",
                "Chave Gemini",
                true,
                Instant.now());

        when(cofrePort.buscarPorChave("GOVBI_GEMINI_API_KEY")).thenReturn(Optional.of(segredo));

        var response = controller.buscar("GOVBI_GEMINI_API_KEY", "token-correto");
        var body = response.getBody();

        assertEquals("GOVBI_GEMINI_API_KEY", body.chave());
        assertTrue(body.valorCadastrado());
        assertFalse(body.fingerprintSha256().contains("segredo-real-nao-deve-sair"));
        assertEquals("govbi-ia", body.sistema());
    }

    @Test
    void deveListarMetadadosSemSegredoPorSistema() {
        CofrePort cofrePort = mock(CofrePort.class);
        CofreController controller = new CofreController(cofrePort, "token-correto");
        when(cofrePort.listarPorSistema("govbi-ia")).thenReturn(List.of(
                new CofreSegredo(UUID.randomUUID(), "CHAVE", "valor-secreto", "govbi-ia", "desc", true, Instant.now())
        ));

        var response = controller.listarPorSistema("govbi-ia", "token-correto");

        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).valorCadastrado());
        assertFalse(response.getBody().get(0).fingerprintSha256().contains("valor-secreto"));
    }
}
