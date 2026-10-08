//Copyright ANuj Nautiyal
package com.example.demo.controller;

import com.example.demo.annotations.RateLimiter;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Independent policies let learners compare algorithms without an API key. */
@RestController
public class RateLimitDemoController {
    /** @return the algorithm used by this independently limited route */
    @GetMapping("/demo/fixed")
    @RateLimiter("fixed-demo")
    public Map<String, String> fixed() {
        return Map.of("algorithm", "FIXED_WINDOW");
    }

    /** @return the algorithm used by this independently limited route */
    @GetMapping("/demo/sliding")
    @RateLimiter("sliding-demo")
    public Map<String, String> sliding() {
        return Map.of("algorithm", "SLIDING_WINDOW");
    }

    /** @return the algorithm used by this independently limited route */
    @GetMapping("/demo/token")
    @RateLimiter("token-demo")
    public Map<String, String> token() {
        return Map.of("algorithm", "TOKEN_BUCKET");
    }
}
