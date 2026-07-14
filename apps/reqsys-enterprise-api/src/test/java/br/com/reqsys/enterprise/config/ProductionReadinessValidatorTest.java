package br.com.reqsys.enterprise.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionReadinessValidatorTest {

    @Test
    void naoDeveBloquearQuandoNaoForProd() {
        MockEnvironment env = ambienteBase();
        env.setActiveProfiles("local");

        ProductionReadinessValidator validator = new ProductionReadinessValidator(env);

        assertDoesNotThrow(() -> validator.run(null));
    }

    @Test
    void deveBloquearProdComAuthDesligadaCorsWildcardJwtAusenteECofreSemToken() {
        MockEnvironment env = ambienteBase();
        env.setActiveProfiles("prod");
        env.setProperty("reqsys.security.enabled", "false");
        env.setProperty("reqsys.security.jwt.audiences", "");
        env.setProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri", "");
        env.setProperty("reqsys.cors.allowed-origins", "*");
        env.setProperty("reqsys.cofre.token", "");

        ProductionReadinessValidator validator = new ProductionReadinessValidator(env);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.run(null));
        assertTrue(ex.getMessage().contains("Auth desligada"));
        assertTrue(ex.getMessage().contains("JWT issuer-uri ausente"));
        assertTrue(ex.getMessage().contains("JWT audience ausente"));
        assertTrue(ex.getMessage().contains("CORS com '*'"));
        assertTrue(ex.getMessage().contains("REQSYS_COFRE_TOKEN ausente"));
    }

    @Test
    void deveBloquearProdComIdempotenciaDesligadaSqlRelaxadoUsuarioSaSenhaDefault() {
        MockEnvironment env = ambienteBase();
        env.setActiveProfiles("prod");
        env.setProperty("reqsys.idempotency.enabled", "false");
        env.setProperty("spring.datasource.url", "jdbc:sqlserver://srv:1433;databaseName=reqsys;encrypt=true;trustServerCertificate=true");
        env.setProperty("spring.datasource.username", "sa");
        env.setProperty("spring.datasource.password", "YourStrong!Passw0rd");

        ProductionReadinessValidator validator = new ProductionReadinessValidator(env);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.run(null));
        assertTrue(ex.getMessage().contains("Idempotencia desligada"));
        assertTrue(ex.getMessage().contains("trustServerCertificate=true"));
        assertTrue(ex.getMessage().contains("Usuario SQL Server 'sa'"));
        assertTrue(ex.getMessage().contains("Senha de banco ausente ou default"));
    }

    @Test
    void devePermitirProdComConfiguracaoSegura() {
        MockEnvironment env = ambienteBase();
        env.setActiveProfiles("prod");

        ProductionReadinessValidator validator = new ProductionReadinessValidator(env);

        assertDoesNotThrow(() -> validator.run(null));
    }

    private MockEnvironment ambienteBase() {
        return new MockEnvironment()
                .withProperty("reqsys.security.enabled", "true")
                .withProperty("reqsys.idempotency.enabled", "true")
                .withProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri", "https://login.microsoftonline.com/tenant/v2.0")
                .withProperty("reqsys.security.jwt.audiences", "api://reqsys")
                .withProperty("reqsys.cors.allowed-origins", "https://app.reqsys.exemplo.com")
                .withProperty("reqsys.cofre.token", "token-forte")
                .withProperty("spring.datasource.url", "jdbc:sqlserver://srv:1433;databaseName=reqsys;encrypt=true;trustServerCertificate=false")
                .withProperty("spring.datasource.username", "reqsys_app")
                .withProperty("spring.datasource.password", "senha-segura")
                .withProperty("redmine.api-key", "redmine-key");
    }
}
