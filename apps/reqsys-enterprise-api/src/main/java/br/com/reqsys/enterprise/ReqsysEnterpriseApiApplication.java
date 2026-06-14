package br.com.reqsys.enterprise;

import br.com.reqsys.security.SecurityHeadersConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableRetry
@EnableScheduling
@Import(SecurityHeadersConfig.class)
@SpringBootApplication
public class ReqsysEnterpriseApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReqsysEnterpriseApiApplication.class, args);
    }
}
