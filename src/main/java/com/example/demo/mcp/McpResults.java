//Copyright ANuj Nautiyal
package com.example.demo.mcp;

import com.example.demo.limiting.RateLimitExceeded;
import com.example.demo.weather.WeatherNotConfigured;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/** MCP errors need their own boundary: REST exception advice does not run here. */
@Component
@RequiredArgsConstructor
public class McpResults {
    private final JsonMapper json;

    /**
     * @param data tool output
     * @return matching text and structured JSON results
     */
    public CallToolResult success(Object data) {
        return result(Map.of("data", data), false);
    }

    /**
     * @param error failure
     * @return safe application code without provider/SQL details
     */
    public CallToolResult failure(RuntimeException error) {
        if (error instanceof RateLimitExceeded limited) return error("RATE_LIMITED", "Quota exhausted", limited.retrySeconds());
        if (error instanceof CallNotPermittedException) return error("CIRCUIT_OPEN", "Dependency is recovering", 0);
        if (error instanceof WeatherNotConfigured) return error("WEATHER_NOT_CONFIGURED", "Set WEATHER_API_KEY", 0);
        if (error instanceof IllegalArgumentException) return error("INVALID_ARGUMENT", "Check arguments and configured names", 0);
        return error("DEPENDENCY_UNAVAILABLE", "Dependency temporarily unavailable", 0);
    }

    private CallToolResult error(String code, String message, long retrySeconds) {
        return result(Map.of("code", code, "message", message, "retryAfterSeconds", retrySeconds), true);
    }

    private CallToolResult result(Map<String, Object> payload, boolean error) {
        // Normalize records, durations and enums before the SDK serializes them.
        Map<String, Object> structured = json.convertValue(payload, new TypeReference<Map<String, Object>>() { });
        return CallToolResult.builder().addTextContent(json.writeValueAsString(structured))
                .structuredContent(structured).isError(error).build();
    }
}
