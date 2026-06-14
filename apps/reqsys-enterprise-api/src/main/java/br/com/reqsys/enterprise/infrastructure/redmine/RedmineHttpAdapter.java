package br.com.reqsys.enterprise.infrastructure.redmine;

import br.com.reqsys.enterprise.domain.Requisito;
import br.com.reqsys.enterprise.ports.RedminePort;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class RedmineHttpAdapter implements RedminePort {

    private static final Logger log = LoggerFactory.getLogger(RedmineHttpAdapter.class);

    private static final String[] SUBTAREFAS = {"Frontend", "Backend", "Dados", "QA"};

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;
    private final String projectId;

    public RedmineHttpAdapter(
            RestTemplate restTemplate,
            @Value("${redmine.base-url}") String baseUrl,
            @Value("${redmine.api-key}")  String apiKey,
            @Value("${redmine.project-id:reqsys}") String projectId) {
        this.restTemplate = restTemplate;
        this.baseUrl  = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.apiKey   = apiKey;
        this.projectId = projectId;
    }

    @Override
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public int publicarRequisito(Requisito requisito) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Redmine-API-Key", apiKey);

        String descricao = formatarDescricao(requisito);
        var issueBody = Map.of("issue", new RedmineIssueDto(
                projectId, 2, requisito.titulo(), descricao));

        ResponseEntity<IssueResponse> resp = restTemplate.exchange(
                baseUrl + "issues.json",
                HttpMethod.POST,
                new HttpEntity<>(issueBody, headers),
                IssueResponse.class);

        int issueId = resp.getBody().issue().id();
        log.info("Redmine issue criado: #{} correlationId={}", issueId, requisito.correlationId());

        for (String area : SUBTAREFAS) {
            var sub = Map.of("issue", new RedmineIssueDto(
                    projectId, 2, area + ": " + requisito.titulo(), null, issueId));
            restTemplate.exchange(baseUrl + "issues.json", HttpMethod.POST,
                    new HttpEntity<>(sub, headers), Void.class);
        }
        log.info("Redmine subtarefas criadas para issue #{}", issueId);
        return issueId;
    }

    private String formatarDescricao(Requisito r) {
        StringBuilder sb = new StringBuilder();
        if (r.historiaUsuario() != null) sb.append("h3. História de Usuário\n\n").append(r.historiaUsuario()).append("\n\n");
        if (r.criteriosBdd() != null)    sb.append("h3. Critérios de Aceitação (BDD)\n\n<pre>").append(r.criteriosBdd()).append("</pre>\n\n");
        if (r.confianca() != null)        sb.append("_Confiança do refinamento: ").append(r.confianca()).append("_");
        return sb.toString();
    }

    private record IssueResponse(@JsonProperty("issue") IssueSummary issue) {}
    private record IssueSummary(@JsonProperty("id") int id) {}
}
