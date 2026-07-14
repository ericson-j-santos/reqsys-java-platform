package br.com.reqsys.enterprise.web;

import br.com.reqsys.enterprise.infrastructure.metrics.ReqSysMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class IdempotencyFilterTest {

    @Test
    void deveExigirCorrelationIdEmRotasDeNegocio() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        IdempotencyFilter filter = new IdempotencyFilter(
                jdbcTemplate,
                new ReqSysMetrics(new SimpleMeterRegistry()),
                true);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/solicitacoes");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("CORRELATION_ID_OBRIGATORIO"));
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void deveExigirIdempotencyKeyEmComandosQuandoHabilitado() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        IdempotencyFilter filter = new IdempotencyFilter(
                jdbcTemplate,
                new ReqSysMetrics(new SimpleMeterRegistry()),
                true);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/solicitacoes");
        request.addHeader("X-Correlation-Id", "corr-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("IDEMPOTENCY_KEY_OBRIGATORIO"));
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void devePermitirComandoSemIdempotencyKeyQuandoIdempotenciaDesabilitadaLocalmente() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        IdempotencyFilter filter = new IdempotencyFilter(
                jdbcTemplate,
                new ReqSysMetrics(new SimpleMeterRegistry()),
                false);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/solicitacoes");
        request.addHeader("X-Correlation-Id", "corr-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(200, response.getStatus());
        verifyNoInteractions(jdbcTemplate);
    }
}
