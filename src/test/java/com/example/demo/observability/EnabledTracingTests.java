//Copyright ANuj Nautiyal
package com.example.demo.observability;

import com.example.demo.support.LocalHttp;
import com.example.demo.support.TraceReceiver;
import com.example.demo.support.WeatherStub;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.tracing.enabled=true", "management.opentelemetry.tracing.export.schedule-delay=100ms",
        "app.weather.api-key=private-trace-test-key"})
class EnabledTracingTests {
    private static final TraceReceiver RECEIVER = receiver();
    private static final WeatherStub WEATHER = weather();
    @LocalServerPort private int port;

    @DynamicPropertySource
    static void exporter(DynamicPropertyRegistry properties) {
        properties.add("management.opentelemetry.tracing.export.otlp.endpoint", RECEIVER::endpoint);
        properties.add("app.weather.base-url", WEATHER::baseUrl);
    }

    @Test
    void tracingAloneExportsRequestSpansAndLeavesMetricsUnavailable() throws Exception {
        assertThat(LocalHttp.get(port, "/demo/token")).isEqualTo(200);
        assertThat(RECEIVER.take()).contains("rate-limiting-demo", "/demo/token");
        assertWeatherTracing();
        assertThat(LocalHttp.get(port, "/actuator/prometheus")).isEqualTo(404);
    }

    private void assertWeatherTracing() throws Exception {
        assertThat(LocalHttp.get(port, "/weather?city=London")).isEqualTo(200);
        assertThat(RECEIVER.awaitContaining("http get")).contains("http get").doesNotContain("private-trace-test-key");
        WEATHER.fail();
        assertThat(LocalHttp.get(port, "/weather?city=London")).isEqualTo(503);
        assertThat(RECEIVER.awaitContaining("Weather request failed"))
                .contains("Weather request failed").doesNotContain("private-trace-test-key");
    }

    @AfterAll
    static void closeReceiver() {
        RECEIVER.close();
        WEATHER.close();
    }

    private static TraceReceiver receiver() {
        try { return new TraceReceiver(); }
        catch (java.io.IOException error) { throw new java.io.UncheckedIOException(error); }
    }

    private static WeatherStub weather() {
        try { return new WeatherStub(false); }
        catch (java.io.IOException error) { throw new java.io.UncheckedIOException(error); }
    }
}
