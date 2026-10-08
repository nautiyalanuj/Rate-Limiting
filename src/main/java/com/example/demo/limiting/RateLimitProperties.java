//Copyright ANuj Nautiyal
package com.example.demo.limiting;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Immutable startup configuration; request data never creates policy entries. */
@ConfigurationProperties("app.rate-limits")
public record RateLimitProperties(Map<String, RateLimitPolicy> policies) {
    public RateLimitProperties {
        if (policies == null || policies.isEmpty()) {
            throw new IllegalArgumentException("At least one named rate-limit policy is required");
        }
        policies = Map.copyOf(policies);
    }
}
