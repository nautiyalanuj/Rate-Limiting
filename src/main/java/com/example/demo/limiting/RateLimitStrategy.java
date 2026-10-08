//Copyright ANuj Nautiyal
package com.example.demo.limiting;

/** Strategies never sleep: they immediately allow or reject one request. */
public interface RateLimitStrategy {
    /** @return admission and a retry delay; implementations must be thread-safe */
    LimitDecision tryAcquire();
}
