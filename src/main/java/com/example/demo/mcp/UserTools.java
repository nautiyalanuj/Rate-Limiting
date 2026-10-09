//Copyright ANuj Nautiyal
package com.example.demo.mcp;

import com.example.demo.service.UserService;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

/** Read the same repository through the users quota and database circuit. */
@Component
@RequiredArgsConstructor
public class UserTools {
    private final UserService users;
    private final McpToolRunner runner;

    /** @return users or a safe MCP error; spends one shared users permit */
    @McpTool(name = "list_users", description = "Read users through the shared users quota and database circuit",
            annotations = @McpTool.McpAnnotations(destructiveHint = false, idempotentHint = false))
    public CallToolResult list() {
        return runner.guarded("users", users::listAll);
    }
}
