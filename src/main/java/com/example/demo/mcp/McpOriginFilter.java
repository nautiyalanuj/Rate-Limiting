//Copyright ANuj Nautiyal
package com.example.demo.mcp;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Native MCP clients omit Origin; reject browser origins to prevent DNS rebinding. */
@Component
public class McpOriginFilter extends OncePerRequestFilter {
    private final String endpoint;

    /** @param endpoint the same path configured for the stateless MCP transport */
    public McpOriginFilter(@Value("${spring.ai.mcp.server.streamable-http.mcp-endpoint:/mcp}") String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !endpoint.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getHeader("Origin") != null) {
            response.sendError(403, "Browser origins are not allowed for this local MCP server");
            return;
        }
        chain.doFilter(request, response);
    }
}
