//Copyright ANuj Nautiyal
package com.example.demo.mcp;

import com.example.demo.support.LocalHttp;
import com.example.demo.weather.WeatherClient;
import com.example.demo.weather.WeatherReport;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.example.demo.support.McpConnection.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.weather.api-key=test-key", "app.rate-limits.policies.weather.capacity=1",
        "app.rate-limits.policies.weather.period=1d", "app.rate-limits.policies.fixed-demo.period=1d",
        "app.rate-limits.policies.users.capacity=1", "app.rate-limits.policies.users.period=1d",
        "app.circuit-breaker.open-duration=1h"})
@ActiveProfiles("demo")
class McpProtocolTests {
    @LocalServerPort private int port;
    @MockitoBean private WeatherClient weather;

    @Test
    void sdkInitializesAndDiscoversBothProtectionTypes() {
        try (var client = open(port)) {
            assertThat(client.getServerInfo().name()).isEqualTo("rate-limiting-demo");
            assertThat(client.listTools().tools()).extracting(tool -> tool.name()).containsExactlyInAnyOrder(
                    "list_rate_limiters", "try_acquire_rate_limit", "list_circuit_breakers", "get_circuit_breaker",
                    "get_weather", "list_users", "test_circuit_breaker");
            assertThat(data(call(client, "list_rate_limiters", Map.of())).get("fixed-demo")).isInstanceOf(Map.class);
            assertThat(data(call(client, "list_circuit_breakers", Map.of())).keySet().stream().map(Object::toString).toList()).contains("weather", "database", "demo");
        }
    }

    @Test
    void schemasRequireNamesAndAdvertisePermitSideEffects() {
        try (var client = open(port)) {
            var acquire = client.listTools().tools().stream().filter(tool -> tool.name().equals("try_acquire_rate_limit"))
                    .findFirst().orElseThrow();
            assertThat(acquire.inputSchema().get("required")).asList().contains("policy");
            assertThat(acquire.annotations().readOnlyHint()).isFalse();
            assertThat(acquire.annotations().idempotentHint()).isFalse();
        }
    }

    @Test
    void explicitMcpPermitAndRestCallsSpendTheSameBudget() throws Exception {
        try (var client = open(port)) {
            assertThat(data(call(client, "try_acquire_rate_limit", Map.of("policy", "fixed-demo"))).get("allowed")).isEqualTo(true);
            assertThat(LocalHttp.get(port, "/demo/fixed")).isEqualTo(200);
            assertThat(LocalHttp.get(port, "/demo/fixed")).isEqualTo(200);
            assertThat(LocalHttp.get(port, "/demo/fixed")).isEqualTo(429);
        }
    }

    @Test
    void weatherToolUsesQuotaBeforeCallingProvider() throws Exception {
        when(weather.current("London")).thenReturn(new WeatherReport("London", "UK", 18, "Sunny"));
        try (var client = open(port)) {
            assertThat(call(client, "get_weather", Map.of("city", "London")).isError()).isFalse();
            assertThat(envelope(call(client, "get_weather", Map.of("city", "London"))).get("code")).isEqualTo("RATE_LIMITED");
            assertThat(LocalHttp.get(port, "/weather?city=London")).isEqualTo(429);
            verify(weather, times(1)).current("London");
        }
    }

    @Test
    void userToolAlsoSharesTheRestQuota() throws Exception {
        try (var client = open(port)) {
            assertThat(envelope(call(client, "list_users", Map.of())).get("data")).asList().isEmpty();
            assertThat(LocalHttp.get(port, "/users")).isEqualTo(429);
        }
    }

    @Test
    void unknownRegistryNamesAreSafeMcpErrors() {
        try (var client = open(port)) {
            var result = call(client, "get_circuit_breaker", Map.of("name", "unconfigured"));
            assertThat(result.isError()).isTrue();
            assertThat(envelope(result).get("code")).isEqualTo("INVALID_ARGUMENT");
            assertThat(call(client, "try_acquire_rate_limit", Map.of("policy", "unconfigured")).isError()).isTrue();
        }
    }

    @Test
    void browserOriginIsRejectedOnRealMcpEndpoint() throws Exception {
        assertThat(LocalHttp.originRequest(port, "/mcp")).isEqualTo(403);
    }
}
