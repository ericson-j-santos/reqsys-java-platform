package br.com.reqsys.enterprise.infrastructure;

import br.com.reqsys.enterprise.ports.CofrePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Seed inicial do cofre para ambiente local (perfil local, H2).
 * Insere as chaves de desenvolvimento se ainda não existirem.
 * Ativo somente quando reqsys.cofre.seed-local=true.
 */
@Component
@ConditionalOnProperty(name = "reqsys.cofre.seed-local", havingValue = "true")
public class CofreSeederLocal implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CofreSeederLocal.class);

    private final CofrePort cofrePort;

    public CofreSeederLocal(CofrePort cofrePort) {
        this.cofrePort = cofrePort;
    }

    @Override
    public void run(String... args) {
        seed("GOVBI_GEMINI_API_KEY",
                System.getenv().getOrDefault("GOVBI_GEMINI_API_KEY", ""),
                "govbi-ia",
                "Google Gemini API Key — obter em https://aistudio.google.com");

        seed("REDMINE_API_KEY",
                "",
                "reqsys-enterprise-api",
                "API Key do Redmine (redmine-c5i6.onrender.com) — preencher via POST /api/v1/cofre/segredo");

        log.info("[Cofre] Seed local concluído. Acesse http://localhost:8081/h2-console para visualizar os dados.");
    }

    private void seed(String chave, String valor, String sistema, String descricao) {
        if (cofrePort.buscarPorChave(chave).isEmpty()) {
            cofrePort.salvar(chave, valor, sistema, descricao);
            log.info("[Cofre] Segredo '{}' inserido.", chave);
        } else {
            log.info("[Cofre] Segredo '{}' já existe — mantido.", chave);
        }
    }
}
