//Copyright ANuj Nautiyal
package com.example.demo.mcp;

import com.example.demo.limiting.LimitDecision;
import com.example.demo.limiting.RateLimitRegistry;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/** Inspect quotas or explicitly spend a permit from any configured algorithm. */
@Component
@RequiredArgsConstructor
public class RateLimiterTools {
    private final RateLimitRegistry limits;
    private final McpToolRunner runner;

    /** @return names, algorithms, capacities and periods without spending quota */
    @McpTool(name = "list_rate_limiters", description = "Inspect configured shared quotas without consuming permits",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, openWorldHint = false))
    public CallToolResult list() {
        return runner.run(() -> new TreeMap<>(limits.policies()));
    }

    /**
     * @param policy fixed policy name
     * @return admission and retry delay
     * consumes one permit if allowed
     */
    @McpTool(name = "try_acquire_rate_limit", description = "Consume one shared REST/MCP permit if available; no dependency call",
            annotations = @McpTool.McpAnnotations(destructiveHint = false, idempotentHint = false, openWorldHint = false))
    public CallToolResult acquire(@McpToolParam(description = "Name from list_rate_limiters", required = true) String policy) {
        return runner.run(() -> {
            LimitDecision decision = limits.acquire(policy);
            return Map.of("policy", policy, "allowed", decision.allowed(),
                    "retryAfterSeconds", decision.allowed() ? 0 : decision.retrySeconds());
        });
    }
}
