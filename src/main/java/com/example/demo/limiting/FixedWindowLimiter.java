//Copyright ANuj Nautiyal
package com.example.demo.limiting;

import java.util.function.LongSupplier;

/** A full quota returns at each boundary; boundary bursts are possible. */
public final class FixedWindowLimiter implements RateLimitStrategy {
    private final RateLimitPolicy policy;
    private final LongSupplier clock;
    private long started;
    private int used;

    /**
     * @param policy quota
     * @param clock monotonic nanoseconds
     */
    public FixedWindowLimiter(RateLimitPolicy policy, LongSupplier clock) {
        this.policy = policy;
        this.clock = clock;
        this.started = clock.getAsLong();
    }

    // The same lock protects reset, check and increment: no double spending.
    @Override
    public synchronized LimitDecision tryAcquire() {
        long now = clock.getAsLong();
        resetIfExpired(now);
        if (used >= policy.capacity()) return LimitDecision.deny(policy.period().toNanos() - (now - started));
        used++;
        return LimitDecision.permit();
    }

    private void resetIfExpired(long now) {
        if (now - started >= policy.period().toNanos()) {
            started = now;
            used = 0;
        }
    }
}
