//Copyright ANuj Nautiyal
package com.example.demo.limiting;

import java.util.function.LongSupplier;

/** Starts full, then replenishes smoothly; fractional tokens are retained. */
public final class TokenBucketLimiter implements RateLimitStrategy {
    private final RateLimitPolicy policy;
    private final LongSupplier clock;
    private double tokens;
    private long updated;

    /**
     * @param policy quota
     * @param clock monotonic nanoseconds
     */
    public TokenBucketLimiter(RateLimitPolicy policy, LongSupplier clock) {
        this.policy = policy;
        this.clock = clock;
        this.tokens = policy.capacity();
        this.updated = clock.getAsLong();
    }

    // Refill and consumption are atomic, including fractional-token changes.
    @Override
    public synchronized LimitDecision tryAcquire() {
        refill(clock.getAsLong());
        if (tokens < 1) return LimitDecision.deny((long) Math.ceil((1 - tokens) / refillRate()));
        simulateDelay();
        tokens--;
        System.out.println("Tokens after decrement: " + tokens);
        return LimitDecision.permit();
    }

    private void simulateDelay() {
        try {
            Thread.sleep(100); // Hold the monitor during the concurrency experiment.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void refill(long now) {
        tokens = Math.min(policy.capacity(), tokens + (now - updated) * refillRate());
        updated = now;
    }

    private double refillRate() {
        return (double) policy.capacity() / policy.period().toNanos();
    }
}
