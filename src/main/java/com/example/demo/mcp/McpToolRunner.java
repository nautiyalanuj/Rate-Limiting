//Copyright ANuj Nautiyal
package com.example.demo.mcp;

import com.example.demo.limiting.LimitDecision;
import com.example.demo.limiting.RateLimitExceeded;
import com.example.demo.limiting.RateLimitRegistry;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Stateless adapters reuse the thread-safe registries owned by the application. */
@Component
@RequiredArgsConstructor
public class McpToolRunner {
    private final RateLimitRegistry limits;
    private final McpResults results;

    /**
     * @param action tool operation
     * @return a successful result or sanitized failure
     */
    public CallToolResult run(Supplier<?> action) {
        try { return results.success(action.get()); }
        catch (RuntimeException error) { return results.failure(error); }
    }

    /**
     * @param policy shared REST/MCP quota name
     * @param action dependency operation; runs only when admitted
     * @return safe MCP result; denied calls never affect circuit failure counts
     */
    public CallToolResult guarded(String policy, Supplier<?> action) {
        return run(() -> {
            LimitDecision decision = limits.acquire(policy);
            if (!decision.allowed()) throw new RateLimitExceeded(policy, decision);
            return action.get();
        });
    }
}
