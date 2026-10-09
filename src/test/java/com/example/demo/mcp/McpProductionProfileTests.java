//Copyright ANuj Nautiyal
package com.example.demo.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;
import static com.example.demo.support.McpConnection.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:mcp-profile-test", "spring.datasource.username=sa",
        "spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.ai.mcp.server.streamable-http.mcp-endpoint=/tools-mcp"})
@ActiveProfiles("mcp-test")
class McpProductionProfileTests {
    @LocalServerPort private int port;

    @Test
    void failureSimulationIsNotPublishedOutsideDemo() {
        try (var client = com.example.demo.support.McpConnection.openAt(port, "/tools-mcp")) {
            assertThat(client.listTools().tools()).extracting(tool -> tool.name()).doesNotContain("test_circuit_breaker");
        }
    }

    @Test
    void originProtectionFollowsConfiguredEndpoint() throws Exception {
        assertThat(com.example.demo.support.LocalHttp.originRequest(port, "/tools-mcp")).isEqualTo(403);
    }
}
