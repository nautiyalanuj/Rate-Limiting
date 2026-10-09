//Copyright ANuj Nautiyal
package com.example.demo.observability;

import com.example.demo.resilience.Breakers;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** One-hot state gauges read breaker state without admitting recovery probes. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.metrics.enabled", havingValue = "true")
public class CircuitStateMeters implements MeterBinder {
    private final Breakers breakers;

    /** @param registry configured metrics destination */
    @Override
    public void bindTo(MeterRegistry registry) {
        breakers.states().keySet().forEach(name -> {
            for (var state : CircuitBreaker.State.values()) bind(registry, name, state);
        });
    }

    private void bind(MeterRegistry registry, String name, CircuitBreaker.State state) {
        Gauge.builder("app.circuit.state", breakers, source -> source.state(name) == state ? 1 : 0)
                .description("One when the dependency is in the labelled circuit state")
                .tag("dependency", name).tag("state", state.name()).register(registry);
    }
}
