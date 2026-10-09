//Copyright ANuj Nautiyal
package com.example.demo.support;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/** In-process OTLP HTTP receiver verifies export without a real telemetry backend. */
public final class TraceReceiver implements AutoCloseable {
    private final HttpServer server;
    private final LinkedBlockingQueue<String> payloads = new LinkedBlockingQueue<>();

    /** @throws IOException if the loopback listener cannot start */
    public TraceReceiver() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/traces", this::receive);
        server.start();
    }

    private void receive(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        try (exchange) {
            payloads.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.ISO_8859_1));
            exchange.getResponseHeaders().set("Content-Type", "application/x-protobuf");
            exchange.sendResponseHeaders(200, -1);
        }
    }

    /** @return local OTLP HTTP endpoint */
    public String endpoint() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/traces";
    }

    /** @return next exported protobuf payload, or null on timeout @throws InterruptedException on interruption */
    public String take() throws InterruptedException {
        return payloads.poll(10, TimeUnit.SECONDS);
    }

    /** @param text span attribute or name @return accumulated exports @throws InterruptedException on interruption */
    public String awaitContaining(String text) throws InterruptedException {
        StringBuilder received = new StringBuilder();
        for (int i = 0; i < 10 && received.indexOf(text) < 0; i++) {
            String payload = payloads.poll(1, TimeUnit.SECONDS);
            if (payload != null) received.append(payload);
        }
        return received.toString();
    }

    /** Stop the receiver after the application has closed. */
    @Override
    public void close() {
        server.stop(0);
    }
}
