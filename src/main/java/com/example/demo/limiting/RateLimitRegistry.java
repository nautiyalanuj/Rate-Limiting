//Copyright ANuj Nautiyal
package com.example.demo.limiting;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Fixed names avoid an unbounded map of attacker-controlled IPs or headers. */
@Component
public class RateLimitRegistry {
    private final Map<String, RateLimitStrategy> limiters;

    /** @param properties fixed names and quotas configured at startup */
    public RateLimitRegistry(RateLimitProperties properties) {
        limiters = properties.policies().entrySet().stream().collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey, entry -> LimiterFactory.create(entry.getValue(), System::nanoTime)));
    }

    /**
     * @param name the annotation policy name; all matching callers share it
     * @return immediate admission or the next possible retry delay
     * @throws IllegalArgumentException when a policy was not configured
     */
    public LimitDecision acquire(String name) {
        RateLimitStrategy limiter = limiters.get(name);
        if (limiter == null) throw new IllegalArgumentException("Unknown rate-limit policy: " + name);
        return limiter.tryAcquire();
    }
}
