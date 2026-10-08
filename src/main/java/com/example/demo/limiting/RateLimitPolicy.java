//Copyright ANuj Nautiyal
package com.example.demo.limiting;

import java.time.Duration;

/** capacity requests per period; invalid settings fail during startup. */
public record RateLimitPolicy(Algorithm algorithm, int capacity, Duration period) {
    public RateLimitPolicy {
        if (algorithm == null || capacity <= 0 || period == null) {
            throw new IllegalArgumentException("Algorithm, positive capacity and period are required");
        }
        if (period.isNegative() || period.isZero() || period.compareTo(Duration.ofDays(1)) > 0) {
            throw new IllegalArgumentException("Rate-limit period must be positive and at most one day");
        }
    }
}
