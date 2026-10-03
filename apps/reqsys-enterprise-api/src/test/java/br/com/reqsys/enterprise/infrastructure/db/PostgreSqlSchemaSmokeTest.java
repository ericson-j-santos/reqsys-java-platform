package br.com.reqsys.enterprise.infrastructure.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@ActiveProfiles("prod")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostgreSqlSchemaSmokeTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("reqsys")
            .withUsername("reqsys")
            .withPassword("reqsys-smoke-only");

    @DynamicPropertySource
    static void configurarPostgreSql(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveCriarSchemaCompletoComTiposTextoPortaveis() {
        Integer totalTabelas = jdbcTemplate.queryForObject("""
                SELECT count(*)
                  FROM information_schema.tables
                 WHERE table_schema = 'dbo'
                   AND table_name IN ('tb_cofre_segredo', 'tb_requisito', 'tb_solicitacao')
                """, Integer.class);

        List<String> tiposTexto = jdbcTemplate.queryForList("""
                SELECT data_type
                  FROM information_schema.columns
                 WHERE table_schema = 'dbo'
                   AND ((table_name = 'tb_requisito'
                         AND column_name IN ('historia_usuario', 'criterios_bdd'))
                        OR (table_name = 'tb_solicitacao' AND column_name = 'texto_bruto'))
                 ORDER BY table_name, column_name
                """, String.class);

        assertEquals(3, totalTabelas);
        assertEquals(List.of("text", "text", "text"), tiposTexto);
    }
}
