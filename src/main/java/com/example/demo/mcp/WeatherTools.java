//Copyright ANuj Nautiyal
package com.example.demo.mcp;

import com.example.demo.weather.WeatherService;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/** The weather quota and circuit are shared with GET /weather. */
@Component
@RequiredArgsConstructor
public class WeatherTools {
    private final WeatherService weather;
    private final McpToolRunner runner;

    /**
     * @param city provider location
     * @return actual weather or a sanitized MCP error
     */
    @McpTool(name = "get_weather", description = "Call weather API through the shared weather quota and circuit; requires WEATHER_API_KEY",
            annotations = @McpTool.McpAnnotations(destructiveHint = false, idempotentHint = false))
    public CallToolResult current(@McpToolParam(description = "Nonblank city, at most 100 characters", required = true) String city) {
        return runner.guarded("weather", () -> weather.current(city));
    }
}
