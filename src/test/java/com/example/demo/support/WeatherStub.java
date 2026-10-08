//Copyright ANuj Nautiyal
package com.example.demo.support;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** A local HTTP provider exercises the real configured transport and timeout. */
public class WeatherStub implements AutoCloseable {
    private final HttpServer server;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final CountDownLatch entered = new CountDownLatch(1);
    private final CountDownLatch release = new CountDownLatch(1);
    private final AtomicInteger requests = new AtomicInteger();
    private volatile boolean stalled;

    public WeatherStub(boolean stall) throws IOException {
        stalled = stall;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/current.json", this::respond);
        server.setExecutor(executor);
        server.start();
    }

    public URI baseUrl() {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
    }

    private void respond(HttpExchange exchange) throws IOException {
        entered.countDown();
        requests.incrementAndGet();
        try { if (stalled) release.await(5, TimeUnit.SECONDS); }
        catch (InterruptedException error) { Thread.currentThread().interrupt(); }
        byte[] body = payload().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length);
        try (var output = exchange.getResponseBody()) { output.write(body); }
    }

    public boolean received() throws InterruptedException {
        return entered.await(5, TimeUnit.SECONDS);
    }

    public void stall() {
        stalled = true;
    }

    public int requests() {
        return requests.get();
    }

    private String payload() {
        return """
                {"location":{"name":"London","country":"UK"},
                 "current":{"temp_c":18.0,"condition":{"text":"Sunny"}}}
                """;
    }

    @Override
    public void close() {
        release.countDown();
        server.stop(0);
        executor.shutdownNow();
    }
}
