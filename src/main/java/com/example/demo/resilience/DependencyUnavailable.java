//Copyright ANuj Nautiyal
package com.example.demo.resilience;

/** Deliberately omits provider URLs, API keys, and raw exception messages. */
public class DependencyUnavailable extends RuntimeException {
    /** Create a safe error without carrying the secret-bearing provider exception. */
    public DependencyUnavailable() {
        super("Dependency temporarily unavailable; please retry later");
    }
}
