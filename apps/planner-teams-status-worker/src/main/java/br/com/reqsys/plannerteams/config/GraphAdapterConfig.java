package br.com.reqsys.plannerteams.config;

import br.com.reqsys.graph.MockGraphAdapter;
import br.com.reqsys.graph.PlannerPort;
import br.com.reqsys.graph.TeamsPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
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
}
