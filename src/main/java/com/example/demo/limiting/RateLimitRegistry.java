//Copyright ANuj Nautiyal
package com.example.demo.limiting;

import java.util.Map;
import java.util.stream.Collectors;
import com.example.demo.observability.RateLimitMeter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Fixed names avoid an unbounded map of attacker-controlled IPs or headers. */
@Component
public class RateLimitRegistry {
    private final Map<String, RateLimitStrategy> limiters;
    private final Map<String, RateLimitPolicy> policies;

    /** @param properties fixed names and quotas configured at startup */
    public RateLimitRegistry(RateLimitProperties properties) {
        this(properties, (RateLimitMeter) null);
    }

    /**
     * @param properties quotas
     * @param meters optional instrumentation
     */
    @Autowired
    public RateLimitRegistry(RateLimitProperties properties, ObjectProvider<RateLimitMeter> meters) {
        this(properties, meters.getIfAvailable());
    }

    private RateLimitRegistry(RateLimitProperties properties, RateLimitMeter meter) {
        policies = properties.policies();
        limiters = policies.entrySet().stream().collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey, entry -> strategy(entry.getKey(), entry.getValue(), meter)));
    }

    private RateLimitStrategy strategy(String name, RateLimitPolicy policy, RateLimitMeter meter) {
        var limiter = LimiterFactory.create(policy, System::nanoTime);
        return meter == null ? limiter : meter.decorate(name, limiter);
    }

    /**
     * @param name the annotation policy name; all matching callers share it
     * @return immediate admission or the next possible retry delay
     * @throws IllegalArgumentException when a policy was not configured
     */
    public LimitDecision acquire(String name) {
        if (name == null) throw new IllegalArgumentException("A policy name is required");
        RateLimitStrategy limiter = limiters.get(name);
        if (limiter == null) throw new IllegalArgumentException("Unknown rate-limit policy: " + name);
        return limiter.tryAcquire();
    }

    /** @return immutable configured policies; reading them never consumes permits */
    public Map<String, RateLimitPolicy> policies() {
        return policies;
    }
}
