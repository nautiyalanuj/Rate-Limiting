//Copyright ANuj Nautiyal
package com.example.demo.limiting;

/** Carries a safe policy name and delay, never downstream exception details. */
public class RateLimitExceeded extends RuntimeException {
    private final long retrySeconds;

    /**
     * @param policy exhausted quota name
     * @param decision the denied admission
     */
    public RateLimitExceeded(String policy, LimitDecision decision) {
        super("Request quota exhausted for policy: " + policy);
        this.retrySeconds = decision.retrySeconds();
    }

    /** @return the earliest suggested retry in whole seconds */
    public long retrySeconds() {
        return retrySeconds;
    }
}
