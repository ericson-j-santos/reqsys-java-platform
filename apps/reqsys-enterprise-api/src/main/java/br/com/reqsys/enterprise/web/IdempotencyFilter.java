package br.com.reqsys.enterprise.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Set<String> METODOS_COMANDO = Set.of("POST", "PUT", "PATCH", "DELETE");

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

        if (METODOS_COMANDO.contains(request.getMethod())) {
            String idempotencyKey = request.getHeader("Idempotency-Key");
            if (isBlank(idempotencyKey)) {
                erro(response, HttpServletResponse.SC_BAD_REQUEST, "IDEMPOTENCY_KEY_OBRIGATORIO", "Header Idempotency-Key e obrigatorio para comandos.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }

    private void erro(HttpServletResponse response, int status, String codigo, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"codigo\":\"" + codigo + "\",\"mensagem\":\"" + mensagem + "\"}");
    }
}
