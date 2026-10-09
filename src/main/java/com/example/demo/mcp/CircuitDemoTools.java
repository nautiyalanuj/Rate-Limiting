//Copyright ANuj Nautiyal
package com.example.demo.mcp;

import com.example.demo.service.CircuitDemoService;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Deliberate failures are restricted to the existing local demo profile. */
@Component
@Profile("demo")
@RequiredArgsConstructor
public class CircuitDemoTools {
    private final CircuitDemoService demo;
    private final McpToolRunner runner;

    /**
     * @param fail simulate an outage
     * @return success, dependency failure, or circuit denial
     */
    @McpTool(name = "test_circuit_breaker", description = "Demo only: simulate success/failure through the shared demo quota and circuit",
            annotations = @McpTool.McpAnnotations(destructiveHint = false, idempotentHint = false, openWorldHint = false))
    public CallToolResult call(@McpToolParam(description = "true fails; false attempts recovery", required = true) boolean fail) {
        return runner.guarded("circuit-demo", () -> demo.call(fail));
    }
}
