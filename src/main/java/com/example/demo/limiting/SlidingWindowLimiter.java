//Copyright ANuj Nautiyal
package com.example.demo.limiting;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.LongSupplier;

/** Keep accepted timestamps only: at most capacity entries are retained. */
public final class SlidingWindowLimiter implements RateLimitStrategy {
    private final RateLimitPolicy policy;
    private final LongSupplier clock;
    private final Deque<Long> accepted = new ArrayDeque<>();

    /**
     * @param policy quota
     * @param clock monotonic nanoseconds
     */
    public SlidingWindowLimiter(RateLimitPolicy policy, LongSupplier clock) {
        this.policy = policy;
        this.clock = clock;
    }

    // Eviction and insertion share a lock so concurrent callers see one quota.
    @Override
    public synchronized LimitDecision tryAcquire() {
        long now = clock.getAsLong();
        evictExpired(now);
        if (accepted.size() >= policy.capacity()) {
            return LimitDecision.deny(policy.period().toNanos() - (now - accepted.getFirst()));
        }
        accepted.addLast(now);
        return LimitDecision.permit();
    }

    private void evictExpired(long now) {
        while (!accepted.isEmpty() && now - accepted.getFirst() >= policy.period().toNanos()) {
            accepted.removeFirst();
        }
    }
}
