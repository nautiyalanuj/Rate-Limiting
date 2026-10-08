//Copyright ANuj Nautiyal
package com.example.demo.limiting;

/** Immutable result: denied requests also tell callers when to retry. */
public record LimitDecision(boolean allowed, long retryNanos) {
    /** @return a successful admission with no retry delay */
    public static LimitDecision permit() {
        return new LimitDecision(true, 0);
    }

    /**
     * @param nanos retry delay
     * @return a rejection with a strictly positive delay
     */
    public static LimitDecision deny(long nanos) {
        return new LimitDecision(false, Math.max(1, nanos));
    }

    /** @return a whole-second HTTP delay rounded up, never zero */
    public long retrySeconds() {
        return Math.max(1, (long) Math.ceil(retryNanos / 1_000_000_000.0));
    }
}
