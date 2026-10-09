//Copyright ANuj Nautiyal
package com.example.demo.observability;

import com.example.demo.limiting.RateLimitStrategy;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Record decisions at the shared admission point, including REST and MCP calls. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.metrics.enabled", havingValue = "true")
public class RateLimitMeter {
    private final MeterRegistry registry;

    /**
     * @param policy fixed configured policy name, never caller-controlled tags
     * @param limiter underlying admission strategy
     * @return a strategy that records each decision exactly once
     */
    public RateLimitStrategy decorate(String policy, RateLimitStrategy limiter) {
        Counter admitted = counter(policy, "allowed");
        Counter rejected = counter(policy, "rejected");
        return () -> {
            var decision = limiter.tryAcquire();
            (decision.allowed() ? admitted : rejected).increment();
            return decision;
        };
    }

    private Counter counter(String policy, String outcome) {
        return Counter.builder("app.rate.limit.decisions").description("Shared quota admission decisions")
                .tag("policy", policy).tag("outcome", outcome).register(registry);
    }
}
