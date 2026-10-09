//Copyright ANuj Nautiyal
package com.example.demo.mcp;

import com.example.demo.limiting.*;
import com.example.demo.resilience.*;
import com.example.demo.support.ConcurrentCalls;
import com.example.demo.weather.WeatherNotConfigured;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.*;
import static com.example.demo.support.McpConnection.*;

class McpToolUnitTests {
    private final RateLimitRegistry limits = new RateLimitRegistry(new RateLimitProperties(Map.of("shared",
            new RateLimitPolicy(Algorithm.FIXED_WINDOW, 2, Duration.ofDays(1)))));
    private final McpResults results = new McpResults(JsonMapper.builder().build());
    private final McpToolRunner runner = new McpToolRunner(limits, results);

    @Test
    void concurrentToolCallsCannotSpendMoreThanSharedCapacity() throws Exception {
        assertThat(ConcurrentCalls.allowed(24, () -> !runner.guarded("shared", () -> "ok").isError())).isEqualTo(2);
    }

    @Test
    void rejectionCarriesRetryHintAndNeverRunsDependency() {
        AtomicInteger calls = new AtomicInteger();
        runner.guarded("shared", calls::incrementAndGet);
        runner.guarded("shared", calls::incrementAndGet);
        var denied = runner.guarded("shared", calls::incrementAndGet);
        assertThat(denied.isError()).isTrue();
        assertThat(envelope(denied).get("code")).isEqualTo("RATE_LIMITED");
        assertThat(((Number) envelope(denied).get("retryAfterSeconds")).longValue()).isPositive();
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void unexpectedSqlOrProviderDetailsNeverReachTheModel() {
        var result = runner.run(() -> { throw new IllegalStateException("SECRET key=my-password SQL=select"); });
        assertThat(result.isError()).isTrue();
        assertThat(envelope(result).get("code")).isEqualTo("DEPENDENCY_UNAVAILABLE");
        assertThat(result.content().toString()).doesNotContain("SECRET", "my-password", "SQL");
    }

    @Test
    void catalogInspectionDoesNotSpendPermits() {
        RateLimiterTools tools = new RateLimiterTools(limits, runner);
        tools.list();
        tools.list();
        assertThat(data(tools.acquire("shared")).get("allowed")).isEqualTo(true);
        assertThat(data(tools.acquire("shared")).get("allowed")).isEqualTo(true);
        assertThat(data(tools.acquire("shared")).get("allowed")).isEqualTo(false);
    }

    @Test
    void nullNamesAndInvalidArgumentsAreSafeErrors() {
        assertThat(envelope(new RateLimiterTools(limits, runner).acquire(null)).get("code")).isEqualTo("INVALID_ARGUMENT");
        assertThat(envelope(runner.run(() -> { throw new IllegalArgumentException("private details"); })).get("code"))
                .isEqualTo("INVALID_ARGUMENT");
        assertThat(envelope(runner.run(() -> { throw new WeatherNotConfigured(); })).get("code"))
                .isEqualTo("WEATHER_NOT_CONFIGURED");
    }
}
