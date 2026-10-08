//Copyright ANuj Nautiyal
package com.example.demo.controller;

import com.example.demo.annotations.RateLimiter;
import com.example.demo.service.CircuitDemoService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Simulated failure is exposed only in the local demo profile. */
@RestController
@Profile("demo")
@RequiredArgsConstructor
public class CircuitDemoController {
    private final CircuitDemoService demo;

    /**
     * @param fail simulate an outage
     * @return the dependency result
     */
    @GetMapping("/demo/circuit")
    @RateLimiter("circuit-demo")
    public Map<String, String> call(@RequestParam(defaultValue = "false") boolean fail) {
        return demo.call(fail);
    }

    /** @return current state without spending quota or triggering a probe */
    @GetMapping("/demo/circuit/state")
    public Map<String, String> state() {
        return demo.state();
    }
}
