//Copyright ANuj Nautiyal
package com.example.demo.service;

import com.example.demo.resilience.Breakers;
import com.example.demo.resilience.DependencyUnavailable;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Deliberately fail a local dependency to demonstrate opening and recovery. */
@Service
@Profile("demo")
@RequiredArgsConstructor
public class CircuitDemoService {
    private final Breakers breakers;

    /**
     * @param fail whether to simulate an outage
     * @return the successful response
     */
    public Map<String, String> call(boolean fail) {
        return breakers.execute("demo", () -> simulate(fail));
    }

    private Map<String, String> simulate(boolean fail) {
        if (fail) throw new DependencyUnavailable();
        return Map.of("message", "Simulated dependency succeeded");
    }

    /** @return the current state; reading it does not trigger a recovery probe */
    public Map<String, String> state() {
        return Map.of("state", breakers.state("demo").name());
    }
}
