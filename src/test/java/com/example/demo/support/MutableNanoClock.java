//Copyright ANuj Nautiyal
package com.example.demo.support;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/** Move time forward without sleeping, even when tests use multiple threads. */
public class MutableNanoClock implements LongSupplier {
    private final AtomicLong now = new AtomicLong();

    @Override
    public long getAsLong() {
        return now.get();
    }

    public void advance(Duration duration) {
        now.addAndGet(duration.toNanos());
    }
}
