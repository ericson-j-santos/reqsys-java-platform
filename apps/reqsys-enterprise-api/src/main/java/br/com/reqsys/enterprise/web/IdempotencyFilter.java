package br.com.reqsys.enterprise.web;

import br.com.reqsys.common.idempotency.HashConteudo;
import br.com.reqsys.enterprise.infrastructure.metrics.ReqSysMetrics;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Set<String> METODOS_COMANDO = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final JdbcTemplate jdbcTemplate;
    private final ReqSysMetrics metrics;
    private final boolean idempotencyEnabled;

    public IdempotencyFilter(JdbcTemplate jdbcTemplate,
                             ReqSysMetrics metrics,
                             @Value("${reqsys.idempotency.enabled:true}") boolean idempotencyEnabled) {
        this.jdbcTemplate = jdbcTemplate;
        this.metrics = metrics;
        this.idempotencyEnabled = idempotencyEnabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String correlationId = request.getHeader("X-Correlation-Id");
        if (isBlank(correlationId)) {
            erro(response, HttpServletResponse.SC_BAD_REQUEST, "CORRELATION_ID_OBRIGATORIO", "Header X-Correlation-Id e obrigatorio.");
            return;
        }

        if (!METODOS_COMANDO.contains(request.getMethod()) || !idempotencyEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader("Idempotency-Key");
        if (isBlank(idempotencyKey)) {
            erro(response, HttpServletResponse.SC_BAD_REQUEST, "IDEMPOTENCY_KEY_OBRIGATORIO", "Header Idempotency-Key e obrigatorio para comandos.");
            return;
        }

        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String hashConteudo = gerarHash(cachedRequest);

        IdempotencyRegistro registro = buscarRegistro(idempotencyKey);
        if (registro != null) {
            if (!registro.hashConteudo().equals(hashConteudo)) {
                metrics.idempotenciaConflito();
                erro(response, HttpServletResponse.SC_CONFLICT, "IDEMPOTENCY_KEY_CONFLITANTE",
                        "Idempotency-Key ja utilizada com payload diferente.");
                return;
            }
            if ("CONCLUIDO".equalsIgnoreCase(registro.status()) && registro.respostaJson() != null) {
                metrics.idempotenciaReplay();
                response.setStatus(registro.httpStatus());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(registro.respostaJson());
                return;
            }
        } else {
            reservarRegistro(idempotencyKey, hashConteudo);
            metrics.idempotenciaReservada();
        }

        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(cachedRequest, wrappedResponse);
            String corpoResposta = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
            if (wrappedResponse.getStatus() < 500) {
                concluirRegistro(idempotencyKey, wrappedResponse.getStatus(), corpoResposta);
                metrics.idempotenciaConcluida();
            } else {
                marcarErro(idempotencyKey, wrappedResponse.getStatus(), corpoResposta);
                metrics.idempotenciaErro();
            }
        } finally {
            wrappedResponse.copyBodyToResponse();
        }
    }

    private String gerarHash(CachedBodyHttpServletRequest request) {
        String canonico = request.getMethod() + "|" + request.getRequestURI() + "|" +
                nullSafe(request.getQueryString()) + "|" +
                new String(request.getCachedBody(), StandardCharsets.UTF_8);
        return HashConteudo.sha256(canonico);
    }

    private IdempotencyRegistro buscarRegistro(String key) {
        try {
            List<IdempotencyRegistro> registros = jdbcTemplate.query("""
                    SELECT TOP 1 hash_conteudo, status, resposta_json, COALESCE(http_status, 200) AS http_status
                    FROM dbo.tb_idempotencia
                    WHERE idempotency_key = ?
                    """, (rs, rowNum) -> new IdempotencyRegistro(
                    rs.getString("hash_conteudo"),
                    rs.getString("status"),
                    rs.getString("resposta_json"),
                    rs.getInt("http_status")), key);
            return registros.isEmpty() ? null : registros.get(0);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("Falha ao consultar idempotencia. Verifique migration tb_idempotencia.", ex);
        }
    }

    private void reservarRegistro(String key, String hashConteudo) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO dbo.tb_idempotencia
                        (id, idempotency_key, hash_conteudo, status, resposta_json, http_status)
                    VALUES
                        (?, ?, ?, 'PENDENTE', NULL, NULL)
                    """, UUID.randomUUID(), key, hashConteudo);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("Falha ao reservar chave de idempotencia.", ex);
        }
    }

    private void concluirRegistro(String key, int httpStatus, String respostaJson) {
        jdbcTemplate.update("""
                UPDATE dbo.tb_idempotencia
                   SET status = 'CONCLUIDO', resposta_json = ?, http_status = ?, atualizado_em_utc = SYSUTCDATETIME()
                 WHERE idempotency_key = ?
                """, respostaJson, httpStatus, key);
    }

    private void marcarErro(String key, int httpStatus, String respostaJson) {
        jdbcTemplate.update("""
                UPDATE dbo.tb_idempotencia
                   SET status = 'ERRO', resposta_json = ?, http_status = ?, atualizado_em_utc = SYSUTCDATETIME()
                 WHERE idempotency_key = ?
                """, respostaJson, httpStatus, key);
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }

    private String nullSafe(String valor) {
        return valor == null ? "" : valor;
    }

    private void erro(HttpServletResponse response, int status, String codigo, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"codigo\":\"" + codigo + "\",\"mensagem\":\"" + mensagem + "\"}");
    }

    private record IdempotencyRegistro(String hashConteudo, String status, String respostaJson, int httpStatus) {}
}
