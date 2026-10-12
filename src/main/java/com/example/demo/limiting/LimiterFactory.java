//Copyright ANuj Nautiyal
package com.example.demo.limiting;

import java.util.function.LongSupplier;

/** The registry depends on the interface rather than a particular algorithm. */
public final class LimiterFactory {
    private LimiterFactory() { }

    /**
     * @param policy validated quota and algorithm
     * @param clock monotonic nanoseconds, injectable for deterministic tests
     * @return an independent thread-safe limiter
     */
    public static RateLimitStrategy create(RateLimitPolicy policy, LongSupplier clock) {
        return switch (policy.algorithm()) {
            case FIXED_WINDOW -> new FixedWindowLimiter(policy, clock);
            //case SLIDING_WINDOW -> new SlidingWindowLimiter(policy, clock);
            //case TOKEN_BUCKET -> new TokenBucketLimiter(policy, clock);
            case SLIDING_WINDOW -> new SlidingWindowLimiterv2(policy, clock);
            case TOKEN_BUCKET -> new TokenBucketLimiterv2(policy, clock);
            default -> throw new IllegalArgumentException("Unsupported algorithm: " + policy.algorithm());
        };
    }
}
