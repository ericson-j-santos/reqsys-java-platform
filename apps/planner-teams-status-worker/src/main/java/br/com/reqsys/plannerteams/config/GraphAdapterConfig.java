package br.com.reqsys.plannerteams.config;

import br.com.reqsys.graph.MicrosoftGraphProperties;
import br.com.reqsys.graph.MicrosoftGraphTeamsAdapter;
import br.com.reqsys.graph.MicrosoftGraphTokenClient;
import br.com.reqsys.graph.MockGraphAdapter;
import br.com.reqsys.graph.PlannerPort;
import br.com.reqsys.graph.TeamsPort;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(MicrosoftGraphProperties.class)
public class GraphAdapterConfig {

    @Bean
    @Profile({"default", "mock", "test"})
    public MockGraphAdapter mockGraphAdapter() {
        return new MockGraphAdapter();
    }

    @Bean
    @Profile({"default", "mock", "test"})
    public TeamsPort teamsPort(MockGraphAdapter adapter) {
        return adapter;
    }

    @Bean
    @Profile({"default", "mock", "test"})
    public PlannerPort plannerPort(MockGraphAdapter adapter) {
        return adapter;
    }

    @Bean
    @Profile("graph")
    public MicrosoftGraphTokenClient microsoftGraphTokenClient(
            MicrosoftGraphProperties properties,
            WebClient.Builder webClientBuilder
    ) {
        return new MicrosoftGraphTokenClient(properties, webClientBuilder);
    }

    @Bean
    @Profile("graph")
    public TeamsPort microsoftGraphTeamsPort(
            MicrosoftGraphProperties properties,
            MicrosoftGraphTokenClient tokenClient,
            WebClient.Builder webClientBuilder
    ) {
        return new MicrosoftGraphTeamsAdapter(properties, tokenClient, webClientBuilder);
    }
}
