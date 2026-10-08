//Copyright ANuj Nautiyal
package com.example.demo.weather;

import com.example.demo.resilience.DependencyUnavailable;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class WeatherProviderErrorTests {
    private final RestClient.Builder builder = RestClient.builder().baseUrl("https://api.weatherapi.com/v1");
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

    @ParameterizedTest
    @ValueSource(strings = {"{\"error\":{\"code\":1005}}", "{\"error\":{\"code\":9999}}", "{}", "invalid-json"})
    void other400ErrorsAreServiceFailures(String body) {
        server.expect(anything()).andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON).body(body));
        assertThatThrownBy(() -> client().current("London")).isInstanceOf(DependencyUnavailable.class);
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 429, 500, 503})
    void providerAuthQuotaAndOutagesAreSanitized(int status) {
        server.expect(anything()).andRespond(withStatus(HttpStatus.valueOf(status)).body("test-key"));
        assertThatThrownBy(() -> client().current("London")).isInstanceOf(DependencyUnavailable.class)
                .hasMessageNotContaining("test-key");
        server.verify();
    }

    private WeatherClient client() {
        WeatherProperties properties = new WeatherProperties(URI.create("https://api.weatherapi.com/v1"), "test-key",
                Duration.ofSeconds(1), Duration.ofSeconds(1));
        return new WeatherClient(builder.build(), properties);
    }
}
