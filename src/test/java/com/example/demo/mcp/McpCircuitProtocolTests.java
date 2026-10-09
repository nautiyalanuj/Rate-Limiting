//Copyright ANuj Nautiyal
package com.example.demo.mcp;

import com.example.demo.support.LocalHttp;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;
import static com.example.demo.support.McpConnection.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.circuit-breaker.open-duration=1h", "app.weather.api-key="})
@ActiveProfiles("demo")
class McpCircuitProtocolTests {
    @LocalServerPort private int port;

    @Test
    void mcpFailuresOpenTheCircuitSeenByRest() throws Exception {
        try (var client = open(port)) {
            for (int i = 0; i < 4; i++) assertThat(envelope(call(client, "test_circuit_breaker", Map.of("fail", true)))
                    .get("code")).isEqualTo("DEPENDENCY_UNAVAILABLE");
            assertThat(envelope(call(client, "test_circuit_breaker", Map.of("fail", false))).get("code")).isEqualTo("CIRCUIT_OPEN");
            assertThat(data(call(client, "get_circuit_breaker", Map.of("name", "demo"))).get("state")).isEqualTo("OPEN");
            assertThat(LocalHttp.get(port, "/demo/circuit")).isEqualTo(503);
        }
    }

    @Test
    void missingWeatherKeyIsReportedWithoutRawException() {
        try (var client = open(port)) {
            var result = call(client, "get_weather", Map.of("city", "London"));
            assertThat(result.isError()).isTrue();
            assertThat(envelope(result).get("code")).isEqualTo("WEATHER_NOT_CONFIGURED");
        }
    }
}
