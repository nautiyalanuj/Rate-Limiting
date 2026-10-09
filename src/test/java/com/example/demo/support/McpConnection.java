//Copyright ANuj Nautiyal
package com.example.demo.support;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/** Use the real SDK so tests verify transport and protocol, not a REST imitation. */
public final class McpConnection {
    private McpConnection() { }

    /**
     * @param port local server port
     * @return initialized client for the default endpoint
     */
    public static McpSyncClient open(int port) {
        return openAt(port, "/mcp");
    }

    /**
     * @param port local port
     * @param endpoint configured MCP path
     * @return initialized client
     */
    public static McpSyncClient openAt(int port, String endpoint) {
        var transport = HttpClientStreamableHttpTransport.builder("http://127.0.0.1:" + port)
                .endpoint(endpoint).supportedProtocolVersions(List.of("2025-11-25"))
                .openConnectionOnStartup(false).build();
        McpSyncClient client = McpClient.sync(transport).requestTimeout(Duration.ofSeconds(10)).build();
        try { client.initialize(); return client; }
        catch (RuntimeException error) { client.close(); throw error; }
    }

    /**
     * @param client initialized connection
     * @param tool discovered name
     * @param arguments schema values
     * @return result
     */
    public static CallToolResult call(McpSyncClient client, String tool, Map<String, Object> arguments) {
        return client.callTool(new CallToolRequest(tool, arguments));
    }

    /**
     * @param result SDK response
     * @return its structured JSON object
     */
    public static Map<?, ?> envelope(CallToolResult result) {
        return (Map<?, ?>) result.structuredContent();
    }

    /**
     * @param result object-valued success
     * @return application data inside the envelope
     */
    public static Map<?, ?> data(CallToolResult result) {
        return (Map<?, ?>) envelope(result).get("data");
    }
}
