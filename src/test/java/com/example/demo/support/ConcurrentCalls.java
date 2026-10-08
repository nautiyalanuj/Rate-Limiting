//Copyright ANuj Nautiyal
package com.example.demo.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/** Release all workers together so tests exercise competing admissions. */
public final class ConcurrentCalls {
    private ConcurrentCalls() { }

    public static long allowed(int count, BooleanSupplier call) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(count);
        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<Boolean>> results = submit(pool, count, start, call);
            start.countDown();
            return successes(results);
        } finally { pool.shutdownNow(); }
    }

    private static List<Future<Boolean>> submit(ExecutorService pool, int count,
            CountDownLatch start, BooleanSupplier call) {
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            results.add(pool.submit(() -> { start.await(); return call.getAsBoolean(); }));
        }
        return results;
    }

    private static long successes(List<Future<Boolean>> results) throws Exception {
        long allowed = 0;
        for (Future<Boolean> result : results) {
            if (result.get(5, TimeUnit.SECONDS)) allowed++;
        }
        return allowed;
    }
}
