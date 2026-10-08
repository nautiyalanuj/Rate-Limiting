//Copyright ANuj Nautiyal
package com.example.demo.web;

import com.example.demo.resilience.Breakers;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.rate-limits.policies.fixed-demo.period=1d",
        "app.rate-limits.policies.sliding-demo.period=1d", "app.rate-limits.policies.token-demo.period=1d"})
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class DemoHttpTests {
    @Autowired private MockMvc mvc;
    @Autowired private Breakers breakers;

    @ParameterizedTest
    @MethodSource("limiterRoutes")
    void controllerAnnotationReturns429WithRetryAfter(String path) throws Exception {
        String before = breakers.state("demo").name();
        for (int i = 0; i < 3; i++) mvc.perform(get(path)).andExpect(status().isOk());
        mvc.perform(get(path)).andExpect(status().isTooManyRequests()).andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
        assertThat(breakers.state("demo").name()).isEqualTo(before);
    }

    @Test
    void circuitDemoFailsThenStopsCallingTheDependency() throws Exception {
        for (int i = 0; i < 4; i++) mvc.perform(get("/demo/circuit").param("fail", "true"))
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.code").value("DEPENDENCY_UNAVAILABLE"));
        mvc.perform(get("/demo/circuit")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("CIRCUIT_OPEN"));
        mvc.perform(get("/demo/circuit/state")).andExpect(jsonPath("$.state").value("OPEN"));
    }

    @Test
    void existingUserRoutesWorkWithoutAnExternalDatabase() throws Exception {
        mvc.perform(get("/users")).andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(get("/users/00000000-0000-0000-0000-000000000001")).andExpect(status().isOk());
    }

    private static Stream<String> limiterRoutes() {
        return Stream.of("/demo/fixed", "/demo/sliding", "/demo/token");
    }
}
