//Copyright ANuj Nautiyal
package com.example.demo.support;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;

/** Real HTTP requests verify REST and MCP share a running server. */
public final class LocalHttp {
    private LocalHttp() { }

    /**
     * @param port local port
     * @param path route
     * @return HTTP status
     */
    public static int get(int port, String path) throws Exception {
        return HttpClient.newHttpClient().send(request(port, path).GET().build(),
                HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    /**
     * @param port local port
     * @param path MCP route
     * @return status for an untrusted-origin POST
     */
    public static int originRequest(int port, String path) throws Exception {
        HttpRequest request = request(port, path).header("Origin", "https://untrusted.example")
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString("{}" )).build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    private static HttpRequest.Builder request(int port, String path) {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).timeout(Duration.ofSeconds(10));
    }
}
