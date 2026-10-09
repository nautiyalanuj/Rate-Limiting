//Copyright ANuj Nautiyal
package com.example.demo.mcp;

import com.example.demo.resilience.Breakers;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/** Inspection never runs a dependency or triggers half-open recovery. */
@Component
@RequiredArgsConstructor
public class CircuitBreakerTools {
    private final Breakers breakers;
    private final McpToolRunner runner;

    /** @return independent dependency names and their current states */
    @McpTool(name = "list_circuit_breakers", description = "Inspect all circuit states without running recovery probes",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, openWorldHint = false))
    public CallToolResult list() {
        return runner.run(() -> new TreeMap<>(breakers.states()));
    }

    /**
     * @param name configured dependency name
     * @return its state without admission
     */
    @McpTool(name = "get_circuit_breaker", description = "Read one configured circuit state without changing it",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, openWorldHint = false))
    public CallToolResult state(@McpToolParam(description = "Name from list_circuit_breakers", required = true) String name) {
        return runner.run(() -> Map.of("name", name, "state", breakers.state(name).name()));
    }
}
