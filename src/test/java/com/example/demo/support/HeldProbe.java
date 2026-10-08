//Copyright ANuj Nautiyal
package com.example.demo.support;

import com.example.demo.resilience.Breakers;
import java.util.concurrent.*;

/** Hold one admitted network call to check other callers cannot also probe. */
public class HeldProbe implements AutoCloseable {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final CountDownLatch entered = new CountDownLatch(1);
    private final CountDownLatch release = new CountDownLatch(1);
    private final Future<String> result;

    public HeldProbe(Breakers breakers) {
        result = worker.submit(() -> breakers.execute("weather", this::call));
    }

    private String call() {
        entered.countDown();
        try {
            if (!release.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Probe was never released");
            return "recovered";
        } catch (InterruptedException error) { Thread.currentThread().interrupt(); throw new IllegalStateException(error); }
    }

    public boolean started() throws InterruptedException {
        return entered.await(5, TimeUnit.SECONDS);
    }

    public String finish() throws Exception {
        release.countDown();
        return result.get(5, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        release.countDown();
        worker.shutdownNow();
    }
}
