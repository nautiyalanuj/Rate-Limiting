//Copyright ANuj Nautiyal
package com.example.demo.support;

import java.time.*;
import java.util.concurrent.atomic.AtomicReference;

/** Circuit-breaker cooldown tests move an injected wall clock explicitly. */
public class MutableClock extends Clock {
    private final AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-01-01T00:00:00Z"));

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return Clock.fixed(instant(), zone);
    }

    @Override
    public Instant instant() {
        return now.get();
    }

    public void advance(Duration duration) {
        now.updateAndGet(value -> value.plus(duration));
    }
}
