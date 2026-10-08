//Copyright ANuj Nautiyal
package com.example.demo.resilience;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** These defaults favor a quick demonstration; tune them for real traffic. */
@ConfigurationProperties("app.circuit-breaker")
public record BreakerProperties(int windowSize, int minimumCalls, float failureThreshold, Duration openDuration) {
    public BreakerProperties {
        if (windowSize < 1 || minimumCalls < 1 || minimumCalls > windowSize) {
            throw new IllegalArgumentException("Minimum calls must be between 1 and window size");
        }
        if (!(failureThreshold > 0 && failureThreshold <= 100) || openDuration == null
                || openDuration.isNegative() || openDuration.isZero()) {
            throw new IllegalArgumentException("Positive open duration and failure threshold in (0,100] required");
        }
    }
}
