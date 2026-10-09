//Copyright ANuj Nautiyal
package com.example.demo.observability;

import com.example.demo.limiting.RateLimitRegistry;
import com.example.demo.mcp.RateLimiterTools;
import com.example.demo.resilience.Breakers;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.metrics.enabled=true", "app.rate-limits.policies.fixed-demo.capacity=1",
        "app.rate-limits.policies.fixed-demo.period=1d"})
@AutoConfigureMockMvc
class EnabledMetricsTests {
    @Autowired private MockMvc mvc;
    @Autowired private MeterRegistry registry;
    @Autowired private RateLimiterTools tools;
    @Autowired private RateLimitRegistry limits;
    @Autowired private Breakers breakers;
    @Autowired private ApplicationContext context;

    @Test
    void restAndMcpShareDecisionCountersAndScrapeDoesNotSpendQuota() throws Exception {
        mvc.perform(get("/demo/fixed")).andExpect(status().isOk());
        tools.acquire("fixed-demo");
        mvc.perform(get("/demo/fixed")).andExpect(status().isTooManyRequests());
        mvc.perform(get("/actuator/prometheus")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("app_rate_limit_decisions_total")));
        assertThat(count("allowed")).isEqualTo(1);
        assertThat(count("rejected")).isEqualTo(2);
    }

    @Test
    void invalidNamesCannotCreateUnboundedMetricTags() {
        int before = registry.getMeters().size();
        assertThatThrownBy(() -> limits.acquire("untrusted-name")).isInstanceOf(IllegalArgumentException.class);
        assertThat(registry.getMeters()).hasSize(before);
    }

    @Test
    void circuitStateGaugesFollowTransitionsWithoutCallingDependency() {
        assertThat(state("CLOSED")).isEqualTo(1);
        for (int i = 0; i < 4; i++) assertThatThrownBy(() -> breakers.execute("weather", () -> {
            throw new IllegalStateException("unavailable");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(state("OPEN")).isEqualTo(1);
        assertThat(state("CLOSED")).isZero();
    }

    @Test
    void metricsFlagDoesNotEnableTracingOrOtherManagementEndpoints() throws Exception {
        assertThat(context.getBeansOfType(SpanExporter.class)).isEmpty();
        mvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
        mvc.perform(get("/actuator/health")).andExpect(status().isNotFound());
    }

    private double count(String outcome) {
        return registry.get("app.rate.limit.decisions").tag("policy", "fixed-demo")
                .tag("outcome", outcome).counter().count();
    }

    private double state(String state) {
        return registry.get("app.circuit.state").tag("dependency", "weather").tag("state", state).gauge().value();
    }
}
