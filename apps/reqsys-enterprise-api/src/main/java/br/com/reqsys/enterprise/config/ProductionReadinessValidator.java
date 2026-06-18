package br.com.reqsys.enterprise.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class ProductionReadinessValidator implements ApplicationRunner {

    private final Environment environment;

    public ProductionReadinessValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!isProd()) return;

        List<String> bloqueios = new ArrayList<>();

        boolean authEnabled = Boolean.parseBoolean(prop("reqsys.security.enabled", "true"));
        String issuerUri = prop("spring.security.oauth2.resourceserver.jwt.issuer-uri", "");
        String audiences = prop("reqsys.security.jwt.audiences", "");
        String corsOrigins = prop("reqsys.cors.allowed-origins", "");
        String cofreToken = prop("reqsys.cofre.token", "");
        String datasourceUrl = prop("spring.datasource.url", "");
        String datasourceUser = prop("spring.datasource.username", "");
        String datasourcePassword = prop("spring.datasource.password", "");
        String redmineApiKey = prop("redmine.api-key", "");

        if (!authEnabled) {
            bloqueios.add("Auth desligada em profile prod.");
        }
        if (issuerUri.isBlank()) {
            bloqueios.add("JWT issuer-uri ausente em profile prod.");
        }
        if (audiences.isBlank()) {
            bloqueios.add("JWT audience ausente em profile prod.");
        }
        if (contemWildcardCors(corsOrigins)) {
            bloqueios.add("CORS com '*' em profile prod.");
        }
        if (cofreToken.isBlank()) {
            bloqueios.add("REQSYS_COFRE_TOKEN ausente; cofre administrativo bloqueado.");
        }
        if (datasourceUrl.toLowerCase(Locale.ROOT).contains("trustservercertificate=true")) {
            bloqueios.add("SQL Server com trustServerCertificate=true em profile prod.");
        }
        if (datasourceUser.equalsIgnoreCase("sa")) {
            bloqueios.add("Usuario SQL Server 'sa' nao permitido em profile prod.");
        }
        if (datasourcePassword.isBlank() || datasourcePassword.contains("YourStrong!Passw0rd")) {
            bloqueios.add("Senha de banco ausente ou default em profile prod.");
        }
        if (redmineApiKey.isBlank()) {
            bloqueios.add("REDMINE_API_KEY ausente em profile prod.");
        }

        if (!bloqueios.isEmpty()) {
            throw new IllegalStateException("Producao bloqueada por gates de seguranca: " + String.join(" | ", bloqueios));
        }
    }

    private boolean isProd() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("prod") || profile.equalsIgnoreCase("production"));
    }

    private String prop(String chave, String padrao) {
        return environment.getProperty(chave, padrao).trim();
    }

    private boolean contemWildcardCors(String csv) {
        if (csv == null || csv.isBlank()) return false;
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .anyMatch("*"::equals);
    }
}
