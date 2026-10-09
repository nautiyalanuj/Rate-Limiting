//Copyright ANuj Nautiyal
package com.example.demo.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DisabledObservabilityTests {
    @Autowired private MockMvc mvc;
    @Autowired private ApplicationContext context;
    @Autowired private MeterRegistry registry;

    @Test
    void defaultStartupExposesNoManagementEndpoints() throws Exception {
        mvc.perform(get("/actuator/prometheus")).andExpect(status().isNotFound());
        mvc.perform(get("/actuator/health")).andExpect(status().isNotFound());
        mvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
    }

    @Test
    void optionalInstrumentationAndExportersAreAbsent() {
        assertThat(context.getBeansOfType(RateLimitMeter.class)).isEmpty();
        assertThat(context.getBeansOfType(CircuitStateMeters.class)).isEmpty();
        assertThat(context.getBeansOfType(SpanExporter.class)).isEmpty();
        assertThat(registry.getMeters()).isEmpty();
    }
}
