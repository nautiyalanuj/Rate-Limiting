//Copyright ANuj Nautiyal
package com.example.demo.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/** The library synchronizes state changes; remote calls run outside its locks. */
@Component
public class Breakers {
    private final Map<String, CircuitBreaker> instances;

    /** @param properties shared settings; each dependency keeps independent state */
    @Autowired
    public Breakers(BreakerProperties properties) {
        this(properties, Clock.systemUTC());
    }

    // Tests supply a clock; production uses the system clock for recovery timing.
    Breakers(BreakerProperties properties, Clock clock) {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(configuration(properties, clock));
        instances = Map.of("weather", registry.circuitBreaker("weather"),
                "database", registry.circuitBreaker("database"), "demo", registry.circuitBreaker("demo"));
    }

    private CircuitBreakerConfig configuration(BreakerProperties settings, Clock clock) {
        return CircuitBreakerConfig.custom().slidingWindowSize(settings.windowSize())
                .minimumNumberOfCalls(settings.minimumCalls()).failureRateThreshold(settings.failureThreshold())
                .waitDurationInOpenState(settings.openDuration()).permittedNumberOfCallsInHalfOpenState(1)
                .ignoreExceptions(IllegalArgumentException.class).clock(clock).build();
    }

    /**
     * @param name configured dependency name
     * @param action dependency call, executed outside state-management locks
     * @param <T> result type
     * @return the dependency result
     * @throws io.github.resilience4j.circuitbreaker.CallNotPermittedException if admission is refused
     */
    public <T> T execute(String name, Supplier<T> action) {
        return instance(name).executeSupplier(action);
    }

    /**
     * @param name dependency name
     * @return state without admitting a recovery probe
     */
    public CircuitBreaker.State state(String name) {
        return instance(name).getState();
    }

    private CircuitBreaker instance(String name) {
        if (name == null) throw new IllegalArgumentException("A circuit breaker name is required");
        CircuitBreaker breaker = instances.get(name);
        if (breaker == null) throw new IllegalArgumentException("Unknown circuit breaker: " + name);
        return breaker;
    }

    /** @return a snapshot of dependency states without executing any recovery probes */
    public Map<String, String> states() {
        return instances.entrySet().stream().collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey, entry -> entry.getValue().getState().name()));
    }
}
